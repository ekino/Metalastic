# Metalastic v1.2.9 Release Notes

This is a maintenance release containing dependency and build-toolchain updates only. There are no changes to the runtime library, generated metamodel code, or the Query DSL.

## 🧰 Dependency Updates

### Build toolchain
- **Spotless** `8.6.0` → `8.7.0` (`com.diffplug.spotless`) (#109)
- **detekt** `2.0.0-alpha.3` → `2.0.0-alpha.5` (`dev.detekt`) (#108, #111)
- **Gradle Wrapper** `9.5.1` → `9.6.0` (#113)
- **Maven Publish plugin** `0.36.0` → `0.37.0` (`com.vanniktech.maven.publish`) (#110)

### CI
- **actions/checkout** `6` → `7` (#112)

### Documentation (dev)
- **PostCSS** `8.5.6` → `8.5.15` (`/docs`, dev dependency) (#114)

## ✅ No Breaking Changes

All changes are dependency bumps with no public API impact. Generated metamodel code and the Query DSL are unchanged. No action is required from consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
    id("com.ekino.oss.metalastic") version "1.2.9"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.9")
    ksp("com.ekino.oss:metalastic-processor:1.2.9")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.9")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
