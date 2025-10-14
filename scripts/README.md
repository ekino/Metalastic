# Metalastic Release Scripts

Scripts for automating release management and GitLab integration.

## Available Scripts

### `create-gitlab-release.sh`

Automatically creates a GitLab Release from a git tag and its corresponding release notes file.

#### Features

- ✅ Auto-detects release notes file based on tag name
- ✅ Creates release with proper title and description
- ✅ Adds links to Maven packages
- ✅ Links to release notes file in repository
- ✅ Supports both core and DSL module releases
- ✅ Dry-run mode for testing
- ✅ Colored output and progress indicators

#### Prerequisites

```bash
# Install dependencies (macOS)
brew install curl jq git

# Create GitLab Personal Access Token
# Visit: https://gitlab.ekino.com/-/user_settings/personal_access_tokens
# Required scopes: api
```

#### Usage

```bash
# Set your GitLab token (recommended)
export GITLAB_TOKEN="glpat-xxxxxxxxxxxx"

# Create release for core modules
./scripts/create-gitlab-release.sh v2.0.6

# Create release for DSL module
./scripts/create-gitlab-release.sh elasticsearch-dsl-v5.0.12-1.2

# Dry run (see what would happen without creating)
./scripts/create-gitlab-release.sh --dry-run v2.0.6

# With token as argument
./scripts/create-gitlab-release.sh --token glpat-xxxxxxxxxxxx v2.0.6

# Custom release notes file
./scripts/create-gitlab-release.sh --notes-file /path/to/notes.md v2.0.6
```

#### How It Works

1. **Tag Validation**: Verifies the git tag exists locally
2. **Notes Detection**: Auto-locates release notes file:
   - Core: `realease-notes/RELEASE_NOTES_v{version}.md`
   - DSL: `realease-notes/RELEASE_NOTES_elasticsearch-dsl-v{version}.md`
3. **Content Extraction**: Reads release notes and extracts title
4. **API Call**: Creates GitLab release via REST API
5. **Asset Links**: Adds links to:
   - Maven package registry
   - Release notes markdown file

#### Examples

**Creating releases for v2.0.6:**

```bash
$ export GITLAB_TOKEN="glpat-xxxxxxxxxxxx"
$ ./scripts/create-gitlab-release.sh v2.0.6

ℹ Creating GitLab release:
ℹ   Project:      iperia%2Fmetalastic
ℹ   Tag:          v2.0.6
ℹ   Release name: Metalastic v2.0.6 Release Notes
ℹ   Commit SHA:   343624e
ℹ   Notes file:   /Users/.../realease-notes/RELEASE_NOTES_v2.0.6.md
ℹ   Assets:       2 links
ℹ Sending request to GitLab API...
✓ Release created successfully!
✓ URL: https://gitlab.ekino.com/api/v4/projects/iperia%2Fmetalastic/releases/v2.0.6

ℹ Release details:
  Name: Metalastic v2.0.6 Release Notes
  Tag: v2.0.6
  Created: 2025-10-14T12:30:00.000Z
  Author: Your Name
```

**Dry run:**

```bash
$ ./scripts/create-gitlab-release.sh --dry-run v2.0.6

⚠ DRY RUN - Not creating release
ℹ Would send payload:
{
  "name": "Metalastic v2.0.6 Release Notes",
  "tag_name": "v2.0.6",
  "description": "# Metalastic v2.0.6 Release Notes\n\n...",
  "assets": {
    "links": [...]
  }
}
```

#### Troubleshooting

**Error: Release already exists**

```bash
# Delete existing release first (via GitLab UI or API)
curl --request DELETE \
  --header "PRIVATE-TOKEN: $GITLAB_TOKEN" \
  "https://gitlab.ekino.com/api/v4/projects/iperia%2Fmetalastic/releases/v2.0.6"

# Then retry
./scripts/create-gitlab-release.sh v2.0.6
```

**Error: Tag not found**

```bash
# Make sure tag exists locally
git fetch --tags
git tag -l | grep v2.0.6

# If missing, pull from remote
git pull --tags
```

**Error: Release notes file not found**

```bash
# Check available release notes
ls -1 realease-notes/

# Specify file manually
./scripts/create-gitlab-release.sh \
  --notes-file realease-notes/RELEASE_NOTES_v2.0.6.md \
  v2.0.6
```

## Workflow Integration

### Manual Workflow

```bash
# 1. Create and merge feature
git checkout -b feat/my-feature
# ... make changes ...
git commit -m "feat: my feature"
# ... create MR, get approved, merge ...

# 2. Prepare release
git checkout master
git pull

# 3. Create release notes
vim realease-notes/RELEASE_NOTES_v2.0.7.md

# 4. Update version (if needed for DSL)
vim modules/elasticsearch-dsl/build.gradle.kts

# 5. Commit and tag
git add realease-notes/
git commit -m "docs: add release notes for v2.0.7"
git push
git tag -a v2.0.7 -m "Release v2.0.7"
git push origin v2.0.7

# 6. Wait for CI to publish artifacts

# 7. Create GitLab release
export GITLAB_TOKEN="glpat-xxxxxxxxxxxx"
./scripts/create-gitlab-release.sh v2.0.7
```

### Automated Workflow (Future)

You can integrate this script into your `.gitlab-ci.yml` to automatically create releases:

```yaml
create-release:
  stage: release
  image: alpine:latest
  before_script:
    - apk add --no-cache bash curl jq git
  script:
    - ./scripts/create-gitlab-release.sh $CI_COMMIT_TAG
  rules:
    - if: $CI_COMMIT_TAG
  only:
    - tags
```

## API Reference

The script uses the [GitLab Releases API](https://docs.gitlab.com/ee/api/releases/):

- **Endpoint**: `POST /api/v4/projects/:id/releases`
- **Authentication**: Personal Access Token with `api` scope
- **Response**: 201 Created (success), 409 Conflict (already exists)

## Contributing

When adding new scripts:

1. Make them executable: `chmod +x scripts/your-script.sh`
2. Add usage documentation in this README
3. Include error handling and colored output
4. Support `--help` and `--dry-run` flags
5. Use consistent logging functions
