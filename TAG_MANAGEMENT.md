# Tag Management and Publishing Guide

This document explains how to manage releases and publish Metalastic to Maven Central and Gradle Plugin Portal using GitHub Actions.

## Table of Contents

- [Overview](#overview)
- [Versioning Strategy](#versioning-strategy)
- [Release Process](#release-process)
- [GitHub Actions Workflows](#github-actions-workflows)
- [Troubleshooting](#troubleshooting)

## Overview

Metalastic uses **unified versioning** with automatic publishing via GitHub Actions. All 6 artifacts share the same version and are released together atomically.

**Single tag publishes everything**:
- `com.ekino.oss:metalastic-core`
- `com.ekino.oss:metalastic-processor`
- `com.ekino.oss:metalastic-bom`
- `com.ekino.oss:metalastic-elasticsearch-dsl` (rolling, Spring Data ES 5.4-5.5)
- `com.ekino.oss:metalastic-elasticsearch-dsl-5.3` (frozen, Spring Data ES 5.0-5.3)
- `com.ekino.oss.metalastic` Gradle plugin

**Benefits**:
- ✅ Guaranteed compatibility across all artifacts
- ✅ Simple version management for users
- ✅ Single GitHub release (no clutter)
- ✅ Industry-standard practice (Spring Boot, Ktor, etc.)

## Versioning Strategy

### Unified Semantic Versioning

**Format**: `v{MAJOR}.{MINOR}.{PATCH}`

All modules follow semantic versioning at the **project level**:
- **MAJOR** (X.0.0): Breaking change in **any** module
- **MINOR** (x.Y.0): New feature in **any** module (backward compatible)
- **PATCH** (x.y.Z): Bug fix in **any** module (backward compatible)

**Examples**:
- `v1.0.0` - First official release
- `v1.0.1` - Patch release (e.g., DSL bug fix)
- `v1.1.0` - Minor release (e.g., new field type in core)
- `v2.0.0` - Major release (breaking change in any module)

### Published Artifacts

**Single tag** `v1.0.0` publishes to:

**Maven Central** (`com.ekino.oss`):
```
metalastic-core:1.0.0
metalastic-processor:1.0.0
metalastic-bom:1.0.0
metalastic-elasticsearch-dsl:1.0.0        # Rolling (5.4-5.5)
metalastic-elasticsearch-dsl-5.3:1.0.0    # Frozen (5.0-5.3)
```

**Gradle Plugin Portal**:
```
Plugin ID: com.ekino.oss.metalastic
Version: 1.0.0
```

### Elasticsearch DSL Variants

Two DSL artifacts support different Spring Data Elasticsearch versions:

| Artifact | Strategy | Spring Data ES | Transitive Version |
|----------|----------|----------------|-------------------|
| `elasticsearch-dsl` | Rolling | 5.4.x - 5.5.x | 5.5.6 |
| `elasticsearch-dsl-5.3` | Frozen | 5.0.x - 5.3.x | 5.3.13 |

**Both artifacts** share the same Metalastic version (e.g., 1.0.0) and are released together.

## Release Process

### Prerequisites

1. **Clean working directory**
   ```bash
   git status  # Should be clean
   ```

2. **All tests passing**
   ```bash
   ./gradlew clean build
   ./gradlew check
   ```

3. **Up to date with remote**
   ```bash
   git pull origin master
   ```

4. **Release notes prepared**
   ```bash
   # Ensure release-notes/RELEASE_NOTES_v1.0.0.md exists
   ls release-notes/RELEASE_NOTES_v1.0.0.md
   ```

### Unified Release Process

**One tag publishes all 6 artifacts**:

1. **Prepare release notes**
   ```bash
   # Create comprehensive release notes
   # File: release-notes/RELEASE_NOTES_v1.0.0.md
   # See template below and existing release notes for reference
   ```

2. **Commit release notes**
   ```bash
   git add release-notes/RELEASE_NOTES_v1.0.0.md
   git commit -m "docs: add release notes for v1.0.0"
   git push origin master
   ```

3. **Create and push tag**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

4. **Monitor GitHub Actions**
   - Go to: https://github.com/ekino/Metalastic/actions
   - Watch the "Publish Release" workflow
   - Verify all steps complete successfully

5. **Verify publication** (15-30 minutes)
   - **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
     - Search for: `com.ekino.oss:metalastic-core:1.0.0`
     - Verify all 5 Maven artifacts appear
   - **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
     - Search for plugin version: `1.0.0`
     - Initial submission requires manual approval (1-2 business days)
     - Subsequent releases typically auto-approved
   - **GitHub Release**: https://github.com/ekino/Metalastic/releases/tag/v1.0.0
     - Verify release created automatically with correct release notes

### Example Release Scenarios

#### Scenario 1: Bug Fix in DSL Module
```bash
# Version: 1.0.0 → 1.0.1
# Change: Fixed BoolQuery issue in DSL module

# Create release notes
vim release-notes/RELEASE_NOTES_v1.0.1.md
# Content: "Bug fix in elasticsearch-dsl module only"

# Commit and tag
git add release-notes/RELEASE_NOTES_v1.0.1.md
git commit -m "docs: add release notes for v1.0.1"
git tag v1.0.1
git push origin master v1.0.1

# Result: All 6 artifacts published at 1.0.1
```

#### Scenario 2: New Feature in Core
```bash
# Version: 1.0.0 → 1.1.0
# Change: Added GeoPoint field support in core

# Create release notes
vim release-notes/RELEASE_NOTES_v1.1.0.md
# Content: "New feature in core & processor modules"

# Commit and tag
git add release-notes/RELEASE_NOTES_v1.1.0.md
git commit -m "docs: add release notes for v1.1.0"
git tag v1.1.0
git push origin master v1.1.0

# Result: All 6 artifacts published at 1.1.0
```

#### Scenario 3: Breaking Change
```bash
# Version: 1.0.0 → 2.0.0
# Change: Renamed DSL method (breaking change)

# Create release notes with migration guide
vim release-notes/RELEASE_NOTES_v2.0.0.md
# Content: Breaking changes section + migration guide

# Commit and tag
git add release-notes/RELEASE_NOTES_v2.0.0.md
git commit -m "docs: add release notes for v2.0.0"
git tag v2.0.0
git push origin master v2.0.0

# Result: All 6 artifacts published at 2.0.0
```

### Release Notes Template

**File**: `release-notes/RELEASE_NOTES_v{VERSION}.md`

Use the existing `RELEASE_NOTES_v1.0.0.md` as a reference template.

**Key sections**:
- Overview
- Breaking Changes (if any)
- New Features (by module: Core, DSL, Gradle Plugin)
- Bug Fixes (by module)
- Installation instructions with BOM
- Spring Data ES compatibility matrix
- Maven coordinates summary

### Publishing SNAPSHOTs

SNAPSHOTs are automatically published on every commit to `master` via the "Manual Publish" workflow.

**Manual SNAPSHOT publish**:
```bash
# Trigger via GitHub UI
# Go to Actions → Manual Publish → Run workflow
```

**Local SNAPSHOT testing**:
```bash
./gradlew publishToMavenLocal
# Artifacts available in ~/.m2/repository/
```

## GitHub Actions Workflows

### Build Workflow

**File**: `.github/workflows/build.yml`

**Triggers**:
- Push to any branch
- Pull requests

**Actions**:
- Runs tests
- Runs code quality checks (spotless, detekt)
- Validates build

### Publish Workflow

**File**: `.github/workflows/publish.yml`

**Triggers**:
- Tag push matching `v*`

**Actions**:
1. Detects version from tag (e.g., `v1.0.0` → `1.0.0`)
2. Builds all modules
3. Runs tests
4. Signs artifacts with GPG
5. Publishes **all 5 artifacts** to Maven Central:
   - metalastic-core
   - metalastic-processor
   - metalastic-bom
   - metalastic-elasticsearch-dsl
   - metalastic-elasticsearch-dsl-5.3
6. Publishes gradle-plugin to Gradle Plugin Portal
7. Creates GitHub Release automatically with release notes

**Required Secrets** (configured in GitHub repository settings):

_Maven Central secrets:_
- `ORG_GRADLE_PROJECT_mavenCentralUsername` - Maven Central username
- `ORG_GRADLE_PROJECT_mavenCentralPassword` - Maven Central token
- `ORG_GRADLE_PROJECT_signingInMemoryKey` - GPG private key (base64)
- `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` - GPG key password

_Gradle Plugin Portal secrets:_
- `GRADLE_PUBLISH_KEY` - Plugin Portal API key
- `GRADLE_PUBLISH_SECRET` - Plugin Portal API secret

### Manual Publish Workflow

**File**: `.github/workflows/manual-publish.yml`

**Triggers**:
- Manual workflow dispatch

**Actions**:
- Publishes SNAPSHOT versions
- Useful for testing publication process

## Troubleshooting

### Tag Push Doesn't Trigger Workflow

**Check**:
1. Tag format matches `v*` (e.g., `v1.0.0`)
2. Tag was pushed to GitHub: `git ls-remote --tags origin`
3. Workflow file exists and is valid: `.github/workflows/publish.yml`

**Fix**:
```bash
# Delete and recreate tag
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
git tag v1.0.0
git push origin v1.0.0
```

### Publication Fails - Authentication Error

**Symptoms**: "401 Unauthorized" or "403 Forbidden"

**Fix**:
1. Verify secrets are set in GitHub repository settings
2. Check Maven Central credentials are valid
3. Regenerate Maven Central token if needed

### Publication Fails - Signing Error

**Symptoms**: "Failed to sign artifact" or GPG errors

**Fix**:
1. Verify `signingInMemoryKey` is base64-encoded properly
2. Verify `signingInMemoryKeyPassword` matches the key
3. Check GPG key is not expired

**Export GPG key for GitHub**:
```bash
# Export private key
gpg --armor --export-secret-keys YOUR_KEY_ID | base64

# Use the output as ORG_GRADLE_PROJECT_signingInMemoryKey
```

### Artifacts Not Appearing on Maven Central

**Timeline**:
- Artifacts appear in Maven Central search: ~15-30 minutes
- Artifacts available for download: immediately after sync

**Verify**:
1. Check publication succeeded in GitHub Actions logs
2. Search Maven Central: https://central.sonatype.com/search?q=metalastic
3. Check POM is correct: https://repo1.maven.org/maven2/com/ekino/oss/metalastic-core/

### Wrong Version Published

**Scenario**: Published v1.0.1 but meant v1.1.0

**Fix**:
1. **Do NOT delete from Maven Central** - Releases are immutable
2. Publish the correct version: `v1.1.0`
3. Mark incorrect version as deprecated in release notes

### Deleting a Tag

**Local**:
```bash
git tag -d v1.0.0
```

**Remote**:
```bash
git push origin :refs/tags/v1.0.0
```

**Note**: Deleting a tag does NOT unpublish from Maven Central. Published artifacts are immutable.

### Build Fails - "Could not determine version"

**Symptoms**: Version shows as "unknown" or fails to build

**Cause**: Git environment not configured properly

**Fix**:
```bash
# Ensure git history is available
git fetch --tags --force

# Verify tags are visible
git tag -l

# Check version detection
./gradlew properties | grep version
```

### Gradle Plugin Portal - Publication Fails

**Symptoms**: `publishPlugins` task fails with authentication error

**Fix**:
1. Verify `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` are set in GitHub repository settings
2. Check credentials at: https://plugins.gradle.org/ (Profile → API Keys)
3. Regenerate API key if needed

**Local testing**:
```bash
# Add credentials to ~/.gradle/gradle.properties
gradle.publish.key=<your-api-key>
gradle.publish.secret=<your-api-secret>

# Validate configuration
./gradlew :modules:gradle-plugin:publishPlugins --validate-only
```

### Gradle Plugin Portal - Pending Approval

**Symptoms**: Plugin published but not visible in search

**Timeline**:
- **First submission**: Manual review by Gradle team (1-2 business days)
- **Subsequent versions**: Typically auto-approved

**Check status**:
- Email notification to registered account
- Plugin Portal: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

### Release Notes Missing

**Symptoms**: GitHub Release created but no custom release notes

**Cause**: Missing `release-notes/RELEASE_NOTES_v{VERSION}.md` file

**Result**: GitHub auto-generates release notes from commits

**Fix**: Create release notes file before tagging

### Multiple Artifacts Failed to Publish

**Symptoms**: Some Maven artifacts published, others failed

**Investigation**:
1. Check GitHub Actions logs for specific error
2. Look for signing errors or network issues
3. Verify all module builds succeeded

**Fix**:
- Delete failed tag: `git push origin :refs/tags/v1.0.0`
- Fix underlying issue
- Create and push tag again

## Best Practices

### Before Releasing

- [ ] All tests pass: `./gradlew clean build`
- [ ] Code formatted: `./gradlew spotlessApply`
- [ ] Quality checks pass: `./gradlew check`
- [ ] Documentation updated (README, CLAUDE.md if needed)
- [ ] Release notes created: `release-notes/RELEASE_NOTES_v{VERSION}.md`
- [ ] Release notes comprehensive (covers all module changes)
- [ ] Version number follows semantic versioning

### Version Numbering

**Use semantic versioning at project level**:
- **MAJOR**: Breaking changes in **any** public API
- **MINOR**: New features in **any** module, backward compatible
- **PATCH**: Bug fixes in **any** module, backward compatible

**Examples**:
- DSL bug fix only → PATCH (1.0.0 → 1.0.1)
- Core new feature → MINOR (1.0.0 → 1.1.0)
- Processor breaking change → MAJOR (1.0.0 → 2.0.0)

### Release Notes Content

**Essential sections**:
1. **Overview** - What's in this release
2. **Breaking Changes** - Always include (use "None" if applicable)
3. **New Features** - By module (Core, DSL, Gradle Plugin)
4. **Bug Fixes** - By module
5. **Installation** - With BOM examples
6. **Spring Data ES Compatibility** - Matrix showing which DSL artifact to use
7. **Maven Coordinates** - Complete summary

**Clarity for users**:
- If only one module changed, state it clearly
- If change is breaking, provide migration guide
- Always show which Spring Data ES versions are supported

### Communication

After successful release:
- [ ] Announce in GitHub Discussions
- [ ] Update main README badges if needed
- [ ] Social media announcement (if applicable)
- [ ] Update documentation website (if applicable)

## Additional Resources

- **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
- **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
- **GitHub Repository**: https://github.com/ekino/Metalastic
- **GitHub Actions**: https://github.com/ekino/Metalastic/actions
- **Issues**: https://github.com/ekino/Metalastic/issues

## Support

For questions or issues:
1. Check this guide's troubleshooting section
2. Search existing issues: https://github.com/ekino/Metalastic/issues
3. Create new issue with details about your problem

---

**Note**: This guide reflects the **unified versioning strategy** adopted for v1.0.0+. All artifacts share the same version and are released together atomically.
