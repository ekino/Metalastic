# Metalastic Elasticsearch DSL v1.0 for Spring Data ES 5.4.x

**Release Date:** TBD

## Overview

First release of the Metalastic Elasticsearch DSL module for **Spring Data Elasticsearch 5.4.x**.

This module provides type-safe, fluent query builders using generated metamodels from the Metalastic processor.

## Version Compatibility

| Component | Version |
|-----------|---------|
| **DSL Module** | `1.0` |
| **Spring Data Elasticsearch** | `5.4.+` |
| **Elasticsearch Java Client** | `8.15.5` |
| **Metalastic Core** | `1.0.0+` |
| **Metalastic Processor** | `1.0.0+` |

## Installation

```kotlin
// build.gradle.kts
dependencies {
    // Core Metalastic (generates metamodels)
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")

    // DSL module for Spring Data ES 5.4.x
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.4:1.0")

    // Your Spring Data Elasticsearch dependency
    implementation("org.springframework.data:spring-data-elasticsearch:5.4.+")
}
```

## Key Features

### Type-Safe Query Building

```kotlin
import com.example.metamodels.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.status term "active"
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

### Supported Query Types

- **Full-text queries**: `match`, `multiMatch`, `matchPhrase`, `matchPhrasePrefix`
- **Term-level queries**: `term`, `terms`, `termsSet`, `wildcard`, `prefix`, `regexp`
- **Boolean queries**: `bool`, `shouldAtLeastOneOf`, `disMax`
- **Range queries**: `range`, `greaterThan`, `lowerThan`, `mustBeBetween` (with UntypedRangeQuery support)
- **Nested queries**: `nested` with path-aware builders
- **Specialized queries**: `fuzzy`, `exist`, `geoDistance`, `moreLikeThis`

### Two Query Syntaxes

#### Modern Operator Syntax
```kotlin
boolQueryDsl {
    must + { product.name match "search" }
    filter + { product.active term true }
}
```

#### Classical Method Syntax
```kotlin
boolQueryDsl {
    mustDsl { product.name match "search" }
    filterDsl { product.active term true }
}
```

## What's New in DSL v1.0

- ✅ Initial release with comprehensive query type support
- ✅ Type-safe field path resolution
- ✅ Google Guava Range integration for range queries
- ✅ Automatic value conversion for dates, enums, and primitives
- ✅ Nested query support with path validation
- ✅ Operator overloading for intuitive query composition
- ✅ Elasticsearch Java 8.15+ UntypedRangeQuery support

## Requirements

- **Java**: 21 or higher
- **Kotlin**: 2.2.20 or higher
- **Spring Data Elasticsearch**: 5.4.x
- **Gradle**: 8.x or higher

## Documentation

For complete documentation, examples, and architecture details, see:
- **Main Release Notes**: [Metalastic v1.0.0](RELEASE_NOTES_v1.0.0.md)
- **GitHub Repository**: https://github.com/ekino/Metalastic
- **Documentation**: https://github.com/ekino/Metalastic/blob/master/README.md

## Upgrade Path

If you're using a different Spring Data Elasticsearch version, use the corresponding DSL variant:
- Spring Data ES 5.0.x → `metalastic-elasticsearch-dsl-5.0:1.0`
- Spring Data ES 5.1.x → `metalastic-elasticsearch-dsl-5.1:1.0`
- Spring Data ES 5.2.x → `metalastic-elasticsearch-dsl-5.2:1.0`
- Spring Data ES 5.3.x → `metalastic-elasticsearch-dsl-5.3:1.0`
- Spring Data ES 5.5.x → `metalastic-elasticsearch-dsl-5.5:1.0`

## License

MIT License - Copyright (c) 2025 ekino

## Support

For questions, issues, or feature requests:
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Discussions**: https://github.com/ekino/Metalastic/discussions
