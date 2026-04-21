# Metalastic v1.2.4 Release Notes

## 🐛 Bug Fixes

### Java Record Support

Java records annotated with `@Document` are now handled correctly by the KSP processor (#84). Previously the generated `Meta*` class for a record was emitted with an empty field list because KSP2 does not expose record components as properties and `@Field`'s `@Target(FIELD/METHOD)` targets leave the annotation invisible to the existing property/getter walks.

The processor now pairs each annotated canonical-constructor parameter with its matching accessor function, and the graph builder explores both constructor parameters and zero-arg accessor functions for nested-type discovery. Object and nested record references (`@Field(Object)` / `@Field(Nested)`) are now followed correctly.

As a side-effect cleanup, a latent bug where `@MultiField` on interface getters was silently ignored has been fixed.

**Before (v1.2.3):**
```java
@Document(indexName = "product")
public record Product(
    @Field(type = FieldType.Keyword) String id,
    @Field(type = FieldType.Text) String title
) {}
```
→ `MetaProduct` generated with no fields.

**After (v1.2.4):**
→ `MetaProduct.id`, `MetaProduct.title` generated and path-traversable as expected.

## 📦 Dependency & Build Upgrades

### KotlinPoet 2.3.0

Upgraded from KotlinPoet 2.2.0 to 2.3.0 (code-generation group bump, #71).

[KotlinPoet Changelog →](https://github.com/square/kotlinpoet/blob/main/docs/changelog.md)

### Spotless 8.4.0

Upgraded from Spotless 8.3.0 to 8.4.0 (code-quality group bump, #68).

### Google Guava 33.6.0-jre

Upgraded from Guava 33.5.0-jre to 33.6.0-jre (#78).

### Gradle Wrapper 9.4.1

Upgraded from Gradle 9.4.0 to 9.4.1 (#69).

[Gradle 9.4.1 Release Notes →](https://docs.gradle.org/9.4.1/release-notes.html)

### Gradle Plugin Publish 2.1.1

Upgraded from 2.1.0 to 2.1.1 (#70).

## 🔧 CI / Maintenance

- Added the `github-actions` ecosystem to Dependabot configuration (#72), enabling automatic bumps for all reusable actions.
- Bumped GitHub Actions used in CI: `actions/checkout` 4→6 (#76), `actions/setup-java` 4→5 (#77), `actions/setup-node` 4→6 (#80), `actions/cache` 4→5 (#81), `actions/upload-artifact` 4→7 (#82), `actions/upload-pages-artifact` 3→5 (#83), `actions/configure-pages` 4→6 (#79), `actions/deploy-pages` 4→5 (#74), `dorny/test-reporter` 1→3 (#75), `softprops/action-gh-release` 2→3 (#73).

## ✅ No Breaking Changes

This release maintains full backward compatibility with v1.2.3. No migration steps required for consumers — Java record support is an additive bug fix: classes that already worked continue to work, and Java records that silently produced empty metamodels now produce correct ones.

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.ekino.oss.metalastic") version "1.2.4"
}
```

### Using Manual Dependencies

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.2.4")
    ksp("com.ekino.oss:metalastic-processor:1.2.4")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.2.4")
}
```

Then rebuild:

```bash
./gradlew clean build
```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
