# Metalastic v1.2.7 Release Notes

## ✨ Features

### `containsTerms` — `Collection<Enum<T>>` overload

Added the symmetric counterpart to v1.2.6's `terms(Collection<Enum<T>>)`: `Metamodel<out Collection<T>>.containsTerms(Collection<T>)` for `T : Enum<T>`. Closes the same JVM-erasure gap (Collection-of-enum couldn't satisfy the `Collection<FieldValue>` escape hatch) for the collection-field side of the API (#97).

```kotlin
product.statuses containsTerms listOf(Status.ACTIVE, Status.PENDING)
```

### `terms` / `containsTerms` — `Collection<String>` overloads

`String` is the most common runtime case (user input, query parameters, dynamic filters) where the `Collection<FieldValue>` escape hatch was previously the only option. Added direct `Collection<String>` support with the same `@JvmName` trick to dodge the JVM erasure clash with `Collection<FieldValue>`:

```kotlin
val countries: Set<String> = userInput.parseCountries()
product.country terms countries
product.tags    containsTerms userInput.tags
```

`Collection<String>` and `Collection<Enum<T>>` are now the two natively supported `Collection<T>` element types. `FieldValue` remains the escape hatch for other types — the deliberate foot-gun guard against arbitrary `Collection<Any>`.

### `BoolQueryDsl.minimumShouldMatch`

The DSL docs heavily reference `minimumShouldMatch(1)` inside the `boolQueryDsl { }` block, but the call only existed on the underlying `BoolQuery.Builder`. Surface added to `BoolQueryDsl` with both `String?` and `Int` overloads.

```kotlin
BoolQuery.of {
    it.boolQueryDsl {
        should + { /* … */ }
        minimumShouldMatch(1)
    }
}
```

## 📚 Documentation

### Documented `containsTerms`

The `containsTerms` family was completely undocumented prior to this release. Added a "Contains Terms (Collection Field)" subsection to the Query DSL guide covering vararg per type, the new enum and string Collection forms, the FieldValue escape hatch (cross-link), and the receiver-constraint behaviour.

### Doc-snippet correctness sweep

A two-agent audit probe-compiled every Kotlin snippet in `README.md`, `CLAUDE.md`, `docs/index.md`, and `docs/guide/*.md` against the real API. Many copy-paste-broken patterns were fixed:

- **`BoolQuery.of { boolQueryDsl { … } }`** (~30 occurrences) → `BoolQuery.of { it.boolQueryDsl { … } }`. Also fixed in the kdoc of `BoolQueryDsl.kt`.
- **`import com.metalastic.dsl.*`** → `com.ekino.oss.metalastic.elasticsearch.dsl` (the real package).
- **`wildcard` → `wildCard`** (camel C).
- **`exist(field)` → `field.exist()`** (no-arg extension).
- **`multiMatch(value, f1, f2, …)` → `listOf(f1, f2, …).multiMatch(value)`** (Collection extension). Same for `moreLikeThis`.
- **`shouldAtLeastOneOf { … }`** (wrong arg shape) → `disMax({ tieBreaker(…) }) { … }`.
- **`NestedQuery.of { path(metamodel) … }`** → the supported DSL helper `Container<*>.nested({ setupBlock }) { … }`.
- **`geoDistance GeoPoint.of {…} within "10km"`** → rewrote to the real `(latitude, longitude, distance, unit, block)` signature.
- **`product.createdAt mustBeBetween (start to end)`** → corrected to the real "value between two field bounds" signature.
- **`product.tags term/terms …`** on a `Collection<String>` field → `containsTerm`/`containsTerms`.
- Stale `1.0.1` artifact versions in `CLAUDE.md` → `1.2.7`.
- Plugin DSL property `className` → `registryClassName`.
- VitePress anchor slug fixed.
- Dead `PUBLISHING.md` link removed.

Every revised snippet was probe-compiled against the real test fixtures.

## ✅ No Breaking Changes

All additions are purely additive. The new `Collection<String>`, `Collection<Enum<T>>`, and `minimumShouldMatch` overloads do not affect existing call sites. Kotlin call-site syntax is unchanged — only JVM bytecode method names differ via `@JvmName`.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.8"
    id("com.ekino.oss.metalastic") version "1.2.7"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.7")
    ksp("com.ekino.oss:metalastic-processor:1.2.7")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.7")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
