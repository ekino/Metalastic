# Metalastic elasticsearch-dsl v5.0.12-1.2 Release Notes

**Release Date**: October 2025
**Previous DSL Release**: elasticsearch-dsl-v5.0.12-1.1
**Core Release**: v2.0.6
**Spring Data ES Compatibility**: 5.0.12
**Elasticsearch Client**: 8.5.3

This release simplifies nested query handling by removing the strict mode configuration feature. Validation now uses a simpler always-on warning approach that works consistently across all environments.

## 🎯 Version Information

**Module**: `com.metalastic:elasticsearch-dsl`
**Version**: `5.0.12-1.2`
**Tag**: `elasticsearch-dsl-v5.0.12-1.2`

**Version Format**: `{spring-data-es-version}-{dsl-version}`
- Spring Data ES version: **5.0.12** (unchanged)
- DSL version: **1.2** (incremented from 1.1)

## 🔄 Breaking Changes

### Removal of Strict Mode

**Change**: The strict mode system property `metalastic.dsl.strict` has been removed.

**Reason**: Configuration complexity in Spring Boot test contexts due to JVM property initialization timing. The simpler warning-based approach provides sufficient developer guidance.

**Before (v1.1)**:
```kotlin
// Gradle plugin configuration (no longer supported)
metalastic {
    dsl {
        strictMode = true
    }
}

// Or system property (no longer supported)
java -Dmetalastic.dsl.strict=true -jar application.jar
```

**After (v1.2)**:
```kotlin
// No configuration needed - warnings always enabled
```

## ✨ Changes

### 1. Simplified Nested Query Validation

**Enhancement**: Nested queries now always warn when used on non-nested fields, without requiring configuration.

**Behavior Changes**:

| Aspect | v1.1 (with strict mode) | v1.2 (simplified) |
|--------|------------------------|-------------------|
| Configuration | Required `strictMode = true` | None required |
| Default behavior | Warn only | Always warn |
| Strict behavior | Throw exception | N/A (removed) |
| Spring Boot tests | Complex configuration | Works automatically |
| Production | Requires system property | Works automatically |

**Code Example**:
```kotlin
import com.metalastic.elasticsearch.dsl.*
import com.example.MetaProduct.Companion.product

// If product.categories is NOT marked with @Field(type = FieldType.Nested)
query {
    nested {
        path(product.categories)
        query {
            must {
                product.categories.name match "Electronics"
            }
        }
    }
}

// Always logs warning (no configuration needed):
// [WARN] Nested query used on non-nested field 'categories'.
// The field should be marked with @Field(type = FieldType.Nested) in the
// Elasticsearch mapping. The query will be applied as a regular bool query instead.
```

**Benefits**:
- ✅ **Simpler API**: No configuration required
- ✅ **Better UX**: Warnings instead of exceptions
- ✅ **Consistent**: Works the same in all environments
- ✅ **Graceful**: Falls back to regular bool query instead of failing

### 2. Implementation Details

**Removed**:
- System property check: `System.getProperty("metalastic.dsl.strict")`
- Conditional error/warn logic in `nested()` function
- `StrictModeTest.kt` (98 lines)

**Simplified**:
- `nested()` function now always logs warnings for non-nested fields
- No more conditional exception throwing
- Reduced code complexity by 21 lines

**Files Modified**:
- `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/QueryVariantDsl.kt`
  - Removed `strictMode` companion property
  - Simplified `nested()` to always warn

## 🔧 Technical Details

### Commits Included

Since `elasticsearch-dsl-v5.0.12-1.1`:

- `e581f11` Merge branch 'feat/remove-strict-mode' into 'master'
- `8aac02c` feat: remove strict mode configuration, simplify nested query validation

### Files Modified

**Main Changes**:
- `modules/elasticsearch-dsl/src/main/kotlin/com/metalastic/elasticsearch/dsl/QueryVariantDsl.kt`
  - Removed `strictMode` companion property (system property check)
  - Simplified `nested()` function to always warn
  - Reduced code complexity (17 lines removed, 4 lines added)

**Documentation**:
- `modules/elasticsearch-dsl/README.md`
  - Removed "Strict Mode" section (92 lines)
  - Kept logging configuration examples

**Tests**:
- `modules/elasticsearch-dsl/src/test/kotlin/com/metalastic/elasticsearch/dsl/StrictModeTest.kt`
  - Deleted entirely (98 lines)

### Dependencies

**Unchanged**:
- Spring Data Elasticsearch: `5.0.12`
- Elasticsearch Java Client: `8.5.3`
- Google Guava: `33.3.1-jre` (Range support)
- Kotlin: `2.2.20`

## 🔄 Migration Guide

### From elasticsearch-dsl v5.0.12-1.1 to v5.0.12-1.2

#### 1. Update Dependency

```kotlin
dependencies {
    // Before
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.1")

    // After
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.2")
}
```

#### 2. Remove Strict Mode Configuration

If you had strict mode configured via Gradle plugin:

```kotlin
// REMOVE THIS BLOCK:
metalastic {
    dsl {
        strictMode = true
    }
}
```

#### 3. Remove System Properties

