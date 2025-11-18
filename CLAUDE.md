# Metalastic

A QueryDSL-inspired type-safe metamodel generator for Elasticsearch in Kotlin.

## Project Overview

Metalastic is a multi-module Kotlin project that automatically generates type-safe metamodel classes for Spring Data Elasticsearch documents. The library includes an annotation processor that generates metamodel classes (default prefix: `Meta`) for all classes annotated with `@Document`, plus a query DSL module for type-safe Elasticsearch query building.

**End Goal**: Provide a reusable library that integrates into Java/Kotlin projects to automatically generate type-safe query builders from existing Spring Data Elasticsearch document classes.

## Multi-Module Structure

### Core Modules

- **core**: Runtime library with metamodel base types, field definitions, and path traversal API
- **processor**: KSP annotation processor that generates metamodel classes from `@Document` annotated classes
- **gradle-plugin**: Gradle plugin providing type-safe DSL configuration for the annotation processor
- **test**: Integration tests verifying the annotation processor with real Spring Data Elasticsearch documents

### Query DSL Modules

- **elasticsearch-dsl-{version}**: Type-safe Elasticsearch query builder using generated metamodels
  - **Multi-version support**: 2 artifacts based on Spring Data ES API compatibility
  - **Version format**: `{dsl-version}` (e.g., `1.0.0`)
  - **Artifacts**:
    - `metalastic-elasticsearch-dsl-5.0:1.0.0` (Spring Data ES 5.0-5.3, brings 5.3.13 transitively)
    - `metalastic-elasticsearch-dsl-5.4:1.0.0` (Spring Data ES 5.4-5.5, brings 5.5.6 transitively)
  - **Features**: BoolQueryDsl, QueryVariantDsl, type-safe query construction, runtime version warnings
  - **Implementation difference**: Only RangeQueryUtils.kt differs (~60 lines) due to elasticsearch-java 8.15 UntypedRangeQuery API

### BOM Module

- **bom**: Bill of Materials for version alignment
  - Provides dependency management for all Metalastic artifacts
  - Simplifies version management for consumers
  - Use with `implementation(platform("com.ekino.oss:metalastic-bom:1.0.0"))`

## Goals

- Type-safe query construction using generated metamodels
- Fluent API inspired by QueryDSL for SQL databases
- Comprehensive Elasticsearch query type support
- Kotlin-first design with idiomatic APIs
- Compile-time validation through type safety
- Automatic metamodel generation from `@Document` classes
- Full support for all `@Field` annotation FieldType configurations
- Java and Kotlin consumer project support
- Type-safe Gradle plugin with discoverable DSL configuration
- Runtime type tracking for advanced query building

## Technology Stack

### Core & Processor
- **Language**: Kotlin 2.2.20
- **Java Version**: Java 21
- **Build Tool**: Gradle with Kotlin DSL
- **Spring Data Elasticsearch**: 5.5.6
- **Annotation Processing**: KSP 2.2.20-2.0.3
- **Code Generation**: KotlinPoet 2.2.0
- **Testing**: Kotest v5.9.1 (ShouldSpec format)

### Elasticsearch DSL Modules
- **Multi-version support**: 2 modules based on elasticsearch-java API compatibility
- **Modules**:
  - `elasticsearch-dsl-5.0` for Spring Data ES 5.0-5.3 (elasticsearch-java 8.5-8.13)
  - `elasticsearch-dsl-5.4` for Spring Data ES 5.4-5.5 (elasticsearch-java 8.15-8.18, uses UntypedRangeQuery)
- **Version-specific dependencies**: Each module brings latest Spring Data ES in its range as transitive dependency
- **Google Guava**: For Range support (all versions)

## Development Notes

### Project Structure
- Multi-module Gradle project with Kotlin DSL
- **modules/core**: Runtime in `src/main/kotlin/`, tests in `src/test/kotlin/`
- **modules/processor**: Three-phase annotation processor (COLLECTING, BUILDING, WRITING)
- **modules/gradle-plugin**: Type-safe configuration DSL
- **modules/bom**: Bill of Materials for version alignment (published)
- **modules/elasticsearch-dsl-5.0**: DSL for Spring Data ES 5.0-5.3 (published, brings 5.3.13 transitively)
- **modules/elasticsearch-dsl-5.4**: DSL for Spring Data ES 5.4-5.5 (published, brings 5.5.6 transitively, uses UntypedRangeQuery)
- **modules/test**: End-to-end integration tests

