# Metalastic

A QueryDSL-like library for Elasticsearch in Kotlin, inspired by http://querydsl.com/

## Project Overview

This is a multi-module project that provides a type-safe, fluent query builder for Elasticsearch similar to QueryDSL's approach for SQL databases. The library includes an annotation processor that generates QIndex classes for all classes annotated with Spring Data Elasticsearch's @Document annotation.

**End Goal**: Create a reusable library that can be integrated into other Java/Kotlin projects to automatically generate type-safe query builders from existing Spring Data Elasticsearch document classes.

## Multi-Module Structure

- **core**: Core DSL runtime library containing field definitions, index abstractions, and the DSL API
- **processor**: Annotation processor that generates QIndex classes from @Document annotated classes
- **gradle-plugin**: Gradle plugin providing type-safe DSL configuration for the annotation processor
- **test**: Integration tests that verify the annotation processor works correctly with real Spring Data Elasticsearch documents

## Goals

- Type-safe query construction
- Fluent API similar to QueryDSL
- Support for common Elasticsearch query types
- Kotlin-first design with idiomatic APIs
- Compile-time query validation where possible
- Annotation processing for Spring Data Elasticsearch @Document classes
- Generate QIndex classes from @Document annotated classes (following QueryDSL naming convention)
- Handle all @Field annotation FieldType configurations
- Provide a reusable library for integration into other projects
- Support both Java and Kotlin consumer projects
- Type-safe Gradle plugin with discoverable DSL configuration

## Technology Stack

- **Language**: Kotlin
- **Java Version**: Java 21
- **Build Tool**: Gradle with Kotlin DSL
- **Target**: Elasticsearch query generation
- **Dependencies**: Spring Data Elasticsearch 5.5.1
- **Annotation Processing**: KSP (Kotlin Symbol Processing) for processing @Document annotations
- **Testing**: Kotest v5.9.1 (ShouldSpec format), MockK
- **Logging**: KotlinLogging for test output (no println statements)

## Development Notes

- Multi-module project structure using Gradle with Kotlin DSL
- **modules/core** module: Core DSL runtime in `src/main/kotlin/`, tests in `src/test/kotlin/`
- **modules/processor** module: Annotation processor implementation
- **modules/gradle-plugin** module: Gradle plugin with type-safe DSL configuration
- **modules/test** module: Integration tests for the entire library
- Annotation processor will scan for `@Document` annotations from Spring Data Elasticsearch
- Generated QIndex classes will follow Q* naming convention (similar to QueryDSL)
- Library will be published for consumption by other projects
- Annotation processor runs at compile-time in consumer projects
- Support for all Spring Data Elasticsearch FieldType values (Text, Keyword, Date, Long, Integer, Short, Byte, Double, Float, Boolean, Binary, Object, Nested, Ip, etc.)
- Generate type-safe predicates for common Elasticsearch query types (term, match, range, etc.)

## Build and Test

- Build all modules: `./gradlew build`
- Format code: `./gradlew spotlessApply`
- Check formatting: `./gradlew spotlessCheck`
- Run all checks: `./gradlew check`
- Test DSL module: `./gradlew :modules:core:test`
- Test processor module: `./gradlew :modules:processor:test`
- Test integration: `./gradlew :modules:test:test`
- Publish locally: `./gradlew publishToMavenLocal`

## Code Style Guidelines

- **No star imports**: Use explicit imports only
- **Code Quality**: Use Spotless with ktfmt Google Style for formatting and detekt for static analysis
- **Linting**: All code must pass Spotless formatting checks and detekt checks
- **KSP**: Use Kotlin Symbol Processing for annotation processing (not kapt)

## Generated DSL Structure

The annotation processor generates two main types of artifacts:

### 1. Document Metamodel Classes