If you were passing system properties:

```bash
# REMOVE THIS:
# java -Dmetalastic.dsl.strict=true -jar application.jar

# Or in Gradle:
# tasks.test {
#     systemProperty("metalastic.dsl.strict", "true")
# }
```

#### 4. Control Warning Visibility

Warnings are now always enabled. To control their visibility, use your logging framework:

**Spring Boot** (`application.yml`):
```yaml
logging:
  level:
    com.metalastic.elasticsearch.dsl: WARN  # Show warnings (recommended)
    # Or suppress:
    # com.metalastic.elasticsearch.dsl: ERROR
```

**Logback** (`logback.xml`):
```xml
<configuration>
  <logger name="com.metalastic.elasticsearch.dsl" level="WARN"/>
</configuration>
```

#### 5. Fix Incorrect Mappings

If you see warnings, update your document model:

```kotlin
@Document(indexName = "products")
data class Product(
    @Id val id: String,

    @Field(type = FieldType.Text)
    val name: String,

    // ❌ Before - causes warnings
    @Field(type = FieldType.Object)
    val categories: List<Category>,

    // ✅ After - no warnings
    @Field(type = FieldType.Nested)
    val categories: List<Category>
)

data class Category(
    @Field(type = FieldType.Keyword)
    val id: String,

    @Field(type = FieldType.Text)
    val name: String
)
```

## 📋 Usage Examples

### Basic Nested Query (No Configuration Needed)

```kotlin
import com.metalastic.elasticsearch.dsl.*
import com.example.MetaProduct.Companion.product

// Nested query on properly configured field
val query = NestedQuery.of {
    path(product.categories)  // categories is marked as @Field(type = FieldType.Nested)
    query {
        boolQueryDsl {
            must + {
                product.categories.name match "Electronics"
                product.categories.priority greaterThan 5
            }
        }
    }
}
// ✅ No warnings - field is properly configured
```

### Handling Warnings

```kotlin
// If field is NOT nested
val query = NestedQuery.of {
    path(product.metadata)  // metadata is @Field(type = FieldType.Object)
    query {
        boolQueryDsl {
            must + {
                product.metadata.key match "value"
            }
        }
    }
}
// ⚠️ Warning logged: "Nested query used on non-nested field 'metadata'"
// Query still executes as regular bool query (graceful degradation)
```

## 🎯 Compatibility Matrix

### Spring Data Elasticsearch Versions

| Spring Data ES | Elasticsearch | DSL Module | Status |
|----------------|---------------|------------|--------|
| 5.0.x | 8.11.x | 5.0.12-1.2 | ✅ **Recommended** |
| 5.1.x | 8.11.x | 5.0.12-1.2 | ✅ Compatible |
| 5.2.x | 8.11.x | 5.0.12-1.2 | ✅ Compatible |
| 5.3.x | 8.13.x | 5.0.12-1.2 | ⚠️ Should work |
| 5.4.x | 8.14.x | 5.0.12-1.2 | ⚠️ Should work |
| 5.5.x | 8.15.x | 5.0.12-1.2 | ⚠️ Should work |

**Note**: While newer Spring Data ES versions may work, this release is specifically tested against 5.0.12.

### Metalastic Core Versions

| Core Version | DSL Version | Compatible |
|--------------|-------------|------------|
| 2.0.6 | 5.0.12-1.2 | ✅ **Recommended** |
| 2.0.5 | 5.0.12-1.2 | ✅ Compatible |
| 2.0.4 | 5.0.12-1.2 | ✅ Compatible |

## 📊 Code Metrics

- **Lines removed**: 21 (net: 17 removed, 4 added)
- **Test lines removed**: 98
- **Documentation lines removed**: 92
- **Configuration complexity**: 100% reduction (no config needed)
- **Total simplification**: 210+ lines removed

## 🚀 What's Next

### Planned for elasticsearch-dsl v5.0.12-1.3+

- Additional query type support (filters, aggregations)
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
    <version>5.0.12-1.2</version>
</dependency>
```

```kotlin
// Gradle (Kotlin DSL)
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.2")
}
```

```groovy
// Gradle (Groovy DSL)
dependencies {
    implementation 'com.metalastic:elasticsearch-dsl:5.0.12-1.2'
}
```

## 🔗 Related Releases

This DSL release is coordinated with:
- **Metalastic Core v2.0.6**: Removes strict mode configuration from Gradle plugin

## 📝 Testing

All changes have been tested with:
- ✅ Unit tests with Kotest v5.9.1
- ✅ Nested query validation tests
- ✅ Logging output verification
- ✅ Integration with Spring Data ES 5.0.12
- ✅ Elasticsearch 8.11.x compatibility
- ✅ Backward compatibility tests

---

**Full Changelog**: [elasticsearch-dsl-v5.0.12-1.1...elasticsearch-dsl-v5.0.12-1.2](https://gitlab.ekino.com/iperia/metalastic/-/compare/elasticsearch-dsl-v5.0.12-1.1...elasticsearch-dsl-v5.0.12-1.2)

**GitLab Package**: https://gitlab.ekino.com/iperia/metalastic/-/packages

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