### Generation Behavior
- Default class prefix: `Meta` (configurable via `classPrefix` option)
- Generated classes are regular classes (not data objects) for constructor parameter support
- Each document metamodel includes a companion object with singleton instance
- Centralized `Metamodels` registry as data object with `entries()` function
- Full generic type system with `T : Any?` parameters
- Runtime type tracking via `kotlin.reflect.KType`

### Processor Architecture
- **COLLECTING Phase**: Discover `@Document` classes, build dependency graph
- **BUILDING Phase**: Generate KotlinPoet TypeSpec objects
- **WRITING Phase**: Write files to `build/generated/ksp/{sourceSet}/kotlin/`

## Build and Test

```bash
# Build all modules
./gradlew build

# Format code (ktfmt Google Style)
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run all checks (spotless + detekt)
./gradlew check

# Test individual modules
./gradlew :modules:core:test
./gradlew :modules:processor:test
./gradlew :modules:test:test

# Test DSL modules
./gradlew :modules:elasticsearch-dsl-5.0:test
./gradlew :modules:elasticsearch-dsl-5.4:test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Code Style Guidelines

- **No star imports**: Use explicit imports only
- **Formatting**: Spotless with ktfmt Google Style (run `./gradlew spotlessApply`)
- **Static Analysis**: detekt for code quality checks
- **Testing**: Kotest ShouldSpec format (no JUnit)
- **Logging**: KSPLogger in processor, KotlinLogging in tests (no println)
- **Annotation Processing**: KSP only (not kapt)

## Generated DSL Structure

The annotation processor generates two main types of artifacts with a modern generic type system.

### 1. Document Metamodel Classes

```kotlin
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Generated("com.ekino.oss.metalastic.processor.MetalasticSymbolProcessor")
class MetaTestDocument<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<TestDocument>(),
) : Document<T>(parent, name, nested, fieldType) {

    @JvmField
    val id: KeywordField<String> = keyword<String>("id")

    @JvmField
    val name: TextField<String> = text<String>("name")

    @JvmField
    val address: MetaAddress<Address> =
        MetaAddress(this, "address", false, typeOf<Address>())

    @JvmField
    val tags: MetaTag<Tag> =
        MetaTag(this, "tags", true, typeOf<Tag>())  // nested = true

    override fun indexName(): String = INDEX_NAME

    companion object {
        const val INDEX_NAME = "test_document"

        @JvmField
        @JvmStatic
        val testDocument: MetaTestDocument<TestDocument> =
            MetaTestDocument(fieldType = typeOf<TestDocument>())
    }
}

// Object field classes for object/nested types
class MetaAddress<T : Any?>(
    parent: ObjectField<*>?,
    name: String,
    nested: Boolean,
    fieldType: KType,
) : ObjectField<T>(parent, name, nested, fieldType) {

    @JvmField
    val city: TextField<String> = text<String>("city")

    @JvmField
    val street: TextField<String> = text<String>("street")

    @JvmField
    val zipCode: KeywordField<String> = keyword<String>("zipCode")
}
```

### 2. Centralized Metamodels Registry

```kotlin
import com.ekino.oss.metalastic.coreDocument
import com.metalastictest.integration.MetaTestDocument.Companion.testDocument
import com.metalastictest.integration.MetaExampleDocument.Companion.exampleDocument
import jakarta.annotation.Generated
import kotlin.jvm.JvmStatic
import kotlin.sequences.Sequence

@Generated(
    value = ["com.ekino.oss.metalastic.processor.MetalasticSymbolProcessor"],
    date = "2025-10-10T10:00:00Z"
)
object Metamodels {
    /**
     * Returns a sequence of all generated metamodel instances.
     */
    @JvmStatic
    fun entries(): Sequence<Document<*>> = sequenceOf(
        testDocument,
        exampleDocument,
        // ... other documents
    )
}
```

**Access Pattern**:
```kotlin
// Import from companion object
import com.metalastictest.integration.MetaTestDocument.Companion.testDocument

// Use directly
testDocument.name.path() shouldBe "name"
testDocument.address.city.path() shouldBe "address.city"