```kotlin
// Document metamodel classes (regular classes with constructor parameters)
class QTestDocument(
    parent: ObjectField? = null,
    fieldName: String = "",
    nested: Boolean = false,
) : Index("test_document", parent, fieldName, nested) {
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")
    
    @JvmField
    val name: TextField<String> = textField<String>("name")
    
    @JvmField
    val address: QAddress = QAddress(this, "address", false)
    
    @JvmField
    val tags: QTag = QTag(this, "tags", true) // nested = true
}

// Object field classes for nested/object types
class QAddress(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {
    @JvmField
    val city: TextField<String> = textField<String>("city")
    
    @JvmField
    val street: TextField<String> = textField<String>("street")
    
    @JvmField
    val zipCode: KeywordField<String> = keywordField<String>("zipCode")
}
```

### 2. Central Metamodels Registry

```kotlin
// Centralized registry for all document metamodels
@Generated("com.metalastic.processor.MetalasticSymbolProcessor")
data object Metamodels {
    /**
     * Metamodel for @Document class [com.metalastic.integration.TestDocument]
     */
    @JvmField
    val testDocument: QTestDocument = QTestDocument()
    
    /**
     * Metamodel for @Document class [com.metalastic.integration.ExampleDocument]
     */
    @JvmField
    val exampleDocument: QExampleDocument = QExampleDocument()
    
    // ... other document metamodels
}
```

## Real-World Example Transformation

The annotation processor should transform complex Spring Data Elasticsearch documents like this:

```java
@Document(indexName = "module_path_iperia")
public class IndexModulePath implements ElasticsearchIdentifiable<ModulePath> {
    
    @Id
    @Field(type = FieldType.Keyword)
    private TypedId<ModulePath> id;
    
    @MultiField(mainField = @Field(type = FieldType.Long), otherFields = {
        @InnerField(suffix = "search", type = FieldType.Text)
    })
    private Long longCode;
    
    @Field(type = FieldType.Object)
    private IndexTrainingAgency trainingAgency;
    
    @Field(type = FieldType.Nested)
    private Set<IndexAddress> addresses;
    
    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private Date lastUpdatedDate;
    
    @Field(type = FieldType.Boolean)
    private Boolean trainingAgencyAssociation;
    
    // ... other fields
}
```

Into a type-safe Kotlin DSL:

```kotlin
class QIndexModulePath(
    parent: ObjectField? = null,
    fieldName: String = "",
    nested: Boolean = false,
) : Index("module_path_iperia", parent, fieldName, nested) {
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")
    
    @JvmField
    val longCode: LongField<Long> = longField<Long>("longCode")
    
    @JvmField
    val trainingAgency: QIndexTrainingAgency = QIndexTrainingAgency(this, "trainingAgency", false)
    
    @JvmField
    val addresses: QIndexAddress = QIndexAddress(this, "addresses", true) // nested = true
    
    @JvmField
    val lastUpdatedDate: DateField<Date> = dateField<Date>("lastUpdatedDate")
    
    @JvmField
    val trainingAgencyAssociation: BooleanField<Boolean> = booleanField<Boolean>("trainingAgencyAssociation")
    
    // ... other fields
}

class QIndexTrainingAgency(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")
    
    @JvmField
    val legalPerson: QIndexLegalPerson = QIndexLegalPerson(this, "legalPerson", false)
}

class QIndexAddress(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")
    
    @JvmField
    val city: TextField<String> = textField<String>("city")
    
    @JvmField
    val departmentCode: KeywordField<String> = keywordField<String>("departmentCode")
    
    @JvmField
    val region: KeywordField<String> = keywordField<String>("region")
    
    @JvmField
    val location: GeoPointField = geoPointField("location")
}

// Access through centralized registry
val document = Metamodels.indexModulePath
```

## Path Traversal Requirements

The generated DSL should support dotted notation path traversal to obtain the full field path:

