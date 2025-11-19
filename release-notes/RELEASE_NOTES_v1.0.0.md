# Metalastic v1.0.0 - Official Open Source Release

**Release Date:** TBD

## 🎉 First Official Release on Maven Central

This is the first official open source release of **Metalastic**, a QueryDSL-inspired type-safe metamodel generator for Elasticsearch in Kotlin.

Metalastic automatically generates type-safe metamodel classes from Spring Data Elasticsearch `@Document` annotated classes, enabling compile-time validation and IDE auto-completion for Elasticsearch queries.

## Overview

Metalastic provides a complete solution for type-safe Elasticsearch development:
- **Automatic metamodel generation** from your existing `@Document` classes
- **Type-safe query DSL** with fluent API and operator overloading
- **Full Spring Data ES integration** with multi-version support
- **Zero runtime overhead** - all magic happens at compile time

## What's Included

This release includes **7 artifacts** published to Maven Central:

| Artifact | Version | Description |
|----------|---------|-------------|
| `metalastic-core` | `1.0.0` | Runtime library with metamodel base types |
| `metalastic-processor` | `1.0.0` | KSP annotation processor for code generation |
| `metalastic-gradle-plugin` | `1.0.0` | Type-safe Gradle plugin for configuration |
| `metalastic-bom` | `1.0.0` | Bill of Materials for version alignment |
| `metalastic-elasticsearch-dsl` | `1.0.0` | Query DSL for Spring Data ES 6.0.x (rolling) |
| `metalastic-elasticsearch-dsl-5.5` | `1.0.0` | Query DSL for Spring Data ES 5.4-5.5 (frozen) |
| `metalastic-elasticsearch-dsl-5.3` | `1.0.0` | Query DSL for Spring Data ES 5.0-5.3 (frozen) |

## New Features

### 🔧 Core & Processor

#### Automatic Metamodel Generation
- **KSP-based annotation processor** generates type-safe metamodel classes from `@Document` classes
- **Type-safe path traversal** for navigating nested object structures
- **Full field type support** including all Spring Data Elasticsearch field types
- **@MultiField support** with inner field generation
- **Runtime type tracking** using Kotlin `KType` for advanced type inspection
- **Java interoperability** with `@JvmField` annotations

#### Architecture Highlights
- **Three-phase processing**: COLLECTING → BUILDING → WRITING
- **Graph-based model building** handles circular references and complex relationships
- **Terminal objects**: `SelfReferencingObject` and `UnModellableObject` for edge cases
- **Debug reporting**: Optional markdown reports for processor insights
- **Import optimization**: Intelligent package proximity and conflict resolution

#### Field Type Support
Supports all Spring Data Elasticsearch field types:
- Simple types: `Keyword`, `Text`, `Integer`, `Long`, `Double`, `Float`, `Date`, `Boolean`
- Complex types: `Object`, `Nested`
- Specialized types: `GeoPoint`, `Binary`
- Multi-fields with `@InnerField` annotations

### 🚀 Elasticsearch DSL Module

#### Type-Safe Query Building

Three artifact variants support different Spring Data Elasticsearch versions:

**Rolling Artifact** (`metalastic-elasticsearch-dsl`):
- Supports: **Spring Data ES 6.0.x**
- Brings transitively: Spring Data ES **6.0.0**
- Uses: elasticsearch-java 8.18+

**Frozen Artifact** (`metalastic-elasticsearch-dsl-5.5`):
- Supports: **Spring Data ES 5.4.x - 5.5.x**
- Brings transitively: Spring Data ES **5.5.6**
- Uses: elasticsearch-java 8.15+ **UntypedRangeQuery** API

**Frozen Artifact** (`metalastic-elasticsearch-dsl-5.3`):
- Supports: **Spring Data ES 5.0.x - 5.3.x**
- Brings transitively: Spring Data ES **5.3.13**
- Uses: elasticsearch-java 8.5-8.13 API

#### Query Types Supported

- **Full-text queries**: `match`, `multiMatch`, `matchPhrase`, `matchPhrasePrefix`
- **Term-level queries**: `term`, `terms`, `termsSet`, `wildcard`, `prefix`, `regexp`
- **Boolean queries**: `bool`, `shouldAtLeastOneOf`, `disMax`
- **Range queries**: `range`, `greaterThan`, `lowerThan`, `mustBeBetween`
- **Nested queries**: `nested` with path-aware builders
- **Specialized queries**: `fuzzy`, `exist`, `geoDistance`, `moreLikeThis`

#### DSL Features

