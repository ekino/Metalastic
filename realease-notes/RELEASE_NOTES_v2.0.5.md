# Metalastic v2.0.5 Release Notes

**Release Date**: January 2025
**Previous Release**: v2.0.4

This release brings important enhancements to the elasticsearch-dsl module with strict mode configuration support, improved nested query validation, and comprehensive documentation updates.

## ✨ New Features

### Gradle Plugin: Centralized Configuration Constants

**Enhancement**: All configuration constants are now centralized in `PluginConstants` object for better maintainability and consistency.

**Implementation**:
- `PluginConstants.Metamodels`: Default values for metamodel generation (`packageName`, `className`, `classPrefix`)
- `PluginConstants.Features`: Feature flags (`generateJavaCompatibility`, `generatePrivateClassMetamodels`)
- `PluginConstants.Reporting`: Debug reporting configuration

**Impact**:
- ✅ Single source of truth for all default values
- ✅ Easier maintenance and updates
- ✅ Better consistency across configuration layers

**Files Modified**: `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/PluginConstants.kt`

### DSL Module: Strict Mode Configuration

**Feature**: New Gradle plugin DSL extension for configuring elasticsearch-dsl strict mode behavior.

**Configuration**:
```kotlin
metalastic {
    elasticsearchDsl {
        strictMode {
            enabled = true  // Enable strict nested query validation
        }
    }
}
```

**Usage**:
```kotlin
// KSP argument automatically configured by Gradle plugin
ksp {
    arg("elasticsearch.dsl.strictMode", "true")
}
```

**Impact**:
- ✅ Type-safe configuration through Gradle plugin
- ✅ Runtime validation of nested query usage
- ✅ Better developer experience with early error detection

**Files Modified**:
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/MetalasticExtension.kt`
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/MetalasticPlugin.kt`

### DSL Module: Nested Query Validation

**Feature**: Runtime validation and warnings for incorrect nested query usage.

**What it does**:
- Detects when `nested()` query is used on non-nested fields
- Logs warnings with detailed field information
- Helps developers catch configuration mistakes early

**Example**:
```kotlin
// If product.category is NOT marked as nested in @Field annotation
query {
    nested {
        path(product.category)  // ⚠️ Warning logged
        query { /* ... */ }
    }
}

// Warning message:
// [WARN] Field 'category' is not marked as nested in @Field annotation.
// Nested queries on non-nested fields may not work as expected.
```

**Impact**:
- ✅ Prevents runtime Elasticsearch errors
- ✅ Clearer feedback during development
- ✅ Reduces debugging time

**Files Modified**: `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/NestedQueryDsl.kt`

### DSL Module: Container Type Restrictions

**Enhancement**: Nested query DSL now properly restricts to `Container` types only.

**Type Safety**:
```kotlin
// Only Container fields (ObjectField, Document) can be used in nested queries
fun <T : Container<*>> nested(block: NestedQueryDsl<T>.() -> Unit)

// Prevents invalid usage like:
nested {
    path(product.id)  // ❌ Compile error - KeywordField is not a Container
}
```

**Impact**:
- ✅ Compile-time safety for nested queries
- ✅ Prevents API misuse
- ✅ Better IDE autocomplete support

**Files Modified**: `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/NestedQueryDsl.kt`

## 📝 Documentation

### Comprehensive Architecture Documentation Overhaul

**Updates**:
- Complete rewrite of `CLAUDE.md` with v2.0+ architecture patterns
- New nested query documentation with examples
- Enhanced logging setup documentation
- Updated TAG_MANAGEMENT.md with dual versioning details
- Improved PROCESSOR_ARCHITECTURE.md

**New Sections**:
- Terminal object types (`SelfReferencingObject`, `UnModellableObject`)
- Generic type system with `T : Any?` parameters
- Runtime type tracking with `KType`
- MultiField support patterns
- Nested query best practices

**Impact**:
- ✅ Easier onboarding for new developers
- ✅ Clear migration paths for existing users
- ✅ Better understanding of design decisions

## 🔄 Migration Guide

### From v2.0.4 to v2.0.5

**No breaking changes** - this release is backward compatible.

#### Update Dependencies

**Core modules**:
```kotlin
dependencies {
    implementation("com.metalastic:core:2.0.5")
    ksp("com.metalastic:processor:2.0.5")
}
```