// Or iterate all metamodels
Metamodels.entries().forEach { doc ->
    println(doc.indexName())
}
```

## Core Type System

### Generic Type Hierarchy

All metamodel types use generic type parameters with runtime `KType` tracking:

```kotlin
sealed class Metamodel<T : Any?>(
    val fieldType: KType,
)

abstract class Container<T : Any?>(
    fieldType: KType,
) : Metamodel<T>(fieldType) {
    // Field registration for path traversal
}

abstract class ObjectField<T : Any?>(
    val parent: ObjectField<*>?,
    val name: String,
    val nested: Boolean,
    fieldType: KType,
) : Container<T>(fieldType)

abstract class Document<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType,
) : ObjectField<T>(parent, name, nested, fieldType) {
    abstract fun indexName(): String
}
```

### Field Types

All field types are generic with type-safe construction:

```kotlin
// Simple fields
val id: KeywordField<String> = keyword<String>("id")
val name: TextField<String> = text<String>("name")
val age: IntegerField<Int> = integer<Int>("age")
val active: BooleanField<Boolean> = boolean<Boolean>("active")
val score: DoubleField<Double> = double<Double>("score")
val createdAt: DateField<Date> = date<Date>("createdAt")

// Object fields (reference to metamodel)
val address: MetaAddress<Address> = MetaAddress(this, "address", false, typeOf<Address>())

// Nested fields (nested = true)
val tags: MetaTag<Tag> = MetaTag(this, "tags", true, typeOf<Tag>())
```

### Terminal Object Types

For cases where full metamodel generation is not possible:

#### SelfReferencingObject

Handles circular references (e.g., Category → parent: Category):

```kotlin
class MetaCategory<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<Category>(),
) : Document<T>(parent, name, nested, fieldType) {

    @JvmField
    val name: TextField<String> = text<String>("name")

    // Circular reference - generates SelfReferencingObject
    @JvmField
    val parent: SelfReferencingObject<MetaCategory<*>> =
        SelfReferencingObject(this, "parent", false, typeOf<Category>())

    companion object {
        @JvmField
        @JvmStatic
        val category: MetaCategory<Category> = MetaCategory()
    }
}
```

#### UnModellableObject

Handles types that cannot be fully modeled (e.g., `Map<String, Any>`, unresolved types):

```kotlin
class MetaDocument<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<Document>(),
) : Document<T>(parent, name, nested, fieldType) {

    @JvmField
    val id: KeywordField<String> = keyword<String>("id")

    // Complex type that can't be modeled - generates UnModellableObject
    @JvmField
    val metadata: UnModellableObject =
        UnModellableObject(this, "metadata", false, typeOf<Map<String, Any>>())

    companion object {
        @JvmField
        @JvmStatic
        val document: MetaDocument<Document> = MetaDocument()
    }
}
```

### MultiField Support

Handles `@MultiField` annotations with `@InnerField`:

```java
@MultiField(
    mainField = @Field(type = FieldType.Long),
    otherFields = {
        @InnerField(suffix = "search", type = FieldType.Text),
        @InnerField(suffix = "keyword", type = FieldType.Keyword)
    }
)
private Long longCode;
```

Generated code:

```kotlin
class MetaDocument<T : Any?>(...) : Document<T>(...) {

    // Main field
    @JvmField
    val longCode: LongField<Long> = long<Long>("longCode")

    // Inner fields in nested object
    object LongCodeMultiField {
        @JvmField
        val search: TextField<String> = text<String>("longCode.search")

        @JvmField
        val keyword: KeywordField<String> = keyword<String>("longCode.keyword")
    }
}
```

## Path Traversal API

The generated metamodels support comprehensive path traversal:

```kotlin
import com.example.MetaTestDocument.Companion.testDocument

// Root path
testDocument.path() shouldBe ""

// Simple field paths
testDocument.id.path() shouldBe "id"
testDocument.name.path() shouldBe "name"

// Nested object paths
testDocument.address.city.path() shouldBe "address.city"
testDocument.address.street.path() shouldBe "address.street"

// Nested field paths (with nested flag)
testDocument.tags.name.path() shouldBe "tags.name"

// Nested detection
testDocument.address.city.isNestedPath() shouldBe false
testDocument.tags.name.isNestedPath() shouldBe true
testDocument.tags.name.nestedPaths().toList() shouldContainExactly listOf("tags")

// Parent traversal
testDocument.address.city.parents() shouldContain testDocument.address

