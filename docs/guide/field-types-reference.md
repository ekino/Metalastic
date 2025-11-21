# Field Types Reference

Metalastic supports all Spring Data Elasticsearch field types with type-safe accessors. Each `@Field` annotation is mapped to a corresponding Metalastic field class with full generic type support.

## Field Type Mapping

| Spring Data ES FieldType | Elasticsearch Type | Metalastic Field Class | Kotlin Type |
|--------------------------|-------------------|------------------------|-------------|
| `FieldType.Text` | text | `TextField<String>` | `String` |
| `FieldType.Keyword` | keyword | `KeywordField<String>` | `String` |
| `FieldType.Integer` | integer | `IntegerField<Int>` | `Int` |
| `FieldType.Long` | long | `LongField<Long>` | `Long` |
| `FieldType.Double` | double | `DoubleField<Double>` | `Double` |
| `FieldType.Float` | float | `FloatField<Float>` | `Float` |
| `FieldType.Short` | short | `ShortField<Short>` | `Short` |
| `FieldType.Byte` | byte | `ByteField<Byte>` | `Byte` |
| `FieldType.Boolean` | boolean | `BooleanField<Boolean>` | `Boolean` |
| `FieldType.Date` | date | `DateField<T>` | `Date`, `Instant`, `LocalDate`, etc. |
| `FieldType.Object` | object | `MetaClassName<T>` | Custom class |
| `FieldType.Nested` | nested | `MetaClassName<T>` (nested=true) | `List<T>` |

## Text Fields

Text fields are analyzed for full-text search, while keyword fields are used for exact matching, aggregations, and sorting.

### TextField

Used for full-text search with analysis:

```java
@Field(type = FieldType.Text)
private String title;
```

Generated:
```kotlin
@JvmField
val title: TextField<String> = text("title")
```

**Elasticsearch mapping:**
- Analyzed for full-text search
- Supports match, match_phrase, match_phrase_prefix queries
- Cannot be used for sorting or aggregations (use keyword instead)

### KeywordField

Used for exact matching, sorting, and aggregations:

```java
@Field(type = FieldType.Keyword)
private String status;
```

Generated:
```kotlin
@JvmField
val status: KeywordField<String> = keyword("status")
```

**Elasticsearch mapping:**
- Not analyzed (exact matching only)
- Efficient for term queries, sorting, and aggregations
- Limited to 256 characters by default (configurable)

## Numeric Fields

Metalastic supports all Elasticsearch numeric types with corresponding Kotlin types.

### Integer and Long

```java
@Field(type = FieldType.Integer)
private Integer count;

@Field(type = FieldType.Long)
private Long id;
```

Generated:
```kotlin
@JvmField
val count: IntegerField<Int> = integer("count")

@JvmField
val id: LongField<Long> = long("id")
```

**Range:**
- `Integer`: -2³¹ to 2³¹-1
- `Long`: -2⁶³ to 2⁶³-1

### Floating Point

```java
@Field(type = FieldType.Double)
private Double price;

@Field(type = FieldType.Float)
private Float rating;
```

Generated:
```kotlin
@JvmField
val price: DoubleField<Double> = double("price")

@JvmField
val rating: FloatField<Float> = float("rating")
```

**Precision:**
- `Double`: 64-bit IEEE 754 floating point
- `Float`: 32-bit IEEE 754 floating point

### Short and Byte

```java
@Field(type = FieldType.Short)
private Short priority;

@Field(type = FieldType.Byte)
private Byte flags;
```

Generated:
```kotlin
@JvmField
val priority: ShortField<Short> = short("priority")

@JvmField
val flags: ByteField<Byte> = byte("flags")
```

## Date and Boolean Fields

### DateField

Supports multiple Java date/time types:

```java
@Field(type = FieldType.Date)
private Date createdAt;

@Field(type = FieldType.Date)
private Instant timestamp;

@Field(type = FieldType.Date)
private LocalDate publishDate;
```

Generated:
```kotlin
@JvmField
val createdAt: DateField<Date> = date("createdAt")

@JvmField
val timestamp: DateField<Instant> = date("timestamp")

@JvmField
val publishDate: DateField<LocalDate> = date("publishDate")
```

**Supported types:**
- `java.util.Date`
- `java.time.Instant`
- `java.time.LocalDate`
- `java.time.LocalDateTime`
- `java.time.ZonedDateTime`

