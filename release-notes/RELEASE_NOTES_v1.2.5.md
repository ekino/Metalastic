# Metalastic v1.2.5 Release Notes

## 📦 Dependency Updates

### Kotlin 2.3.21

Upgraded from Kotlin 2.3.20 to 2.3.21 (#86).

[Kotlin 2.3.21 Changelog →](https://github.com/JetBrains/kotlin/blob/master/ChangeLog.md)

### KSP 2.3.7

Upgraded `com.google.devtools.ksp` and `com.google.devtools.ksp:symbol-processing-api` from 2.3.6 to 2.3.7 (#86).

[KSP Releases →](https://github.com/google/ksp/releases)

### Detekt 2.0.0-alpha.3

Upgraded `dev.detekt` from 2.0.0-alpha.2 to 2.0.0-alpha.3 (#87).

[Detekt 2.0.0-alpha.3 Release Notes →](https://github.com/detekt/detekt/releases/tag/v2.0.0-alpha.3)

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.4. No migration steps required for consumers — this is a routine maintenance release that bumps Kotlin, KSP, and Detekt to their latest patch versions.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.7"
    id("com.ekino.oss.metalastic") version "1.2.5"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.5")
    ksp("com.ekino.oss:metalastic-processor:1.2.5")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.5")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
