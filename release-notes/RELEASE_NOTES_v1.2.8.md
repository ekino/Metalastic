# Metalastic v1.2.8 Release Notes

This is a maintenance release containing dependency updates only. The headline change is the upgrade to **Kotlin 2.4.0** and **KSP 2.3.9**.

## 🧰 Dependency Updates

### Build toolchain
- **Kotlin** `2.3.21` → `2.4.0` (`org.jetbrains.kotlin.jvm`) (#106)
- **KSP** `2.3.8` → `2.3.9` (`com.google.devtools.ksp`, incl. `symbol-processing-api` / `symbol-processing-common-deps`) (#106)
- **Spotless** `8.5.1` → `8.6.0` (`com.diffplug.spotless`) (#102)

### Runtime / logging
- **kotlin-logging-jvm** `8.0.02` → `8.0.4` (`io.github.oshai`) (#100, #104)

### Test dependencies
- **kotlin-compile-testing** `0.12.1` → `0.13.0` (`dev.zacsweers.kctfork:core`) (#105)
- **MockK** `1.14.9` → `1.14.11` (`io.mockk`) (#103)
- **JUnit Jupiter** `6.0.3` → `6.1.0` (`org.junit.jupiter`) (#99)

## ✅ No Breaking Changes

All changes are dependency bumps with no public API impact. Generated metamodel code and the Query DSL are unchanged. The Kotlin 2.4.0 / KSP 2.3.9 upgrade is transparent to consumers — Kotlin's binary compatibility guarantees apply, and KSP 2.3.9 is a patch update.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
    id("com.ekino.oss.metalastic") version "1.2.8"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.8")
    ksp("com.ekino.oss:metalastic-processor:1.2.8")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.8")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
