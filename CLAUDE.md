# QElasticsearch

A QueryDSL-like library for Elasticsearch in Kotlin, inspired by http://querydsl.com/

## Project Overview

This is a multi-module project that provides a type-safe, fluent query builder for Elasticsearch similar to QueryDSL's approach for SQL databases. The library includes an annotation processor that generates QIndex classes for all classes annotated with Spring Data Elasticsearch's @Document annotation.

**End Goal**: Create a reusable library that can be integrated into other Java/Kotlin projects to automatically generate type-safe query builders from existing Spring Data Elasticsearch document classes.

## Multi-Module Structure

- **core**: Core DSL runtime library containing field definitions, index abstractions, and the DSL API
- **processor**: Annotation processor that generates QIndex classes from @Document annotated classes
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

The annotation processor will generate DSL objects that follow this pattern:

```kotlin
// For nested/object fields
class QAddress(parent: ObjectField?, path: String, nested: Boolean = false) : ObjectField(parent, path, nested) {
    val city: KeywordField<String> = keywordField<String>("city")
    val street: TextField<String> = textField<String>("street")
    val zipCode: KeywordField<String> = keywordField<String>("zipCode")
}

// For document indexes
object QAllTypesDocument : Index("alltypes") {
    val longField: LongField<Long> = longField<Long>("longField")
    val floatField: FloatField<Float> = floatField<Float>("floatField")
    val doubleField: DoubleField<Double> = doubleField<Double>("doubleField")
    val booleanField: BooleanField<Boolean> = booleanField<Boolean>("booleanField")
    val dateField: DateField<String> = dateField<String>("dateField")
    val byteField: ByteField<Byte> = byteField<Byte>("byteField")
    val shortField: ShortField<Short> = shortField<Short>("shortField")
    val halfFloatField: HalfFloatField = halfFloatField("halfFloatField")
    val scaledFloatField: ScaledFloatField = scaledFloatField("scaledFloatField")
    val dateNanosField: DateNanosField = dateNanosField("dateNanosField")
    val ipField: IpField = ipField("ipField")
    val geoPointField: GeoPointField = geoPointField("geoPointField")
    val geoShapeField: GeoShapeField = geoShapeField("geoShapeField")
    val completionField: CompletionField = completionField("completionField")
    val tokenCountField: TokenCountField = tokenCountField("tokenCountField")
    val percolatorField: PercolatorField = percolatorField("percolatorField")
    val rankFeatureField: RankFeatureField = rankFeatureField("rankFeatureField")
    val rankFeaturesField: RankFeaturesField = rankFeaturesField("rankFeaturesField")
    val flattenedField: FlattenedField = flattenedField("flattenedField")
    val shapeField: ShapeField = shapeField("shapeField")
    val pointField: PointField = pointField("pointField")
    val constantKeywordField: ConstantKeywordField = constantKeywordField("constantKeywordField")
    val wildcardField: WildcardField = wildcardField("wildcardField")
    val integerRangeField: IntegerRangeField = integerRangeField("integerRangeField")
    val floatRangeField: FloatRangeField = floatRangeField("floatRangeField")
    val longRangeField: LongRangeField = longRangeField("longRangeField")
    val doubleRangeField: DoubleRangeField = doubleRangeField("doubleRangeField")
    val dateRangeField: DateRangeField = dateRangeField("dateRangeField")
    val ipRangeField: IpRangeField = ipRangeField("ipRangeField")
    val address: QAddress = QAddress(this, "address", false)
    val activities: QActivity = QActivity(this, "activities", true)
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
object QIndexModulePath : Index("module_path_iperia") {
    val id: KeywordField<String> = keywordField<String>("id")
    val longCode: QLongCodeMultiField = QLongCodeMultiField(this, "longCode")
    val trainingAgency: QIndexTrainingAgency = QIndexTrainingAgency(this, "trainingAgency", false)
    val addresses: QIndexAddress = QIndexAddress(this, "addresses", true)
    val lastUpdatedDate: DateField<String> = dateField<String>("lastUpdatedDate")
    val trainingAgencyAssociation: BooleanField<Boolean> = booleanField<Boolean>("trainingAgencyAssociation")
    // ... other fields
}

class QLongCodeMultiField(parent: ObjectField, path: String) : MultiField<LongField<Long>>(parent, LongField(parent, path)) {
    val search: TextField<String> = textField<String>("search")
}

class QIndexTrainingAgency(parent: ObjectField?, path: String, nested: Boolean = false) : ObjectField(parent, path, nested) {
    val id: KeywordField<String> = keywordField<String>("id")
    val legalPerson: QIndexLegalPerson = QIndexLegalPerson(this, "legalPerson", false)
}

class QIndexAddress(parent: ObjectField?, path: String, nested: Boolean = false) : ObjectField(parent, path, nested) {
    val id: KeywordField<String> = keywordField<String>("id")
    val city: TextField<String> = textField<String>("city")
    val departmentCode: KeywordField<String> = keywordField<String>("departmentCode")
    val region: KeywordField<String> = keywordField<String>("region")
    val location: GeoPointField = geoPointField("location")
}
```

## Path Traversal Requirements

The generated DSL should support dotted notation path traversal to obtain the full field path:

```kotlin
val document = QIndexModulePath

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
```

This allows for:
- Root-level field path access
- Nested object field path construction
- Full dotted notation support for complex nested structures
- Type-safe field path building for query construction
```

## Publishing

This project is published to GitLab Maven Registry:
- **Repository**: https://gitlab.ekino.com/iperia/qelasticsearch
- **CI/CD Pipeline**: Automatic publishing on master branch pushes
- **Package Registry**: https://gitlab.ekino.com/iperia/qelasticsearch/-/packages
- **Group ID**: `com.qelasticsearch`
- **Artifacts**: `qelasticsearch-dsl`, `qelasticsearch-processor`, `qelasticsearch-test`

### For Consumers
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
        // No credentials needed for project members
    }
}

dependencies {
    implementation("com.qelasticsearch:core:1.0-SNAPSHOT")
    ksp("com.qelasticsearch:processor:1.0-SNAPSHOT")
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