- **Fluent API** with operator overloading (`+` for clause addition)
- **Two syntax styles**: Modern operator syntax and classical method syntax
- **Type-safe field paths** validated at compile time
- **Google Guava Range** integration for range queries
- **Automatic value conversion** for dates, enums, and primitives
- **Nested query support** with path validation

### 🎯 Gradle Plugin

- **Type-safe DSL configuration** with discoverable API
- **Automatic dependency management** (adds core + processor automatically)
- **Source set support** with global and per-source-set configuration
- **Feature flags** for Java compatibility and private class processing
- **Debug reporting** configuration

## Installation

### Using BOM + Gradle Plugin (Recommended)

Add to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.ekino.oss.metalastic") version "1.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM for version alignment
    implementation(platform("com.ekino.oss:metalastic-bom:1.0.0"))

    // Core and processor are added automatically by the plugin

    // Optional: Choose DSL variant based on your Spring Data ES version
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl")       // For 6.0.x
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5")  // For 5.4-5.5
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3")  // For 5.0-5.3
}

metalastic {
    metamodels {
        packageName = "com.example.search"
        className = "Metamodels"
        classPrefix = "Meta"  // Default
    }

    features {
        generateJavaCompatibility = true
        generatePrivateClassMetamodels = false
    }
}
```

**Note**: The Gradle plugin automatically adds `metalastic-core` and `metalastic-processor` dependencies. When using the BOM, versions are omitted for guaranteed compatibility!

### Manual Setup (Without Plugin)

If you prefer not to use the plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
    // Use BOM for version management
    implementation(platform("com.ekino.oss:metalastic-bom:1.0.0"))

    implementation("com.ekino.oss:metalastic-core")
    ksp("com.ekino.oss:metalastic-processor")

    // Optional: DSL module (choose variant)
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl")       // 6.0.x
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5")  // 5.4-5.5
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3")  // 5.0-5.3
}

// Configure KSP options manually
ksp {
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
}
```

### Without BOM

If you prefer explicit versions:

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")
}

plugins {
    id("com.ekino.oss.metalastic") version "1.0.0"
}
```

## Usage Example

### 1. Define Your Document

```kotlin
@Document(indexName = "products")
data class Product(
    @Id
    val id: String,

    @Field(type = FieldType.Text)
    val title: String,

    @Field(type = FieldType.Keyword)
    val category: String,

    @Field(type = FieldType.Double)
    val price: Double,

    @Field(type = FieldType.Object)
    val manufacturer: Manufacturer
)

data class Manufacturer(
    @Field(type = FieldType.Text)
    val name: String,

    @Field(type = FieldType.Keyword)
    val country: String
)
```

### 2. Use Generated Metamodel

```kotlin
import com.example.search.MetaProduct.Companion.product

// Type-safe path traversal
product.title.path() // "title"
product.manufacturer.name.path() // "manufacturer.name"

// All metamodels available in registry
Metamodels.entries().forEach { doc ->
    println(doc.indexName())
}
```

### 3. Build Type-Safe Queries (with DSL module)

```kotlin
import com.example.search.MetaProduct.Companion.product

// Boolean query with multiple clauses
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.price greaterThan 500.0
        }

        filter + {
            product.category term "electronics"
            product.manufacturer.country term "USA"
        }

        should + {
            product.manufacturer.name match "Apple"
            product.manufacturer.name match "Dell"
        }

        minimumShouldMatch(1)
    }
}

// Range queries with Guava Range
product.price.range(Range.closed(100.0, 1000.0))
product.price mustBeBetween Range.open(500.0, 2000.0)

