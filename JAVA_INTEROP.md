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
String indexName = product.getIndexName(); // "product"

// Access fields with proper Java getters
var idField = product.getId();
var nameField = product.getName();
var descriptionField = product.getDescription();
var priceField = product.getPrice();
var isActiveField = product.isActive(); // Note: boolean properties use isXxx()
var createdAtField = product.getCreatedAt();
var categoryField = product.getCategory();
var tagsField = product.getTags();
var searchableTitleField = product.getSearchableTitle();

// Get field paths for Elasticsearch queries
String idPath = idField.getPath(); // "id"
String namePath = nameField.getPath(); // "name"
String isActivePath = isActiveField.getPath(); // "isActive"
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

| Java Property | Generated Accessor | Field Path |
|---------------|-------------------|------------|
| `private String name` | `getName()` | `"name"` |
| `private Integer age` | `getAge()` | `"age"` |
| `private Boolean isActive` | `isActive()` | `"isActive"` |
| `private Boolean enabled` | `getEnabled()` | `"enabled"` |
| `private Date createdAt` | `getCreatedAt()` | `"createdAt"` |

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
QUser.INSTANCE.getUsername()  // Access to username field
QUser.INSTANCE.isActive()     // Access to isActive field (follows boolean convention)
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
            .must(QueryBuilders.matchQuery(product.getName().getPath(), searchTerm));
            
        if (activeOnly) {
            query.filter(QueryBuilders.termQuery(product.isActive().getPath(), true));
        }
        
        return new SearchRequest()
            .indices(product.getIndexName())
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
        var query = QueryBuilders.termQuery(product.isActive().getPath(), true);
        var nameQuery = QueryBuilders.matchQuery(product.getName().getPath(), "search");
    }
}
```

## Troubleshooting

### Common Issues

1. **Generated classes not found**: Ensure KSP is properly configured in your build tool
2. **Boolean accessor naming**: Use `isXxx()` for boolean properties, not `getIsXxx()`
3. **Missing getters/setters**: Ensure all fields have proper getter and setter methods

### Debugging

Enable KSP debug output to see generated files:

```groovy
ksp {
    arg("verbose", "true")
}
```

The generated Q-classes will be in `build/generated/ksp/main/kotlin/` (Gradle) or `target/generated-sources/ksp/` (Maven).