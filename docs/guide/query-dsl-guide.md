<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Query DSL Guide

Build type-safe Elasticsearch queries using generated metamodels. The Metalastic Query DSL provides a fluent API inspired by QueryDSL for SQL databases, with compile-time safety and IDE auto-completion.

## Getting Started

### Overview

The Query DSL module is an optional add-on that works with generated metamodels to provide:

- **Type-safe query construction** - Compile-time validation of field names and types
- **Fluent API** - Intuitive method chaining for query building
- **IDE support** - Full auto-completion for fields and query methods
- **Automatic value conversion** - Handles dates, enums, collections, and custom types

### Installation

Add the DSL module to your dependencies:

```kotlin-vue
dependencies {
    // Core modules (required)
    implementation("com.ekino.oss:metalastic-core:{{ v.metalastic }}")
    ksp("com.ekino.oss:metalastic-processor:{{ v.metalastic }}")

    // Query DSL module (optional) - choose based on your Spring Data ES version
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl:{{ v.dsl.rolling }}")  // 6.0.x (rolling)
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.5:{{ v.dsl.frozen55 }}")  // 5.4-5.5 (frozen)
    // OR
    implementation("com.ekino.oss:metalastic-elasticsearch-dsl-5.3:{{ v.dsl.frozen53 }}")  // 5.0-5.3 (frozen)
}
```

### Version Compatibility

The DSL module uses a rolling release strategy with frozen artifacts for stability:

| Artifact | Strategy | Supported Spring Data ES | Brings Transitively |
|----------|----------|--------------------------|---------------------|
| `metalastic-elasticsearch-dsl` | **Rolling** | 6.0.x (currently) | Spring Data ES {{ v.springDataES.v60 }} |
| `metalastic-elasticsearch-dsl-5.5` | **Frozen** | 5.4.x - 5.5.x | Spring Data ES {{ v.springDataES.v55 }} |
| `metalastic-elasticsearch-dsl-5.3` | **Frozen** | 5.0.x - 5.3.x | Spring Data ES {{ v.springDataES.v53 }} |

**Rolling Release**: The base artifact (`elasticsearch-dsl`) tracks the latest Spring Data ES versions. When breaking changes occur (like the 6.0 release), we freeze the previous version and update the rolling artifact.

## Understanding the DSL Syntax

The Metalastic Query DSL offers **two equivalent syntaxes** for building queries. Both provide identical functionality and type safety - choose based on your preference!

### Two Syntaxes Comparison

| Feature | Operator Syntax (`must + { }`) | Classical Syntax (`mustDsl { }`) |
|---------|-------------------------------|----------------------------------|
| **Style** | Modern, operator-overloaded | Traditional method calls |
| **Readability** | Concise, mirrors ES JSON | Explicit, self-documenting |
| **Searchability** | Uses `+` operator | Easy to grep/search |
| **Learning Curve** | Kotlin-idiomatic | Familiar to all developers |
| **Type Safety** | ✅ Full compile-time | ✅ Full compile-time |
| **IDE Support** | ✅ Complete autocomplete | ✅ Complete autocomplete |

### Operator Syntax (`must + { }`)

Clean, operator-overloaded syntax using the `+` operator:

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
            product.status term ProductStatus.ACTIVE
        }

        filter + {
            product.price greaterThan 100.0
            product.inStock term true
        }

        should + {
            product.brand term "Apple"
            product.brand term "Dell"
        }

        mustNot + {
            product.tags term "discontinued"
        }

        minimumShouldMatch(1)
    }
}
```

**Benefits:**
- ✨ **Natural syntax** - Reads like Elasticsearch JSON structure
- 🎯 **Concise** - Less boilerplate, focus on query logic
- 📖 **Visual clarity** - `+` operator clearly shows clause additions
- 🔗 **Composable** - Easy to see query structure at a glance

### Classical Syntax (`mustDsl { }`)

Traditional method-based syntax with explicit names:

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        mustDsl {
            product.title match "laptop"
            product.status term ProductStatus.ACTIVE
        }

        filterDsl {
            product.price greaterThan 100.0
            product.inStock term true
        }

        shouldDsl {
            product.brand term "Apple"
            product.brand term "Dell"
        }

        mustNotDsl {
            product.tags term "discontinued"
        }

        minimumShouldMatch(1)
    }
}
```

