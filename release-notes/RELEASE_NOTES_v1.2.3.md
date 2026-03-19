# Metalastic v1.2.3 Release Notes

## 🐛 Bug Fixes

### Detekt Findings & License Headers

- Resolved detekt findings and enforced license headers across the codebase (#66)

## 📦 Dependency & Build Upgrades

### Kotlin 2.3.20

Upgraded from Kotlin 2.3.10 to 2.3.20.

### Gradle Wrapper 9.4.0

Upgraded from Gradle 9.3.1 to 9.4.0.

[Gradle 9.4.0 Release Notes →](https://docs.gradle.org/9.4.0/release-notes.html)

### Spotless 8.3.0

Upgraded from Spotless 8.2.1 to 8.3.0.

### Gradle Plugin Publish 2.1.0

Upgraded from Gradle Plugin Publish 2.0.0 to 2.1.0.

### Rollup 4.59.0 (docs)

Upgraded from rollup 4.53.3 to 4.59.0 in the documentation site (#61).

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.2. No migration steps required for consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.ekino.oss.metalastic") version "1.2.3"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.3")
    ksp("com.ekino.oss:metalastic-processor:1.2.3")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.3")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
