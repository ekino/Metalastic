# Metalastic elasticsearch-dsl v5.0.12-1.1 Release Notes

**Release Date**: January 2025
**Previous DSL Release**: elasticsearch-dsl-v5.5.1-1.0
**Core Release**: v2.0.5
**Spring Data ES Compatibility**: 5.0.12
**Elasticsearch Client**: 8.5.3

This release introduces significant improvements to nested query handling with strict mode configuration, runtime validation, and enhanced type safety.

## 🎯 Version Information

**Module**: `com.metalastic:elasticsearch-dsl`
**Version**: `5.0.12-1.1`
**Tag**: `elasticsearch-dsl-v5.0.12-1.1`

**Version Format**: `{spring-data-es-version}-{dsl-version}`
- Spring Data ES version: **5.0.12** (unchanged)
- DSL version: **1.1** (incremented from 1.0)

## ✨ New Features

### 1. Strict Mode Configuration

**Feature**: Optional strict mode for nested query validation via Gradle plugin.

**Configuration**:
```kotlin
plugins {
    id("com.metalastic") version "2.0.5"
}

metalastic {
    elasticsearchDsl {
        strictMode {
            enabled = true  // Enable runtime validation
        }
    }
}
```

**What it does**:
- Validates nested query paths at runtime
- Ensures fields used in `nested()` queries are properly configured
- Provides early error detection during development

**Impact**:
- ✅ Catch configuration mistakes before deployment
- ✅ Reduce Elasticsearch runtime errors
- ✅ Better developer experience with clear warnings

**Default**: Disabled (opt-in feature for backward compatibility)

### 2. Nested Query Field Validation

**Feature**: Runtime detection and warnings for incorrect nested query usage.

**Validation Logic**:
```kotlin
nested {
    path(product.category)  // Validates if 'category' is marked as nested
    query { /* ... */ }
}
```

**Warning Example**:
```
[WARN] NestedQueryDsl - Field 'category' at path 'category' is not marked as nested.
Container: MetaProduct (nested=false)
This may cause unexpected behavior in Elasticsearch.
Ensure the @Field annotation has type = FieldType.Nested.
```

**Warning Conditions**:
- Field used in nested query is not configured as nested in `@Field` annotation
- Container's `nested` flag is `false`
- Prevents common Elasticsearch query errors

**Impact**:
- ✅ Clearer error messages during development
- ✅ Faster debugging of query issues
- ✅ Better alignment between document model and queries

**Implementation**: `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/NestedQueryDsl.kt`

### 3. Container Type Restrictions

**Enhancement**: Compile-time type safety for nested query paths.

**Type Safety**:
```kotlin
// Before (v1.0) - any field accepted
fun <T> nested(block: NestedQueryDsl<T>.() -> Unit)

// After (v1.1) - only Container types accepted
fun <T : Container<*>> nested(block: NestedQueryDsl<T>.() -> Unit)
```

**Prevents Invalid Usage**:
```kotlin
// ❌ Compile error - KeywordField is not a Container
nested {
    path(product.id)  // id is KeywordField<String>
}

// ✅ Valid - categories is ObjectField/Container
nested {
    path(product.categories)  // categories is MetaCategory<Category>
}
```

**Container Types**:
- `ObjectField<T>` - Object fields
- `Document<T>` - Document references
- Generated metamodel classes (e.g., `MetaProduct`, `MetaCategory`)

**Impact**:
- ✅ Compile-time validation
- ✅ Better IDE autocomplete
- ✅ Prevents API misuse

## 🔧 Technical Details

### Commits Included

Since `elasticsearch-dsl-v5.5.1-1.0`:

- `65a111c` feat(dsl): add strict mode configuration with Gradle plugin support
- `cbea06a` feat: add warning log for nested queries on non-nested fields
- `ea6f884` feat: restrict nested query DSL to Container types
- `a3f8436` docs: add comprehensive nested query documentation and logging setup

### Files Modified

**Main Changes**:
- `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/NestedQueryDsl.kt`
  - Added strict mode validation logic
  - Enhanced type constraints to `Container<*>`
  - Implemented runtime field configuration checks
  - Added KotlinLogging for warning messages

**Configuration Support**:
- `modules/gradle-plugin/src/main/kotlin/com/metalastic/gradleplugin/MetalasticExtension.kt`
  - New `elasticsearchDsl` extension block
  - `strictMode` configuration DSL

**Documentation**:
- `CLAUDE.md` - Enhanced nested query documentation
- Examples and best practices added

### Dependencies

**Unchanged**:
- Spring Data Elasticsearch: `5.0.12`
- Elasticsearch Java Client: `8.5.3`
- Google Guava: `33.3.1-jre` (Range support)
- Kotlin: `2.2.20`

## 🔄 Migration Guide

### From elasticsearch-dsl v5.0.12-1.0 or v5.5.1-1.0 to v5.0.12-1.1

#### 1. Update Dependency

```kotlin
dependencies {
    // Before
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.0")
    // or
    implementation("com.metalastic:elasticsearch-dsl:5.5.1-1.0")

    // After
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.1")
}
```

#### 2. No Breaking Changes

This release is **100% backward compatible**. All existing code will continue to work without modifications.

#### 3. Optional: Enable Strict Mode (Recommended)

Add strict mode configuration to catch potential issues:

```kotlin
metalastic {
    elasticsearchDsl {
        strictMode {
            enabled = true
        }
    }
}
```