**Benefits:**
- 📝 **Explicit naming** - Clear intent with `mustDsl`, `shouldDsl` method names
- 🔍 **Searchable** - Easy to find in codebase with standard method search
- 🎓 **Familiar** - Traditional method call syntax, no operators
- 🔄 **Consistent** - Matches common DSL patterns in other libraries

### Mixing Both Syntaxes

Both syntaxes are **fully interchangeable** and can be mixed in the same query:

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        // Use operator syntax for simple clauses
        must + {
            product.title match "laptop"
        }

        // Use classical syntax for complex clauses
        filterDsl {
            product.price.range(Range.closed(100.0, 2000.0))
            product.category term "electronics"
        }

        // Mix freely based on readability
        should + { product.featured term true }
    }
}
```

### Which Syntax Should You Use?

**Our Recommendation:** Choose based on team preference - there's no "wrong" choice!

- **Prefer operator syntax if:** Your team likes concise Kotlin idioms and operator overloading
- **Prefer classical syntax if:** Your team values explicit method names and searchability
- **Mix both if:** You want to optimize readability case-by-case

**Throughout this guide**, we'll show both syntaxes in examples to help you learn both approaches.

### Basic Usage

Import the metamodel from its companion object:

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

// Build a simple query (operator syntax)
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
        }
    }
}

// Same query (classical syntax)
val query = BoolQuery.of {
    boolQueryDsl {
        mustDsl {
            product.title match "laptop"
        }
    }
}
```

## Full-text Queries

Full-text queries analyze the query string and search analyzed fields. These queries are best for natural language search.

### Match Query

Search for terms in a text field:

```kotlin
// Simple match
product.title match "laptop computer"

// With options
product.title.match("laptop") {
    fuzziness("AUTO")
    operator(Operator.And)
    minimumShouldMatch("75%")
}
```

**Use when:**
- Searching user input in text fields
- Need fuzzy matching or typo tolerance
- Want analyzed search (stemming, synonyms, etc.)

### Multi-Match Query

Search across multiple fields:

```kotlin
// Simple multi-match
multiMatch("laptop", product.title, product.description, product.brand)

// With type and options
multiMatch("gaming laptop") {
    fields(product.title, product.description)
    type(MultiMatchQuery.Type.BestFields)
    tieBreaker(0.3)
}
```

**Use when:**
- Searching across multiple text fields
- Want to boost certain fields over others
- Need cross-field matching strategies

### Match Phrase Query

Search for exact phrase in order:

```kotlin
// Exact phrase
product.description matchPhrase "high performance gaming"

// With slop (word distance tolerance)
product.description.matchPhrase("performance gaming") {
    slop(2)  // allows up to 2 words between terms
}
```

**Use when:**
- Need exact phrase matching
- Searching for multi-word terms
- Order of words matters

### Match Phrase Prefix Query

Autocomplete-style prefix matching:

```kotlin
// Prefix match
product.title matchPhrasePrefix "lap"  // matches "laptop", "laptop computer", etc.

// With max expansions
product.title.matchPhrasePrefix("gam") {
    maxExpansions(50)
}
```

**Use when:**
- Building autocomplete features
- Need prefix matching on phrases
- Want fuzzy completion

## Term-level Queries

Term-level queries match exact values without analysis. Use these for structured data like IDs, statuses, numbers, and dates.

### Term Query

Match an exact value:

```kotlin
// Exact match
product.status term Status.ACTIVE
product.category term "electronics"
product.id term "PROD-12345"

// With enums (automatic conversion)
product.status term ProductStatus.IN_STOCK
```

**Use when:**
- Searching keyword fields
- Exact value matching (IDs, statuses, codes)
- Filtering by enums or boolean values

### Terms Query

Match any value from a list:

```kotlin
// Match any category
product.category terms listOf("electronics", "computers", "gaming")

// With sets
product.tags terms setOf("featured", "new", "sale")

// With enum list
product.status terms listOf(Status.ACTIVE, Status.PENDING)
```

