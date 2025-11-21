---
layout: home

hero:
  name: Metalastic
  text: Type-Safe Metamodels for Elasticsearch
  tagline: Automatically generate compile-time validated field accessors from your Spring Data Elasticsearch documents
  image:
    src: /logo.png
    alt: Metalastic Logo
  actions:
    - theme: brand
      text: Get Started
      link: ./guide/getting-started
    - theme: alt
      text: View on GitHub
      link: https://github.com/ekino/Metalastic

features:
  - icon: 🔄
    title: Automatic Code Generation
    details: KSP annotation processor transforms your @Document classes into type-safe Meta-classes at compile time. Zero runtime overhead, instant IDE support.

  - icon: ⚡
    title: Two-Part Architecture
    details: Core processor generates metamodels for path-based queries. Optional Query DSL module provides fluent API with innovative operator syntax for complex searches.

  - icon: 🎯
    title: Compile-Time Type Safety
    details: No more string-based field names. Full compile-time validation of field types, relationships, and nested structures with automatic refactoring support.

  - icon: 🔍
    title: Smart Path Traversal
    details: Automatic dotted notation for nested objects with path() methods. Built-in nested field detection and parent hierarchy tracking.

  - icon: 📦
    title: Multi-Version Support
    details: Rolling + frozen release strategy supports Spring Data Elasticsearch 5.0.x through 6.0.x. Choose stability or track latest versions.

  - icon: ☕
    title: Java & Kotlin Compatible
    details: Kotlin-first design with @JvmField annotations for seamless Java interoperability. Works with both languages equally well.
---

## The Problem

Elasticsearch queries using Spring Data rely on string-based field names:

```java
// String-based field names are error-prone
NativeSearchQuery query = new NativeSearchQueryBuilder()
    .withQuery(
        QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("staus", "active"))  // ❌ Typo: "staus" → "status"
            .filter(QueryBuilders.rangeQuery("prcie").gte(100)) // ❌ Typo: "prcie" → "price"
    )
    .build();

// No IDE autocomplete, no refactoring support, no compile-time validation
// Errors only discovered at runtime!
```

## The Solution: Metamodel Transformation

Metalastic generates type-safe metamodels from your `@Document` classes:

### Your Document Class

```java
@Document(indexName = "products")
public class Product {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    @Field(type = FieldType.Object)
    private Category category;
}
```

### Auto-Generated Metamodel

```kotlin
// Generated MetaProduct.kt - no manual work required!
class MetaProduct<T>(/* ... */) : Document<T>(/* ... */) {
    val id: KeywordField<String> = keyword("id")
    val title: TextField<String> = text("title")
    val price: DoubleField<Double> = double("price")
    val status: KeywordField<ProductStatus> = keyword("status")
    val category: MetaCategory<Category> = MetaCategory(this, "category", false, typeOf<Category>())

    companion object {
        const val INDEX_NAME = "products"
        val product: MetaProduct<Product> = MetaProduct()
    }
}
```

### Type-Safe Usage

```kotlin
import com.example.MetaProduct.Companion.product

// ✅ Compile-time validated field access
product.title.path()          // "title"
product.category.name.path()  // "category.name" (automatic dotted notation)

// ✅ No typos possible - won't compile if field doesn't exist!
val query = QueryBuilders.boolQuery()
    .must(QueryBuilders.termQuery(product.status.path(), ProductStatus.ACTIVE))
    .filter(QueryBuilders.rangeQuery(product.price.path()).gte(100))

// IDE autocomplete shows all available fields ✨
```

## Enhanced with Optional Query DSL

For maximum type safety, add the Query DSL module:

```kotlin
import com.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

// Fluent, type-safe query building
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.status term ProductStatus.ACTIVE
        }

        filter + {
            product.price range Range.closed(500.0, 2000.0)
            product.category.name term "electronics"
        }
    }
}
```

## How It Works

Metalastic uses a **KSP annotation processor** that runs at compile time:

1. **Discovers** `@Document` annotated classes
2. **Generates** type-safe Meta-classes with field definitions
3. **Provides** instant IDE support with autocomplete

Files are generated to `build/generated/ksp/` and appear automatically in your IDE.

::: tip Learn More
See [Understanding Metamodels](/guide/understanding-metamodels.html#how-generation-works) for the complete architecture, three-phase processing details, and type system explanation.
:::

## Two-Part Architecture

### Core: Metamodel Generation (Required)

::: code-group

```kotlin [Gradle Plugin (Recommended)]
plugins {
    id("com.google.devtools.ksp") version "2.3.2"
    id("com.ekino.oss.metalastic") version "1.0.0"
}

metalastic {
    metamodels {
        packageName = "com.example.search"
        className = "Metamodels"
    }
}
```

```kotlin [Direct Dependencies]
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
}

ksp {
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
}
```

:::

Use generated metamodels with any query builder:
- Standard Elasticsearch Java client
- Spring Data Elasticsearch repository queries
- Native search queries
- Custom query construction

### Query DSL: Enhanced Type Safety (Optional)

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")
}
```

Adds fluent query API with:
- Innovative operator syntax (`must + { }`)
- Type-safe range queries with Google Guava Range
- Automatic value conversion for dates, enums, collections
- Nested query support with path detection

## Why Metalastic?

**For Developers:**
- Write queries faster with IDE autocomplete
- Catch errors at compile time, not in production
- Refactor confidently - field renames update everywhere
- Reduce boilerplate with generated code

**For Teams:**
- Consistent query patterns across the codebase
- Self-documenting field structure
- Easier code reviews with type-checked queries
- Lower maintenance overhead

**For Your Application:**
- Zero runtime overhead - all work happens at compile time
- No reflection or runtime code generation
- Minimal library footprint
- Battle-tested with Spring Data ES 5.0-6.0

<div class="tip custom-block" style="margin-top: 2rem;">
  <p class="custom-block-title">Ready to eliminate string-based field names?</p>
  <p>Follow our <a href="./guide/getting-started">Quick Start guide</a> to transform your Elasticsearch queries in 5 minutes.</p>
</div>
