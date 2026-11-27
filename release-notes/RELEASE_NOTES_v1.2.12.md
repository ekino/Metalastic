# Metalastic v1.2.12 Release Notes

This is a maintenance release rolling up a batch of dependency and build-toolchain updates. There are no code or API changes and no breaking changes.

## 🧰 Dependency Updates

### Kotlin & code generation
- **KSP** `2.3.13` → `2.3.14` (`com.google.devtools.ksp`) (#164)

### Build toolchain
- **Spotless** `8.10.4` → `8.11.0` (`com.diffplug.spotless`) (#160)
- **Gradle Wrapper** `9.9.0` → `9.9.2` (#163, #165)

### Logging
- **kotlin-logging** `8.0.4` → `8.0.7` (`io.github.oshai:kotlin-logging-jvm`) (#162)

### Tests
- **MockK** `1.14.11` → `1.14.12` (`io.mockk:mockk`) (#161)

## ✅ No Breaking Changes

This release contains only dependency and build-toolchain updates. The public API and the Query DSL are unchanged. No action is required beyond bumping the version.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.14"
    id("com.ekino.oss.metalastic") version "1.2.12"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.12")
    ksp("com.ekino.oss:metalastic-processor:1.2.12")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.12")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