**Use when:**
- Filtering by multiple values (OR logic)
- Building faceted search
- "Any of" filtering

### Terms Set Query

Match a minimum number of terms:

```kotlin
// Match at least N terms
product.tags.termsSet(listOf("new", "sale", "featured")) {
    minimumShouldMatchField("required_matches")
    // OR
    minimumShouldMatchScript("Math.min(params.num_terms, 2)")
}
```

**Use when:**
- Need flexible term matching
- Dynamic minimum should match requirements
- Advanced filtering logic

### Wildcard Query

Pattern matching with wildcards:

```kotlin
// * matches zero or more characters
product.sku wildcard "PROD-*-2024"

// ? matches single character
product.code wildcard "AB?-123"

// Case insensitive
product.email.wildcard("*@example.com") {
    caseInsensitive(true)
}
```

**Use when:**
- Pattern-based search
- Partial matching needed
- Building search filters

### Prefix Query

Match terms starting with a prefix:

```kotlin
// Simple prefix
product.code prefix "ABC"
product.sku prefix "PROD-2024"

// With case insensitivity
product.email.prefix("user") {
    caseInsensitive(true)
}
```

**Use when:**
- Autocomplete on keyword fields
- Filtering by prefix
- Building type-ahead search

### Regexp Query

Regular expression matching:

```kotlin
// Basic regexp
product.email regexp "[a-z]+@[a-z]+\\.[a-z]+"
product.sku regexp "PROD-[0-9]{4}-.*"

// With flags
product.code.regexp("[A-Z]{3}-[0-9]+") {
    flags("ALL")
    maxDeterminizedStates(10000)
}
```

**Use when:**
- Complex pattern matching
- Validation-style queries
- Advanced filtering logic

## Boolean Queries

Boolean queries combine multiple queries using boolean logic (must, should, filter, must_not). Remember, you can use **either operator syntax** (`must +`) **or classical syntax** (`mustDsl`) - or mix both!

### Basic Boolean Query (Operator Syntax)

Using the modern `+` operator for clause additions:

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        // must: All conditions must match (affects score)
        must + {
            product.title match "laptop"
            product.status term Status.ACTIVE
        }

        // filter: All conditions must match (no scoring)
        filter + {
            product.price range 500.0.fromInclusive()..2000.0
            product.category term "electronics"
        }

        // should: At least one should match (boosts score)
        should + {
            product.brand term "Apple"
            product.brand term "Dell"
            product.brand term "Lenovo"
        }

        // must_not: Must not match any
        mustNot + {
            product.tags term "discontinued"
        }

        // Minimum should match
        minimumShouldMatch(1)
    }
}
```

### Basic Boolean Query (Classical Syntax)

Same query using explicit method names:

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val query = BoolQuery.of {
    boolQueryDsl {
        // must: All conditions must match (affects score)
        mustDsl {
            product.title match "laptop"
            product.status term Status.ACTIVE
        }

        // filter: All conditions must match (no scoring)
        filterDsl {
            product.price range 500.0.fromInclusive()..2000.0
            product.category term "electronics"
        }

        // should: At least one should match (boosts score)
        shouldDsl {
            product.brand term "Apple"
            product.brand term "Dell"
            product.brand term "Lenovo"
        }

        // must_not: Must not match any
        mustNotDsl {
            product.tags term "discontinued"
        }

        // Minimum should match
        minimumShouldMatch(1)
    }
}
```

### Nested Boolean Queries

The DSL provides a built-in `bool { }` function to create nested boolean queries inside any clause. This is cleaner than manually constructing `BoolQuery.of { }`:

```kotlin
val complexQuery = BoolQuery.of {
    boolQueryDsl {
        must + {
            // Use the built-in bool { } function for nested logic
            bool {
                should + {
                    product.title match "laptop"
                    product.description match "laptop"
                }
                minimumShouldMatch(1)
            }
        }

        filter + {
            product.inStock term true
            product.price range 0.0.fromExclusive()..null  // Price > 0
        }
    }
}
```