// Document-to-Document references
import com.example.MetaExampleDocument.Companion.exampleDocument

exampleDocument.testDocument.name.path() shouldBe "testDocument.name"
exampleDocument.testDocument.address.city.path() shouldBe "testDocument.address.city"
```

## Elasticsearch DSL Modules

### Overview

The `elasticsearch-dsl-{version}` modules provide type-safe query builders using generated metamodels.

**Multi-version Support**: Separate artifacts for Spring Data ES 5.0, 5.1, 5.2, 5.3, 5.4, and 5.5
**Versioning**: Semantic versioning (e.g., `1.0.0`). Choose artifact matching your Spring Data ES version.

### Version Compatibility Matrix

| Artifact | Spring Data ES | Elasticsearch Java | Maven Coordinate | Source Module |
|----------|---------------|-------------------|------------------|---------------|
| elasticsearch-dsl-5.0 | 5.0.12 | 8.5.3 | `metalastic-elasticsearch-dsl-5.0:1.0.0` | shared-8.5 |
| elasticsearch-dsl-5.1 | 5.1.+ | 8.7.1 | `metalastic-elasticsearch-dsl-5.1:1.0.0` | shared-8.5 |
| elasticsearch-dsl-5.2 | 5.2.+ | 8.11.1 | `metalastic-elasticsearch-dsl-5.2:1.0.0` | shared-8.5 |
| elasticsearch-dsl-5.3 | 5.3.+ | 8.13.4 | `metalastic-elasticsearch-dsl-5.3:1.0.0` | shared-8.5 |
| elasticsearch-dsl-5.4 | 5.4.+ | 8.15.5 | `metalastic-elasticsearch-dsl-5.4:1.0.0` | shared-8.15 |
| elasticsearch-dsl-5.5 | 5.5.+ | 8.18.8 | `metalastic-elasticsearch-dsl-5.5:1.0.0` | shared-8.15 |

### Query Types Supported

- **Full-text**: match, multiMatch, matchPhrase, matchPhrasePrefix
- **Term-level**: term, terms, termsSet, wildcard, prefix, regexp
- **Boolean**: bool, shouldAtLeastOneOf, disMax
- **Range**: range, greaterThan, lowerThan, mustBeBetween
- **Nested**: nested
- **Specialized**: fuzzy, exist, geoDistance, moreLikeThis

### Usage Example

```kotlin
import com.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

// Boolean query with typed occurrences
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

// Nested query
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

### Value Conversion

The DSL module includes type-safe value converters for:
- Primitive types (String, Int, Long, Double, etc.)
- Date types (Date, Instant, LocalDate, LocalDateTime)
- Collections (List, Set)
- Enums
- Custom types via `KType` inspection

## Configuration

### Using Gradle Plugin (Recommended)

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.ekino.oss.metalastic") version "3.0.0"
}

repositories {
    mavenCentral()
}

metalastic {
    metamodels {
        // Global defaults (all source sets)
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

        // Custom source sets
        sourceSet("integration") {
            packageName = "com.example.search.integration"
            className = "IntegrationMetamodels"
        }
    }

    features {
        generateJavaCompatibility = true  // @JvmField annotations (default: true)
        generatePrivateClassMetamodels = false  // Process private classes (default: false)
    }

    reporting {
        enabled = true  // Generate debug reports (default: false)
        outputPath = "build/reports/metalastic/processor-report.md"
    }
}

// Optional: Add elasticsearch-dsl module for query building
dependencies {
    implementation("com.metalastic:elasticsearch-dsl:5.0.12-1.0")
}
```

### Configuration Resolution Strategy

1. **Source set specific** (highest priority) - `metalastic.metamodels.main { ... }`
2. **Global defaults** - `metalastic.metamodels { packageName = "..." }`
3. **Hard-coded defaults** - `Meta` prefix, `com.ekino.oss.metalastic` package
4. **Auto-detection** - From source file paths (src/main/kotlin → "main")

### Manual Configuration (without plugin)

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
}

ksp {
    // Global configuration
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
    arg("metamodels.classPrefix", "Meta")

    // Source set specific
    arg("metamodels.main.package", "com.example.search.main")
    arg("metamodels.main.className", "MainMetamodels")

    // Features
    arg("metalastic.generateJavaCompatibility", "true")
    arg("metalastic.generatePrivateClassMetamodels", "false")

    // Reporting
    arg("metalastic.reportingPath", "build/reports/metalastic/report.md")
}
```

