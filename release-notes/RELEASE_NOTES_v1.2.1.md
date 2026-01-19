# Metalastic v1.2.1 Release Notes

## 📦 Dependency & Build Upgrades

This is a patch release focused on dependency updates and build infrastructure maintenance.

### Gradle 9.3.0

Upgraded from Gradle 9.1 to 9.3.0.

**Highlights:**
- Improved build performance
- Enhanced configuration cache support
- Various bug fixes and stability improvements

[Gradle 9.3.0 Release Notes →](https://docs.gradle.org/9.3.0/release-notes.html)

### Maven Publish Plugin 0.36.0

Upgraded from com.vanniktech.maven.publish 0.35.0 to 0.36.0.

### JUnit Jupiter

Updated to latest JUnit Jupiter version for improved testing support.

### Kotlin Logging JVM

Updated to latest kotlin-logging-jvm version.

### Documentation

Upgraded preact from 10.27.2 to 10.28.2 in the documentation site.

## 🔧 Repository Maintenance

### Branch Rename

The default branch has been renamed from `master` to `main`. Update your local repositories:

```bash
git branch -m master main
git fetch origin
git branch -u origin/main main
git remote set-head origin -a
```

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.0. No migration steps required for consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.4"
    id("com.ekino.oss.metalastic") version "1.2.1"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.1")
    ksp("com.ekino.oss:metalastic-processor:1.2.1")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.1")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