**Benefits of `bool { }` function:**
- ✨ Cleaner syntax - no need for `BoolQuery.of { boolQueryDsl { } }`
- 🎯 Automatic empty query handling - skips query if no conditions added
- 📖 Better readability - clear intent for nested boolean logic

#### Multiple Nested Boolean Queries

```kotlin
val advancedQuery = BoolQuery.of {
    boolQueryDsl {
        must + {
            // First nested bool: title OR description contains search term
            bool {
                should + {
                    product.title match "laptop"
                    product.description match "laptop"
                }
                minimumShouldMatch(1)
            }
        }

        filter + {
            // Second nested bool: brand filters
            bool {
                should + {
                    product.brand term "Apple"
                    product.brand term "Dell"
                    product.brand term "Lenovo"
                }
                minimumShouldMatch(1)
            }
        }

        // Simple conditions alongside nested bools
        mustNot + {
            product.tags term "discontinued"
        }
    }
}
```

### Mixing Both Syntaxes

Use whichever syntax makes your code most readable:

```kotlin
val mixedQuery = BoolQuery.of {
    boolQueryDsl {
        // Operator syntax for simple conditions
        must + {
            product.status term Status.ACTIVE
        }

        // Classical syntax for complex filters
        filterDsl {
            product.price range 100.0.fromInclusive()..1000.0
            product.category term "electronics"
            product.rating range 4.0.fromInclusive()..null
        }

        // Back to operator for simple should clause
        should + { product.featured term true }
    }
}
```

### Boolean Logic Breakdown

| Occurrence | Behavior | Affects Score | Use For |
|------------|----------|---------------|---------|
| `must` / `mustDsl` | All queries must match | Yes | Required conditions with relevance scoring |
| `filter` / `filterDsl` | All queries must match | No | Required conditions without scoring (faster) |
| `should` / `shouldDsl` | At least one should match | Yes | Optional conditions that boost score |
| `mustNot` / `mustNotDsl` | Must not match any | No | Exclusion filters |

**Performance Tip:** Use `filter` instead of `must` for conditions that don't need relevance scoring - it's more efficient!

## Range Queries

Query numeric, date, or string fields with range constraints.

### Using Guava Range

```kotlin
import com.google.common.collect.Range

// Closed range [min, max]
product.price.range(Range.closed(100.0, 500.0))

// Open range (min, max)
product.price.range(Range.open(0.0, 1000.0))

// Half-open ranges
product.price.range(Range.closedOpen(100.0, 500.0))  // [100, 500)
product.price.range(Range.openClosed(100.0, 500.0))  // (100, 500]

// Unbounded ranges
product.price.range(Range.atLeast(500.0))   // >= 500
product.price.range(Range.atMost(1000.0))   // <= 1000
product.price.range(Range.greaterThan(100.0))  // > 100
product.price.range(Range.lessThan(1000.0))    // < 1000
```

### Convenience Methods

```kotlin
// Greater than (>)
product.price greaterThan 100.0
product.quantity greaterThan 0

// Less than (<)
product.price lowerThan 1000.0
product.rating lowerThan 5.0

// Between (closed range)
product.createdAt mustBeBetween (startDate to endDate)
```

### Mathematical Notation with Kotlin Ranges

Metalastic provides **mathematical notation** for range queries using Kotlin operators (`..` and `..<`) combined with `fromInclusive()` and `fromExclusive()` extension functions. This approach offers a natural, type-safe way to express range boundaries.

#### StartBound Overview

The `StartBound` inline value class enables zero-overhead range notation with compile-time type safety:

```kotlin
// Basic pattern:
// lowerBound.fromInclusive()..upperBound    →  [lowerBound, upperBound]
// lowerBound.fromInclusive()..<upperBound   →  [lowerBound, upperBound)
// lowerBound.fromExclusive()..upperBound    →  (lowerBound, upperBound]
// lowerBound.fromExclusive()..<upperBound   →  (lowerBound, upperBound)
```

#### All Bracket Combinations

