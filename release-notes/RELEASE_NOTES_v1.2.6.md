# Metalastic v1.2.6 Release Notes

## ✨ Features

### Terms query — `Collection<Enum<T>>` overload

Added a type-safe `Metamodel<T>.terms(Collection<T>)` overload for `T : Enum<T>`, complementing the existing vararg enum overload and the `Collection<FieldValue>` escape hatch. The Kotlin call site keeps the familiar `field terms enumList` shape — `@JvmName` is used to dodge the JVM signature clash with the `Collection<FieldValue>` overload (#95).

```kotlin
product.status terms listOf(Status.ACTIVE, Status.PENDING)
```

Mirrored across all three DSL artifacts (`elasticsearch-dsl`, `-5.5`, `-5.3`).

## 📚 Documentation

### Clarified `terms` query — vararg vs `FieldValue` escape hatch (#96)

The DSL guide previously advertised several `terms` forms that did not compile (`field terms listOf("a", "b")`, `field terms setOf(...)`, `field terms collectionVariable`). Updated the affected snippets in `query-dsl-guide.md` and `examples.md` to use the actual API — the typed vararg form or, for runtime collections, an explicit `coll.map { FieldValue.of(it) }` — and added a new subsection explaining the design intent: the `FieldValue` boundary is deliberate so the DSL never silently `toString()`s arbitrary objects.

## 📦 Dependency Updates

### KSP 2.3.8

Upgraded `com.google.devtools.ksp` and `com.google.devtools.ksp:symbol-processing-api` from 2.3.7 to 2.3.8 (#92).

[KSP Releases →](https://github.com/google/ksp/releases)

### Gradle Wrapper 9.5.1

Upgraded the Gradle wrapper from 9.4.1 → 9.5.0 → 9.5.1 (#90, #94).

[Gradle Releases →](https://github.com/gradle/gradle/releases)

### Spotless 8.5.1

Upgraded `com.diffplug.spotless` from 8.4.0 to 8.5.1 (#93).

[Spotless Releases →](https://github.com/diffplug/spotless/releases)

### kotlin-logging-jvm 8.0.02

Upgraded `io.github.oshai:kotlin-logging-jvm` from 8.0.01 to 8.0.02 (#89).

[kotlin-logging Releases →](https://github.com/oshai/kotlin-logging/releases)

## 🧪 Maintenance

### Migrated KSP processor tests to KSP2-native test harness (#91)

Replaced `tschuchortdev/kotlin-compile-testing` 1.6.0 (capped at Kotlin 1.9.24) with `dev.zacsweers.kctfork` 0.12.1, which tracks current Kotlin and only ships KSP2 — matching the production processor runtime. This is an internal change with no effect on consumer code; three Java-record collecting-phase specs that exercise kctfork KSP2 gaps are skipped at the unit level and still validated end-to-end via the real Gradle KSP2 pipeline in `:modules:test`.

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.5. The new `terms(Collection<Enum<T>>)` overload is purely additive — existing code continues to compile and behave identically.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.8"
    id("com.ekino.oss.metalastic") version "1.2.6"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.6")
    ksp("com.ekino.oss:metalastic-processor:1.2.6")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.6")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
