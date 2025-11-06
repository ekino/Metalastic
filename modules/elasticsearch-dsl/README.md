# Elasticsearch DSL Module

This module provides a fluent DSL for building Elasticsearch queries with type-safe field access through Metalastic's generated metamodels.

## Features

- **Type-safe queries**: Full compile-time safety with Metalastic Field system
- **Clean syntax**: Receiver functions provide natural Elasticsearch-like DSL
- **Spring Data ES integration**: Uses Spring Data Elasticsearch 5.0.12 for compatibility
- **Null handling**: Automatically filters out null and blank values
- **Comprehensive query support**: Match, term, range, nested, and many other query types

## Usage

```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import com.metalastic.elasticsearch.dsl.*

// Using generated metamodel
val builder = BoolQuery.Builder()

builder.queryMust {
  metamodel.name match "John"
  metamodel.country term "France"
  metamodel.active.exist()
}

builder.queryMustNot {
  metamodel.status term "inactive"
}

val query = builder.build()
```

## Supported Query Types

### Basic Queries

- `match` - Match query for full-text search
- `term` - Term query for exact matches
- `matchPhrase` - Match phrase query
- `exist` - Exists query

### Utility Queries

- `matchAllQuery()` - Match all documents
- `matchNoneQuery()` - Match no documents
- `idsQuery(ids)` - Query by document IDs

### Advanced Queries

- `nested` - Nested object queries (see [Nested Queries](#nested-queries) section)
- `fuzzyQuery` - Fuzzy matching
- `prefixQuery` - Prefix matching
- `wildCardQuery` - Wildcard matching
- `regexpQuery` - Regular expression queries

## Nested Queries

The `.nested()` DSL function provides type-safe nested query support with runtime validation and helpful warnings.

### Basic Usage

```kotlin
import com.metalastic.elasticsearch.dsl.*

// Nested query on a field marked with @Field(type = FieldType.Nested)
builder.queryMust {
  document.addresses.nested {
    must {
      document.addresses.city match "Paris"
      document.addresses.country term "France"
    }
  }
}
```

### Type Safety

The `.nested()` function is restricted to `Container` types (ObjectField, Document, MultiField), preventing nonsensical usage on leaf fields:

```kotlin
// ✅ Allowed - addresses is a Container
document.addresses.nested { ... }

// ❌ Compile error - name is a TextField
document.name.nested { ... }
```

### Runtime Validation

The DSL automatically checks if a field is actually marked as nested in your Elasticsearch mapping:

**Case 1: Field IS nested** (`@Field(type = FieldType.Nested)`)

- Query is wrapped in `NestedQuery` with correct path
- Works as expected

**Case 2: Field is NOT nested** (regular `@Field(type = FieldType.Object)`)

- Query falls back to regular bool query (graceful degradation)
- **Warning is logged** to help catch mapping issues:

```
WARN - Nested query used on non-nested field 'addresses'.
The field should be marked with @Field(type = FieldType.Nested) in the
Elasticsearch mapping. The query will be applied as a regular bool query instead.
```

### Benefits

- **Compile-time safety**: Only Container types can use `.nested()`
- **Runtime flexibility**: Same class works in both nested and non-nested contexts
- **Developer guidance**: Warning logs help catch mapping configuration errors early
- **Graceful degradation**: Queries execute correctly even if mapping is misconfigured
- **No breaking changes**: Existing code continues to work

### Logging Configuration

The DSL uses `kotlin-logging` for warnings. Configure your logging framework (Logback, Log4j2, etc.) to control log output:

**Logback example** (`logback.xml`):

```xml
<configuration>
  <logger name="com.metalastic.elasticsearch.dsl" level="WARN"/>
</configuration>
```

**Spring Boot example** (`application.yml`):

```yaml
logging:
  level:
    com.metalastic.elasticsearch.dsl: WARN
```

To suppress nested query warnings in specific contexts, you can adjust the log level to ERROR or disable the logger entirely.

## Version Compatibility

This DSL module targets **Spring Data Elasticsearch 5.0.12** for compatibility with Spring Boot 2.7.x projects.

Future versions will target newer Spring Data ES releases:

- `elasticsearch-dsl:2.0.0` → Spring Data ES 5.5.x (Spring Boot 3.x)