| Mathematical Notation | Kotlin Syntax | Meaning |
|-----------------------|---------------|---------|
| `[10, 100]` | `10.fromInclusive()..100` | 10 ≤ x ≤ 100 (both included) |
| `[10, 100)` | `10.fromInclusive()..<100` | 10 ≤ x < 100 (exclude upper) |
| `(10, 100]` | `10.fromExclusive()..100` | 10 < x ≤ 100 (exclude lower) |
| `(10, 100)` | `10.fromExclusive()..<100` | 10 < x < 100 (both excluded) |

#### Closed Ranges (Both Bounds Included)

```kotlin
// [10, 100] → 10 ≤ x ≤ 100
product.price range 10.0.fromInclusive()..100.0

// Same with variables
val minPrice = 10.0
val maxPrice = 100.0
product.price range minPrice.fromInclusive()..maxPrice
```

**When to use:**
- Most common range type
- Include boundary values in results
- Equivalent to `Range.closed(10.0, 100.0)`

#### Half-Open Ranges

```kotlin
// [10, 100) → 10 ≤ x < 100 (exclude upper bound)
product.price range 10.0.fromInclusive()..<100.0

// (10, 100] → 10 < x ≤ 100 (exclude lower bound)
product.price range 10.0.fromExclusive()..100.0

// (10, 100) → 10 < x < 100 (exclude both bounds)
product.price range 10.0.fromExclusive()..<100.0
```

**When to use:**
- Precise boundary control
- Avoid overlapping ranges
- Mathematical accuracy requirements

#### Unbounded Ranges with Null

Use `null` to represent infinity (∞):

```kotlin
// [10, ∞) → x ≥ 10
product.price range 10.0.fromInclusive()..null

// (10, ∞) → x > 10
product.price range 10.0.fromExclusive()..null

// (-∞, 100] → x ≤ 100
product.price range null.fromInclusive()..100.0

// (-∞, 100) → x < 100
product.price range null.fromInclusive()..<100.0
```

**When to use:**
- Open-ended ranges
- "At least" or "at most" constraints
- Clearer than separate `greaterThan`/`lowerThan` calls

#### Complete Example

```kotlin
import com.ekino.oss.metalastic.dsl.*
import com.example.MetaProduct.Companion.product

val searchQuery = BoolQuery.of {
    boolQueryDsl {
        filter + {
            // Price between $50 and $200 (inclusive)
            product.price range 50.0.fromInclusive()..200.0

            // Rating at least 4.0 (no upper limit)
            product.rating range 4.0.fromInclusive()..null

            // Stock level greater than 0 (exclusive lower bound)
            product.stockLevel range 0.fromExclusive()..null

            // Discount less than 50% (exclusive upper bound)
            product.discount range null.fromInclusive()..<50.0
        }
    }
}
```

#### Comparison with Guava Range

Both syntaxes are supported - choose based on preference:

| Mathematical | StartBound Syntax | Guava Range Syntax |
|--------------|-------------------|--------------------|
| `[10, 100]` | `10.fromInclusive()..100` | `Range.closed(10, 100)` |
| `[10, 100)` | `10.fromInclusive()..<100` | `Range.closedOpen(10, 100)` |
| `(10, 100]` | `10.fromExclusive()..100` | `Range.openClosed(10, 100)` |
| `(10, 100)` | `10.fromExclusive()..<100` | `Range.open(10, 100)` |
| `[10, ∞)` | `10.fromInclusive()..null` | `Range.atLeast(10)` |
| `(10, ∞)` | `10.fromExclusive()..null` | `Range.greaterThan(10)` |
| `(-∞, 100]` | `null.fromInclusive()..100` | `Range.atMost(100)` |
| `(-∞, 100)` | `null.fromInclusive()..<100` | `Range.lessThan(100)` |

**StartBound advantages:**
- 📐 Natural mathematical notation
- 🎯 Kotlin-idiomatic operators (`..` and `..<`)
- 📝 Visual clarity with bracket semantics
- ∞ Explicit unbounded ranges with `null`

**Guava Range advantages:**
- 🔧 Familiar to Java developers
- 📚 Standard library (Google Guava)
- 🔄 Reusable Range objects
- 🎓 Well-documented API

### Date Range Queries

