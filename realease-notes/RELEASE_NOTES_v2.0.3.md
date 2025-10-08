# Metalastic v2.0.3 Release Notes

**Release Date**: January 2025
**Previous Release**: v2.0.2

This release introduces the **elasticsearch-dsl module** - a comprehensive query building DSL with type-safe field access, complete KDoc documentation, and innovative operator syntax. It also includes important improvements to the Gradle plugin's custom source set support.

## 🌟 Major New Features

### New elasticsearch-dsl Module

The headline feature of v2.0.3 is the **elasticsearch-dsl module** - a complete query building DSL that provides type-safe, fluent API for constructing Elasticsearch queries using Metalastic's generated metamodels.

#### Key Features

- **Type-safe query building**: Use metamodel field references instead of string literals
- **Comprehensive query coverage**: Support for all major Elasticsearch query types
- **Innovative `clause + { }` syntax**: Modern operator-overloaded DSL for cleaner code
- **Full KDoc documentation**: 200+ documented functions with Elasticsearch doc links
- **Integration with metamodels**: Seamless integration with generated Q-classes

#### Installation

```kotlin
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:2.0.3")
}
```

#### Query Types Supported

**Full-text Queries**:
- `match` - Full-text search with relevance scoring
- `matchPhrase` - Phrase matching
- `matchPhrasePrefix` - Prefix phrase matching
- `multiMatch` - Multi-field search

**Term-level Queries**:
- `term` - Exact value matching
- `terms` - Multiple exact values
- `termsSet` - Minimum matching terms
- `wildCard` - Wildcard patterns
- `prefix` - Prefix matching
- `regexp` - Regular expression queries

**Boolean Queries**:
- `bool` - Boolean query composition with must/should/filter/mustNot
- `shouldAtLeastOneOf` - Shorthand for should clauses
- `disMax` - Disjunction max query

**Range Queries**:
- `range` - Generic range queries
- `greaterThan`, `greaterThanEqualTo` - Lower bound queries
- `lowerThan`, `lowerThanEqualTo` - Upper bound queries
- `mustBeBetween` - Closed range queries

**Specialized Queries**:
- `nested` - Nested object queries
- `fuzzy` - Fuzzy matching
- `exist` - Field existence check
- `geoDistance` - Geographic distance queries
- `moreLikeThis` - Similar document queries

#### Usage Examples

**Basic Query Building**:
```kotlin
import com.metalastic.elasticsearch.dsl.*

val document = Metamodels.product

// Using operator syntax
BoolQuery.of {
  boolQueryDsl {
    must + {
      document.title match "laptop"
      document.status term Status.ACTIVE
    }

    filter + {
      document.price.range(Range.closed(500.0, 2000.0))
      document.inStock term true
    }

    should + {
      document.brand term "Dell"
      document.tags.containsTerms("featured", "sale")
    }

    mustNot + {
      document.category term "refurbished"
    }
  }
}
```

**Using Function Syntax**:
```kotlin
BoolQuery.of {
  boolQueryDsl {
    mustDsl {
      document.title match "laptop"
      document.status term Status.ACTIVE
    }

    filterDsl {
      document.price.range(Range.closed(500.0, 2000.0))
    }
  }
}
```

**Nested Queries**:
```kotlin
BoolQuery.of {
  boolQueryDsl {
    must + {
      document.reviews.nested {
        must + {
          document.reviews.rating greaterThanEqualTo 4.0
          document.reviews.verified term true
        }
      }
    }
  }
}
```

### Comprehensive KDoc Documentation

All DSL functions now include detailed KDoc documentation:

- **QueryVariantDsl**: Class-level documentation explaining the DSL's purpose, features, and supported query types
- **All 200+ query functions**: Individual KDoc with descriptions and links to official Elasticsearch documentation
- **BoolQueryDsl**: Enhanced documentation for all boolean query occurrences (must, mustNot, should, filter)
- **Operator overloads**: Documented operator syntax with usage examples
- **Cross-references**: Links between related DSL classes

#### Documentation Pattern

Each function follows this pattern:
```kotlin
/**
 * creates
 * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
 */
@VariantDsl
infix fun Metamodel<String>.match(value: String?) = match(value) {}
```

### Custom Source Set Support in Gradle Plugin

Enhanced the Gradle plugin with better support for custom source sets:

#### Global Defaults Inheritance

Custom source sets now inherit global defaults when not explicitly configured:

