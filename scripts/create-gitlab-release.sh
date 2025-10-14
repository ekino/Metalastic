#!/usr/bin/env bash

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
RELEASE_NOTES_DIR="${PROJECT_ROOT}/realease-notes"

# GitLab configuration
GITLAB_URL="${GITLAB_URL:-https://gitlab.ekino.com}"
PROJECT_ID="${GITLAB_PROJECT_ID:-iperia%2Fmetalastic}"  # URL-encoded project path

# Usage function
usage() {
    cat <<EOF
Usage: $0 [OPTIONS] <tag-name>

Create a GitLab release from a git tag and its corresponding release notes file.

OPTIONS:
    -h, --help              Show this help message
    -t, --token TOKEN       GitLab API token (or set GITLAB_TOKEN env var)
    -p, --project-id ID     GitLab project ID (default: iperia%2Fmetalastic)
    -n, --notes-file FILE   Path to release notes file (auto-detected if not provided)
    -d, --dry-run           Show what would be done without creating the release

EXAMPLES:
    # Using GITLAB_TOKEN environment variable
    export GITLAB_TOKEN="your-gitlab-token"
    $0 v2.0.6

    # Using token as argument
    $0 --token glpat-xxxxxxxxxxxx v2.0.6

    # With custom release notes file
    $0 --notes-file /path/to/notes.md v2.0.6

    # For elasticsearch-dsl releases
    $0 elasticsearch-dsl-v5.0.12-1.2

    # Dry run
    $0 --dry-run v2.0.6

REQUIREMENTS:
    - curl
    - jq
    - git

GITLAB_TOKEN:
    Create a personal access token at:
    ${GITLAB_URL}/-/user_settings/personal_access_tokens

    Required scopes: api

EOF
    exit 1
}

# Logging functions
log_info() {
    echo -e "${BLUE}ℹ${NC} $*"
}

log_success() {
    echo -e "${GREEN}✓${NC} $*"
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $*"
}

log_error() {
    echo -e "${RED}✗${NC} $*"
}

# Check dependencies
check_dependencies() {
    local missing=()

    for cmd in curl jq git; do
        if ! command -v "$cmd" &> /dev/null; then
            missing+=("$cmd")
        fi
    done

    if [ ${#missing[@]} -gt 0 ]; then
        log_error "Missing required dependencies: ${missing[*]}"
        log_info "Install them using:"
        log_info "  brew install ${missing[*]}"
        exit 1
    fi
}

# Parse arguments
TAG_NAME=""
GITLAB_TOKEN="${GITLAB_TOKEN:-}"
NOTES_FILE=""
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            ;;
        -t|--token)
            GITLAB_TOKEN="$2"
            shift 2
            ;;
        -p|--project-id)
            PROJECT_ID="$2"
            shift 2
            ;;
        -n|--notes-file)
            NOTES_FILE="$2"
            shift 2
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -*)
            log_error "Unknown option: $1"
            usage
            ;;
        *)
            TAG_NAME="$1"
            shift
            ;;
    esac
done

# Validate inputs
if [ -z "$TAG_NAME" ]; then
    log_error "Tag name is required"
    usage
fi

if [ -z "$GITLAB_TOKEN" ]; then
    log_error "GitLab token is required"
    log_info "Set GITLAB_TOKEN environment variable or use --token option"
    exit 1
fi

check_dependencies

# Verify tag exists
if ! git rev-parse "$TAG_NAME" &> /dev/null; then
    log_error "Tag '$TAG_NAME' does not exist"
    log_info "Available tags:"
    git tag -l | tail -10
    exit 1
fi

# Auto-detect release notes file if not provided
if [ -z "$NOTES_FILE" ]; then
    # Try different naming patterns
    if [[ "$TAG_NAME" =~ ^elasticsearch-dsl- ]]; then
        # DSL release: elasticsearch-dsl-v5.0.12-1.2 -> RELEASE_NOTES_elasticsearch-dsl-v5.0.12-1.2.md
        NOTES_FILE="${RELEASE_NOTES_DIR}/RELEASE_NOTES_${TAG_NAME}.md"
    else
        # Core release: v2.0.6 -> RELEASE_NOTES_v2.0.6.md
        NOTES_FILE="${RELEASE_NOTES_DIR}/RELEASE_NOTES_${TAG_NAME}.md"
    fi
fi

# Verify release notes file exists
if [ ! -f "$NOTES_FILE" ]; then
    log_error "Release notes file not found: $NOTES_FILE"
    log_info "Available release notes:"
    ls -1 "$RELEASE_NOTES_DIR"
    exit 1
fi

# Read release notes
RELEASE_NOTES=$(cat "$NOTES_FILE")

# Extract release title from first heading in notes
RELEASE_TITLE=$(echo "$RELEASE_NOTES" | grep -m 1 '^#' | sed 's/^# //' || echo "$TAG_NAME")

