# Metalastic

A type-safe metamodel library for Elasticsearch in Kotlin that generates compile-time field definitions from Spring Data Elasticsearch document classes.

[![GitHub Actions](https://github.com/ekino/Metalastic/workflows/Build%20and%20Test/badge.svg)](https://github.com/ekino/Metalastic/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.ekino.oss/metalastic-core)](https://central.sonatype.com/namespace/com.ekino.oss)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-purple)](https://kotlinlang.org/)
[![Spring Data ES](https://img.shields.io/badge/Spring%20Data%20ES-5.5.4-green)](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)

## Table of Contents

- [Overview](#overview)
- [Why Metalastic?](#why-metalastic)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [How It Works](#how-it-works)
- [Enhanced Query Building DSL](#enhanced-query-building-dsl)
- [Examples](#examples)
- [Advanced Features](#advanced-features)
  - [Smart Field Name Resolution](#smart-field-name-resolution)
  - [Multi-field Support](#multi-field-support)
  - [Document-to-Document References](#document-to-document-references)
  - [Complex Type Support](#complex-type-support)
  - [Self-Referencing Documents](#self-referencing-documents)
  - [Unmodellable Objects](#unmodellable-objects)
  - [Generated KDoc with Field Hierarchy](#generated-kdoc-with-field-hierarchy)
  - [Discovery and Iteration](#discovery-and-iteration)
- [Configuration](#configuration)
- [Supported Field Types](#supported-field-types)
- [Compatibility Matrix](#compatibility-matrix)
- [Troubleshooting](#troubleshooting)
- [Development](#development)
- [Publishing](#publishing)
- [Roadmap](#roadmap)
- [License](#license)

## Overview

Metalastic provides **compile-time code generation** to create type-safe, fluent field definitions for Elasticsearch documents. Inspired by QueryDSL's approach for SQL databases, this library generates Meta-classes for Spring Data Elasticsearch `@Document` annotated classes, enabling type-safe field access and path construction.

### Why Metalastic?

- 🚫 **No more string-based field names** - Compile-time validation prevents typos
- 🔍 **IDE auto-completion** - Full IntelliSense support for nested document structures
- 🛡️ **Type safety** - Compile-time verification of field types and relationships
- 📦 **Zero runtime dependencies** - Generated code uses only your existing Spring Data ES
- 🔄 **Automatic updates** - Regenerated when document classes change
- 🎯 **QueryDSL-inspired** - Familiar API for developers coming from JPA/QueryDSL

## Key Features

- ✅ **Type-safe field access** - `document.name.path()` instead of `"name"`
- ✅ **Smart field naming** - Uses `@Field(name)` when it follows Kotlin conventions
- ✅ **Nested object support** - `document.address.city.path()` for nested structures
- ✅ **Path traversal** - Automatic dotted notation for Elasticsearch queries
- ✅ **All field types** - Complete support for every Elasticsearch field type
- ✅ **Multi-field support** - Handle `@MultiField` annotations with inner fields
- ✅ **Complex type support** - Terminal ObjectField generation for complex generics
- ✅ **Document references** - Type-safe cross-document relationships
- ✅ **Centralized registry** - `Metamodels` object for discovering all documents
- ✅ **Java compatibility** - Works seamlessly with Java projects
- ✅ **Version agnostic** - Runtime detection of Spring Data ES capabilities
- ✅ **Enhanced DSL** - Innovative `clause + { }` syntax for fluent query building
- ✅ **Advanced range queries** - Google Guava Range integration with mathematical notation

## Quick Start

### 1. Apply Required Plugins

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"  // Required for code generation
    id("com.ekino.oss.metalastic") version "3.0.0"
}

repositories {
    mavenCentral()  // That's it! No special configuration needed
}

// Dependencies are automatically added by the Metalastic plugin
```

**Important**: Both KSP and Metalastic plugins are required. The KSP plugin performs the code generation, while the Metalastic plugin simplifies configuration and adds necessary dependencies.

### 3. Configure Metamodels (Optional)

```kotlin
// build.gradle.kts
metalastic {
    metamodels {
        main {
            packageName = "com.example.search.metamodels"
            className = "SearchMetamodels"
        }
        test {
            packageName = "com.example.test.metamodels"
            className = "TestMetamodels"
        }
        fallbackPackage = "com.example.metamodels"
        fallbackClassName = "GlobalMetamodels"
    }

    generateJavaCompatibility = true // default: true
    generatePrivateClassMetamodels = false // default: false
    reportingPath = "build/reports/metalastic/processor-report.md"
}
```

### Alternative: Manual KSP Setup

If you prefer not to use the plugin, you can configure manually:

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:3.0.0")
    ksp("com.ekino.oss:metalastic-processor:3.0.0")
}

ksp {
    arg("metamodels.main.package", "com.example.search.metamodels")
    arg("metamodels.main.className", "SearchMetamodels")
    arg("metalastic.generatePrivateClassMetamodels", "false")
    arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
```

### 3. Use Generated Code

```kotlin
// Access type-safe field paths
val document = Metamodels.person
document.name.path() shouldBe "name"
document.address.city.path() shouldBe "address.city"

// Use in Elasticsearch queries
SearchSourceBuilder()
    .query(
        QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery(document.name.path(), "John"))
            .filter(QueryBuilders.rangeQuery(document.age.path()).gte(25))
    )
```

## Enhanced Query Building DSL

The `elasticsearch-dsl` module provides an innovative query building DSL with type-safe, fluent syntax:

### Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:5.0.12-1.0")
}
```

**Version Format**: `{spring-data-es-version}-{dsl-version}`
- **5.0.12** = Compatible with Spring Data Elasticsearch 5.0.12
- **1.0** = elasticsearch-dsl module version 1.0

### Innovative `clause + { }` Syntax

The DSL provides **two equivalent syntaxes** for building queries - choose the one that fits your style:

#### Modern Operator Syntax (`clause + { }`)

Clean, operator-overloaded syntax for intuitive query building:

```kotlin
// Modern DSL with operator overloading
builder.boolQueryDsl {
    must + {
        document.name match "elasticsearch"
        document.status term "active"
    }
    mustNot + { document.status term "deleted" }
    should + { document.priority beGreaterThanEqualTo 5 }
    filter + { document.category term "tutorial" }
}
```

**Key Benefits:**
- ✨ **Operator overloading** - Uses `+` operator for natural clause composition
- 🎯 **Type-safe** - Full compile-time validation of field types and operations
- 📖 **Readable** - Closely mirrors Elasticsearch JSON query structure
- 🔗 **Composable** - Easy to combine multiple clauses and conditions

#### Classical Method Syntax (`mustDsl { }`)

Traditional method-based syntax for those who prefer explicit naming:

```kotlin
// Classical DSL with explicit method names
builder.boolQueryDsl {
    mustDsl {
        document.name match "elasticsearch"
        document.status term "active"
    }
    mustNotDsl { document.status term "deleted" }
    shouldDsl { document.priority beGreaterThanEqualTo 5 }
    filterDsl { document.category term "tutorial" }
}
```

**Key Benefits:**
- 📝 **Explicit naming** - Clear intent with `mustDsl`, `shouldDsl`, etc.
- 🔍 **Searchable** - Easy to find in code with standard method names
- 🎓 **Familiar** - Traditional method call syntax
- 🔄 **Interchangeable** - Use alongside operator syntax in the same query

Both syntaxes produce identical queries and offer the same type safety and features. Choose based on your team's preference!

### Advanced Range Queries with Google Guava

Mathematical notation for range queries using Google Guava Range:

```kotlin
import com.google.common.collect.Range

builder.boolQueryDsl {
    must + {
        // Traditional Google Guava Range syntax
        document.price range Range.closedOpen(10.0, 100.0)  // [10, 100)
        document.score range Range.atLeast(0.8)             // [0.8, ∞)
        document.age range Range.lessThan(65)               // (-∞, 65)

        // Convenient comparison operators
        document.priority greaterThanEqualTo 5   // >= 5
        document.priority lowerThanEqualTo 10    // <= 10
        document.score greaterThan 0.5           // > 0.5
        document.age lowerThan 65                // < 65

        // Mathematical range notation with StartBound
        document.price range 10.0.fromInclusive()..100.0    // [10, 100]
        document.price range 10.0.fromInclusive()..<100.0   // [10, 100)
        document.price range 10.0.fromExclusive()..100.0    // (10, 100]
        document.price range 10.0.fromExclusive()..<100.0   // (10, 100)

        // Partial bounds with StartBound
        document.price range 10.0.fromInclusive()..null     // [10, ∞)
        document.price range null.fromInclusive()..100.0    // (-∞, 100]
    }
}
```

**StartBound Features:**
- 📐 **Mathematical notation** - `fromInclusive()` for `[`, `fromExclusive()` for `(`
- 🎯 **Kotlin operators** - `..` for closed upper bound, `..<` for open upper bound
- ∞ **Unbounded ranges** - Use `null` for infinity (`null..100` or `10..null`)
- 🔧 **Type-safe** - Compile-time validation of comparable types

### Complete DSL Features

Both syntaxes support the full range of Elasticsearch query capabilities:

```kotlin
val builder = BoolQuery.Builder()

// Using modern operator syntax
builder.boolQueryDsl {
    // Must clauses (all conditions required)
    must + {
        metamodel.name match "John Doe"
        metamodel.status term "active"
        metamodel.score beGreaterThanEqualTo 0.8
    }

    // Should clauses (boost scoring)
    should + {
        metamodel.category term "premium"
        metamodel.tags.termsQuery(listOf("important", "featured"))
    }

    // Must not clauses (exclusions)
    mustNot + {
        metamodel.status term "deleted"
        metamodel.flags.term("spam")
    }

    // Filter clauses (no scoring impact)
    filter + {
        metamodel.timestamp beInRange Range.atLeast(yesterday)
        metamodel.region term currentRegion
    }

    // Nested queries with type safety
    must + {
        metamodel.address.nestedQuery {
            must + {
                metamodel.address.city term "Paris"
                metamodel.address.country term "France"
            }
        }
    }
}

// Or using classical method syntax
builder.boolQueryDsl {
    mustDsl {
        metamodel.name match "John Doe"
        metamodel.status term "active"
        metamodel.score beGreaterThanEqualTo 0.8
    }

    shouldDsl {
        metamodel.category term "premium"
        metamodel.tags.termsQuery(listOf("important", "featured"))
    }

    mustNotDsl {
        metamodel.status term "deleted"
        metamodel.flags.term("spam")
    }

    filterDsl {
        metamodel.timestamp beInRange Range.atLeast(yesterday)
        metamodel.region term currentRegion
    }

    // Mix both syntaxes in the same query
    must + {
        metamodel.address.nestedQuery {
            mustDsl {
                metamodel.address.city term "Paris"
                metamodel.address.country term "France"
            }
        }
    }
}

val query = Query(builder.build())
```

### Spring Data Integration

Seamless integration with Spring Data Elasticsearch:

```kotlin
import org.springframework.data.elasticsearch.client.elc.NativeQuery

val nativeQuery = NativeQuery.builder()
    .withQuery(Query(builder.build()))
    .withMaxResults(20)
    .withMinScore(0.5f)
    .build()

// Use with ElasticsearchOperations
val results = elasticsearchOperations.search(nativeQuery, Document::class.java)
```

## How It Works

```mermaid
graph LR
    A[Spring Data ES<br>@Document Classes] --> B[Metalastic<br>Annotation Processor]
    B --> C[Generated<br>Meta-Classes]
    C --> D[Type-safe<br>Query Building]

    A1[@Document Person] --> B
    A2[@Field annotations] --> B
    A3[Nested objects] --> B

    B --> C1[MetaPerson.kt]
    B --> C2[Metamodels.kt]
    B --> C3[Object fields]
```

**Compilation Process:**
1. **Scan** for `@Document` annotated classes
2. **Parse** `@Field` annotations and nested structures
3. **Generate** type-safe Meta-classes with field definitions
4. **Create** centralized `Metamodels` registry
5. **Compile** everything together for immediate use

## Examples

### Input: Spring Data Elasticsearch Document

```java
@Document(indexName = "person")
public class Person {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Integer)
    private Integer age;

    @Field(type = FieldType.Object)
    private Address address;

    @Field(type = FieldType.Nested)
    private List<Activity> activities;
}

public class Address {
    @Field(type = FieldType.Text)
    private String city;

    @Field(type = FieldType.Keyword)
    private String country;
}

public class Activity {
    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Date)
    private Date timestamp;
}
```

### Generated: Type-safe Metamodels

#### Document Class
```kotlin
class MetaPerson(
    parent: ObjectField? = null,
    fieldName: String = "",
    nested: Boolean = false,
) : Index("person", parent, fieldName, nested) {

    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")

    @JvmField
    val name: TextField<String> = textField<String>("name")

    @JvmField
    val age: IntegerField<Int> = integerField<Int>("age")

    @JvmField
    val address: MetaAddress = MetaAddress(this, "address", false)

    @JvmField
    val activities: MetaActivity = MetaActivity(this, "activities", true) // nested = true
}
```

#### Object Field Classes
```kotlin
class MetaAddress(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {

    @JvmField
    val city: TextField<String> = textField<String>("city")

    @JvmField
    val country: KeywordField<String> = keywordField<String>("country")
}

class MetaActivity(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {

    @JvmField
    val name: TextField<String> = textField<String>("name")

    @JvmField
    val timestamp: DateField<Date> = dateField<Date>("timestamp")
}
```

#### Centralized Registry
```kotlin
@Generated("com.ekino.oss.metalastic.processor.MetalasticSymbolProcessor")
data object Metamodels {
    /**
     * Metamodel for @Document class [com.example.Person]
     */
    @JvmField
    val person: MetaPerson = MetaPerson()

    /**
     * Returns all available metamodels for discovery and iteration.
     */
    fun entries(): List<Index> = listOf(person)
}
```

### Usage: Type-safe Field Access

```kotlin
// Access document metamodel
val document = Metamodels.person

// Root level fields
document.name.path() shouldBe "name"
document.age.path() shouldBe "age"

// Object fields with dotted notation
document.address.city.path() shouldBe "address.city"
document.address.country.path() shouldBe "address.country"

// Nested fields (automatically detected)
document.activities.name.path() shouldBe "activities.name"
document.activities.timestamp.path() shouldBe "activities.timestamp"

// Enhanced path information
document.address.city.isNestedPath() shouldBe false        // object field
document.activities.name.isNestedPath() shouldBe true      // nested field
document.activities.name.nestedPaths() shouldBe listOf("activities")
```

### Usage: Elasticsearch Queries

```kotlin
// Type-safe query construction
val person = Metamodels.person

val searchRequest = SearchRequest()
    .indices(person.indexName)
    .source(
        SearchSourceBuilder()
            .query(
                QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery(person.name.path(), "John"))
                    .filter(QueryBuilders.rangeQuery(person.age.path()).gte(25))
                    .filter(QueryBuilders.termQuery(person.address.country.path(), "USA"))
            )
            .sort(person.age.path(), SortOrder.DESC)
    )

// Nested queries with type safety
val nestedQuery = QueryBuilders.nestedQuery(
    "activities",
    QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(person.activities.name.path(), "programming"))
        .filter(QueryBuilders.rangeQuery(person.activities.timestamp.path()).gte("2024-01-01")),
    ScoreMode.Avg
)
```

## Advanced Features

### Smart Field Name Resolution

Metalastic intelligently resolves property names from `@Field(name)` annotations, following Kotlin naming conventions:

```java
@Document(indexName = "example")
public class ExampleDocument {
    // Uses annotation name when it follows camelCase conventions
    @Field(type = FieldType.Boolean, name = "active")
    private boolean isActive;

    // Keeps original property name for non-conventional names
    @Field(type = FieldType.Text, name = "search_content")
    private String searchableText;

    // Keeps original property name for invalid identifiers
    @Field(type = FieldType.Text, name = "987field")
    private String numericField;
}
```

Generates:
```kotlin
class MetaExampleDocument(/* ... */) : Document<ExampleDocument>(/* ... */) {
    // ✅ Uses "active" - follows camelCase convention
    @JvmField
    val active: BooleanField<Boolean> = boolean("active")

    // ✅ Keeps "searchableText" - "search_content" doesn't follow camelCase
    @JvmField
    val searchableText: TextField<String> = text("search_content")

    // ✅ Keeps "numericField" - "987field" is invalid identifier
    @JvmField
    val numericField: TextField<String> = text("987field")
}
```

**Benefits:**
- 🎯 **Convention-compliant** - Generated properties follow Kotlin camelCase standards
- 🔄 **Dual-name system** - Annotation names used for Elasticsearch, property names for Kotlin
- 🛡️ **Safe fallback** - Invalid or non-conventional names keep original property names
- 📝 **IDE-friendly** - Better code completion and consistency

### Multi-field Support

Metalastic handles `@MultiField` annotations with inner fields:

```java
@MultiField(
    mainField = @Field(type = FieldType.Text),
    otherFields = {
        @InnerField(suffix = "keyword", type = FieldType.Keyword),
        @InnerField(suffix = "search", type = FieldType.Text, analyzer = "search_analyzer")
    }
)
private String title;
```

Generates:

```kotlin
class MetaTitleMultiField(
    parent: ObjectField?,
    path: String,
) : MultiField<TextField<String>>(parent, TextField(parent, path)) {

    @JvmField
    val keyword: KeywordField<String> = keywordField<String>("keyword")

    @JvmField
    val search: TextField<String> = textField<String>("search")
}

// Usage:
val doc = Metamodels.article
doc.title.path() shouldBe "title"                    // main field
doc.title.keyword.path() shouldBe "title.keyword"   // inner field
doc.title.search.path() shouldBe "title.search"     // inner field
```

### Document-to-Document References

Cross-document relationships are fully supported:

```java
@Document(indexName = "order")
public class Order {
    @Field(type = FieldType.Object)
    private Customer customer; // References another @Document class
}

@Document(indexName = "customer")
public class Customer {
    @Field(type = FieldType.Text)
    private String name;
}
```

Generates type-safe cross-references:

```kotlin
val order = Metamodels.order
order.customer.name.path() shouldBe "customer.name"
```

### Complex Type Support

Metalastic intelligently handles edge cases where full Meta-class generation isn't possible or necessary, using specialized terminal object classes.

#### Self-Referencing Documents

When a document references itself (circular dependency), Metalastic generates a `SelfReferencingObject` to prevent infinite recursion:

```java
@Document(indexName = "category")
public class Category {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Object)
    private Category parentCategory;  // Self-reference ∞
}
```

Generates:

```kotlin
class MetaCategory<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType
) : Document<T>(...) {

    @JvmField
    val id: KeywordField = keywordField("id")

    @JvmField
    val name: TextField = textField("name")

    // ∞ Self-reference - terminates here to avoid infinite recursion
    @JvmField
    val parentCategory: SelfReferencingObject<Category?> =
        SelfReferencingObject(this, "parentCategory", false, typeOf<Category?>())
}
```

**Usage:**
```kotlin
val category = Metamodels.category
category.parentCategory.path() shouldBe "parentCategory"  // ✅ Path construction works
category.parentCategory.isNestedPath() shouldBe false     // ✅ Nested detection works
// category.parentCategory.name  // ❌ Cannot traverse further (prevents infinite recursion)
```

#### Unmodellable Objects

For complex generic types, collections with unknown structure, or external library types, Metalastic generates `UnModellableObject`:

```java
@Document(indexName = "product")
public class Product {
    @Field(type = FieldType.Keyword)
    private String id;

    // Complex types that cannot be modeled at compile-time
    @Field(type = FieldType.Object)
    private Map<String, Any> metadata;  // 🚫 Dynamic structure

    @Field(type = FieldType.Nested)
    private Map<String, Set<Integer>> tags;  // 🚫 Complex generics
}
```

Generates:

```kotlin
class MetaProduct<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType
) : Document<T>(...) {

    @JvmField
    val id: KeywordField = keywordField("id")

    // 🚫 Unmodellable - structure unknown at compile-time
    @JvmField
    val metadata: UnModellableObject<Map<String, Any>> =
        UnModellableObject(this, "metadata", false, typeOf<Map<String, Any>>())

    // 🚫 Unmodellable - complex nested generics
    @JvmField
    val tags: UnModellableObject<Map<String, Set<Integer>>> =
        UnModellableObject(this, "tags", true, typeOf<Map<String, Set<Integer>>>())
}
```

**Common scenarios for UnModellableObject:**
- 🚫 Dynamic collections: `Map<String, Any>`, `Map<String, *>`
- 🚫 Complex generics: `Map<String, List<Set<T>>>`
- 🚫 External library types without `@Field` annotations
- 🚫 Collections with unknown element structure

**Benefits of terminal objects:**
- ✅ **Type-safe field access** - `document.metadata.path() == "metadata"`
- ✅ **Proper nested detection** - Automatic `nested=true` for `FieldType.Nested`
- ✅ **Path traversal support** - Full dotted notation compatibility
- ✅ **Clear indicators** - ∞ for self-referencing, 🚫 for unmodellable
- ✅ **No compilation errors** - Graceful handling of edge cases
- ✅ **Runtime type safety** - Preserves type information with `KType`

### Generated KDoc with Field Hierarchy

Metalastic generates comprehensive KDoc documentation for all metamodel classes, including a **visual field hierarchy** that provides an at-a-glance view of your document structure.

#### Visual Tree Representation

Every generated Meta-class includes a field hierarchy tree in its KDoc. Here's an example of what the generated documentation looks like:

**Generated Class:**
```kotlin
class MetaPerson<T : Any?>(...) : Document<T>(...) {
    @JvmField val id: KeywordField
    @JvmField val firstName: TextField
    @JvmField val email: KeywordField MultiField
    // ... other fields
}
```

**Generated KDoc:**

The class documentation includes a visual tree showing all fields:

```
Metamodel for Elasticsearch index `person`.

This class was automatically generated by Metalastic annotation processor
from the source class [com.example.Person].

## Field Hierarchy

MetaPerson<T>
├── id: KeywordField
├── firstName: TextField
├── lastName: KeywordField
├── age: IntegerField
├── email: KeywordField MultiField
│   ├── keyword: KeywordField
│   ├── search: TextField
│   └── sortable: KeywordField
├── metadata: UnModellableObject 🚫
├── parentPerson: SelfReferencingObject ∞
├── address: Address
│   └── ...Address structure
└── activities: Activity (nested)
    └── ...Activity structure (nested)

**Do not modify this file directly.** Any changes will be overwritten
during the next compilation. To modify the metamodel structure, update the
annotations on the source document class.

@see com.example.Person
```

#### Hierarchy Indicators

The field hierarchy uses clear visual indicators:

| Indicator | Meaning | Example |
|-----------|---------|---------|
| `TextField` | Simple field type | `name: TextField` |
| `KeywordField MultiField` | Multi-field with main type | `email: KeywordField MultiField` |
| `Address` | Object field (traversable) | `address: Address` |
| `(nested)` | Nested field marker | `activities: Activity (nested)` |
| `🚫` | Unmodellable object | `metadata: UnModellableObject 🚫` |
| `∞` | Self-referencing field | `parent: SelfReferencingObject ∞` |
| `└── ...Structure` | Indicates nested structure | `└── ...Address structure` |

#### Benefits

- 📖 **Quick overview** - Understand document structure at a glance
- 🎯 **IDE integration** - Visible in hover tooltips and documentation views
- 🔍 **Easy navigation** - Click through to nested structures
- 🚦 **Clear indicators** - Immediately see field types and special cases
- 📝 **Self-documenting** - Generated docs stay in sync with code

### Discovery and Iteration

Use the `Metamodels.entries()` function for runtime discovery:

```kotlin
// Find all available metamodels
val allMetamodels = Metamodels.entries()

// Programmatic access
val indexNames = Metamodels.entries().map { it.indexName }

// Dynamic query building
fun buildSearchAcrossAllIndices(term: String): SearchRequest {
    val indices = Metamodels.entries().map { it.indexName }.toTypedArray()
    return SearchRequest(*indices)
        .source(SearchSourceBuilder().query(QueryBuilders.queryStringQuery(term)))
}
```

## Configuration

### Gradle Plugin DSL (Recommended)

The Gradle plugin provides a **type-safe, discoverable DSL** for configuration:

```kotlin
// build.gradle.kts
metalastic {
    metamodels {
        // Source-set specific configuration
        main {
            packageName = "com.example.search.metamodels"
            className = "SearchMetamodels"
            classPrefix = "Meta"  // default: "Meta"
        }
        test {
            packageName = "com.example.test.metamodels"
            className = "TestMetamodels"
            classPrefix = "Test"  // Custom prefix for test classes
        }

        // Global defaults (applied when source-set specific config is not set)
        packageName = "com.example.metamodels"
        className = "GlobalMetamodels"
        classPrefix = "Meta"  // default: "Meta"
    }

    generateJavaCompatibility = true  // default: true
    generatePrivateClassMetamodels = false  // default: false
    reportingPath = "build/reports/metalastic/processor-report.md"
}
```

### Manual KSP Configuration

For projects that prefer direct KSP configuration:

```kotlin
ksp {
    // Source-set specific configuration
    arg("metamodels.main.package", "com.example.search.metamodels")
    arg("metamodels.main.className", "SearchMetamodels")
    arg("metamodels.main.classPrefix", "Meta")  // default: "Meta"

    arg("metamodels.test.package", "com.example.test.metamodels")
    arg("metamodels.test.className", "TestMetamodels")
    arg("metamodels.test.classPrefix", "Test")  // Custom prefix for test classes

    // Global defaults (applied when source-set specific config is not set)
    arg("metamodels.package", "com.example.metamodels")
    arg("metamodels.className", "GlobalMetamodels")
    arg("metamodels.classPrefix", "Meta")  // default: "Meta"

    // Feature toggles
    arg("metalastic.generateJavaCompatibility", "true")  // default: true
    arg("metalastic.generatePrivateClassMetamodels", "false")  // default: false

    // Debug reporting (generates detailed build reports)
    arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `metamodels.{sourceSet}.package` | Custom package for specific source set | `com.ekino.oss.metalastic.metamodels.{sourceSet}` |
| `metamodels.{sourceSet}.className` | Custom class name for specific source set | `Metamodels` |
| `metamodels.{sourceSet}.classPrefix` | Custom class prefix for specific source set | `Meta` |
| `metamodels.package` | Global default package | `com.ekino.oss.metalastic` |
| `metamodels.className` | Global default class name | `Metamodels` |
| `metamodels.classPrefix` | Global default class prefix | `Meta` |
| `metalastic.generateJavaCompatibility` | Add `@JvmField` for Java interop | `true` |
| `metalastic.generatePrivateClassMetamodels` | Generate metamodels for private `@Document` classes | `false` |
| `metalastic.reportingPath` | Path for debug reports (relative to project root) | disabled |

### Configuration Resolution Strategy

Metalastic uses a **hierarchical configuration resolution** for metamodel generation. Source-set specific configurations take precedence over global defaults:

1. **Source-set specific** configuration (highest priority)
2. **Global defaults** (metamodels.packageName, className, classPrefix)
3. **Built-in defaults** (lowest priority)

**Example: Understanding Configuration Inheritance**

```kotlin
metalastic {
    metamodels {
        // Global defaults - apply to all source sets unless overridden
        packageName = "com.example.metamodels"
        className = "GlobalMetamodels"
        classPrefix = "Q"  // Use QueryDSL-style prefix globally

        // Main source set - inherits global classPrefix="Q"
        main {
            packageName = "com.example.search.metamodels"  // Override package
            className = "SearchMetamodels"                  // Override class name
            // classPrefix inherited from global: "Q"
        }

        // Test source set - inherits all global defaults
        test {
            // packageName inherited: "com.example.metamodels"
            // className inherited: "GlobalMetamodels"
            // classPrefix inherited: "Q"
        }

        // Custom source set - inherits global classPrefix
        sourceSet("integrationTest") {
            packageName = "com.example.integration.metamodels"
            className = "IntegrationMetamodels"
            // classPrefix inherited from global: "Q"
        }
    }
}
```

**Generated Classes:**
- Main: `com.example.search.metamodels.QIndexPerson` (uses "Q" prefix from global)
- Test: `com.example.metamodels.QTestDocument` (uses "Q" prefix from global)
- IntegrationTest: `com.example.integration.metamodels.QIntegrationDocument` (uses "Q" prefix from global)

**Complete Override Example:**

```kotlin
metalastic {
    metamodels {
        classPrefix = "Q"  // Global default

        main {
            classPrefix = "Meta"  // Override for main source set only
        }
    }
}
```

- Main classes: `MetaIndexPerson` (overridden)
- Test classes: `QTestDocument` (inherited from global)

### Private Class Handling

By default, Metalastic **excludes private classes** from metamodel generation to keep generated code clean and focused on publicly accessible documents. When other classes reference private classes in their fields, the types are automatically replaced with `UnExposablePrivateClass` for type safety.

```kotlin
// Private classes are excluded by default (recommended)
@Document(indexName = "internal_audit")
private class InternalAuditDocument { /* ... */ }
// → No MetaInternalAuditDocument generated

// But references to private classes are handled safely
@Document(indexName = "public_report")
class PublicReport {
    @Field(type = FieldType.Object)
    val auditData: InternalAuditDocument // Private class reference
}
// → Generates: ObjectField<UnExposablePrivateClass>
```

To **include private classes** in generation (not recommended for most use cases):

**Using Gradle Plugin DSL:**
```kotlin
metalastic {
    generatePrivateClassMetamodels = true
}
```

**Using Manual KSP Configuration:**
```kotlin
ksp {
    arg("metalastic.generatePrivateClassMetamodels", "true")
}
```

### Processor Reporting

Metalastic provides comprehensive reporting to help you understand the code generation process, troubleshoot issues, and optimize build performance.

#### When to Use Reporting

- 🐛 **Debugging generation issues** - Understand why specific fields or classes aren't generating
- ⚡ **Performance optimization** - Identify slow processing phases
- 📊 **Build analysis** - Track what's being generated and when
- 🔍 **Documentation** - Keep a record of metamodel generation for your project
- 🚀 **CI/CD insights** - Monitor processor performance in pipelines

#### Enabling Reporting

**Using Gradle Plugin DSL (Recommended):**
```kotlin
metalastic {
    reporting {
        enabled = true  // Uses default path: build/reports/metalastic/processor-report.md
    }
}
```

**With Custom Path:**
```kotlin
metalastic {
    reporting {
        enabled = true
        outputPath = "docs/metalastic/generation-report.md"
    }
}
```

**Using Manual KSP Configuration:**
```kotlin
ksp {
    arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
```

**Note**: With the Gradle plugin, you only need to set `enabled = true` - the default output path is automatically configured to `build/reports/metalastic/processor-report.md`.

#### Report Contents

The generated markdown report includes:

1. **📋 Table of Contents** - Quick navigation across multiple compilation runs
2. **⏱️ Performance Metrics** - Detailed timing for each processing phase:
   - `COLLECTING` - Scanning and discovering @Document classes
   - `BUILDING` - Creating metamodel representations
   - `WRITING` - Generating Kotlin source files
   - `COMPLETE` - Final validation and cleanup
3. **📊 Generation Statistics**:
   - Number of @Document classes discovered
   - Number of Meta-classes generated
   - Total fields processed
   - Files written to disk
4. **🔍 Detailed Processing Log**:
   - Per-property field type detection
   - Nested object resolution
   - Import conflict resolution
   - Path construction details
5. **❌ Error Diagnostics** - Clear error messages with context
6. **📈 Historical Tracking** - Multiple compilation runs in the same report

#### Example Report

```markdown
# Metalastic Processor Reports

## 📋 Table of Contents
- [Report 1 - 2025-01-18 15:06:17](#report-1---2025-01-18-150617) - ✅ Success - Duration: 254ms - 12 Meta-classes
- [Report 2 - 2025-01-18 15:12:33](#report-2---2025-01-18-151233) - ✅ Success - Duration: 58ms - 12 Meta-classes

---

## Report 1 - 2025-01-18 15:06:17

### ⏱️ Performance Metrics
| Phase | Duration (ms) | Percentage |
|-------|---------------|------------|
| COLLECTING | 196 | 77% |
| BUILDING | 25 | 10% |
| WRITING | 32 | 13% |
| **TOTAL** | **253** | **100%** |

### 📊 Generation Statistics
- **Documents Discovered**: 12
- **Meta-classes Generated**: 12
- **Object Fields Generated**: 34
- **Total Fields Processed**: 156
- **Files Written**: 13

### 🔍 Detailed Processing Log

#### Phase: COLLECTING (196ms)
[15:06:17.138] 🔍 DEBUG: Starting document discovery
[15:06:17.142] 🔍 DEBUG: Found @Document class: com.example.Person
[15:06:17.145] 🔍 DEBUG: Found @Document class: com.example.Address
[15:06:17.148] 🔍 DEBUG: Processing property 'name' with @Field(type = Text)
[15:06:17.151] 🔍 DEBUG: Processing property 'email' with @MultiField
[15:06:17.154] 🔍 DEBUG: Detected inner field: email.keyword (Keyword)
[15:06:17.157] 🔍 DEBUG: Processing nested object: activities (FieldType.Nested)
[15:06:17.379] ✅ SUCCESS: Discovered 12 models to process

#### Phase: BUILDING (25ms)
[15:06:17.380] 🔍 DEBUG: Building metamodel for Person
[15:06:17.383] 🔍 DEBUG: Generating field: id (KeywordField)
[15:06:17.386] 🔍 DEBUG: Generating field: name (TextField)
[15:06:17.389] 🔍 DEBUG: Generating multi-field: email
[15:06:17.392] 🔍 DEBUG: Generating nested object: activities
[15:06:17.404] ✅ SUCCESS: Built 12 metamodel representations

#### Phase: WRITING (32ms)
[15:06:17.405] 🔍 DEBUG: Writing MetaPerson.kt
[15:06:17.408] 🔍 DEBUG: Writing MetaAddress.kt
[15:06:17.411] 🔍 DEBUG: Writing MetaActivity.kt
[15:06:17.414] 🔍 DEBUG: Writing Metamodels.kt with 12 entries
[15:06:17.437] ✅ SUCCESS: Wrote 13 Kotlin files

#### Phase: COMPLETE (0ms)
[15:06:17.438] ✅ SUCCESS: Code generation complete

### 📝 Generated Files
1. `com.example.metamodels.MetaPerson`
2. `com.example.metamodels.MetaAddress`
3. `com.example.metamodels.MetaActivity`
4. `com.example.metamodels.MetaOrder`
   ... (8 more)
12. `com.example.metamodels.Metamodels`

---

## Report 2 - 2025-01-18 15:12:33

### ⏱️ Performance Metrics
| Phase | Duration (ms) | Percentage |
|-------|---------------|------------|
| COLLECTING | 45 | 78% |
| BUILDING | 8 | 14% |
| WRITING | 5 | 8% |
| **TOTAL** | **58** | **100%** |

*(Incremental compilation - much faster!)*
```

#### Interpreting Performance Metrics

**COLLECTING Phase (typically 60-80% of time)**:
- Scans source files for @Document classes
- Parses @Field annotations
- Resolves type hierarchies
- **Optimization tip**: Minimize unnecessary Spring Data ES annotations

**BUILDING Phase (typically 10-20% of time)**:
- Creates internal metamodel representations
- Resolves field types and relationships
- Handles complex generics and nested structures
- **Optimization tip**: Flatten deeply nested structures if possible

**WRITING Phase (typically 10-20% of time)**:
- Generates Kotlin source code
- Optimizes imports
- Writes files to disk
- **Fast with incremental compilation**

#### Using Reports for Troubleshooting

**Missing Field?**
- Search report for the property name
- Check if it was discovered during COLLECTING
- Look for error messages about type resolution

**Slow Build?**
- Check COLLECTING phase duration
- Review number of documents discovered
- Consider splitting large document classes

**Generation Error?**
- Look for ❌ ERROR messages in detailed log
- Check error diagnostics section
- Verify @Field annotations are correct

## Supported Field Types

Metalastic supports all Spring Data Elasticsearch field types through **runtime detection**:

| Elasticsearch Type | Generated Field Class | Kotlin Type |
|-------------------|----------------------|-------------|
| `text` | `TextField<String>` | `String` |
| `keyword` | `KeywordField<String>` | `String` |
| `long` | `LongField<Long>` | `Long` |
| `integer` | `IntegerField<Int>` | `Int` |
| `short` | `ShortField<Short>` | `Short` |
| `byte` | `ByteField<Byte>` | `Byte` |
| `double` | `DoubleField<Double>` | `Double` |
| `float` | `FloatField<Float>` | `Float` |
| `date` | `DateField<Date>` | `Date`, `LocalDate`, `LocalDateTime` |
| `boolean` | `BooleanField<Boolean>` | `Boolean` |
| `object` | Generated Meta-class or Terminal ObjectField | Custom objects |
| `nested` | Generated Meta-class (nested=true) or Terminal ObjectField | Collections |
| `geo_point` | `GeoPointField` | `GeoPoint` |
| `ip` | `IpField<String>` | `String` |
| *...and many more* | *Auto-detected* | *Type-safe* |

**Object and Nested Field Generation**:
- **Meta-class generation**: Used for simple objects, custom classes, and @Document references
- **Terminal ObjectField**: Used for complex generic types (e.g., `Map<String, List<String>>`) that don't warrant their own Meta-classes

### Version Compatibility

The processor uses **runtime field type detection**, automatically supporting:
- ✅ **All Spring Data ES versions** (5.0.x through latest)
- ✅ **Forward compatibility** - New field types automatically supported
- ✅ **Graceful degradation** - Unknown types logged but don't break compilation
- ✅ **Zero configuration** - Uses your project's Spring Data ES version

## Compatibility Matrix

### Core Dependencies

| Component | Version | Compatibility |
|-----------|---------|---------------|
| **Java** | 21+ | ✅ Required |
| **Kotlin** | 2.2.0+ | ✅ Required |
| **Gradle** | 8.5+ | ✅ Recommended |

### Spring Data Elasticsearch Compatibility

| Spring Data ES | Elasticsearch | Metalastic Core | elasticsearch-dsl |
|----------------|---------------|-----------------|-------------------|
| 5.5.x | 8.15.x | ✅ Full | ✅ Full |
| 5.4.x | 8.14.x | ✅ Full | ✅ Full |
| 5.3.x | 8.13.x | ✅ Full | ✅ Full |
| 5.2.x | 8.12.x | ✅ Full | ✅ Full |
| 5.1.x | 8.11.x | ✅ Full | ✅ Full |
| 5.0.x | 8.7.x+ | ✅ Full | ✅ Full |

### Framework Integration

| Framework | Support Level | Notes |
|-----------|---------------|-------|
| **Spring Boot** | ✅ Full | Auto-configuration compatible |
| **Spring Data** | ✅ Full | Native integration |
| **Elasticsearch Java Client** | ✅ Full | Direct query building |
| **Jackson** | ✅ Full | JSON serialization support |

### Build Tools

| Tool | Status | Configuration |
|------|--------|---------------|
| **Gradle (Kotlin DSL)** | ✅ Recommended | Type-safe plugin DSL |
| **Gradle (Groovy DSL)** | ✅ Supported | Manual configuration |
| **Maven** | ⚠️ Manual | KSP configuration required |

### Artifact Compatibility

| Module | Group ID | Latest Version | Versioning Strategy |
|--------|----------|----------------|-------------------|
| **Core Runtime** | `com.ekino.oss:metalastic-core` | `3.0.0` | Semantic versioning |
| **Annotation Processor** | `com.ekino.oss:metalastic-processor` | `3.0.0` | Semantic versioning |
| **Enhanced DSL** | `com.ekino.oss:metalastic-elasticsearch-dsl` | `5.0.12-1.0` | `{spring-data-es}-{dsl-version}` |
| **Gradle Plugin** | `com.ekino.oss:metalastic-gradle-plugin` | `3.0.0` | Semantic versioning |

### elasticsearch-dsl Version Matrix

| elasticsearch-dsl | Spring Data ES | Elasticsearch | DSL Features |
|-------------------|---------------|---------------|--------------|
| **5.0.12-1.0** | 5.0.12 | 8.7.x+ | ✅ Full DSL, Range queries, JCV |
| **5.1.x-1.x** | 5.1.x | 8.11.x | 🔄 Future release |
| **5.2.x-1.x** | 5.2.x | 8.12.x | 🔄 Future release |

### Feature Matrix by Module

| Feature | Core | elasticsearch-dsl | Notes |
|---------|------|-------------------|-------|
| Type-safe field access | ✅ | ✅ | Basic metamodel functionality |
| Path traversal | ✅ | ✅ | Dotted notation support |
| Nested field support | ✅ | ✅ | Automatic nested detection |
| Bool query DSL | ❌ | ✅ | Advanced query building |
| Range queries | ❌ | ✅ | Google Guava integration |
| Clause + { } syntax | ❌ | ✅ | Innovative operator overloading |
| Native query integration | ❌ | ✅ | Spring Data ES NativeQuery |
| JSON validation | ❌ | ✅ | JCV integration for testing |

### Version Upgrade Path

| From | To | Breaking Changes | Migration Guide |
|------|----|-----------------|-----------------|
| 1.x | 2.x | ✅ Package restructure | Update imports, use Gradle plugin |
| 2.0 | 2.1+ | ❌ None | Drop-in replacement |
| Core only | + elasticsearch-dsl | ❌ None | Add `elasticsearch-dsl:5.0.12-1.0` dependency |
| elasticsearch-dsl 5.0.12-1.0 | 5.0.12-1.1 | ❌ None | DSL feature updates only |
| elasticsearch-dsl 5.0.12-x | 5.1.x-1.0 | ⚠️ Possible | Follow Spring Data ES migration guide |

### elasticsearch-dsl Versioning Strategy

**Format**: `{spring-data-elasticsearch-version}-{dsl-version}`

**Examples**:
- `5.0.12-1.0` = Compatible with Spring Data ES 5.0.12, DSL features version 1.0
- `5.1.0-1.0` = Compatible with Spring Data ES 5.1.0, DSL features version 1.0
- `5.0.12-1.1` = Compatible with Spring Data ES 5.0.12, DSL features version 1.1

**Benefits**:
- ✅ **Clear compatibility** - Version immediately shows Spring Data ES compatibility
- ✅ **Independent evolution** - DSL features can evolve independently of Spring Data ES
- ✅ **Easy selection** - Choose version based on your Spring Data ES version
- ✅ **Future-proof** - Supports multiple Spring Data ES versions simultaneously

## Troubleshooting

### Generated Code Not Visible in IDE

**Problem**: Meta-classes are generated but not visible in IDE auto-completion.

**Solutions**:
1. **Sync Gradle project**:
   ```bash
   ./gradlew clean build --refresh-dependencies
   ```
2. **Invalidate IDE caches** (IntelliJ IDEA):
   - File → Invalidate Caches / Restart
3. **Check generated sources directory**:
   - Look in `build/generated/ksp/main/kotlin/`
   - Verify files are actually generated
4. **Ensure KSP plugin is applied**:
   ```kotlin
   plugins {
       id("com.google.devtools.ksp") version "2.2.20-2.0.3"
   }
   ```

### KSP Not Running

**Problem**: No files generated in `build/generated/ksp/`.

**Solutions**:
1. **Verify both plugins are applied**:
   ```kotlin
   plugins {
       id("com.google.devtools.ksp") version "2.2.20-2.0.3"
       id("com.ekino.oss.metalastic") version "3.0.0"
   }
   ```
2. **Check for `@Document` annotations**:
   - Processor only runs if `@Document` classes exist
   - Verify import: `org.springframework.data.elasticsearch.annotations.Document`
3. **Enable KSP debugging**:
   ```kotlin
   ksp {
       arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
   }
   ```
4. **Clean and rebuild**:
   ```bash
   ./gradlew clean kspKotlin
   ```

### Import Conflicts

**Problem**: Ambiguous imports or naming conflicts with generated classes.

**Solutions**:
1. **Use custom package configuration**:
   ```kotlin
   metalastic {
       metamodels {
           main {
               packageName = "com.example.search.metamodels"
               className = "SearchMetamodels"
           }
       }
   }
   ```
2. **Use qualified imports**:
   ```kotlin
   import com.example.search.metamodels.Metamodels
   import com.example.search.metamodels.MetaPerson
   ```

### Private Class Not Generating Metamodel

**Problem**: Private `@Document` class doesn't generate Meta-class.

**Solution**: This is expected behavior. Private classes are excluded by default. To include them:

```kotlin
metalastic {
    generatePrivateClassMetamodels = true
}
```

**Note**: Including private class metamodels is generally not recommended.

### Field Not Generating

**Problem**: Specific field missing from generated Meta-class.

**Check**:
1. **Field has `@Field` annotation**:
   ```java
   @Field(type = FieldType.Text)
   private String name;
   ```
2. **Field is not static or transient**
3. **Getter method has `@Field` for interfaces**:
   ```java
   @Field(type = FieldType.Text)
   String getName();
   ```

### Build Performance Issues

**Problem**: Slow compilation with KSP.

**Solutions**:
1. **Enable Gradle configuration cache**:
   ```kotlin
   // gradle.properties
   org.gradle.configuration-cache=true
   ```
2. **Increase Gradle daemon memory**:
   ```kotlin
   // gradle.properties
   org.gradle.jvmargs=-Xmx2048m
   ```
3. **Use incremental compilation** (enabled by default in KSP)

### Metamodels Class Not Found

**Problem**: `Metamodels` object not found at runtime.

**Solutions**:
1. **Check source set configuration** matches where you're using it:
   ```kotlin
   metalastic {
       metamodels {
           main {  // For src/main/kotlin
               packageName = "com.example.metamodels"
           }
           test {  // For src/test/kotlin
               packageName = "com.example.test.metamodels"
           }
       }
   }
   ```
2. **Verify import matches configured package**:
   ```kotlin
   import com.example.metamodels.Metamodels
   ```

### Getting Help

If you encounter issues not covered here:

1. **Enable debug reporting**:
   ```kotlin
   metalastic {
       reportingPath = "build/reports/metalastic/processor-report.md"
   }
   ```
2. **Check the generated report** in `build/reports/metalastic/`
3. **Review KSP logs**: Look for errors in build output
4. **Report issues**: https://gitlab.ekino.com/iperia/metalastic/issues

## Development

### Project Structure

```
Metalastic/
├── modules/
│   ├── core/                    # DSL runtime library
│   │   ├── src/main/kotlin/     # Field classes, Index base classes
│   │   └── src/test/kotlin/     # Unit tests for DSL
│   ├── elasticsearch-dsl/       # Enhanced query building DSL
│   │   ├── src/main/kotlin/     # Innovative clause + { } syntax, range queries
│   │   └── src/test/kotlin/     # DSL integration tests with JCV validation
│   ├── processor/               # KSP annotation processor
│   │   ├── src/main/kotlin/     # Code generation logic
│   │   └── src/test/kotlin/     # Processor unit tests
│   ├── gradle-plugin/           # Gradle plugin with type-safe DSL
│   │   ├── src/main/kotlin/     # Plugin implementation and configuration DSL
│   │   └── src/main/resources/  # Plugin descriptor files
│   └── test/                    # Integration tests
│       └── src/test/kotlin/     # End-to-end tests with real Spring Data ES
├── build.gradle.kts             # Root build configuration
├── settings.gradle.kts          # Multi-module settings
└── CLAUDE.md                    # Project instructions and context
```

### Building and Testing

```bash
# Clone and build
git clone https://gitlab.ekino.com/iperia/metalastic.git
cd metalastic

# Build all modules
./gradlew build

# Format code (required before committing)
./gradlew spotlessApply

# Run specific module tests
./gradlew :modules:core:test          # DSL runtime tests
./gradlew :modules:processor:test     # Processor tests
./gradlew :modules:test:test          # Integration tests

# Run all quality checks
./gradlew check

# Publish to local repository
./gradlew publishToMavenLocal
```

### Code Quality Standards

- **ktfmt Google Style** - Consistent formatting with `./gradlew spotlessApply`
- **No star imports** - All imports explicit
- **Kotest ShouldSpec** - All tests use Kotest v5.9.1 format
- **KotlinLogging** - Structured logging in tests (no println)
- **100% Kotlin** - Kotlin-first with Java interoperability
- **KSP over kapt** - Modern annotation processing

### Architecture Highlights

**Modern Processor Architecture**:
- 🏗️ **Phase-based processing** - COLLECTING → BUILDING → WRITING → COMPLETE
- 📊 **Performance monitoring** - Built-in timing and reporting for each phase
- 🔍 **Enhanced debugging** - Comprehensive logging and markdown reports
- ⚡ **Optimized code generation** - Efficient KSP-based processing
- 🛡️ **Error resilience** - Robust error handling with detailed diagnostics

**Key Design Decisions**:
- **Regular classes over data objects** - Enables constructor parameters for relationships
- **Default constructor values** - Clean instantiation patterns
- **@JvmField annotations** - Optimal Java interoperability
- **Parent hierarchy traversal** - Automatic path building for nested structures
- **Advanced import management** - Package proximity optimization and conflict resolution

## Contributing

1. **Follow code style** - Run `./gradlew spotlessApply` before committing
2. **Write tests** - Use Kotest ShouldSpec format for all new features
3. **No star imports** - Keep all imports explicit
4. **Update documentation** - Include examples for new features
5. **Ensure quality** - All checks must pass: `./gradlew check`

### Adding New Field Types

When Spring Data Elasticsearch adds new field types:

1. **No code changes needed** - Runtime detection automatically picks them up
2. **Add tests** - Create test cases in the integration module
3. **Update documentation** - Add examples if the field type has special behavior

## Publishing

Metalastic uses a **dual publication strategy** to handle different versioning needs:

### 📦 Publication Strategy

**Two separate publication workflows:**

1. **Core Modules** (`core`, `processor`, `gradle-plugin`)
   - **Tag Format**: `v2.1.0`
   - **Publishes**:
     - `com.ekino.oss:metalastic-core:3.0.0`
     - `com.ekino.oss:metalastic-processor:3.0.0`
     - `com.ekino.oss:metalastic-gradle-plugin:3.0.0`

2. **elasticsearch-dsl Module**
   - **Tag Format**: `elasticsearch-dsl-v5.0.12-1.0`
   - **Publishes**: `com.ekino.oss:metalastic-elasticsearch-dsl:5.0.12-1.0`

### 🚀 Release Process

**For Core Modules:**
```bash
git tag v2.1.0
git push origin v2.1.0
# Triggers CI/CD → publishes core, processor, gradle-plugin
```

**For elasticsearch-dsl Module:**
```bash
git tag elasticsearch-dsl-v5.0.12-1.0
git push origin elasticsearch-dsl-v5.0.12-1.0
# Triggers CI/CD → publishes elasticsearch-dsl:5.0.12-1.0
```

### 📋 Publication Matrix

| Module | Tag Format | Published Version | Repository |
|--------|------------|-------------------|------------|
| **core** | `v2.1.0` | `2.1.0` | GitLab Maven Registry |
| **processor** | `v2.1.0` | `2.1.0` | GitLab Maven Registry |
| **gradle-plugin** | `v2.1.0` | `2.1.0` | GitLab Maven Registry |
| **elasticsearch-dsl** | `elasticsearch-dsl-v5.0.12-1.0` | `5.0.12-1.0` | GitLab Maven Registry |

### 🏢 Repository Information

- **Repository**: https://github.com/ekino/Metalastic
- **Package Registry**: https://central.sonatype.com/namespace/com.ekino.oss
- **Group ID**: `com.ekino.oss`
- **CI/CD**: GitHub Actions with automatic publishing on tag pushes

## Roadmap

### ✅ Completed
- Multi-module project structure with clear separation
- Complete field type support with runtime detection
- Type-safe path traversal with dotted notation
- Object and nested field support
- Document-to-document references
- Centralized Metamodels registry
- Advanced import optimization
- Performance monitoring and debug reporting
- Java compatibility with @JvmField annotations
- Version-agnostic Spring Data ES compatibility
- **Gradle plugin with type-safe DSL configuration**
- **Smart field name resolution** - Convention-aware `@Field(name)` handling
- **Enhanced query building DSL** - Complete elasticsearch-dsl module
- **Innovative `clause + { }` syntax** - Modern operator-overloaded DSL
- **Google Guava Range integration** - Mathematical notation for range queries
- **Spring Data NativeQuery integration** - Seamless query execution
- **JCV JSON validation** - Advanced testing capabilities

### 🚧 In Progress
- Performance optimizations and benchmarking
- StartBound mathematical range notation (10.0..100.0)

### 📋 Future Plans
- Direct Elasticsearch client integration
- Query execution utilities
- IDE plugin for enhanced development experience
- Performance benchmarking and optimization

---

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

Copyright (c) 2025 ekino

---

**Ready to get started?** Check out the [Quick Start](#quick-start) section or dive into the [examples](#examples) to see Metalastic in action! 🚀