# Metalastic v1.2.11 Release Notes

This is a maintenance release. It fixes field-type mapping for `Ip_Range` and `Rank_Features` in the annotation processor, adds a Dokka-generated API reference to the documentation site, and rolls up a batch of dependency and build-toolchain updates. There are no breaking changes.

## 🐛 Bug Fixes

- **Processor:** `Ip_Range` and `Rank_Features` fields now map to their dedicated field classes instead of a generic fallback (#141).
- **Docs:** Keep the API reference out of the sidebar to avoid a SPA 404 (#146).
- **Docs:** Show the latest release tag as the API reference version (#144).
- **Docs:** Override vite to 6.4.3 to clear npm security alerts (#142).

## 📖 Documentation

- Added a Dokka-generated API reference and documented null/empty DSL semantics (#140).
- Dropped the Gradle plugin from the API reference (#145).

## 🧰 Dependency Updates

### Kotlin & code generation
- **Kotlin** `2.4.10` → `2.4.21` (#136, #158)
- **KSP** `2.3.11` → `2.3.13` (#136, #158)
- **KotlinPoet** `2.3.0` → `2.4.0` (#137, #158)

### Build toolchain
- **Spotless** `8.9.0` → `8.10.4` (`com.diffplug.spotless`) (#132, #138, #149, #152)
- **Dokka** → `2.3.0` (`org.jetbrains.dokka`) (#157)
- **Gradle Plugin Publish** `2.1.1` → `2.2.1` (`com.gradle.plugin-publish`) (#139)
- **Gradle Wrapper** `9.7.0` → `9.9.0` (#134, #148, #155)

### Runtime
- **Guava** `33.6.0-jre` → `33.8.0-jre` (`com.google.guava:guava`) (#133, #150, #156)

### Tests
- **kotlin-compile-testing** `0.13.0` → `0.14.0` (#147)
- **JUnit Jupiter** `6.1.2` → `6.1.3` (`org.junit.jupiter:junit-jupiter`) (#131)

### CI
- **actions/setup-java** `v5` → `v6` (#135)
- **gradle/actions/setup-gradle** `v5` → `v6` (#143)

### Docs toolchain
- **@vue/server-renderer** & **vue** bumped in `/docs` (#159)
- **PostCSS** `8.5.15` → `8.5.26` in `/docs` (#130)
- **source-map-js** bumped in `/docs` (#154)

## ✅ No Breaking Changes

The only runtime change is the processor fix for `Ip_Range` / `Rank_Features` field mapping, which produces more accurate metamodels for those field types. The public API and the Query DSL are otherwise unchanged. Regenerate your metamodels (`./gradlew clean build`) to pick up the corrected field classes if you use those types.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.13"
    id("com.ekino.oss.metalastic") version "1.2.11"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.11")
    ksp("com.ekino.oss:metalastic-processor:1.2.11")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.11")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