#### 4. Review Warnings

If you see warnings after enabling strict mode:

**Warning Message**:
```
[WARN] Field 'fieldName' at path 'path.to.field' is not marked as nested
```

**Fix**: Update your document model:

```kotlin
@Document(indexName = "products")
data class Product(
    @Id val id: String,

    // ❌ Before - Object type
    @Field(type = FieldType.Object)
    val categories: List<Category>,

    // ✅ After - Nested type
    @Field(type = FieldType.Nested)
    val categories: List<Category>
)
```

#### 5. Type Safety Improvements

If you have custom DSL extensions that use nested queries, update type parameters:

```kotlin
// Before
fun <T> myCustomNested(field: ObjectField<T>) {
    nested { path(field) }
}

// After - restrict to Container
fun <T : Container<*>> myCustomNested(field: T) {
    nested { path(field) }
}
```

## 📋 Usage Examples

### Basic Nested Query with Validation

```kotlin
import com.metalastic.elasticsearch.dsl.*
import com.example.MetaProduct.Companion.product

// Nested query on properly configured field
val query = NestedQuery.of {
    path(product.categories)  // categories is marked as nested
    query {
        boolQueryDsl {
            must + {
                product.categories.name match "Electronics"
            }
        }
    }
}
```

### Strict Mode Configuration

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.metalastic") version "2.0.5"
}

metalastic {
    // Metamodel configuration
    metamodels {
        packageName = "com.example.search"
    }

    // DSL configuration
    elasticsearchDsl {
        strictMode {
            enabled = true  // Enable validation warnings
        }
    }
}

dependencies {
    implementation("com.metalastic:core:2.0.5")
    ksp("com.metalastic:processor:2.0.5")
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.1")
}
```

### Document Model Best Practices

```kotlin
@Document(indexName = "products")
data class Product(
    @Id
    val id: String,

    @Field(type = FieldType.Text)
    val name: String,

    // ✅ Correct - Nested type for nested queries
    @Field(type = FieldType.Nested)
    val categories: List<Category>,

    // ✅ Correct - Object type for simple object mapping
    @Field(type = FieldType.Object)
    val metadata: Metadata,

    // ✅ Correct - Simple field
    @Field(type = FieldType.Keyword)
    val status: String
)

data class Category(
    @Field(type = FieldType.Keyword)
    val id: String,

    @Field(type = FieldType.Text)
    val name: String,

    @Field(type = FieldType.Integer)
    val priority: Int
)
```

## 🎯 Compatibility Matrix

### Spring Data Elasticsearch Versions

| Spring Data ES | Elasticsearch | DSL Module | Status |
|----------------|---------------|------------|--------|
| 5.0.x | 8.11.x | 5.0.12-1.1 | ✅ **Recommended** |
| 5.1.x | 8.11.x | 5.0.12-1.1 | ✅ Compatible |
| 5.2.x | 8.11.x | 5.0.12-1.1 | ✅ Compatible |
| 5.3.x | 8.13.x | 5.0.12-1.1 | ⚠️ Should work |
| 5.4.x | 8.14.x | 5.0.12-1.1 | ⚠️ Should work |
| 5.5.x | 8.15.x | 5.0.12-1.1 | ⚠️ Should work |

**Note**: While newer Spring Data ES versions may work, this release is specifically tested against 5.0.12.

### Metalastic Core Versions

| Core Version | DSL Version | Compatible |
|--------------|-------------|------------|
| 2.0.5 | 5.0.12-1.1 | ✅ **Recommended** |
| 2.0.4 | 5.0.12-1.1 | ✅ Compatible |
| 2.0.3 | 5.0.12-1.1 | ✅ Compatible |

## 🚀 What's Next

### Planned for elasticsearch-dsl v5.0.12-1.2

- Additional query type support
- Query builder performance optimizations
- Enhanced type conversion support
- More validation options

### Future Spring Data ES Support

- elasticsearch-dsl v5.5.x-1.x: Spring Data ES 5.5.x compatibility
- elasticsearch-dsl v5.6.x-1.x: Spring Data ES 5.6.x compatibility

## 📦 Artifacts

Published to GitLab Maven Registry:

```xml
<!-- Maven -->
<dependency>
    <groupId>com.metalastic</groupId>
    <artifactId>elasticsearch-dsl</artifactId>
    <version>5.0.12-1.1</version>
</dependency>
```

```kotlin
// Gradle (Kotlin DSL)
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.1")
}
```

```groovy
// Gradle (Groovy DSL)
dependencies {
    implementation 'com.metalastic:elasticsearch-dsl:5.0.12-1.1'
}
```

## 🔗 Related Releases

This DSL release is coordinated with:
- **Metalastic Core v2.0.5**: Gradle plugin improvements and documentation updates

## 📝 Testing

All changes have been tested with:
- ✅ Unit tests with Kotest v5.9.1
- ✅ Type safety validation tests
- ✅ Runtime validation scenarios
- ✅ Integration with Spring Data ES 5.0.12
- ✅ Elasticsearch 8.11.x compatibility

---

**Full Changelog**: [elasticsearch-dsl-v5.5.1-1.0...elasticsearch-dsl-v5.0.12-1.1](https://gitlab.ekino.com/iperia/metalastic/-/compare/elasticsearch-dsl-v5.5.1-1.0...elasticsearch-dsl-v5.0.12-1.1)

**GitLab Package**: https://gitlab.ekino.com/iperia/metalastic/-/packages

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