// Nested queries (for nested field types)
val nestedQuery = NestedQuery.of {
    path(product.reviews)  // Type-safe nested path
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

## Spring Data Elasticsearch Compatibility

### Version Support Matrix

| Metalastic DSL Artifact | Spring Data ES Versions | Transitive Version | Elasticsearch Java Client |
|------------------------|------------------------|-------------------|--------------------------|
| `metalastic-elasticsearch-dsl` | **6.0.x** | **6.0.0** | 8.18+ |
| `metalastic-elasticsearch-dsl-5.5` | **5.4.x - 5.5.x** | **5.5.6** | 8.15+ (UntypedRangeQuery) |
| `metalastic-elasticsearch-dsl-5.3` | **5.0.x - 5.3.x** | **5.3.13** | 8.5-8.13 |

### Choosing the Right DSL Artifact

**For Spring Data ES 6.0**:
```kotlin
implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")
```

**For Spring Data ES 5.4 or 5.5**:
```kotlin
implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0.0")
```

**For Spring Data ES 5.0, 5.1, 5.2, or 5.3**:
```kotlin
implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3:1.0.0")
```

### Overriding Spring Data ES Version

Both DSL artifacts bring a transitive Spring Data ES dependency. You can override it:

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")

    // Override to use 5.4.x instead of 5.5.6
    implementation("org.springframework.data:spring-data-elasticsearch:5.4.11")
}
```

## Configuration

### Gradle Plugin Configuration

```kotlin
metalastic {
    metamodels {
        // Global defaults (apply to all source sets)
        packageName = "com.example.search"
        className = "Metamodels"
        classPrefix = "Meta"  // Default

        // Source set specific overrides
        main {
            packageName = "com.example.search.main"
            className = "MainMetamodels"
        }

        test {
            packageName = "com.example.search.test"
            className = "TestMetamodels"
        }
    }

    features {
        generateJavaCompatibility = true  // @JvmField annotations (default: true)
        generatePrivateClassMetamodels = false  // Process private classes (default: false)
    }

    reporting {
        enabled = false  // Generate debug reports (default: false)
        outputPath = "build/reports/metalastic/processor-report.md"
    }
}
```

### Manual KSP Configuration

```kotlin
ksp {
    // Global configuration
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
    arg("metamodels.classPrefix", "Meta")

    // Source set specific
    arg("metamodels.main.package", "com.example.search.main")

    // Features
    arg("metalastic.generateJavaCompatibility", "true")
    arg("metalastic.generatePrivateClassMetamodels", "false")

    // Reporting
    arg("metalastic.reportingPath", "build/reports/metalastic/report.md")
}
```

## Technology Stack

- **Kotlin**: 2.2.20
- **Java**: 21
- **KSP**: 2.2.20-2.0.3
- **Spring Data Elasticsearch**: 5.5.6 (core/processor), version-specific (DSL modules)
- **Elasticsearch Java Client**: 8.18.x (rolling DSL), 8.13.x (frozen DSL)
- **KotlinPoet**: 2.2.0
- **Google Guava**: 33.5.0-jre (DSL modules)
- **Testing**: Kotest 5.9.1

## Requirements

- **Java**: 21 or higher
- **Kotlin**: 2.2.20 or higher
- **Gradle**: 8.x or higher
- **Spring Data Elasticsearch**: 5.0.x - 5.5.x (version-specific per DSL artifact)

## Project Links

- **GitHub**: https://github.com/ekino/Metalastic
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
- **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
- **Documentation**: https://github.com/ekino/Metalastic/blob/master/README.md

## Maven Coordinates Summary

All artifacts published to Maven Central under group ID `com.ekino.oss`:

```kotlin
// Core and processor (automatically added by gradle-plugin)
implementation("com.ekino.oss:metalastic-core:1.0.0")
ksp("com.ekino.oss:metalastic-processor:1.0.0")

// BOM for version alignment
implementation(platform("com.ekino.oss:metalastic-bom:1.0.0"))

// DSL modules (choose one based on Spring Data ES version)
implementation("com.ekino.oss:metalastic-elasticsearch-dsl:1.0.0")       // 6.0.x
implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0.0")  // 5.4-5.5
implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3:1.0.0")  // 5.0-5.3

// Gradle plugin
plugins {
    id("com.ekino.oss.metalastic") version "1.0.0"
}
```

## Distribution

**Published to**:
- **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
- **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

**Group ID**: `com.ekino.oss`

**No authentication required** - Available for all Java/Kotlin projects!

## License

MIT License - Copyright (c) 2025 ekino

See [LICENSE](https://github.com/ekino/Metalastic/blob/master/LICENSE) for full license text.

## Contributing

Contributions are welcome! Please see:
- **Code Style**: ktfmt Google Style (`./gradlew spotlessApply`)
- **Testing**: Kotest ShouldSpec format
- **Quality Checks**: `./gradlew check`
- **Build**: `./gradlew build`

Before submitting a PR:
1. Run `./gradlew spotlessApply` to format code
2. Run `./gradlew check` to verify quality checks pass
3. Run `./gradlew test` to ensure all tests pass
4. Add tests for new functionality

## What's Next?

Future roadmap (tentative):

- **v1.1.0**: Enhanced DSL features, additional query types
- **v1.2.0**: Aggregation DSL support
- **v2.0.0**: Potential multi-platform support (JVM + Native)

## Feedback and Support

For questions, issues, or feature requests:
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Discussions**: https://github.com/ekino/Metalastic/discussions

---

**Thank you for using Metalastic!** 🚀

This release represents the culmination of extensive development and testing. We're excited to bring type-safe Elasticsearch development to the Kotlin ecosystem!
