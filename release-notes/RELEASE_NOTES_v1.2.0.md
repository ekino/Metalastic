# Metalastic v1.2.0 Release Notes

## 📦 Dependency Upgrades

This release focuses on keeping dependencies up-to-date with the latest stable versions.

### Kotlin 2.3.0

Upgraded from Kotlin 2.2.20 to 2.3.0.

**Highlights:**
- Improved K2 compiler performance
- Enhanced type inference

[Kotlin 2.3.0 Release Notes →](https://kotlinlang.org/docs/whatsnew23.html)

### KSP 2.3.4

Upgraded from KSP 2.2.20-2.0.3 to 2.3.4.

**Note:** KSP adopted a new independent versioning scheme in November 2025. The version no longer mirrors the Kotlin version directly.

### Spring Data Elasticsearch 6.0.1

Upgraded from Spring Data Elasticsearch 6.0.0 to 6.0.1 (patch release with bug fixes).

### detekt 2.0.0-alpha.1

Upgraded from detekt 1.23.8 to 2.0.0-alpha.1.

**Migration notes for contributors:**
- Plugin ID changed from `io.gitlab.arturbosch.detekt` to `dev.detekt`
- Extension class moved to `dev.detekt.gradle.extensions.DetektExtension`
- Some configuration property names updated in `detekt.yml`

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.1.0. No migration steps required for consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.4"
    id("com.ekino.oss.metalastic") version "1.2.0"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.0")
    ksp("com.ekino.oss:metalastic-processor:1.2.0")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.0")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
