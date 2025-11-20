# Field Types

Metalastic supports all Spring Data Elasticsearch field types with type-safe accessors.

## Simple Field Types

All simple field types are generated with their corresponding type parameters:

### Text and Keyword

```kotlin
@Field(type = FieldType.Text)
private String title;

// Generated:
val title: TextField<String> = text<String>("title")

@Field(type = FieldType.Keyword)
private String status;

// Generated:
val status: KeywordField<String> = keyword<String>("status")
```

### Numeric Types

```kotlin
@Field(type = FieldType.Integer)
private Integer count;

// Generated:
val count: IntegerField<Int> = integer<Int>("count")

@Field(type = FieldType.Long)
private Long id;

// Generated:
val id: LongField<Long> = long<Long>("id")

@Field(type = FieldType.Double)
private Double price;

// Generated:
val price: DoubleField<Double> = double<Double>("price")

@Field(type = FieldType.Float)
private Float rating;

// Generated:
val rating: FloatField<Float> = float<Float>("rating")
```

### Boolean and Date

```kotlin
@Field(type = FieldType.Boolean)
private Boolean active;

// Generated:
val active: BooleanField<Boolean> = boolean<Boolean>("active")

@Field(type = FieldType.Date)
private Date createdAt;

// Generated:
val createdAt: DateField<Date> = date<Date>("createdAt")
```

## Object and Nested Fields

### Object Fields

Non-nested object types reference their metamodel:

```kotlin
@Field(type = FieldType.Object)
private Address address;

// Generated:
val address: MetaAddress<Address> =
    MetaAddress(this, "address", false, typeOf<Address>())
```

### Nested Fields

Nested objects are marked with `nested = true`:

```kotlin
@Field(type = FieldType.Nested)
private List<Tag> tags;

// Generated:
val tags: MetaTag<Tag> =
    MetaTag(this, "tags", true, typeOf<Tag>())
```

## MultiField Support

Metalastic supports `@MultiField` annotations with inner fields:

```kotlin
@MultiField(
    mainField = @Field(type = FieldType.Long),
    otherFields = {
        @InnerField(suffix = "search", type = FieldType.Text),
        @InnerField(suffix = "keyword", type = FieldType.Keyword)
    }
)
private Long code;
```

Generated code:

```kotlin
// Main field
val code: LongField<Long> = long<Long>("code")

// Inner fields in nested object
object CodeMultiField {
    val search: TextField<String> = text<String>("code.search")
    val keyword: KeywordField<String> = keyword<String>("code.keyword")
}
```

## Path Traversal

All field types support path traversal:

```kotlin
import com.example.MetaProduct.Companion.product

// Simple fields
product.title.path() // "title"

// Nested objects
product.category.name.path() // "category.name"

// Nested field detection
product.tags.name.isNestedPath() // true
product.tags.name.nestedPaths().toList() // ["tags"]
```

## Terminal Object Types

### SelfReferencingObject

For circular references (e.g., Category → parent: Category):

```kotlin
val parent: SelfReferencingObject<MetaCategory<*>> =
    SelfReferencingObject(this, "parent", false, typeOf<Category>())
```

### UnModellableObject

For types that cannot be fully modeled (e.g., `Map<String, Any>`):

```kotlin
val metadata: UnModellableObject =
    UnModellableObject(this, "metadata", false, typeOf<Map<String, Any>>())
```

## Runtime Type Tracking

All fields include runtime type information via `KType`:

```kotlin
val field = product.title
field.fieldType // returns typeOf<String>()
```

This enables advanced type-safe query building and validation.
