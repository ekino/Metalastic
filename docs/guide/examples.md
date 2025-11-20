# Examples

Comprehensive examples of using Metalastic in real-world scenarios.

## E-Commerce Product Search

### Document Model

```java
@Document(indexName = "products")
public class Product {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    @Field(type = FieldType.Nested)
    private List<Review> reviews;
}

public class Review {
    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Boolean)
    private Boolean verified;

    @Field(type = FieldType.Text)
    private String comment;
}
```

### Generated Metamodels

```kotlin
// MetaProduct.kt
class MetaProduct<T : Any?>(
    parent: ObjectField<*>? = null,
    name: String = "",
    nested: Boolean = false,
    fieldType: KType = typeOf<Product>(),
) : Document<T>(parent, name, nested, fieldType) {

    val id: KeywordField<String> = keyword<String>("id")
    val title: TextField<String> = text<String>("title")
    val description: TextField<String> = text<String>("description")
    val price: DoubleField<Double> = double<Double>("price")
    val category: KeywordField<String> = keyword<String>("category")
    val status: KeywordField<ProductStatus> = keyword<ProductStatus>("status")
    val reviews: MetaReview<Review> =
        MetaReview(this, "reviews", true, typeOf<Review>())

    companion object {
        const val INDEX_NAME = "products"
        val product: MetaProduct<Product> = MetaProduct()
    }
}

class MetaReview<T : Any?>(
    parent: ObjectField<*>?,
    name: String,
    nested: Boolean,
    fieldType: KType,
) : ObjectField<T>(parent, name, nested, fieldType) {

    val rating: DoubleField<Double> = double<Double>("rating")
    val verified: BooleanField<Boolean> = boolean<Boolean>("verified")
    val comment: TextField<String> = text<String>("comment")
}
```

### Query Examples

#### 1. Simple Product Search

```kotlin
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.status term ProductStatus.ACTIVE
        }
    }
}
```

#### 2. Price Range Search

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        filter + {
            product.category term "electronics"
            product.price.range(Range.closed(500.0, 2000.0))
        }
    }
}
```

#### 3. Multi-field Search with Boost

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        should + {
            product.title.match("laptop") { boost(2.0f) }
            product.description match "laptop"
        }
        minimumShouldMatch(1)
    }
}
```

#### 4. Search with Nested Query

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
        }

        filter + {
            NestedQuery.of {
                path(product.reviews)
                query {
                    boolQueryDsl {
                        must + {
                            product.reviews.rating greaterThan 4.0
                            product.reviews.verified term true
                        }
                    }
                }
            }
        }
    }
}
```

#### 5. Complex Search with Multiple Criteria

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            DisMaxQuery.of {
                shouldAtLeastOneOf {
                    product.title.match(searchTerm) { boost(3.0f) }
                    product.description.match(searchTerm) { boost(1.0f) }
                }
                tieBreaker(0.3)
            }
        }

        filter + {
            product.status term ProductStatus.ACTIVE
            product.price.range(Range.closed(minPrice, maxPrice))

            if (categories.isNotEmpty()) {
                product.category terms categories
            }
        }

        should + {
            NestedQuery.of {
                path(product.reviews)
                query {
                    boolQueryDsl {
                        must + {
                            product.reviews.rating greaterThan 4.0
                        }
                    }
                }
                scoreMode(ScoreMode.Avg)
            }
        }

        minimumShouldMatch(0)
    }
}
```

## Blog Post Search

### Document Model

```java
@Document(indexName = "blog_posts")
public class BlogPost {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date)
    private Instant publishedAt;

    @Field(type = FieldType.Object)
    private Author author;
}

public class Author {
    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Text)
    private String displayName;
}
```

### Query Examples

#### 1. Search by Tag and Date Range

```kotlin
import com.example.MetaBlogPost.Companion.blogPost

val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            blogPost.tags term "kotlin"
        }

        filter + {
            blogPost.publishedAt.range(
                Range.closed(
                    startDate.toInstant(),
                    endDate.toInstant()
                )
            )
        }
    }
}
```

#### 2. Full-text Search with Author Filter

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            multiMatch(
                searchQuery,
                blogPost.title,
                blogPost.content
            ) type MultiMatchType.BestFields
        }

        filter + {
            blogPost.author.username term authorUsername
        }
    }
}
```

#### 3. Recent Posts by Multiple Tags

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            blogPost.tags terms listOf("kotlin", "elasticsearch", "spring")
        }

        filter + {
            blogPost.publishedAt greaterThan LocalDate.now().minusMonths(6)
        }
    }
}
```

## Path Traversal Examples

### Simple Field Paths

```kotlin
product.title.path() // "title"
product.price.path() // "price"
```

### Nested Object Paths

```kotlin
product.author.username.path() // "author.username"
product.author.displayName.path() // "author.displayName"
```

### Nested Field Paths

```kotlin
product.reviews.rating.path() // "reviews.rating"
product.reviews.comment.path() // "reviews.comment"

// Check if path is nested
product.reviews.rating.isNestedPath() // true
product.author.username.isNestedPath() // false

// Get nested path segments
product.reviews.rating.nestedPaths().toList() // ["reviews"]
```

## Metamodels Registry

Access all generated metamodels:

```kotlin
import com.example.Metamodels

// Iterate all metamodels
Metamodels.entries().forEach { document ->
    println("Index: ${document.indexName()}")
}

// Get specific metamodel
import com.example.MetaProduct.Companion.product
import com.example.MetaBlogPost.Companion.blogPost
```

## Integration with Spring Data Elasticsearch

```kotlin
@Service
class ProductSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    fun searchProducts(searchRequest: ProductSearchRequest): List<Product> {
        val query = buildQuery(searchRequest)

        val searchQuery = NativeQuery.builder()
            .withQuery(query)
            .withPageable(searchRequest.pageable)
            .build()

        return elasticsearchOperations
            .search(searchQuery, Product::class.java)
            .map { it.content }
            .toList()
    }

    private fun buildQuery(request: ProductSearchRequest): Query {
        return BoolQuery.of {
            boolQueryDsl {
                if (request.searchTerm != null) {
                    must + {
                        product.title match request.searchTerm
                    }
                }

                filter + {
                    if (request.category != null) {
                        product.category term request.category
                    }

                    if (request.minPrice != null || request.maxPrice != null) {
                        product.price.range(
                            Range.closed(
                                request.minPrice ?: 0.0,
                                request.maxPrice ?: Double.MAX_VALUE
                            )
                        )
                    }
                }
            }
        }
    }
}
```

## Next Steps

- Check out the Query DSL documentation
- Learn about field types
- Explore configuration options
