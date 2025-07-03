# QElasticsearch

A QueryDSL-like library for Elasticsearch in Kotlin, inspired by http://querydsl.com/

## Project Overview

This project is a library that provides a type-safe, fluent query builder for Elasticsearch similar to QueryDSL's approach for SQL databases. The library includes an annotation processor that generates QIndex classes for all classes annotated with Spring Data Elasticsearch's @Document annotation.

**End Goal**: Create a reusable library that can be integrated into other Java/Kotlin projects to automatically generate type-safe query builders from existing Spring Data Elasticsearch document classes.

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
- **Dependencies**: Spring Data Elasticsearch 5.2.5
- **Annotation Processing**: KAPT/KSP for processing @Document annotations
- **Testing**: JUnit 5, AssertK, MockK

## Development Notes

- Project structure follows standard Kotlin/Gradle conventions
- Main source code in `src/main/kotlin/`
- Tests in `src/test/kotlin/`
- Annotation processor will scan for `@Document` annotations from Spring Data Elasticsearch
- Generated QIndex classes will follow Q* naming convention (similar to QueryDSL)
- Library will be published for consumption by other projects
- Annotation processor runs at compile-time in consumer projects
- Support for all Spring Data Elasticsearch FieldType values (Text, Keyword, Date, Long, Integer, Short, Byte, Double, Float, Boolean, Binary, Object, Nested, Ip, etc.)
- Generate type-safe predicates for common Elasticsearch query types (term, match, range, etc.)

## Code Style Guidelines

- **No star imports**: Use explicit imports only
- **Code Quality**: Use ktlint for formatting and detekt for static analysis
- **Linting**: All code must pass ktlint and detekt checks

## Generated DSL Structure

The annotation processor will generate DSL objects that follow this pattern:

```kotlin
// For nested/object fields
object Address : ObjectFields() {
    val city by keyword()
    val street by text()
    val zipCode by keyword()
}

// For document indexes
object AllTypesIndex : Index("alltypes") {
    val longField by long()
    val floatField by float()
    val doubleField by double()
    val booleanField by boolean()
    val dateField by date()
    val byteField by byte()
    val shortField by short()
    val halfFloatField by halfFloat()
    val scaledFloatField by scaledFloat()
    val dateNanosField by dateNanos()
    val ipField by ip()
    val geoPointField by geoPoint()
    val geoShapeField by geoShape()
    val completionField by completion()
    val tokenCountField by tokenCount()
    val percolatorField by percolator()
    val rankFeatureField by rankFeature()
    val rankFeaturesField by rankFeatures()
    val flattenedField by flattened()
    val shapeField by shape()
    val pointField by point()
    val constantKeywordField by constantKeyword()
    val wildcardField by wildcard()
    val integerRangeField by integerRange()
    val floatRangeField by floatRange()
    val longRangeField by longRange()
    val doubleRangeField by doubleRange()
    val dateRangeField by dateRange()
    val ipRangeField by ipRange()
    val address by objectField(Address, nested = true)
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
        @InnerField(suffix = FieldsConstants.SEARCH_SUFFIX_FIELD, type = FieldType.Text)
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
object IndexModulePathFields : Index("module_path_iperia") {
    val id by keyword()
    val longCode by multiField(long()) {
        field("search", text())
    }
    val trainingAgency by objectField(IndexTrainingAgencyFields)
    val addresses by nestedField(IndexAddressFields)
    val lastUpdatedDate by date(DateFormat.date_time_no_millis)
    val trainingAgencyAssociation by boolean()
    // ... other fields
}

object IndexTrainingAgencyFields : ObjectFields() {
    val id by keyword()
    val legalPerson by objectField(IndexLegalPersonFields)
}

object IndexAddressFields : ObjectFields() {
    val id by keyword()
    val city by text()
    val departmentCode by keyword()
    val region by keyword()
    val location by geoPoint()
}
```

## Path Traversal Requirements

The generated DSL should support dotted notation path traversal to obtain the full field path:

```kotlin
val person: Index

// Path traversal examples
assertThat(person.path).isEqualTo("")
assertThat(person.name.path).isEqualTo("name")
assertThat(person.age.path).isEqualTo("age")
assertThat(person.bio.path).isEqualTo("bio")
assertThat(person.address.city.path).isEqualTo("address.city")
assertThat(person.address.country.path).isEqualTo("address.country")
```

This allows for:
- Root-level field path access
- Nested object field path construction
- Full dotted notation support for complex nested structures
- Type-safe field path building for query construction