## Publishing

### Maven Central

- **Repository**: https://github.com/ekino/Metalastic
- **CI/CD**: GitHub Actions with automatic publishing on tag pushes
- **Package Registry**: https://central.sonatype.com/namespace/com.ekino.oss
- **Group ID**: `com.ekino.oss`
- **License**: MIT License
- **Published modules**: core, processor, elasticsearch-dsl-*

### Gradle Plugin Portal

- **Plugin ID**: `com.ekino.oss.metalastic`
- **Portal**: https://plugins.gradle.org/plugin/com.ekino.oss.metalastic
- **Published module**: gradle-plugin

### Published Artifacts

**Core and DSL modules (Maven Central):**
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")

    // Optional: Query DSL module (choose version matching your Spring Data ES)
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:1.0.0")  // For Spring Data ES 5.5.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.4:1.0.0")  // For Spring Data ES 5.4.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3:1.0.0")  // For Spring Data ES 5.3.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.2:1.0.0")  // For Spring Data ES 5.2.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.1:1.0.0")  // For Spring Data ES 5.1.x
    // OR implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.0:1.0.0")  // For Spring Data ES 5.0.x
}
```

**Gradle Plugin (Gradle Plugin Portal):**
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.ekino.oss.metalastic") version "1.0.0"
}
```

### Versioning Strategy

**Core modules** (core, processor):
- Semantic versioning: `v1.0.0`
- Git tag: `v1.0.0`
- Published to Maven Central
- Automatic from GITHUB_REF_NAME or git describe

**Gradle Plugin**:
- Semantic versioning: `v1.0.0`
- Git tag: `v1.0.0`
- Published to Gradle Plugin Portal
- Automatic from GITHUB_REF_NAME or git describe

**Elasticsearch DSL modules**:
- Multi-version support with separate artifacts per Spring Data ES version
- Version format: `{dsl-version}` (e.g., `1.0.0`) - uses semantic versioning
- Git tags: `elasticsearch-dsl-5.x-v{dsl-version}` (e.g., `elasticsearch-dsl-5.5-v1.0.0`)
- All versions supported: Spring Data ES 5.0, 5.1, 5.2, 5.3, 5.4, and 5.5
- Two shared source modules:
  - `elasticsearch-dsl-shared-8.5` for versions 5.0-5.3 (elasticsearch-java 8.5-8.13)
  - `elasticsearch-dsl-shared-8.15` for versions 5.4-5.5 (elasticsearch-java 8.15+, uses UntypedRangeQuery)

See [TAG_MANAGEMENT.md](TAG_MANAGEMENT.md) for detailed publishing workflows.

## Architecture Design

### Metamodel Generation Pattern

**Two-tier generation**:
1. **Document Metamodel Classes**: Regular Kotlin classes with constructor parameters
2. **Centralized Registry**: Data object with imports from companion objects

### Key Design Decisions

- **Generic Type System**: All types parameterized with `T : Any?` for type safety
- **Runtime Type Tracking**: `KType` parameter for runtime type inspection
- **Classes over Data Objects**: Support for constructor parameters enables nested references
- **Companion Objects**: Each document metamodel has singleton instance in companion
- **Default Constructor Values**: Clean instantiation with optional parent/name/nested parameters
- **@JvmField Annotations**: Optimal Java interoperability
- **Path Building**: Automatic path construction through parent hierarchy traversal
- **Import Optimization**: Package proximity prioritization and conflict resolution
- **Terminal Objects**: `SelfReferencingObject` and `UnModellableObject` for edge cases

### Processor Architecture

**Three-Phase SOLID Design**:

1. **COLLECTING Phase** (`GraphBuilder`):
   - Discover `@Document` annotated classes
   - Build dependency graph via `MetalasticGraph`
   - Resolve circular references
   - Collect field metadata via `FieldCollector`

2. **BUILDING Phase** (`BuildingOrchestrator`):
   - Generate KotlinPoet `TypeSpec` objects via `QClassGenerator`
   - Build centralized `Metamodels` registry via `MetamodelsBuilder`
   - Apply type transformations and private class handling
   - Optimize imports and resolve conflicts