```kotlin
// Access document metamodel through centralized registry
val document = Metamodels.indexModulePath

// Path traversal examples
document.path() shouldBe ""
document.id.path() shouldBe "id"
document.longCode.path() shouldBe "longCode"
document.trainingAgency.id.path() shouldBe "trainingAgency.id"
document.addresses.city.path() shouldBe "addresses.city"

// Enhanced path information with nested detection
document.trainingAgency.id.isNestedPath() shouldBe false
document.addresses.city.isNestedPath() shouldBe true
document.addresses.city.nestedPaths().toList() shouldContainExactly listOf("addresses")

// @Document-to-@Document references work seamlessly
val exampleDoc = Metamodels.exampleDocument
exampleDoc.testDocument.name.path() shouldBe "testDocument.name"
exampleDoc.testDocument.address.city.path() shouldBe "testDocument.address.city"
```

This allows for:
- Root-level field path access
- Nested object field path construction
- Full dotted notation support for complex nested structures
- Type-safe field path building for query construction
```

## Publishing

This project is published to GitLab Maven Registry:
- **Repository**: https://gitlab.ekino.com/iperia/metalastic
- **CI/CD Pipeline**: Automatic publishing on master branch pushes
- **Package Registry**: https://gitlab.ekino.com/iperia/metalastic/-/packages
- **Group ID**: `com.metalastic`
- **Artifacts**: `metalastic-core`, `metalastic-processor`, `metalastic-gradle-plugin`, `metalastic-test`

### For Consumers

**Using Gradle Plugin (Recommended):**
```kotlin
plugins {
    id("com.metalastic") version "2.0.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
        // No credentials needed for project members
    }
}

metalastic {
    metamodels {
        main {
            packageName = "com.example.metamodels"
            className = "SearchMetamodels"
        }
    }
}
```

**Manual Configuration:**
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
        // No credentials needed for project members
    }
}

dependencies {
    implementation("com.metalastic:core:2.0.0")
    ksp("com.metalastic:processor:2.0.0")
}
```

## Architecture Design

### Metamodel Generation Pattern

The library follows a two-tier generation pattern:

1. **Document Metamodel Classes**: Regular Kotlin classes that can be instantiated with constructor parameters, enabling @Document-to-@Document references
2. **Centralized Registry**: A `Metamodels` data object that provides singleton-like access to all document metamodels

### Key Design Decisions

- **Classes over Data Objects**: Document metamodels are generated as regular classes (not data objects) to support constructor parameters for parent/path/nested relationships
- **Default Constructor Values**: All constructors have default values (`parent: ObjectField? = null, fieldName: String = "", nested: Boolean = false`) for clean instantiation
- **@JvmField Annotations**: All field properties are annotated with `@JvmField` for optimal Java interoperability
- **Path Building**: Automatic path construction through parent hierarchy traversal supports deep nested structures
- **Import Optimization**: Advanced import management with package proximity prioritization and conflict resolution

### Constants and Configuration

All generation constants are centralized in `CoreConstants`:

```kotlin
object CoreConstants {
    const val CORE_PACKAGE = "com.metalastic.core"
    const val METAMODELS_PACKAGE = "com.metalastic"
    const val Q_PREFIX = "Q"
    const val INDEX_CLASS = "Index"
    const val OBJECT_FIELDS_CLASS = "ObjectField"
    const val DOCUMENT_ANNOTATION = "org.springframework.data.elasticsearch.annotations.Document"
    const val METAMODELS_CLASS_NAME = "Metamodels"
    const val PRODUCT_NAME = "Metalastic"
}
```

## Memories

- remember to publish to mavenLocal when finish to implement a feature
- use ShouldSpec format with Kotest v5.9.1 for all tests (no JUnit)
- use Enum.entries instead of Enum.values
- use KotlinLogging for test output (no println statements)
- processor uses KSPLogger, not regular logging
- use ktfmt Google Style for formatting (not ktlint)
- run `./gradlew spotlessApply` before committing
- use KSP for annotation processing (not kapt)
- path() is a function, not a property
- isNestedPath() for nested detection, not fieldPath.isNested
- remember to use spotless before any commit