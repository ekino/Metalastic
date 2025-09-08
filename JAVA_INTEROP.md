# QElasticsearch Java Interoperability Guide

QElasticsearch is designed to work seamlessly in mixed Java/Kotlin projects, especially those using QueryDSL. This guide demonstrates how to use QElasticsearch in Java projects.

## Overview

QElasticsearch generates Q-classes that follow QueryDSL naming conventions and are fully compatible with Java code. The generated classes use proper `@JvmName` annotations for optimal Java interoperability.

## Java Integration

### Example Document Class

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "product")
public class Product {
    
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    
    @Field(type = FieldType.Text)
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Integer)
    private Integer price;
    
    @Field(type = FieldType.Boolean)
    private Boolean isActive;
    
    @Field(type = FieldType.Date)
    private Date createdAt;
    
    @Field(type = FieldType.Object)
    private Category category;
    
    @Field(type = FieldType.Nested)
    private List<Tag> tags;
    
    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard")
        }
    )
    private String searchableTitle;
    
    // Constructors
    public Product() {}
    
    public Product(String id, String name, String description, Integer price, Boolean isActive, 
                   Date createdAt, Category category, List<Tag> tags, String searchableTitle) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.category = category;
        this.tags = tags;
        this.searchableTitle = searchableTitle;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    
    public String getSearchableTitle() { return searchableTitle; }
    public void setSearchableTitle(String searchableTitle) { this.searchableTitle = searchableTitle; }
}
```

### Generated Q-Class Usage from Java

```java
// Generated QProduct class is accessible from Java
var product = QProduct.INSTANCE;

// Access index name
String indexName = product.indexName(); // "product"

// Access fields directly (no getters needed for Java interop)
var idField = product.id;
var nameField = product.name;
var descriptionField = product.description;
var priceField = product.price;
var isActiveField = product.isActive;
var createdAtField = product.createdAt;
var categoryField = product.category;
var tagsField = product.tags;
var searchableTitleField = product.searchableTitle;

// Get field paths for Elasticsearch queries
String idPath = idField.path(); // "id"
String namePath = nameField.path(); // "name"  
String isActivePath = isActiveField.path(); // "isActive"
```

## Build Configuration

### Gradle Configuration (build.gradle)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.google.devtools.ksp' version '1.9.21-1.0.15'
}

dependencies {
    // QElasticsearch
    implementation 'com.qelasticsearch:core:1.0.0'
    ksp 'com.qelasticsearch:processor:1.0.0'
    
    // Spring Data Elasticsearch
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
    
    
    // QueryDSL (if using alongside QElasticsearch)
    implementation 'com.querydsl:querydsl-core:5.0.0'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Maven Configuration (pom.xml)

```xml
<dependencies>
    <!-- QElasticsearch -->
    <dependency>
        <groupId>com.qelasticsearch</groupId>
        <artifactId>core</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Spring Data Elasticsearch -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
    </dependency>
    
</dependencies>

<build>
    <plugins>
        <!-- KSP Plugin -->
        <plugin>
            <groupId>com.google.devtools.ksp</groupId>
            <artifactId>ksp-maven-plugin</artifactId>
            <version>1.9.21-1.0.15</version>
            <configuration>
                <processors>
                    <processor>com.qelasticsearch.processor.QElasticsearchSymbolProcessorProvider</processor>
                </processors>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>com.qelasticsearch</groupId>
                    <artifactId>processor</artifactId>
                    <version>1.0.0</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## QueryDSL Compatibility

QElasticsearch follows QueryDSL conventions and can be used alongside QueryDSL for other data sources:

### Side-by-Side Usage

```java
import static com.yourpackage.QProduct.*; // QElasticsearch
import static com.yourpackage.QCustomer.*; // QueryDSL for JPA

public class SearchService {
    
    public void searchProducts() {
        // Use QElasticsearch for Elasticsearch
        var productIndex = QProduct.INSTANCE;
        var nameField = productIndex.getName();
        var isActiveField = productIndex.isActive();
        
        // Build Elasticsearch query using field paths
        String query = """
            {
                "bool": {
                    "must": [
                        {"term": {"%s": true}},
                        {"match": {"%s": "search term"}}
                    ]
                }
            }
            """.formatted(isActiveField.getPath(), nameField.getPath());
    }
    
    public void searchCustomers() {
        // Use QueryDSL for JPA
        var customer = QCustomer.customer;
        
        // Standard QueryDSL syntax
        var result = queryFactory
            .selectFrom(customer)
            .where(customer.name.like("%search%"))
            .fetch();
    }
}
```