```kotlin
import java.time.LocalDate
import java.time.Instant

// Mathematical notation with dates
product.createdAt range LocalDate.now().minusDays(30).fromInclusive()..LocalDate.now()

// Unbounded date range (last 30 days to now)
product.publishedAt range Instant.now().minusSeconds(2592000).fromInclusive()..null

// Traditional Guava Range
product.createdAt.range(
    Range.closed(
        LocalDate.now().minusDays(30),
        LocalDate.now()
    )
)

// Convenience operators
product.publishedAt greaterThan Instant.now().minusSeconds(86400)
product.expiresAt lowerThan LocalDate.now().plusDays(7)
```

## Nested Queries

Query nested objects while maintaining their independent document structure. The DSL provides a built-in `nested { }` function on nested fields for cleaner syntax.

### Using the Built-in `nested { }` Function (Recommended)

The cleanest way to query nested fields:

```kotlin
val query = BoolQuery.of {
    boolQueryDsl {
        must + {
            // Use the built-in nested { } function
            product.reviews.nested {
                must + {
                    product.reviews.rating greaterThan 4.0
                    product.reviews.verified term true
                }
            }
        }
    }
}
```

**Benefits of `nested { }` function:**
- ✨ **Automatic path detection** - No need to specify `path()`
- 🎯 **Cleaner syntax** - Nested query directly on the field
- 🛡️ **Type safety** - Only works on actual nested fields
- ⚠️ **Automatic validation** - Warns at runtime if used on non-nested fields
- 🔄 **Graceful fallback** - Applies as regular bool query if field isn't nested

### Nested with Complex Logic

```kotlin
val searchQuery = BoolQuery.of {
    boolQueryDsl {
        must + {
            product.title match "laptop"
        }

        filter + {
            // Nested query with complex boolean logic
            product.reviews.nested {
                must + {
                    product.reviews.rating range 4.0.fromInclusive()..5.0
                }

                should + {
                    product.reviews.author match "verified_buyer"
                    product.reviews.helpful greaterThan 10
                }

                filter + {
                    product.reviews.createdAt greaterThan LocalDate.now().minusMonths(6)
                }

                minimumShouldMatch(1)
            }
        }
    }
}
```

### Safety Validation and Logging

The `nested { }` function includes **automatic validation** to prevent common mistakes:

```kotlin
// ⚠️ Attempting to use nested on a non-nested field
product.address.nested {  // address is @Field(type = FieldType.Object), NOT Nested
    must + {
        product.address.city term "Paris"
    }
}
```

**What happens:**
1. The DSL detects that `address` is not marked as `@Field(type = FieldType.Nested)`
2. Logs a warning message:
   ```
   WARN: Nested query used on non-nested field 'address'.
   The field should be marked with @Field(type = FieldType.Nested) in the Elasticsearch mapping.
   The query will be applied as a regular bool query instead.
   ```
3. **Gracefully falls back** to a regular bool query (doesn't fail)

**Enabling logging:**

The DSL uses `io.github.oshai:kotlin-logging` for structured logging. To see warnings:

```kotlin
// In your logback.xml or similar
<logger name="com.ekino.oss.metalastic.elasticsearch.dsl" level="WARN"/>
```

**Benefits:**
- 🐛 **Catch mapping errors early** - Identifies misconfigured fields during development
- 🔍 **Clear error messages** - Explains exactly what's wrong and how to fix it
- 🛡️ **No runtime failures** - Query still executes, just without nested semantics
- 📊 **Production monitoring** - Helps identify mapping issues in production

### Traditional `NestedQuery.of` Syntax

You can also use the traditional syntax for more control (e.g., custom score mode):

```kotlin
val nestedQuery = NestedQuery.of {
    // Manually specify the nested path
    path(product.reviews)

    // Query within nested context
    query {
        boolQueryDsl {
            must + {
                product.reviews.rating greaterThan 4.0
                product.reviews.verified term true
            }
        }
    }

    // Custom score mode
    scoreMode(NestedQuery.ScoreMode.Avg)
}
```

**Use when:**
- Need custom score mode configuration
- Building queries dynamically
- Prefer explicit path specification
- Don't want automatic validation (traditional syntax doesn't validate)

### Why Use Nested Queries?

Nested queries are essential when you need to maintain relationships between fields in array/collection objects.

