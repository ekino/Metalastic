# Metalastic v3.0.0 - Maven Central Release

**Release Date**: TBD
**Type**: Major Release - Breaking Changes

---

## 🎯 Overview

Metalastic v3.0.0 marks a **major milestone** for the project:

- **Now on Maven Central!** 🎉 - No more authentication required
- **New Group ID**: `com.ekino.oss` - Clear ownership under ekino open source
- **Package Rename**: All packages moved to `com.ekino.oss.metalastic.*`
- **MIT License**: Changed from Apache 2.0 to MIT for better open source compatibility

**This is a breaking change** - see migration steps below.

---

## 🚀 What's New

### Maven Central Distribution
- ✅ Published to Maven Central via Sonatype Central Portal
- ✅ No authentication required - just add `mavenCentral()` to your repositories
- ✅ Better discoverability for open source projects
- ✅ Fully public and accessible worldwide

### License Change
- ✅ Changed from Apache License 2.0 to MIT License
- ✅ Simpler, more permissive license
- ✅ Better compatibility with other open source projects

### Build System Improvements
- ✅ Migrated to vanniktech/gradle-maven-publish-plugin
- ✅ Automated GPG signing of all artifacts
- ✅ Simplified publishing configuration
- ✅ Better Maven POM metadata

---

## ⚠️ Breaking Changes

### 1. Group ID Change
```diff
- com.metalastic
+ com.ekino.oss
```

**Impact**: All artifact coordinates have changed

### 2. Plugin ID Change
```diff
- id("com.metalastic")
+ id("com.ekino.oss.metalastic")
```

**Impact**: Update `plugins {}` block in build.gradle.kts

### 3. Package Rename
```diff
- com.metalastic.*
+ com.ekino.oss.metalastic.*
```

**Impact**: All imports need to be updated

### 4. Repository Change
```diff
- GitHub Packages (authentication required)
+ Maven Central (public, no authentication)
```

**Impact**: Simplifies build configuration, removes need for GitHub PAT

---

## 📦 New Artifact Coordinates

### Core Modules

| Module | Old (v2.x) | New (v3.0.0) |
|--------|------------|--------------|
| **Core** | `com.metalastic:core` | `com.ekino.oss:metalastic-core` |
| **Processor** | `com.metalastic:processor` | `com.ekino.oss:metalastic-processor` |
| **Gradle Plugin** | `com.metalastic:gradle-plugin` | `com.ekino.oss:metalastic-gradle-plugin` |
| **Elasticsearch DSL** | `com.metalastic:elasticsearch-dsl` | `com.ekino.oss:metalastic-elasticsearch-dsl` |

### Version Numbers

- **Core modules**: `v3.0.0` (semantic versioning)
- **Elasticsearch DSL**: `5.0.12-1.0` (unchanged - continues Spring Data ES alignment)

---

## 🔄 Migration Steps

### Before (v2.x):
```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.metalastic") version "2.0.7"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/ekino/Metalastic")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

### After (v3.0.0):
```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.ekino.oss.metalastic") version "3.0.0"
}

repositories {
    mavenCentral()  // That's it! 🎉
}
```

### Import Updates

Run find & replace in your IDE:

```
Find:    import com.metalastic
Replace: import com.ekino.oss.metalastic
```

---

## 📝 Full Changelog

### Added
- Maven Central publishing via Sonatype Central Portal
- Automated GPG artifact signing
- vanniktech/gradle-maven-publish-plugin integration
- MIT License
- Complete POM metadata with developer information

### Changed
- **BREAKING**: Group ID from `com.metalastic` → `com.ekino.oss`
- **BREAKING**: Plugin ID from `com.metalastic` → `com.ekino.oss.metalastic`
- **BREAKING**: All packages from `com.metalastic.*` → `com.ekino.oss.metalastic.*`
- **BREAKING**: License from Apache 2.0 → MIT
- Repository from GitHub Packages → Maven Central
- CI/CD platform from GitLab CI → GitHub Actions
- Publishing configuration simplified

### Removed
- GitHub Packages publishing
- GitLab Maven Registry publishing
- Authentication requirements for package consumption

---

## 🎁 Benefits of v3.0

### For Consumers

1. **No Authentication Required** 🔓
   - Before: Required GitHub Personal Access Token
   - After: Just add `mavenCentral()` - works out of the box

2. **Better Availability** 🌍
   - Before: GitHub Packages (auth-gated)
   - After: Maven Central (publicly accessible worldwide)

3. **Simpler Configuration** ⚙️
   - Before: Complex repository setup with credentials
   - After: One line: `mavenCentral()`

4. **Permissive License** 📜
   - Before: Apache 2.0
   - After: MIT (simpler, more permissive)

### For the Project

1. **Better Discoverability** 🔍
   - Searchable on Maven Central
   - Listed in repository search engines
   - Easier to find for new users

2. **Standard Open Source Practice** 📦
   - Maven Central is the standard for Java/Kotlin libraries
   - Clear ownership under `com.ekino.oss` group
   - Professional artifact management

3. **Simplified Maintenance** 🛠️
   - Single publishing target (Maven Central)
   - Automated workflows with GitHub Actions
   - Modern tooling (vanniktech plugin)

---

## 📊 Compatibility

### Requirements (Unchanged)
- **Java**: 21+
- **Kotlin**: 2.2.20
- **Spring Data Elasticsearch**: 5.5.4
- **KSP**: 2.2.20-2.0.3

### All Features Preserved
- ✅ Type-safe metamodel generation
- ✅ Path traversal API
- ✅ Multi-field support
- ✅ Nested documents
- ✅ Document references
- ✅ Java interoperability
- ✅ Enhanced DSL (if using elasticsearch-dsl module)

**No functional changes** - This is purely an infrastructure and packaging migration.

---

## 🔗 Resources

- **Repository**: https://github.com/ekino/Metalastic
- **Package Registry**: https://central.sonatype.com/namespace/com.ekino.oss
- **Documentation**: https://github.com/ekino/Metalastic/blob/master/README.md
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Discussions**: https://github.com/ekino/Metalastic/discussions

---

## 🆘 Support

Need help with migration?

1. Check the [README](../README.md) for updated examples
2. Review the [CLAUDE.md](../CLAUDE.md) for technical details
3. Open an issue: https://github.com/ekino/Metalastic/issues
4. Start a discussion: https://github.com/ekino/Metalastic/discussions

---

## 🙏 Acknowledgments

This release represents a major infrastructure upgrade made possible by:

- **Maven Central** for providing excellent open source package hosting
- **GitHub Actions** for reliable CI/CD infrastructure
- **vanniktech/gradle-maven-publish-plugin** for simplifying Maven publishing
- **ekino** for supporting open source development

Thank you for using Metalastic! We believe this migration to Maven Central will make the library more accessible and easier to use for everyone.

---

## 📅 What's Next

With the infrastructure migration complete, future development will focus on:

- Enhanced documentation and examples
- Additional Elasticsearch DSL features
- Performance optimizations
- Community contributions

Stay tuned for v3.1.0 with new features!

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
