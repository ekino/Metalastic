# Metalastic v2.0.4 Release Notes

**Release Date**: January 2025
**Previous Release**: v2.0.3

This is a maintenance release that fixes CI artifacts upload in GitLab pipelines.

## 🔧 Bug Fixes

### Fixed CI Artifacts Upload Path

**Issue**: GitLab CI pipelines were showing warnings when trying to upload build artifacts:
```
WARNING: */build/libs/: no matching files
ERROR: No files to upload
```

**Root Cause**: The artifact path pattern `"*/build/libs/"` didn't match the multi-module Gradle project structure.

**Fix**: Updated `.gitlab-ci.main.kts` to use correct paths:
- Build job: `"modules/*/build/libs/*.jar"` and `"modules/*/build/libs/*.pom"`
- Publish job: `"modules/*/build/libs/*.jar"` and `"modules/*/build/libs/*.pom"`

**Impact**:
- ✅ No more warning messages in pipeline logs
- ✅ Build artifacts properly uploaded to GitLab
- ✅ JARs and POMs downloadable from pipeline UI
- ✅ Cleaner pipeline output

**Note**: This was a cosmetic issue only - Maven package publication was never affected. All previous versions (v2.0.3 and earlier) were successfully published to GitLab Maven Registry.

## 📦 Artifacts

All artifacts are published to GitLab Maven Registry.

### Core Modules (v2.0.4)

- `com.metalastic:core:2.0.4`
- `com.metalastic:processor:2.0.4`
- `com.metalastic:gradle-plugin:2.0.4`
- `com.metalastic:elasticsearch-dsl:2.0.4`

### elasticsearch-dsl Versioning Note

The elasticsearch-dsl module is available in two versions:
- **`elasticsearch-dsl:2.0.4`** - Version tied to core release (this release)
- **`elasticsearch-dsl:5.5.1-1.0`** - Separate DSL versioning (released with v2.0.3)

Both versions are identical in functionality. Use `5.5.1-1.0` if you want version clarity about Spring Data ES compatibility.

## 🔄 Migration Guide

### From v2.0.3 to v2.0.4

**No code changes required** - this is a pure CI/CD infrastructure fix.

Simply update your dependency versions:

```kotlin
dependencies {
    implementation("com.metalastic:core:2.0.4")
    ksp("com.metalastic:processor:2.0.4")
}
```

Or use the Gradle plugin:

```kotlin
plugins {
    id("com.metalastic") version "2.0.4"
}
```

## 🎯 Version Matrix

### Core Modules

| Module | Version | Tag | Description |
|--------|---------|-----|-------------|
| core | 2.0.4 | v2.0.4 | DSL runtime library |
| processor | 2.0.4 | v2.0.4 | KSP annotation processor |
| gradle-plugin | 2.0.4 | v2.0.4 | Gradle plugin |
| elasticsearch-dsl | 2.0.4 | v2.0.4 | Query building DSL |

### Compatibility

| Spring Data ES | Elasticsearch | Metalastic | Status |
|----------------|---------------|------------|--------|
| 5.5.x | 8.15.x | 2.0.4 | ✅ Full Support |
| 5.4.x | 8.14.x | 2.0.4 | ✅ Full Support |
| 5.3.x | 8.13.x | 2.0.4 | ✅ Full Support |
| 5.2.x | 8.11.x | 2.0.4 | ✅ Full Support |

## 📝 Technical Details

### Files Modified

- `.gitlab-ci.main.kts`: Updated artifact paths in build and publish jobs

### CI/CD Improvements

**Before**:
```kotlin
artifacts {
    paths("*/build/libs/")
}
```

**After**:
```kotlin
artifacts {
    paths(
        "modules/*/build/libs/*.jar",
        "modules/*/build/libs/*.pom"
    )
}
```

This ensures the glob pattern matches the actual Gradle multi-module structure and captures both JAR and POM files.

---

**Full Changelog**: [v2.0.3...v2.0.4](https://gitlab.ekino.com/iperia/metalastic/-/compare/v2.0.3...v2.0.4)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