# Get commit SHA for the tag
TAG_SHA=$(git rev-parse "$TAG_NAME")

# Get tag message if any
TAG_MESSAGE=$(git tag -l --format='%(contents)' "$TAG_NAME" || echo "")

# Determine release name based on tag pattern
if [[ "$TAG_NAME" =~ ^elasticsearch-dsl- ]]; then
    # DSL release
    VERSION=$(echo "$TAG_NAME" | sed 's/^elasticsearch-dsl-v//')
    RELEASE_NAME="Elasticsearch DSL ${VERSION}"
    MILESTONE_TITLE="elasticsearch-dsl-${VERSION}"
else
    # Core release
    VERSION=$(echo "$TAG_NAME" | sed 's/^v//')
    RELEASE_NAME="Metalastic Core ${VERSION}"
    MILESTONE_TITLE="v${VERSION}"
fi

# Override with title from notes if more descriptive
if [ "$RELEASE_TITLE" != "$TAG_NAME" ]; then
    RELEASE_NAME="$RELEASE_TITLE"
fi

# Build release assets links
ASSETS_LINKS='[]'

# Add Maven package links
if [[ "$TAG_NAME" =~ ^elasticsearch-dsl- ]]; then
    # DSL module
    ASSETS_LINKS=$(jq -n --arg url "${GITLAB_URL}/iperia/metalastic/-/packages" \
                         --arg name "📦 Maven Packages (elasticsearch-dsl)" \
                         '[{"url": $url, "name": $name, "link_type": "package"}]')
else
    # Core modules
    ASSETS_LINKS=$(jq -n --arg url "${GITLAB_URL}/iperia/metalastic/-/packages" \
                         --arg name "📦 Maven Packages (core, processor, gradle-plugin)" \
                         '[{"url": $url, "name": $name, "link_type": "package"}]')
fi

# Add release notes file link
ASSETS_LINKS=$(echo "$ASSETS_LINKS" | jq --arg url "${GITLAB_URL}/iperia/metalastic/-/blob/${TAG_NAME}/realease-notes/$(basename "$NOTES_FILE")" \
                                         --arg name "📄 Release Notes (Markdown)" \
                                         '. + [{"url": $url, "name": $name, "link_type": "runbook"}]')

# Build JSON payload
PAYLOAD=$(jq -n \
    --arg name "$RELEASE_NAME" \
    --arg tag_name "$TAG_NAME" \
    --arg description "$RELEASE_NOTES" \
    --argjson assets "$ASSETS_LINKS" \
    '{
        name: $name,
        tag_name: $tag_name,
        description: $description,
        assets: {
            links: $assets
        }
    }')

# Show what will be done
log_info "Creating GitLab release:"
log_info "  Project:      ${PROJECT_ID}"
log_info "  Tag:          ${TAG_NAME}"
log_info "  Release name: ${RELEASE_NAME}"
log_info "  Commit SHA:   ${TAG_SHA}"
log_info "  Notes file:   ${NOTES_FILE}"
log_info "  Assets:       $(echo "$ASSETS_LINKS" | jq length) links"

if [ "$DRY_RUN" = true ]; then
    log_warn "DRY RUN - Not creating release"
    log_info "Would send payload:"
    echo "$PAYLOAD" | jq .
    exit 0
fi

# Create the release
log_info "Sending request to GitLab API..."

RESPONSE=$(curl -s -w "\n%{http_code}" \
    --request POST \
    --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" \
    --header "Content-Type: application/json" \
    --data "$PAYLOAD" \
    "${GITLAB_URL}/api/v4/projects/${PROJECT_ID}/releases")

# Extract HTTP status code
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

# Check response
if [ "$HTTP_CODE" -eq 201 ]; then
    RELEASE_URL=$(echo "$BODY" | jq -r '._links.self')
    log_success "Release created successfully!"
    log_success "URL: ${RELEASE_URL}"

    # Show release details
    echo
    log_info "Release details:"
    echo "$BODY" | jq -r '"  Name: \(.name)\n  Tag: \(.tag_name)\n  Created: \(.created_at)\n  Author: \(.author.name)"'

    exit 0
elif [ "$HTTP_CODE" -eq 409 ]; then
    log_error "Release already exists for tag '$TAG_NAME'"
    log_info "View existing release at:"
    log_info "  ${GITLAB_URL}/iperia/metalastic/-/releases/${TAG_NAME}"
    log_info ""
    log_info "To update it, first delete the existing release via GitLab UI or API:"
    log_info "  curl --request DELETE --header \"PRIVATE-TOKEN: \$GITLAB_TOKEN\" \\"
    log_info "    \"${GITLAB_URL}/api/v4/projects/${PROJECT_ID}/releases/${TAG_NAME}\""
    exit 1
else
    log_error "Failed to create release (HTTP $HTTP_CODE)"
    log_error "Response:"
    echo "$BODY" | jq -r '.message // .error // .'
    exit 1
fi
