# Metalastic v1.0.0 - Official Open Source Release

**Release Date:** TBD

## 🎉 First Official Release on Maven Central

This is the first official open source release of **Metalastic**, a QueryDSL-inspired type-safe metamodel generator for Elasticsearch in Kotlin.

Metalastic automatically generates type-safe metamodel classes from Spring Data Elasticsearch `@Document` annotated classes, enabling compile-time validation and IDE auto-completion for Elasticsearch queries.

## Key Features

### 🔧 Core Functionality
- **Automatic Metamodel Generation**: KSP-based annotation processor generates type-safe metamodel classes from `@Document` classes
- **Type-Safe Path Traversal**: Navigate nested object structures with compile-time safety
- **Full Field Type Support**: Handles all Spring Data Elasticsearch field types including `@MultiField` and nested objects
- **Runtime Type Tracking**: Uses Kotlin `KType` for advanced type inspection
- **Java Interoperability**: Full support for Java projects with `@JvmField` annotations

### 📦 Modules

1. **metalastic-core**: Runtime library with metamodel base types and field definitions
2. **metalastic-processor**: KSP annotation processor for code generation
3. **metalastic-gradle-plugin**: Type-safe Gradle plugin for easy configuration
4. **metalastic-elasticsearch-dsl**: Query builder DSL with Spring Data Elasticsearch integration

### 🚀 Query DSL (elasticsearch-dsl module)

- Type-safe Elasticsearch query construction
- Supports: `match`, `term`, `bool`, `range`, `nested`, and more
- Fluent API with operator overloading (`+` for clause addition)
- Version-aligned with Spring Data Elasticsearch

### 🎯 Architecture Highlights

- **Three-Phase Processing**: COLLECTING → BUILDING → WRITING
- **Graph-Based Model Building**: Handles circular references and complex relationships
- **Terminal Objects**: `SelfReferencingObject` and `UnModellableObject` for edge cases
- **Debug Reporting**: Optional markdown reports for processor insights
- **Import Optimization**: Intelligent package proximity and conflict resolution

## Distribution

**Published to Maven Central**:
- No authentication required
- Available for all Java/Kotlin projects
- Group ID: `com.ekino.oss`

## Getting Started

### Installation

#### Using Gradle Plugin (Recommended)

Add to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.2"
    id("com.ekino.oss.metalastic") version "3.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Optional: Query DSL module for type-safe query building
    // Choose the version matching your Spring Data Elasticsearch version
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0")  // For Spring Data ES 5.5.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.4:1.0")  // For Spring Data ES 5.4.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.0:1.0")  // For Spring Data ES 5.0.x
}
```

**Note**: The Gradle plugin automatically adds `metalastic-core` and `metalastic-processor` dependencies. No need to add them manually!

#### Manual Setup (Without Plugin)

If you prefer not to use the plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")

    // Optional: Query DSL module
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0")  // Choose your version
}

// Configure KSP options manually
ksp {
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
}
```

### Configuration

```kotlin
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

    reporting {
        enabled = false
        outputPath = "build/reports/metalastic/processor-report.md"
    }
}
```

### Usage Example

Given a Spring Data Elasticsearch document:

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
```

Metalastic generates:

```kotlin
import com.example.search.metamodels.MetaProduct.Companion.product

// Type-safe path traversal
product.title.path() // "title"
product.manufacturer.name.path() // "manufacturer.name"

// Query DSL (with elasticsearch-dsl module)
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.price greaterThan 500.0
        }
        filter + {
            product.category term "electronics"
        }
    }
}
```

## Maven Coordinates

All artifacts are published to Maven Central under group ID `com.ekino.oss`:

| Artifact | Version | Usage |
|----------|---------|-------|
| `metalastic-gradle-plugin` | `3.0.0` | Gradle plugin (automatically adds core + processor) |
| `metalastic-core` | `1.0.0` | Runtime library (added automatically by plugin) |
| `metalastic-processor` | `1.0.0` | KSP processor (added automatically by plugin) |
| `metalastic-elasticsearch-dsl-5.5` | `1.0` | Optional query DSL for Spring Data ES 5.5.x |
| `metalastic-elasticsearch-dsl-5.4` | `1.0` | Optional query DSL for Spring Data ES 5.4.x |
| `metalastic-elasticsearch-dsl-5.0` | `1.0` | Optional query DSL for Spring Data ES 5.0.x |

**Recommended setup** (using Gradle plugin):

```kotlin
plugins {
    id("com.ekino.oss.metalastic") version "3.0.0"
}

dependencies {
    // Optional: Add DSL module for query building (choose your Spring Data ES version)
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0")
}
```

**Manual setup** (without plugin):

```kotlin
dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
}
```

## Technology Stack

- **Kotlin**: 2.2.21
- **Java**: 21
- **KSP**: 2.3.2
- **Spring Data Elasticsearch**: 5.5.6 (core/processor), version-specific (DSL modules)
- **KotlinPoet**: 2.2.0
- **Testing**: Kotest 5.9.1

## Project Links

- **GitHub**: https://github.com/ekino/Metalastic
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Maven Central**: https://central.sonatype.com/namespace/com.ekino.oss
- **Documentation**: https://github.com/ekino/Metalastic/blob/master/README.md

## Requirements

- Java 21 or higher
- Kotlin 2.2.21 or higher (2.2.20+ supported)
- Gradle 8.x or higher
- Spring Data Elasticsearch 5.x

## License

MIT License - Copyright (c) 2025 ekino

## Contributing

Contributions are welcome! Please see:
- **Code Style**: ktfmt Google Style
- **Testing**: Kotest ShouldSpec format
- **Quality**: Run `./gradlew spotlessApply && ./gradlew check`

## What's Next?

- **v1.1.0**: Enhanced DSL features and Spring Data Elasticsearch 5.5.x support for DSL module
- **v1.2.0**: Additional query types and aggregation support
- **v2.0.0**: Potential multi-platform support (JVM + Native)

---

**Thank you for using Metalastic!** 🚀

For questions, issues, or feature requests, please visit: https://github.com/ekino/Metalastic/issues
