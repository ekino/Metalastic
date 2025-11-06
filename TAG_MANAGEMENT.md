# Tag Management and Publishing Guide

This document explains how to manage releases and publish Metalastic to Maven Central using GitHub Actions.

## Table of Contents

- [Overview](#overview)
- [Versioning Strategy](#versioning-strategy)
- [Release Process](#release-process)
- [GitHub Actions Workflows](#github-actions-workflows)
- [Troubleshooting](#troubleshooting)

## Overview

Metalastic uses **GitHub Actions** to automatically publish artifacts to **Maven Central** when git tags are pushed. The project supports two versioning strategies:

1. **Core Modules** (`core`, `processor`, `gradle-plugin`) - Semantic versioning
2. **Elasticsearch DSL Module** - Version-aligned with Spring Data Elasticsearch

All artifacts are published to Maven Central under the `com.ekino.oss` group ID.

## Versioning Strategy

### Core Modules Versioning

**Format**: `v{MAJOR}.{MINOR}.{PATCH}`

**Examples**:
- `v1.0.0` - First official release
- `v1.1.0` - Minor feature addition
- `v1.0.1` - Patch/bugfix
- `v2.0.0` - Breaking changes

**Publishes**:
```
com.ekino.oss:metalastic-core:1.0.0
com.ekino.oss:metalastic-processor:1.0.0
com.ekino.oss:metalastic-gradle-plugin:1.0.0
```

### Elasticsearch DSL Module Versioning

**Format**: `elasticsearch-dsl-v{SPRING_DATA_ES_VERSION}-{DSL_VERSION}`

**Examples**:
- `elasticsearch-dsl-v5.0.12-1.0` - DSL v1.0 compatible with Spring Data ES 5.0.12
- `elasticsearch-dsl-v5.5.4-1.0` - DSL v1.0 compatible with Spring Data ES 5.5.4

**Publishes**:
```
com.ekino.oss:metalastic-elasticsearch-dsl:5.0.12-1.0
```

**Rationale**: The DSL module version is tied to the Spring Data Elasticsearch version it's compatible with, allowing consumers to match their dependency versions.

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
   - Watch the "Publish to Maven Central" workflow
   - Verify all steps complete successfully

3. **Verify publication**
   - Check Maven Central: https://central.sonatype.com/namespace/com.ekino.oss
   - Search for: `com.ekino.oss:metalastic-core:1.0.0`
   - Artifacts typically appear within 15-30 minutes

4. **Create GitHub Release**
   - Go to: https://github.com/ekino/Metalastic/releases
   - Click "Create a new release"
   - Select the tag you just created
   - Add release notes from `release-notes/RELEASE_NOTES_v1.0.0.md`
   - Publish the release

### Releasing Elasticsearch DSL Module

1. **Update DSL version** (if needed)

   Edit `modules/elasticsearch-dsl/build.gradle.kts`:
   ```kotlin
   val springDataEsVersion = "5.0.12"
   val dslVersion = "1.0"
   ```

2. **Create and push the tag**
   ```bash
   git tag elasticsearch-dsl-v5.0.12-1.0
   git push origin elasticsearch-dsl-v5.0.12-1.0
   ```

3. **Monitor and verify** (same as core modules)

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
- Tag push matching `v*` or `elasticsearch-dsl-v*`

**Actions**:
1. Detects tag type (core modules vs DSL)
2. Builds artifacts
3. Signs with GPG
4. Publishes to Maven Central
5. Creates GitHub Release automatically

**Required Secrets** (configured in GitHub repository settings):
- `ORG_GRADLE_PROJECT_mavenCentralUsername` - Maven Central username
- `ORG_GRADLE_PROJECT_mavenCentralPassword` - Maven Central token
- `ORG_GRADLE_PROJECT_signingInMemoryKey` - GPG private key (base64)
- `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` - GPG key password

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
1. Tag format matches `v*` or `elasticsearch-dsl-v*`
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
