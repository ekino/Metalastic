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

- `nestedQuery` - Nested object queries
- `fuzzyQuery` - Fuzzy matching
- `prefixQuery` - Prefix matching
- `wildCardQuery` - Wildcard matching
- `regexpQuery` - Regular expression queries

## Version Compatibility

This DSL module targets **Spring Data Elasticsearch 5.0.12** for compatibility with Spring Boot 2.7.x projects.

Future versions will target newer Spring Data ES releases:

- `elasticsearch-dsl:2.0.0` → Spring Data ES 5.5.x (Spring Boot 3.x)