**Without nested** (flattened objects - ❌ INCORRECT):
```kotlin
// ❌ These would match incorrectly if reviews are flattened
// Could match review1.rating=5 AND review2.verified=true (wrong!)
BoolQuery.of {
    boolQueryDsl {
        must + {
            product.reviews.rating greaterThan 4.0
            product.reviews.verified term true
        }
    }
}
```

**With nested** (maintains relationships - ✅ CORRECT):
```kotlin
// ✅ Ensures SAME review has rating > 4 AND verified = true
BoolQuery.of {
    boolQueryDsl {
        must + {
            product.reviews.nested {
                must + {
                    product.reviews.rating greaterThan 4.0
                    product.reviews.verified term true
                }
            }
        }
    }
}
```

**Key Point:** Without nested queries, Elasticsearch flattens arrays and loses field relationships. With nested queries, each nested object is indexed as a separate document, maintaining field correlations.

## Specialized Queries

### Fuzzy Query

Find terms similar to the search term (typo tolerance):

```kotlin
// Simple fuzzy
product.title fuzzy "laptpo"  // finds "laptop"

// With fuzziness control
product.name.fuzzy("jhon") {
    fuzziness("AUTO")  // or "1", "2", etc.
    prefixLength(0)
    maxExpansions(50)
}
```

**Fuzziness levels:**
- `"AUTO"` - automatic based on term length (recommended)
- `"0"` - no fuzziness (exact match)
- `"1"` - one character difference
- `"2"` - two character difference

### Exists Query

Check if a field has a value:

```kotlin
// Field must exist and have a non-null value
exist(product.description)
exist(product.tags)

// In boolean query
BoolQuery.of {
    boolQueryDsl {
        filter + {
            exist(product.price)
        }
    }
}
```

### Geo Distance Query

Find documents within a distance from a point:

```kotlin
import co.elastic.clients.elasticsearch.core.search.GeoPoint

// Simple geo distance
product.location geoDistance GeoPoint.of { it.latlon(48.8566, 2.3522) } within "10km"

// With validation mode
product.storeLocation.geoDistance(
    GeoPoint.of { it.latlon(40.7128, -74.0060) },
    "5km"
) {
    validationMethod(GeoValidationMethod.IgnoreMalformed)
}
```

### More Like This Query

Find documents similar to given text or documents:

```kotlin
moreLikeThis {
    // Fields to analyze
    fields(product.title, product.description)

    // Text to compare against
    likeTexts("high performance gaming laptop")

    // Or compare against documents
    // like(Document.of("product-123", "products"))

    // Tuning parameters
    minTermFreq(1)           // minimum term frequency
    maxQueryTerms(12)        // maximum query terms
    minDocFreq(2)            // minimum document frequency
    minWordLength(3)         // minimum word length
}
```

## Value Conversion

The DSL automatically converts Kotlin/Java types to Elasticsearch-compatible values.

### Supported Types

**Primitive Types:**
```kotlin
product.id term "PROD-123"              // String
product.quantity term 100               // Int
product.price term 99.99                // Double
product.active term true                // Boolean
```

**Date Types:**
```kotlin
import java.time.*

product.createdAt greaterThan Date()                    // java.util.Date
product.publishedAt greaterThan Instant.now()           // Instant
product.scheduleDate greaterThan LocalDate.now()        // LocalDate
product.timestamp greaterThan LocalDateTime.now()       // LocalDateTime
product.eventTime greaterThan ZonedDateTime.now()       // ZonedDateTime
```

**Collections:**
```kotlin
// Lists
product.tags terms listOf("featured", "new", "sale")

// Sets
product.categories terms setOf("electronics", "computers")

// Arrays (converted to lists)
product.ids terms arrayOf("id1", "id2", "id3")
```

**Enums:**
```kotlin
enum class ProductStatus {
    ACTIVE, INACTIVE, DISCONTINUED
}

// Automatic enum name conversion
product.status term ProductStatus.ACTIVE  // converts to "ACTIVE"
product.status terms listOf(ProductStatus.ACTIVE, ProductStatus.INACTIVE)
```

### Custom Type Conversion