**Gradle plugin**:
```kotlin
plugins {
    id("com.metalastic") version "2.0.5"
}
```

#### Optional: Enable Strict Mode (Recommended)

If you're using the elasticsearch-dsl module, consider enabling strict mode for better validation:

```kotlin
metalastic {
    elasticsearchDsl {
        strictMode {
            enabled = true
        }
    }
}
```

**Note**: Strict mode is opt-in and disabled by default to maintain backward compatibility.

#### Review Nested Query Usage

If you see warnings like:
```
[WARN] Field 'fieldName' is not marked as nested in @Field annotation
```

**Fix**: Update your document class to mark the field as nested:

```kotlin
@Document(indexName = "products")
data class Product(
    @Id val id: String,

    // Before
    @Field(type = FieldType.Object)
    val categories: List<Category>,

    // After - add nested = true
    @Field(type = FieldType.Nested)
    val categories: List<Category>
)
```

## 📦 Artifacts

All artifacts are published to GitLab Maven Registry.

### Core Modules (v2.0.5)

- `com.metalastic:core:2.0.5`
- `com.metalastic:processor:2.0.5`
- `com.metalastic:gradle-plugin:2.0.5`

### elasticsearch-dsl Module

The elasticsearch-dsl module follows independent versioning:
- **Latest DSL version**: `5.0.12-1.1` (see separate release notes)
- **Spring Data ES compatibility**: 5.0.12

**Usage**:
```kotlin
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.1")
}
```

## 🎯 Version Matrix

### Core Modules

| Module | Version | Tag | Description |
|--------|---------|-----|-------------|
| core | 2.0.5 | v2.0.5 | Runtime library with metamodel base types |
| processor | 2.0.5 | v2.0.5 | KSP annotation processor |
| gradle-plugin | 2.0.5 | v2.0.5 | Gradle configuration plugin |

### Compatibility

| Spring Data ES | Elasticsearch | Metalastic Core | DSL Module | Status |
|----------------|---------------|-----------------|------------|--------|
| 5.5.x | 8.15.x | 2.0.5 | 5.0.12-1.1 | ✅ Full Support |
| 5.4.x | 8.14.x | 2.0.5 | 5.0.12-1.1 | ✅ Full Support |
| 5.3.x | 8.13.x | 2.0.5 | 5.0.12-1.1 | ✅ Full Support |
| 5.2.x | 8.11.x | 2.0.5 | 5.0.12-1.1 | ✅ Full Support |
| 5.0.x | 8.11.x | 2.0.5 | 5.0.12-1.1 | ✅ Full Support |

## 📝 Technical Details

### Commits Included

- `65a111c` feat(dsl): add strict mode configuration with Gradle plugin support
- `a3f8436` docs: add comprehensive nested query documentation and logging setup
- `cbea06a` feat: add warning log for nested queries on non-nested fields
- `ea6f884` feat: restrict nested query DSL to Container types
- `6cfa1e0` docs: comprehensive documentation overhaul for v2.0+ architecture
- `55ddf11` feat(gradle-plugin): centralize configuration constants in PluginConstants

### Files Modified

**Gradle Plugin**:
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/PluginConstants.kt` (new)
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/MetalasticExtension.kt`
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/MetalasticPlugin.kt`

**Elasticsearch DSL**:
- `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/NestedQueryDsl.kt`

**Documentation**:
- `CLAUDE.md`
- `TAG_MANAGEMENT.md`
- `PROCESSOR_ARCHITECTURE.md`
- Various documentation files

### Testing

All changes have been tested with:
- ✅ Unit tests (Kotest v5.9.1)
- ✅ Integration tests in test module
- ✅ Gradle plugin configuration tests
- ✅ Local Maven publication verification

## 🔗 Related Releases

This release is coordinated with:
- **elasticsearch-dsl v5.0.12-1.1**: New DSL features and validation (separate release notes)

## 🚀 What's Next

Planned for future releases:
- Spring Data ES 5.5.x support in DSL module
- Additional query type support
- Performance optimizations
- Enhanced IDE integration

---

**Full Changelog**: [v2.0.4...v2.0.5](https://gitlab.ekino.com/iperia/metalastic/-/compare/v2.0.4...v2.0.5)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