3. **WRITING Phase** (`FileWriter`):
   - Write generated files to `build/generated/ksp/{sourceSet}/kotlin/`
   - Generate optional debug reports via `Reporter`
   - Track performance metrics

### Constants and Configuration

**CoreConstants** (Processor):
```kotlin
object CoreConstants {
    const val CORE_PACKAGE = "com.ekino.oss.metalastic.core"
    const val FALLBACK_METAMODELS_PACKAGE = "com.ekino.oss.metalastic"
    const val META_PREFIX = "Meta"
    const val MULTIFIELD_POSTFIX = "MultiField"
    const val DOCUMENT_CLASS = "Document"
    const val OBJECT_FIELDS_CLASS = "ObjectField"
    const val METAMODELS_CLASS_NAME = "Metamodels"
    const val DOCUMENT_ANNOTATION = "org.springframework.data.elasticsearch.annotations.Document"
}
```

**PluginConstants** (Gradle Plugin):
```kotlin
object PluginConstants {
    object Metamodels {
        const val DEFAULT_PACKAGE = "com.ekino.oss.metalastic"
        const val DEFAULT_CLASS_NAME = "Metamodels"
        const val DEFAULT_CLASS_PREFIX = "Meta"
    }

    object Features {
        const val DEFAULT_GENERATE_JAVA_COMPATIBILITY = true
        const val DEFAULT_GENERATE_PRIVATE_CLASS_METAMODELS = false
    }

    object Reporting {
        const val DEFAULT_ENABLED = false
        const val DEFAULT_OUTPUT_PATH = "build/reports/metalastic/processor-report.md"
    }
}
```

## Debug Reporting

When `reporting.enabled = true`, the processor generates append-only markdown reports:

**Report Features**:
- Timestamped sections (one per build)
- Auto-generated table of contents
- Three-phase timing breakdown
- Detailed field processing logs
- Configuration tracking per source set
- Performance metrics with emoji indicators

**Example Report Structure**:
```markdown
# Metalastic Processor Reports

## 📋 Table of Contents
- [Report 1 - 2025-10-10 10:00:00](#report-1---2025-10-10-100000)

---

## report-1---2025-10-10-100000
**Generated:** 2025-10-10 10:00:00

### 📋 Detailed Log
[19:10:42.459] 🔍 DEBUG: Found 42 Meta classes:
[19:10:42.574] 🔍 DEBUG: 🔬 COLLECTING phase completed in 2.060s 📊
[19:10:42.719] 🔍 DEBUG: 👷️ BUILDING phase completed in 144ms 📊
[19:10:42.782] 🔍 DEBUG: 📝 WRITING phase completed in 62ms 📊
[19:10:42.783] 🔍 DEBUG: 🏁 Metalastic processor completed in 2.270s 📊
```

See [PROCESSOR_ARCHITECTURE.md](PROCESSOR_ARCHITECTURE.md) for complete architecture documentation.

## Development Memories

### Build & Test
- Publish to `mavenLocal` when finishing a feature: `./gradlew publishToMavenLocal`
- Use ShouldSpec format with Kotest v5.9.1 (no JUnit)
- Use `Enum.entries` instead of deprecated `Enum.values()`
- Use KotlinLogging for test output (no println statements)
- Processor uses KSPLogger (not regular logging)

### Code Style
- Use ktfmt Google Style: `./gradlew spotlessApply`
- No star imports (explicit imports only)
- Run spotless before committing
- Use KSP for annotation processing (not kapt)

### API Patterns
- `path()` is a function, not a property
- `isNestedPath()` for nested detection (not `fieldPath.isNested`)
- Generic type parameters required: `<T : Any?>`
- Runtime type tracking: `typeOf<T>()`
- Companion object access pattern for metamodels

### Architecture
- Three-phase processor: COLLECTING → BUILDING → WRITING
- Graph-based model building
- Terminal objects for circular refs and unmodellable types
- Default prefix is `Meta` (not `Q`)
- Document class (not Index class)

## Additional Documentation

- [PROCESSOR_ARCHITECTURE.md](PROCESSOR_ARCHITECTURE.md) - Comprehensive processor architecture
- [TAG_MANAGEMENT.md](TAG_MANAGEMENT.md) - Publishing and tag management guide
- [PUBLISHING.md](PUBLISHING.md) - Consumer usage guide

**Note**: v1.0.0 will be the first official release on Maven Central. Currently preparing for release (not yet published).