The DSL uses Kotlin's `KType` reflection for type conversion. Complex types are automatically handled through the type system.

## Best Practices

### 1. Choose Your Syntax Style Consistently

Pick a primary syntax style for your project and use it consistently:

```kotlin
// ✅ Good - Consistent operator syntax throughout
val query = BoolQuery.of {
    boolQueryDsl {
        must + { product.title match "laptop" }
        filter + { product.status term Status.ACTIVE }
        should + { product.featured term true }
    }
}

// ✅ Also good - Consistent classical syntax throughout
val query = BoolQuery.of {
    boolQueryDsl {
        mustDsl { product.title match "laptop" }
        filterDsl { product.status term Status.ACTIVE }
        shouldDsl { product.featured term true }
    }
}

// ⚠️ Acceptable - Mix for readability if needed
val query = BoolQuery.of {
    boolQueryDsl {
        must + { product.title match "laptop" }  // Simple: operator
        filterDsl {  // Complex: classical for clarity
            product.status term Status.ACTIVE
            product.price range 100.0.fromInclusive()..1000.0
            product.rating range 4.0.fromInclusive()..null
        }
    }
}
```

**Team Guidelines:**
- Document your team's preferred syntax in coding standards
- Use code reviews to maintain consistency
- Both syntaxes are equally valid - choose what works for your team

### 2. Prefer Mathematical Notation for Ranges

Use StartBound notation for clearer intent:

```kotlin
// ✅ Best - Mathematical notation is most readable
product.price range 50.0.fromInclusive()..200.0     // [50, 200]
product.rating range 4.0.fromInclusive()..null      // [4.0, ∞)
product.discount range null.fromInclusive()..<50.0  // (-∞, 50)

// ✅ Good - Guava Range for complex ranges
product.price.range(Range.closed(50.0, 200.0))

// ⚠️ Less clear - Use for simple comparisons only
product.price greaterThan 50.0
product.rating lowerThan 5.0
```

### 3. Import Metamodels from Companion Objects

```kotlin
// ✅ Good
import com.example.MetaProduct.Companion.product
product.title match "laptop"

// ❌ Avoid
val product = MetaProduct<Product>()  // creates unnecessary instances
```

### 4. Use Type-safe Enums

```kotlin
// ✅ Good - compile-time safety
product.status term ProductStatus.ACTIVE

// ❌ Avoid - typo-prone
product.status term "ACTIVE"
```

### 5. Leverage IDE Autocomplete

The metamodels provide full IDE support:
- ✅ Field name completion
- ✅ Type checking
- ✅ Refactoring support
- ✅ Documentation hints

### 6. Use `filter` Instead of `must` for Non-scoring

```kotlin
// ✅ Good - more efficient
BoolQuery.of {
    boolQueryDsl {
        filter + {
            product.category term "electronics"
            product.inStock term true
        }
    }
}

// ❌ Slower - unnecessary scoring
BoolQuery.of {
    boolQueryDsl {
        must + {
            product.category term "electronics"
            product.inStock term true
        }
    }
}
```

**Why:** `filter` clauses are cached and don't calculate relevance scores, making them significantly faster for exact-match conditions.

### 7. Combine Query Types Appropriately

```kotlin
val searchQuery = BoolQuery.of {
    boolQueryDsl {
        // Full-text search for relevance
        must + {
            product.title match searchTerm
        }

        // Exact filters (use filter for performance)
        filter + {
            product.status term Status.ACTIVE
            product.price range minPrice.fromInclusive()..maxPrice
        }

        // Optional boosts
        should + {
            product.featured term true  // boost featured products
        }
    }
}
```

### 8. Use Nested Queries for Nested Fields

```kotlin
// ✅ Good - maintains relationships
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

// ❌ Wrong - loses relationships
BoolQuery.of {
    boolQueryDsl {
        must + {
            product.reviews.rating greaterThan 4.0  // Wrong context
        }
    }
}
```

## Next Steps

- [Examples](/guide/examples) - See complete real-world examples
- [Field Types Reference](/guide/field-types-reference) - Learn about all field types
- [Understanding Metamodels](/guide/understanding-metamodels) - Deep dive into metamodel generation
