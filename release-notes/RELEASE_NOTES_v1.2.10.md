# Metalastic v1.2.10 Release Notes

This is a maintenance release containing dependency and build-toolchain updates only. There are no changes to the runtime library, generated metamodel code, or the Query DSL.

## 🧰 Dependency Updates

### Kotlin toolchain
- **Kotlin** `2.4.0` → `2.4.10` (kotlin group) (#122, #126)
- **KSP** `2.3.9` → `2.3.11` (kotlin group) (#122, #126)

### Build toolchain
- **Spotless** `8.7.0` → `8.9.0` (`com.diffplug.spotless`) (#125)
- **detekt** `2.0.0-alpha.5` → `2.0.0-alpha.6` (`dev.detekt`) (#128)
- **Gradle Wrapper** `9.6.0` → `9.7.0` (#127)

### Tests
- **JUnit Jupiter** `6.1.0` → `6.1.2` (`org.junit.jupiter:junit-jupiter`) (#116, #121)

### CI
- **actions/setup-node** `6` → `7` (#124)

## 🛠️ Maintenance

- **Build:** Dropped the redundant `-Xcontext-parameters` compiler flag now that it is the default in the current Kotlin toolchain (#123).

## ✅ No Breaking Changes

All changes are dependency bumps and a build-config cleanup with no public API impact. Generated metamodel code and the Query DSL are unchanged. No action is required from consumers.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.11"
    id("com.ekino.oss.metalastic") version "1.2.10"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.10")
    ksp("com.ekino.oss:metalastic-processor:1.2.10")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.10")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
