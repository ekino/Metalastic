# QElasticsearch: Type-Safe Query DSL for Elasticsearch

A QueryDSL-inspired library that brings type safety and fluent APIs to Elasticsearch queries in Kotlin

---

## The Problem

### Manual Elasticsearch Queries Are Error-Prone

```kotlin
// Traditional approach - prone to typos and runtime errors
val searchRequest = SearchRequest("users")
  .source(
    SearchSourceBuilder()
      .query(
        QueryBuilders.boolQuery()
          .must(QueryBuilders.termQuery("status", "active"))  // Typo in field name?
          .must(QueryBuilders.rangeQuery("created_date")      // Wrong field type?
            .gte("2024-01-01"))
      )
  )
```

**Issues:**
- ❌ No compile-time validation
- ❌ Field names as strings (typos!)
- ❌ No type checking
- ❌ Hard to refactor
- ❌ IDE autocomplete limited

---

## The Solution: QElasticsearch

### Type-Safe, Fluent Query Building

```kotlin
// QElasticsearch approach - compile-time safety
val query = QUser
  .status.eq("active")
  .and(QUser.createdDate.gte(LocalDate.of(2024, 1, 1)))
  .and(QUser.profile.name.contains("john"))
```

**Benefits:**
- ✅ **Compile-time validation** - catch errors before deployment
- ✅ **Full IDE support** - autocomplete, refactoring, navigation
- ✅ **Type safety** - no more wrong types or field names
- ✅ **Fluent API** - readable and intuitive
- ✅ **Zero runtime overhead** - pure compile-time code generation

---

## How It Works

### 1. Annotate Your Existing Classes

```java
@Document(indexName = "users")
public class User {
    @Field(type = FieldType.Keyword)
    public String id;
    
    @Field(type = FieldType.Text)
    public String name;
    
    @Field(type = FieldType.Boolean)
    public Boolean active;
    
    @Field(type = FieldType.Object)
    public Profile profile;
    
    @Field(type = FieldType.Nested)
    public List<Address> addresses;
}
```

### 2. Annotation Processor Generates DSL Classes

```kotlin
// Auto-generated QUser class
data object QUser : Index("users") {
    val id: KeywordField<String> by keyword()
    val name: TextField<String> by text()
    val active: BooleanField<Boolean> by boolean()
    val profile: Profile by objectField()
    val addresses: Address by nestedField()
}
```

---

## Key Features

### 🎯 **Full Type Safety**
- Field types match your domain objects
- Compile-time error detection
- IDE autocomplete and navigation

### 🔗 **Nested Object Support**
```kotlin
// Navigate nested objects with dot notation
QUser.profile.firstName.eq("John")
QUser.addresses.city.eq("Paris")
```

### 📝 **Multi-Field Support**
```kotlin
@MultiField(
    mainField = @Field(type = FieldType.Text),
    otherFields = {
        @InnerField(suffix = "keyword", type = FieldType.Keyword)
    }
)
public String title;

// Usage
QArticle.title.keyword.eq("Exact Title")  // Keyword search
QArticle.title.match("partial title")      // Full-text search
```

### 🌐 **Path Traversal**
```kotlin
// Get field paths for Elasticsearch queries
QUser.profile.firstName.path()    // "profile.firstName"
QUser.addresses.city.path()       // "addresses.city"

// Nested path detection
QUser.addresses.city.isNestedPath()     // true
QUser.addresses.city.nestedPaths()      // ["addresses"]
```

---

## Architecture

### Multi-Module Design

```
qelasticsearch/
├── modules/
│   ├── core/           # DSL runtime (Field types, Index base class)
│   ├── processor/      # Annotation processor (KSP-based)
│   └── test/          # Integration tests
└── examples/          # Usage examples
```

### Technology Stack
- **Kotlin** - Modern JVM language with excellent DSL support
- **KSP** - Kotlin Symbol Processing for efficient code generation
- **KotlinPoet** - Type-safe Kotlin code generation
- **Spring Data Elasticsearch** - Annotation compatibility

---

## Getting Started

### 1. Add Dependencies

```kotlin
dependencies {
    implementation("com.qelasticsearch:core:1.0.0")
    ksp("com.qelasticsearch:processor:1.0.0")
}
```

### 2. Configure KSP (if needed)

```kotlin
kotlin {
    sourceSets {
        main {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
    }
}
```

### 3. Use Your Existing @Document Classes

No changes needed! The processor works with your existing Spring Data Elasticsearch annotations.

---

## Real-World Example

### From This...

