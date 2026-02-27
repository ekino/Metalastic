# Metalastic v1.2.2 Release Notes

## 📦 Dependency & Build Upgrades

This is a patch release focused on dependency updates and build tooling maintenance.

### Kotlin 2.3.10

Upgraded from Kotlin 2.3.0 to 2.3.10.

### KSP 2.3.6

Upgraded from KSP 2.3.4 to 2.3.6.

### Gradle Wrapper 9.3.1

Upgraded from Gradle 9.3.0 to 9.3.1.

[Gradle 9.3.1 Release Notes →](https://docs.gradle.org/9.3.1/release-notes.html)

### Detekt 2.0.0-alpha.2

Upgraded from detekt 2.0.0-alpha.1 to 2.0.0-alpha.2.

### Spotless 8.2.1

Upgraded from Spotless 8.1.0 to 8.2.1.

### MockK 1.14.9

Upgraded from MockK 1.14.7 to 1.14.9.

### JUnit Jupiter 6.0.3

Upgraded from JUnit Jupiter 6.0.2 to 6.0.3.

### Kotlin Logging 8.0.01

Upgraded from kotlin-logging 7.0.14 to 8.0.01.

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.1. No migration steps required for consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.ekino.oss.metalastic") version "1.2.2"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.2")
    ksp("com.ekino.oss:metalastic-processor:1.2.2")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.2")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