### BooleanField

```java
@Field(type = FieldType.Boolean)
private Boolean active;
```

Generated:
```kotlin
@JvmField
val active: BooleanField<Boolean> = boolean("active")
```

## Structured Fields

### Object Fields

Non-nested object types reference their generated metamodel:

```java
@Field(type = FieldType.Object)
private Address address;
```

Generated:
```kotlin
@JvmField
val address: MetaAddress<Address> =
    MetaAddress(this, "address", false, typeOf<Address>())
```

**Characteristics:**
- Not indexed as nested documents
- Fields are flattened in Elasticsearch
- Cannot query relationships independently
- More efficient than nested fields

**Path traversal:**
```kotlin
product.address.city.path() // "address.city"
product.address.street.path() // "address.street"
```

### Nested Fields

Nested objects maintain independent documents within the parent:

```java
@Field(type = FieldType.Nested)
private List<Comment> comments;
```

Generated:
```kotlin
@JvmField
val comments: MetaComment<List<Comment>> =
    MetaComment(this, "comments", true, typeOf<List<Comment>>())
```

**Characteristics:**
- Indexed as separate hidden documents
- Maintains object relationships
- Requires nested queries
- Higher storage and query cost

**Nested detection:**
```kotlin
product.comments.author.isNestedPath() // true
product.comments.author.nestedPaths().toList() // ["comments"]
```

## Advanced Features

### MultiField Support

MultiField allows indexing the same field in multiple ways:

```java
@MultiField(
    mainField = @Field(type = FieldType.Text),
    otherFields = {
        @InnerField(suffix = "keyword", type = FieldType.Keyword),
        @InnerField(suffix = "search", type = FieldType.Text)
    }
)
private String fulltext;
```

Generated:
```kotlin
@JvmField
val fulltext: FulltextMultiField = FulltextMultiField(this, "fulltext")

inner class FulltextMultiField(
    parent: ObjectField<*>,
    mainFieldName: String,
) : MultiField<String, TextField<String>>(
    parent,
    TextField(parent, mainFieldName, typeOf<String>()),
    typeOf<String>()
) {
    @JvmField
    val keyword: KeywordField<String> = keyword("keyword")

    @JvmField
    val search: TextField<String> = text("search")
}
```

**Usage:**
```kotlin
// Main field (analyzed text)
fulltext.path() // "fulltext"

// Inner fields
fulltext.keyword.path() // "fulltext.keyword" (exact match)
fulltext.search.path() // "fulltext.search" (custom analyzer)

// Access main field directly
fulltext.mainField // TextField<String>
```

**Common use cases:**
- Text field with keyword subfield for sorting/aggregations
- Multiple analyzers for different search strategies
- Numeric field with keyword for exact matching

### Terminal Object Types

#### SelfReferencingObject

Handles circular references (e.g., Category → parent: Category):

```java
@Document(indexName = "categories")
public class Category {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Object)
    private Category parent;  // Circular reference
}
```

Generated:
```kotlin
@JvmField
val parent: SelfReferencingObject<MetaCategory<*>> =
    SelfReferencingObject(this, "parent", false, typeOf<Category>())
```

**Usage:**
```kotlin
category.parent.path() // "parent"
// Further traversal is not possible to prevent infinite recursion
```

#### UnModellableObject

For types that cannot be fully modeled (e.g., `Map<String, Any>`, unresolved types):

```java
@Field(type = FieldType.Object)
private Map<String, Object> metadata;
```

Generated:
```kotlin
@JvmField
val metadata: UnModellableObject =
    UnModellableObject(this, "metadata", false, typeOf<Map<String, Any>>())
```

**Usage:**
```kotlin
metadata.path() // "metadata"
// Field-level access is not available
```

### Runtime Type Tracking

All fields include runtime type information via Kotlin's `KType`:

```kotlin
val field = product.title
field.fieldType() // returns typeOf<String>()

val dateField = product.createdAt
dateField.fieldType() // returns typeOf<Date>()
```

This enables:
- Advanced type-safe query building
- Runtime type validation
- Custom type converters in the Query DSL
- Generic query construction utilities

## See Also

- [Understanding Metamodels](/guide/understanding-metamodels) - Learn how metamodels are generated
- [Query DSL Guide](/guide/query-dsl-guide) - Build type-safe queries with these field types
- [Configuration](/guide/configuration) - Customize metamodel generation