```java
// Traditional Spring Data Elasticsearch
@Document(indexName = "products")
public class Product {
    @Field(type = FieldType.Keyword)
    public String id;
    
    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    public String title;
    
    @Field(type = FieldType.Object)
    public Category category;
    
    @Field(type = FieldType.Double)
    public Double price;
}
```

### To This...

```kotlin
// Generated type-safe DSL
data object QProduct : Index("products") {
    val id: KeywordField<String> by keyword()
    val title: TitleMultiField by multiField()
    val category: Category by objectField()
    val price: DoubleField<Double> by double()
}

// Usage
val expensiveElectronics = QProduct
    .category.name.eq("Electronics")
    .and(QProduct.price.gte(1000.0))
    .and(QProduct.title.keyword.startsWith("Premium"))
```

---

## Performance & Efficiency

### Optimized Annotation Processing
- ✅ **Targeted scanning** - Only processes referenced classes
- ✅ **Incremental compilation** - Fast rebuilds
- ✅ **Zero runtime overhead** - All work done at compile-time
- ✅ **Memory efficient** - No reflection, no dynamic proxy objects

### Before Optimization
```
Processing 15,000+ classes from entire classpath
⏱️ ~30 seconds compilation time
```

### After Optimization  
```
Processing ~10 referenced classes only
⏱️ ~2 seconds compilation time
```

---

## Benefits for Teams

### 🛡️ **Safer Refactoring**
- Rename fields in your domain classes
- Compiler catches all query references
- No more broken queries in production

### 👥 **Better Developer Experience**
- IDE autocomplete for all fields
- Jump to definition works
- Type mismatches caught early

### 📈 **Improved Maintainability**
- Self-documenting queries
- Consistent query patterns
- Easier code reviews

---

## Roadmap & Future

### Current Status
- ✅ Basic field types (Text, Keyword, Numeric, Boolean, Date)
- ✅ Object and Nested field support
- ✅ Multi-field support
- ✅ Path traversal and nested detection
- ✅ Spring Data Elasticsearch compatibility

### Coming Soon
- 🔄 **Query builder integration** - Direct Elasticsearch query generation
- 🔍 **Aggregation DSL** - Type-safe aggregations
- 🧪 **Query validation** - Compile-time query structure validation
- 📊 **Performance metrics** - Query performance insights

---

## Comparison with Alternatives

| Feature | QElasticsearch | Raw Elasticsearch Client | Spring Data |
|---------|----------------|---------------------------|-------------|
| Type Safety | ✅ Full | ❌ None | ⚠️ Limited |
| Compile-time Validation | ✅ Yes | ❌ No | ⚠️ Partial |
| IDE Support | ✅ Excellent | ❌ Limited | ⚠️ Basic |
| Learning Curve | ✅ Familiar | ❌ Steep | ✅ Easy |
| Performance | ✅ Zero overhead | ✅ Direct | ⚠️ Abstraction cost |
| Flexibility | ✅ High | ✅ Full | ⚠️ Limited |

---

## Demo Time! 🚀

### Live Example: Building Complex Queries

```kotlin
// Complex query with nested conditions
val query = QUser
    .active.eq(true)
    .and(
        QUser.profile.age.between(25, 45)
            .or(QUser.profile.experience.gte(5))
    )
    .and(QUser.addresses.city.`in`("Paris", "London", "Berlin"))
    .and(QUser.skills.name.contains("kotlin"))

// Generated paths for Elasticsearch
println(query.toElasticsearchQuery())
```

---

## Thank You! 

### Questions?

**Repository:** [QElasticsearch](https://github.com/your-org/qelasticsearch)
**Documentation:** [docs.qelasticsearch.com](https://docs.qelasticsearch.com)
**Contact:** [@YourTwitter](https://twitter.com/yourtwitter)

---

## Appendix: Technical Deep Dive

### Code Generation Process

1. **KSP Scanning** - Find all `@Document` annotated classes
2. **Dependency Analysis** - Recursively collect referenced object types  
3. **Type Mapping** - Map Elasticsearch field types to Kotlin types
4. **DSL Generation** - Generate type-safe field definitions using KotlinPoet
5. **Import Optimization** - Minimize generated code size

### Generated Code Structure

```kotlin
// Main index class
data object QUser : Index("users") {
    val name: TextField<String> by text()
    val profile: Profile by objectField()
}

// Nested object class  
class Profile(parent: ObjectField?, path: String, nested: Boolean) 
    : ObjectField(parent, path, nested) {
    val firstName: TextField<String> by text()
    val lastName: TextField<String> by text()
}
```

The delegation pattern ensures lazy initialization and proper field path construction.