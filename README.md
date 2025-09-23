# Metalastic

A type-safe metamodel library for Elasticsearch in Kotlin that generates compile-time field definitions from Spring Data Elasticsearch document classes.

[![GitLab CI/CD](https://gitlab.ekino.com/iperia/metalastic/badges/master/pipeline.svg)](https://gitlab.ekino.com/iperia/metalastic/pipelines)
[![Maven Repository](https://img.shields.io/badge/maven-GitLab%20Registry-blue)](https://gitlab.ekino.com/iperia/metalastic/-/packages)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple)](https://kotlinlang.org/)
[![Spring Data ES](https://img.shields.io/badge/Spring%20Data%20ES-5.5.1-green)](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)

## Overview

Metalastic provides **compile-time code generation** to create type-safe, fluent field definitions for Elasticsearch documents. Inspired by QueryDSL's approach for SQL databases, this library generates Q-classes for Spring Data Elasticsearch `@Document` annotated classes, enabling type-safe field access and path construction.

### Why Metalastic?

- 🚫 **No more string-based field names** - Compile-time validation prevents typos
- 🔍 **IDE auto-completion** - Full IntelliSense support for nested document structures
- 🛡️ **Type safety** - Compile-time verification of field types and relationships
- 📦 **Zero runtime dependencies** - Generated code uses only your existing Spring Data ES
- 🔄 **Automatic updates** - Regenerated when document classes change
- 🎯 **QueryDSL-inspired** - Familiar API for developers coming from JPA/QueryDSL

## Key Features

- ✅ **Type-safe field access** - `document.name.path()` instead of `"name"`
- ✅ **Nested object support** - `document.address.city.path()` for nested structures
- ✅ **Path traversal** - Automatic dotted notation for Elasticsearch queries
- ✅ **All field types** - Complete support for every Elasticsearch field type
- ✅ **Multi-field support** - Handle `@MultiField` annotations with inner fields
- ✅ **Complex type support** - Terminal ObjectField generation for complex generics
- ✅ **Document references** - Type-safe cross-document relationships
- ✅ **Centralized registry** - `Metamodels` object for discovering all documents
- ✅ **Java compatibility** - Works seamlessly with Java projects
- ✅ **Version agnostic** - Runtime detection of Spring Data ES capabilities

## Quick Start

### 1. Add Dependencies

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
    }
}

dependencies {
    implementation("com.metalastic:core:1.0-SNAPSHOT")
    ksp("com.metalastic:processor:1.0-SNAPSHOT")
}
```

### 2. Configure Processor (Optional)

```kotlin
// build.gradle.kts
ksp {
    // Customize generated package and class names
    arg("metamodels.main.package", "com.example.search.metamodels")
    arg("metamodels.main.className", "SearchMetamodels")

    // Include private classes in metamodel generation (default: false)
    arg("metalastic.generatePrivateClassMetamodels", "true")

    // Enable debug reporting (optional)
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

## How It Works

```mermaid
graph LR
    A[Spring Data ES<br>@Document Classes] --> B[Metalastic<br>Annotation Processor]
    B --> C[Generated<br>Q-Classes]
    C --> D[Type-safe<br>Query Building]

    A1[@Document Person] --> B
    A2[@Field annotations] --> B
    A3[Nested objects] --> B

    B --> C1[QPerson.kt]
    B --> C2[Metamodels.kt]
    B --> C3[Object fields]
```

**Compilation Process:**
1. **Scan** for `@Document` annotated classes
2. **Parse** `@Field` annotations and nested structures
3. **Generate** type-safe Q-classes with field definitions
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
class QPerson(
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
    val address: QAddress = QAddress(this, "address", false)

    @JvmField
    val activities: QActivity = QActivity(this, "activities", true) // nested = true
}
```

#### Object Field Classes
```kotlin
class QAddress(
    parent: ObjectField?,
    path: String,
    nested: Boolean,
) : ObjectField(parent, path, nested) {

    @JvmField
    val city: TextField<String> = textField<String>("city")

    @JvmField
    val country: KeywordField<String> = keywordField<String>("country")
}

class QActivity(
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
@Generated("com.metalastic.processor.MetalasticSymbolProcessor")
data object Metamodels {
    /**
     * Metamodel for @Document class [com.example.Person]
     */
    @JvmField
    val person: QPerson = QPerson()

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
class QTitleMultiField(
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

For complex generic types that don't warrant their own Q-classes, Metalastic generates terminal ObjectField instances:

```java
@Document(indexName = "document")
public class ExampleDocument {
    // Complex types that don't need full Q-classes
    @Field(type = FieldType.Object)
    private Map<String, List<String>> metadata;

    @Field(type = FieldType.Nested)
    private Map<String, Set<Integer>> complexNested;
}
```

Generates terminal ObjectField instances:

```kotlin
class QExampleDocument(
    parent: ObjectField? = null,
    fieldName: String = "",
    nested: Boolean = false,
) : Index("document", parent, fieldName, nested) {

    // Terminal ObjectField for Object type
    @JvmField
    val metadata: ObjectField = object : ObjectField(parent = this, name = "metadata", nested = false) {}

    // Terminal ObjectField for Nested type
    @JvmField
    val complexNested: ObjectField = object : ObjectField(parent = this, name = "complexNested", nested = true) {}
}
```

This approach provides:
- ✅ **Type-safe field access** - `document.metadata.path() == "metadata"`
- ✅ **Proper nested detection** - Automatic nested=true for `FieldType.Nested`
- ✅ **Path traversal support** - Full dotted notation compatibility
- ✅ **No unnecessary classes** - Avoids generating Q-classes for complex generics
- ✅ **Clean object model** - Simple ObjectField instances with proper inheritance

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

### KSP Processor Options

Configure the annotation processor through KSP arguments in `build.gradle.kts`:

```kotlin
ksp {
    // Package and class name customization
    arg("metamodels.main.package", "com.example.search.metamodels")
    arg("metamodels.main.className", "SearchMetamodels")
    arg("metamodels.test.package", "com.example.test.metamodels")
    arg("metamodels.test.className", "TestMetamodels")

    // Global fallbacks
    arg("metamodels.package", "com.example.metamodels")
    arg("metamodels.className", "GlobalMetamodels")

    // Feature toggles
    arg("metalastic.generateJavaCompatibility", "true")  // default: true

    // Debug reporting (generates detailed build reports)
    arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `metamodels.{sourceSet}.package` | Custom package for specific source set | `com.metalastic.metamodels.{sourceSet}` |
| `metamodels.{sourceSet}.className` | Custom class name for specific source set | `Metamodels` |
| `metamodels.package` | Global fallback package | `com.metalastic` |
| `metamodels.className` | Global fallback class name | `Metamodels` |
| `metalastic.generateJavaCompatibility` | Add `@JvmField` for Java interop | `true` |
| `metalastic.generatePrivateClassMetamodels` | Generate metamodels for private `@Document` classes | `false` |
| `metalastic.reportingPath` | Path for debug reports (relative to project root) | disabled |

### Private Class Handling

By default, Metalastic **excludes private classes** from metamodel generation to keep generated code clean and focused on publicly accessible documents. When other classes reference private classes in their fields, the types are automatically replaced with `UnExposablePrivateClass` for type safety.

```kotlin
// Private classes are excluded by default (recommended)
@Document(indexName = "internal_audit")
private class InternalAuditDocument { /* ... */ }
// → No QInternalAuditDocument generated

// But references to private classes are handled safely
@Document(indexName = "public_report")
class PublicReport {
    @Field(type = FieldType.Object)
    val auditData: InternalAuditDocument // Private class reference
}
// → Generates: ObjectField<UnExposablePrivateClass>
```

To **include private classes** in generation (not recommended for most use cases):

```kotlin
ksp {
    arg("metalastic.generatePrivateClassMetamodels", "true")
}
```

### Debug Reporting

Enable detailed processor reporting to understand code generation:

```kotlin
ksp {
    arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
```

This generates a comprehensive markdown report with:
- **Performance metrics** for each processing phase
- **Detailed logs** of document and field processing
- **Generation statistics** (number of classes, files written)
- **Error diagnostics** for troubleshooting
- **Table of contents** for easy navigation between compilation runs

Example report sections:
```markdown
# Metalastic Processor Reports

## 📋 Table of Contents
- [Report 1 - 2025-09-18 15:06:17](#report-1---2025-09-18-150617) - Duration: 254ms
- [Report 2 - 2025-09-18 15:06:18](#report-2---2025-09-18-150618) - Duration: 58ms

## Performance Metrics
| Phase | Duration (ms) |
|-------|---------------|
| COLLECTING | 196 |
| BUILDING | 25 |
| WRITING | 32 |
| **TOTAL** | **253** |

## Detailed Log
[15:06:17.138] 🔍 DEBUG: Processing property 'name' with @Field(type = Text)
[15:06:17.379] 🔍 DEBUG: Discovered 12 models to process
[15:06:17.438] 🔍 DEBUG: Generated Metamodels with 12 Q-class entries
```

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
| `object` | Generated Q-class or Terminal ObjectField | Custom objects |
| `nested` | Generated Q-class (nested=true) or Terminal ObjectField | Collections |
| `geo_point` | `GeoPointField` | `GeoPoint` |
| `ip` | `IpField<String>` | `String` |
| *...and many more* | *Auto-detected* | *Type-safe* |

**Object and Nested Field Generation**:
- **Q-class generation**: Used for simple objects, custom classes, and @Document references
- **Terminal ObjectField**: Used for complex generic types (e.g., `Map<String, List<String>>`) that don't warrant their own Q-classes

### Version Compatibility

The processor uses **runtime field type detection**, automatically supporting:
- ✅ **All Spring Data ES versions** (5.0.x through latest)
- ✅ **Forward compatibility** - New field types automatically supported
- ✅ **Graceful degradation** - Unknown types logged but don't break compilation
- ✅ **Zero configuration** - Uses your project's Spring Data ES version

## Development

### Project Structure

```
Metalastic/
├── modules/
│   ├── core/                    # DSL runtime library
│   │   ├── src/main/kotlin/     # Field classes, Index base classes
│   │   └── src/test/kotlin/     # Unit tests for DSL
│   ├── processor/               # KSP annotation processor
│   │   ├── src/main/kotlin/     # Code generation logic
│   │   └── src/test/kotlin/     # Processor unit tests
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

Metalastic is published to GitLab Maven Registry:

- **Repository**: https://gitlab.ekino.com/iperia/metalastic
- **Package Registry**: https://gitlab.ekino.com/iperia/metalastic/-/packages
- **Group ID**: `com.metalastic`
- **Artifacts**: `core`, `processor`, `test`

CI/CD automatically publishes on master branch pushes.

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

### 🚧 In Progress
- Enhanced query building DSL
- Performance optimizations
- Comprehensive documentation and examples

### 📋 Future Plans
- Direct Elasticsearch client integration
- Query execution utilities
- IDE plugin for enhanced development experience
- Performance benchmarking and optimization

---

**Ready to get started?** Check out the [Quick Start](#quick-start) section or dive into the [examples](#examples) to see Metalastic in action! 🚀