# Understanding Metamodels

Metalastic automatically generates type-safe metamodel classes from your Spring Data Elasticsearch `@Document` annotated classes. These metamodels provide compile-time safety and IDE auto-completion for building Elasticsearch queries.

## What are Metamodels?

A metamodel is a generated Kotlin class that mirrors your Elasticsearch document structure. It provides type-safe accessors for all fields, enabling you to reference document fields without using string literals.

### Example

Given this Spring Data Elasticsearch document:

```java
@Document(indexName = "products")
public class Product {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Double)
    private Double price;
}
```

Metalastic generates:

```kotlin
class MetaProduct<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<Product>(),
) : Document<T>(parent, name, nested, fieldType) {

    @JvmField
    val id: KeywordField<String> = keyword("id")

    @JvmField
    val title: TextField<String> = text("title")

    @JvmField
    val price: DoubleField<Double> = double("price")

    override fun indexName(): String = "products"

    companion object {
        @JvmField
        val product: MetaProduct<Product> = MetaProduct()
    }
}
```

## How Generation Works

Metalastic uses KSP (Kotlin Symbol Processing) to generate metamodels during compilation. The processor runs in three phases:

### 1. COLLECTING Phase
- Discovers all `@Document` annotated classes
- Builds a dependency graph for nested types
- Resolves circular references
- Collects field metadata from `@Field` annotations

### 2. BUILDING Phase
- Generates KotlinPoet `TypeSpec` objects for each metamodel
- Builds the centralized `Metamodels` registry
- Applies type transformations
- Optimizes imports and resolves conflicts

### 3. WRITING Phase
- Writes generated files to `build/generated/ksp/{sourceSet}/kotlin/`
- Generates optional debug reports
- Tracks performance metrics

## The Type System

### Generic Type Parameters

All metamodel classes use generic type parameters with runtime type tracking:

```kotlin
class MetaProduct<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<Product>(),
) : Document<T>(parent, name, nested, fieldType)
```

The `T : Any?` parameter allows the metamodel to be reused in different contexts while maintaining type safety.

### Runtime Type Tracking

Every field includes runtime type information via Kotlin's `KType`:

```kotlin
@JvmField
val title: TextField<String> = text("title")  // fieldType = typeOf<String>()
```

This enables:
- Advanced type-safe query building
- Runtime type validation
- Custom type converters
- Generic query construction

### Field Type Hierarchy

All field types extend from a common base:

```
Metamodel<T>
├── Container<T>
    ├── ObjectField<T>
    │   ├── Document<T>           (your @Document classes)
    │   └── Inner classes         (nested objects)
    └── Field<T, Self>
        ├── TextField<T>
        ├── KeywordField<T>
        ├── IntegerField<T>
        ├── LongField<T>
        ├── DoubleField<T>
        ├── DateField<T>
        └── ...
```

## Path Traversal

Metamodels automatically build Elasticsearch field paths through parent hierarchy traversal:

```kotlin
import com.example.MetaProduct.Companion.product

// Simple field
product.title.path() // "title"

// Nested object
product.category.name.path() // "category.name"

// Nested field (with nested = true)
product.tags.value.path() // "tags.value"
product.tags.value.isNestedPath() // true
product.tags.value.nestedPaths().toList() // ["tags"]
```

## Metamodels Registry

Metalastic generates a centralized registry for all metamodels:

```kotlin
object Metamodels {
    @JvmStatic
    fun entries(): Sequence<Document<*>> = sequenceOf(
        product,
        category,
        // ... all generated metamodels
    )
}
```

This allows you to:
- Iterate over all metamodels
- Build generic utilities
- Implement dynamic query builders
- Create testing helpers

## Java Compatibility

All generated metamodels include `@JvmField` annotations for seamless Java interop:

```java
// Java usage
import static com.example.MetaProduct.product;

String titlePath = product.title.path(); // "title"
```

## Next Steps

- [Field Types Reference](/guide/field-types-reference) - Learn about all supported field types
- [Query DSL Guide](/guide/query-dsl-guide) - Build type-safe queries with generated metamodels
- [Configuration](/guide/configuration) - Customize metamodel generation
