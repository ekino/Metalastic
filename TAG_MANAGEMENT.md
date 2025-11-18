# Tag Management and Publishing Guide

This document explains how to manage releases and publish Metalastic to Maven Central using GitHub Actions.

## Table of Contents

- [Overview](#overview)
- [Versioning Strategy](#versioning-strategy)
- [Release Process](#release-process)
- [GitHub Actions Workflows](#github-actions-workflows)
- [Troubleshooting](#troubleshooting)

## Overview

Metalastic uses **GitHub Actions** to automatically publish artifacts when git tags are pushed. The project supports two versioning strategies:

1. **Core Modules** (`core`, `processor`) - Published to Maven Central
2. **Gradle Plugin** (`gradle-plugin`) - Published to Gradle Plugin Portal
3. **Elasticsearch DSL Modules** - Published to Maven Central

Core modules are published to Maven Central under the `com.ekino.oss` group ID. The Gradle plugin is published to the Gradle Plugin Portal with plugin ID `com.ekino.oss.metalastic`.

## Versioning Strategy

### Core Modules Versioning

**Format**: `v{MAJOR}.{MINOR}.{PATCH}`

**Examples**:
- `v1.0.0` - First official release
- `v1.1.0` - Minor feature addition
- `v1.0.1` - Patch/bugfix
- `v2.0.0` - Breaking changes

**Publishes to Maven Central**:
```
com.ekino.oss:metalastic-core:1.0.0
com.ekino.oss:metalastic-processor:1.0.0
```

**Publishes to Gradle Plugin Portal**:
```
Plugin ID: com.ekino.oss.metalastic
Version: 1.0.0
URL: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
```

### Elasticsearch DSL Modules Versioning

**Multi-version support**: 2 artifacts based on Spring Data ES API compatibility

**Format**: `elasticsearch-dsl-{min-version}-v{DSL_VERSION}`

**Examples**:
- `elasticsearch-dsl-5.0-v1.0.0` - DSL v1.0.0 for Spring Data ES 5.0.x - 5.3.x
- `elasticsearch-dsl-5.4-v1.0.0` - DSL v1.0.0 for Spring Data ES 5.4.x - 5.5.x

**Publishes** (per tag):
```
# Tag elasticsearch-dsl-5.0-v1.0.0 publishes:
com.ekino.oss:metalastic-elasticsearch-dsl-5.0:1.0.0
(Supports Spring Data ES 5.0-5.3, brings 5.3.13 transitively)

# Tag elasticsearch-dsl-5.4-v1.0.0 publishes:
com.ekino.oss:metalastic-elasticsearch-dsl-5.4:1.0.0
(Supports Spring Data ES 5.4-5.5, brings 5.5.6 transitively)
```

**Rationale**: Consolidated artifacts based on API compatibility reduce maintenance burden while providing runtime version warnings for mismatches.

## Release Process

### Prerequisites

1. **Clean working directory**
   ```bash
   git status  # Should be clean
   ```

2. **All tests passing**
   ```bash
   ./gradlew clean build
   ```

3. **Up to date with remote**
   ```bash
   git pull origin master
   ```

### Releasing Core Modules

1. **Create and push the tag**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Monitor GitHub Actions**
   - Go to: https://github.com/ekino/Metalastic/actions
   - Watch the "Publish Release" workflow
   - Verify all steps complete successfully

3. **Verify publication**
   - **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
     - Search for: `com.ekino.oss:metalastic-core:1.0.0`
     - Artifacts typically appear within 15-30 minutes
   - **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
     - Search for plugin version: `1.0.0`
     - Initial submission requires manual approval (1-2 business days)
     - Subsequent releases typically auto-approved

4. **Create GitHub Release**
   - Go to: https://github.com/ekino/Metalastic/releases
   - Click "Create a new release"
   - Select the tag you just created
   - Add release notes from `release-notes/RELEASE_NOTES_v1.0.0.md`
   - Publish the release

### Releasing Elasticsearch DSL Modules

**Option A: Batch release via GitHub Actions** (recommended)

Use the automated workflow to release multiple DSL variants with built-in validation:

1. **Navigate to GitHub Actions**
   - Go to: https://github.com/ekino/Metalastic/actions
   - Select: "Release DSL Modules (Batch)"
   - Click: "Run workflow"

2. **Configure the release**
   - **Branch**: `master` (or your release branch)
   - **DSL version**: e.g., `1.0.0`, `1.1.0`, `2.0.0`
   - **Variants**: Choose one of:
     - `all` - All 6 variants (5.0 through 5.5)
     - `5.0-5.3` - Only versions using shared-8.5 codebase
     - `5.4-5.5` - Only versions using shared-8.15 codebase
     - `custom` - Specify exact variants (e.g., `5.4,5.5`)
   - **Dry run**: ✅ Check this first (validation only)

3. **Validate with dry-run**
   - Click "Run workflow"
   - Wait for workflow to complete
   - Review the job summary:
     - Version format validation
     - Duplicate tag detection
     - Build verification results
     - Release notes status
     - Maven coordinates preview

4. **Create tags (if validation passes)**
   - Run workflow again with same settings
   - **Dry run**: ❌ Uncheck (to actually create tags)
   - Tags will be created and pushed automatically

5. **Monitor publication**
   - Each tag triggers the "Publish Release" workflow
   - Monitor both workflows (for `all` variants)
   - Verify GitHub Releases are created automatically

**Quick release scenarios**:

```yaml
# Release all variants with version 1.0.0
Variants: all
DSL version: 1.0.0
Creates: elasticsearch-dsl-5.0-v1.0.0, elasticsearch-dsl-5.4-v1.0.0

# Hot-fix for specific variant
Variants: custom
Custom variants: 5.4
DSL version: 1.0.1
Creates: elasticsearch-dsl-5.4-v1.0.1
```

**Option B: Release specific DSL artifact** (manual)

1. **Choose which DSL artifact to release**
   - `5.0` (for Spring Data ES 5.0-5.3)
   - `5.4` (for Spring Data ES 5.4-5.5)

2. **Create and push the tag**
   ```bash
   # Example: Release DSL v1.0.0 for Spring Data ES 5.4-5.5
   git tag elasticsearch-dsl-5.4-v1.0.0
   git push origin elasticsearch-dsl-5.4-v1.0.0
   ```

3. **Monitor and verify** (same as core modules)

**Option C: Release both DSL artifacts together** (manual)

When releasing a new DSL version, release both artifacts with the same DSL version:

```bash
# Tag both artifacts with DSL v1.0.0
git tag elasticsearch-dsl-5.0-v1.0.0
git tag elasticsearch-dsl-5.4-v1.0.0

# Push tags
git push origin elasticsearch-dsl-5.0-v1.0.0 elasticsearch-dsl-5.4-v1.0.0
```

This publishes:
- `com.ekino.oss:metalastic-elasticsearch-dsl-5.0:1.0.0` (Spring Data ES 5.0-5.3)
- `com.ekino.oss:metalastic-elasticsearch-dsl-5.4:1.0.0` (Spring Data ES 5.4-5.5)

**Note**: Option A (batch via GitHub Actions) is recommended as it includes validation, prevents errors, and provides a clear audit trail.

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
- Tag push matching `v*` or `elasticsearch-dsl-*-v*`

**Actions**:
1. Detects tag type (core modules vs DSL)
2. Builds artifacts
3. Signs with GPG
4. Publishes to Maven Central (core, processor, elasticsearch-dsl-*)
5. Publishes gradle-plugin to Gradle Plugin Portal (core releases only)
6. Creates GitHub Release automatically

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

### Batch DSL Release Workflow

**File**: `.github/workflows/release-dsl-batch.yml`

**Triggers**:
- Manual workflow dispatch (GitHub UI only)

**Actions**:
1. Validates version format (semantic versioning)
2. Checks for duplicate tags
3. Builds and tests selected DSL module variants
4. Checks for release notes files (warns if missing)
5. Creates and pushes tags (if not dry-run mode)
6. Generates detailed job summary

**Configuration Options**:
- **DSL version**: The version to release (e.g., `1.0.0`, `1.1.0`)
- **Variants**: Which Spring Data ES versions to release
  - `all` - All 6 variants (5.0-5.5)
  - `5.0-5.3` - Only shared-8.5 based versions
  - `5.4-5.5` - Only shared-8.15 based versions
  - `custom` - Specify exact variants
- **Custom variants**: Comma-separated list when using `custom` mode
- **Dry run**: Validate without creating tags (recommended first step)

**Benefits**:
- Atomic tag creation (all-or-nothing)
- Pre-flight validation prevents common errors
- Build verification ensures modules compile
- Release notes status checking
- Detailed job summary with Maven coordinates
- Safe testing with dry-run mode

## Troubleshooting

### Tag Push Doesn't Trigger Workflow

**Check**:
1. Tag format matches `v*` or `elasticsearch-dsl-*-v*`
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

## Best Practices

### Before Releasing

- [ ] All tests pass: `./gradlew clean build`
- [ ] Code formatted: `./gradlew spotlessApply`
- [ ] Quality checks pass: `./gradlew check`
- [ ] Documentation updated
- [ ] RELEASE_NOTES.md created
- [ ] CLAUDE.md version references updated

### Version Numbering

**Use semantic versioning for core modules**:
- MAJOR: Breaking changes to public API
- MINOR: New features, backward compatible
- PATCH: Bug fixes, backward compatible

**Elasticsearch DSL module**:
- Match Spring Data Elasticsearch version consumer uses
- Increment DSL version for non-breaking improvements
- Create new major DSL version for breaking changes

### Release Notes

Create `release-notes/RELEASE_NOTES_v{VERSION}.md` for each release:

```markdown
# Metalastic v1.0.0

**Release Date:** 2025-01-15

## Highlights

- First official Maven Central release
- Type-safe metamodel generation
- Query DSL support

## Changes

### Added
- Feature X

### Fixed
- Bug Y

### Changed
- Improvement Z

## Maven Coordinates

\`\`\`kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
}
\`\`\`
```

## Additional Resources

- **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
- **GitHub Repository**: https://github.com/ekino/Metalastic
- **GitHub Actions**: https://github.com/ekino/Metalastic/actions
- **Issues**: https://github.com/ekino/Metalastic/issues

## Support

For questions or issues:
1. Check this guide's troubleshooting section
2. Search existing issues: https://github.com/ekino/Metalastic/issues
3. Create new issue with details about your problem