## Java Naming Conventions

### Property Naming Rules

QElasticsearch follows Java naming conventions when generating accessors:

| Java Property | Generated Field | Field Path |
|---------------|-----------------|------------|
| `private String name` | `TextField<String> name` | `"name"` |
| `private Integer age` | `IntegerField<Integer> age` | `"age"` |  
| `private Boolean isActive` | `BooleanField<Boolean> isActive` | `"isActive"` |
| `private Boolean enabled` | `BooleanField<Boolean> enabled` | `"enabled"` |
| `private Date createdAt` | `DateField<Date> createdAt` | `"createdAt"` |

### Java POJO Integration

QElasticsearch automatically detects standard Java getters and setters:

```java
@Document(indexName = "user")
public class User {
    @Field(type = FieldType.Keyword)
    private String username;
    
    @Field(type = FieldType.Boolean)
    private Boolean isActive;
    
    // Standard Java constructors
    public User() {}
    
    public User(String username, Boolean isActive) {
        this.username = username;
        this.isActive = isActive;
    }
    
    // Standard Java getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

// Generated QUser provides:
QUser.INSTANCE.username  // Access to username field
QUser.INSTANCE.isActive  // Access to isActive field
```

## Best Practices

### 1. Consistent Naming

Use consistent naming between your Java classes and Elasticsearch mappings:

```java
@Document(indexName = "product_catalog")
public class Product {
    @Field(type = FieldType.Keyword, name = "product_id")
    private String id; // Maps to "product_id" in Elasticsearch
}
```

### 2. Type Safety

Leverage generated field paths for type-safe query building:

```java
public class ProductSearchService {
    
    public SearchRequest buildSearchRequest(String searchTerm, boolean activeOnly) {
        var product = QProduct.INSTANCE;
        
        var query = QueryBuilders.boolQuery()
            .must(QueryBuilders.matchQuery(product.name.path(), searchTerm));
            
        if (activeOnly) {
            query.filter(QueryBuilders.termQuery(product.isActive.path(), true));
        }
        
        return new SearchRequest()
            .indices(product.indexName())
            .source(new SearchSourceBuilder().query(query));
    }
}
```

### 3. IDE Integration

QElasticsearch works with Java IDEs for autocompletion and refactoring:

- **IntelliJ IDEA**: Full autocompletion for generated Q-classes
- **Eclipse**: Automatic import suggestions and field navigation
- **VSCode**: Java language server provides full support

## Migration from QueryDSL

If you're already using QueryDSL for other data sources, QElasticsearch integrates seamlessly:

### Before (Manual Elasticsearch queries)

```java
public class SearchService {
    public void search() {
        // Manual field names - error prone
        var query = QueryBuilders.termQuery("is_active", true);
        var nameQuery = QueryBuilders.matchQuery("product_name", "search");
    }
}
```

### After (QElasticsearch)

```java
public class SearchService {
    private final QProduct product = QProduct.INSTANCE;
    
    public void search() {
        // Type-safe field access
        var query = QueryBuilders.termQuery(product.isActive.path(), true);
        var nameQuery = QueryBuilders.matchQuery(product.name.path(), "search");
    }
}
```

## Troubleshooting

### Common Issues

1. **Generated classes not found**: Ensure KSP is properly configured in your build tool
2. **Field access**: Use direct field access (`product.name`) instead of getters
3. **Path access**: Use `.path()` method to get field paths for query building

### Debugging

Enable KSP debug output to see generated files:

```groovy
ksp {
    arg("verbose", "true")
}
```

The generated Q-classes will be in `build/generated/ksp/main/kotlin/` (Gradle) or `target/generated-sources/ksp/` (Maven).