```kotlin
metalastic {
  metamodels {
    // Global defaults
    packageName = "com.example.metamodels"
    className = "Metamodels"
    classPrefix = "Meta"

    // Custom source set inherits global defaults
    customSourceSets.create("integration") {
      // Will use "com.example.metamodels" as packageName
      // Will use "Metamodels" as className
      // Will use "Meta" as classPrefix
    }

    // Override specific properties
    customSourceSets.create("e2e") {
      packageName = "com.example.e2e.metamodels" // Override
      // className and classPrefix inherited from global
    }
  }
}
```

#### Implementation Details

- Added `GlobalDefaults` data class for better organization
- Improved `configureCustomSourceSets` to accept and apply global defaults
- Enhanced logging for KSP argument configuration
- Better handling of optional properties across source sets

## 🚀 Other Improvements

### Enhanced IDE Experience

- **IntelliJ IDEA Support**: Better code completion for DSL functions
- **KDoc Integration**: Documentation appears in IDE tooltips and quick documentation views
- **Type Inference**: Improved type inference for complex query chains

### Code Quality

- **Comprehensive Testing**: Full test coverage for elasticsearch-dsl module
- **Spotless Formatting**: All code follows ktfmt Google Style
- **Detekt Validation**: Static analysis ensures code quality

## 🔧 Technical Improvements

### DSL Architecture

- **Receiver Functions**: Clean DSL syntax using Kotlin receiver functions
- **Type-safe Builders**: Compile-time validation of query structure
- **Null Handling**: Automatic filtering of null and blank values
- **Metamodel Integration**: Seamless integration with generated field definitions

### Build Infrastructure

- **Module Organization**: Clean separation of elasticsearch-dsl from core
- **Dependency Management**: Proper dependency configuration for elasticsearch-dsl
- **Publishing**: Separate artifact publication for DSL module

## 📚 Documentation Updates

- **elasticsearch-dsl README**: Complete module documentation with examples
- **Main README**: Updated with elasticsearch-dsl installation and usage
- **KDoc Comments**: 200+ documented DSL functions
- **Release Notes**: This comprehensive release notes document

## 🔄 Migration Guide

### From v2.0.2 to v2.0.3

**No breaking changes** - this release is fully backward compatible.

### Adding elasticsearch-dsl Module

If you want to use the new query building DSL:

1. **Add the dependency**:
```kotlin
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:2.0.3")
}
```

2. **Import the DSL**:
```kotlin
import com.metalastic.elasticsearch.dsl.*
```

3. **Start building queries**:
```kotlin
val document = Metamodels.yourDocument

BoolQuery.of {
  boolQueryDsl {
    must + {
      document.field match "value"
    }
  }
}
```

### Using Custom Source Sets with Global Defaults

If you're using custom source sets in the Gradle plugin:

1. **Define global defaults** (optional but recommended):
```kotlin
metalastic {
  metamodels {
    packageName = "com.example.metamodels"
    className = "Metamodels"
    classPrefix = "Meta"
  }
}
```

2. **Custom source sets automatically inherit** these defaults unless overridden

## 📦 Artifacts

All artifacts are published to GitLab Maven Registry:

- `com.metalastic:core:2.0.3`
- `com.metalastic:processor:2.0.3`
- `com.metalastic:gradle-plugin:2.0.3`
- `com.metalastic:elasticsearch-dsl:2.0.3` ⭐ **NEW**

## 🎯 Version Matrix

### Core Modules

| Module | Version | Description |
|--------|---------|-------------|
| core | 2.0.3 | DSL runtime library |
| processor | 2.0.3 | KSP annotation processor |
| gradle-plugin | 2.0.3 | Gradle plugin |
| **elasticsearch-dsl** | **2.0.3** | **Query building DSL** ⭐ |

### Compatibility

| Spring Data ES | Elasticsearch | Metalastic | Status |
|----------------|---------------|------------|--------|
| 5.5.x | 8.15.x | 2.0.3 | ✅ Full Support |
| 5.4.x | 8.14.x | 2.0.3 | ✅ Full Support |
| 5.3.x | 8.13.x | 2.0.3 | ✅ Full Support |
| 5.2.x | 8.11.x | 2.0.3 | ✅ Full Support |

## 🙏 Acknowledgments

This release represents a major expansion of Metalastic's capabilities with the introduction of the elasticsearch-dsl module. Special thanks to the comprehensive KDoc documentation effort that makes this DSL one of the most well-documented query builders for Elasticsearch.

The elasticsearch-dsl module provides:
- 200+ documented query functions
- Complete coverage of Elasticsearch query types
- Innovative operator syntax
- Seamless metamodel integration
- Professional-grade documentation

---

**Full Changelog**: [v2.0.2...v2.0.3](https://gitlab.ekino.com/iperia/metalastic/-/compare/v2.0.2...v2.0.3)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
