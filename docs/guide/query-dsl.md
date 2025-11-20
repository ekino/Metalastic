# Query DSL

Type-safe Elasticsearch query builder using generated metamodels.

## Overview

The Metalastic Query DSL provides a fluent, type-safe API for building Elasticsearch queries. It's inspired by QueryDSL for SQL databases and leverages the generated metamodels for compile-time safety.

## Installation

Add the DSL module to your dependencies:

```kotlin
dependencies {
    // Choose based on your Spring Data Elasticsearch version
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")  // 5.4-5.5
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3:1.0.0")  // 5.0-5.3
}
```

## Version Compatibility

| Artifact | Strategy | Current Support | Brings Transitively | Use Case |
|----------|----------|----------------|---------------------|----------|
| `metalastic-elasticsearch-dsl` | **Rolling** | 6.0.x | Spring Data ES 6.0.0 | Track latest, get new Spring Data ES support automatically |
| `metalastic-elasticsearch-dsl-5.5` | **Frozen** | 5.4.x - 5.5.x | Spring Data ES 5.5.6 | Stability for Spring Data ES 5.4-5.5 users |
| `metalastic-elasticsearch-dsl-5.3` | **Frozen** | 5.0.x - 5.3.x | Spring Data ES 5.3.13 | Stability for Spring Data ES 5.0-5.3 users |

## Boolean Queries

Build complex boolean queries with type-safe occurrences:

```kotlin
import com.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.status term Status.ACTIVE
        }

        filter + {
            product.price.range(Range.closed(500.0, 2000.0))
            product.category term "electronics"
        }

        should + {
            product.brand term "Apple"
            product.brand term "Dell"
        }

        minimumShouldMatch(1)
    }
}
```

## Query Types

### Full-text Queries

```kotlin
// Match query
product.title match "laptop"

// Multi-match query
multiMatch("search term", product.title, product.description)

// Match phrase
product.description matchPhrase "high performance"

// Match phrase prefix
product.title matchPhrasePrefix "lap"
```

### Term-level Queries

```kotlin
// Term query
product.status term Status.ACTIVE

// Terms query
product.category terms listOf("electronics", "computers")

// Terms set query
product.tags termsSet listOf("new", "sale", "featured")

// Wildcard query
product.sku wildcard "PROD-*-2024"

// Prefix query
product.code prefix "ABC"

// Regexp query
product.email regexp "[a-z]+@[a-z]+\\.[a-z]+"
```

### Range Queries

```kotlin
// Using Guava Range
product.price.range(Range.closed(100.0, 500.0))
product.price.range(Range.open(0.0, 1000.0))
product.price.range(Range.atLeast(500.0))
product.price.range(Range.atMost(1000.0))

// Convenience methods
product.price greaterThan 100.0
product.price lowerThan 1000.0
product.createdAt mustBeBetween (startDate to endDate)
```

### Nested Queries

```kotlin
val nestedQuery = NestedQuery.of {
    path(product.reviews)
    query {
        boolQueryDsl {
            must + {
                product.reviews.rating greaterThan 4.0
                product.reviews.verified term true
            }
        }
    }
}
```

### Specialized Queries

```kotlin
// Fuzzy query
product.title fuzzy "laptpo"  // finds "laptop"

// Exists query
exist(product.description)

// Geo distance query
product.location geoDistance GeoPoint(48.8566, 2.3522) within "10km"

// More like this query
moreLikeThis {
    fields(product.title, product.description)
    likeTexts("sample text")
    minTermFreq(1)
    maxQueryTerms(12)
}
```

## DisMax Queries

Combine queries with the best matching score:

```kotlin
val query = DisMaxQuery.of {
    shouldAtLeastOneOf {
        product.title match "laptop"
        product.description match "laptop"
        product.tags match "laptop"
    }
    tieBreaker(0.7)
}
```

## Value Conversion

The DSL automatically converts Kotlin types to Elasticsearch-compatible values:

- Primitive types (String, Int, Long, Double, etc.)
- Date types (Date, Instant, LocalDate, LocalDateTime)
- Collections (List, Set)
- Enums
- Custom types via `KType` inspection

```kotlin
// Automatic enum conversion
product.status term Status.ACTIVE

// Automatic date conversion
product.createdAt greaterThan LocalDate.now().minusDays(7)

// Collection handling
product.tags terms setOf("featured", "new", "sale")
```

## Best Practices

1. **Import metamodels from companion objects**:
   ```kotlin
   import com.example.MetaProduct.Companion.product
   ```

2. **Use type-safe enums** instead of strings when possible

3. **Leverage IDE autocomplete** for field discovery

4. **Combine queries** using boolean logic for complex searches

5. **Use nested queries** for nested field types

## Next Steps

- Check out comprehensive examples
