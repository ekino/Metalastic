package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * Creates a BoolQueryDsl context for building
 * [bool queries](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query)
 */
fun BoolQuery.Builder.boolQueryDsl(block: BoolQueryDsl.() -> Unit) = apply {
  BoolQueryDsl(this).apply(block)
}

/**
 * Type-safe DSL for building Elasticsearch boolean queries with typed occurrences.
 *
 * Boolean queries combine multiple query clauses using boolean logic. This DSL provides a fluent
 * API for composing queries within the four typed occurrences of a bool query: [must], [mustNot],
 * [should], and [filter].
 *
 * ##
 * [Query Occurrences](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query)
 * - **[must]**: Documents `MUST` match these queries (affects scoring)
 * - **[mustNot]**: Documents `MUST NOT` match these queries (filters only)
 * - **[should]**: Documents `SHOULD` match these queries (affects scoring, optional)
 * - **[filter]**: Documents `MUST` match these queries (does not affect scoring)
 *
 * ## Usage Example
 *
 * ```kotlin
 * val document = Metamodels.product
 *
 * BoolQuery.of {
 *   boolQueryDsl {
 *     // Must match: affects scoring
 *     must + {
 *       document.title match "laptop"
 *       document.status term Status.ACTIVE
 *     }
 *
 *     // Filter: must match but doesn't affect scoring
 *     filter + {
 *       document.price.range(Range.closed(500.0, 2000.0))
 *       document.inStock term true
 *     }
 *
 *     // Should match: boosts score if matched
 *     should + {
 *       document.brand term "Dell"
 *       document.tags.containsTerms("featured", "sale")
 *     }
 *
 *     // Must not match: excludes documents
 *     mustNot + {
 *       document.category term "refurbished"
 *     }
 *   }
 * }
 * ```
 *
 * ## Operator Syntax
 *
 * The DSL supports an operator-based syntax using the `+` operator:
 * ```kotlin
 * bool {
 *   must + {
 *     document.title match "search term"
 *   }
 *   filter + {
 *     document.status term Status.ACTIVE
 *   }
 * }
 * ```
 *
 * @see com.ekino.oss.metalastic.elasticsearch.dsl.QueryVariantDsl for available query types
 * @see
 *   [Boolean query documentation](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query)
 */
@ElasticsearchDsl
class BoolQueryDsl(private val builder: BoolQuery.Builder) {

  data object Must

  data object MustNot

  data object Should

  data object Filter

  val must = Must
  val mustNot = MustNot
  val should = Should
  val filter = Filter

  /**
   * Adds queries to the `must` occurrence.
   *
   * Documents must match all queries in this occurrence. These queries affect the relevance score.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     mustDsl {
   *       document.title match "laptop"
   *       document.status term Status.ACTIVE
   *     }
   *   }
   * }
   * ```
   *
   * @see QueryVariantDsl for available query types
   */
  fun mustDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.must(Query(query)) }).apply(block)
  }

  /**
   * Adds queries to the `must_not` occurrence.
   *
   * Documents must not match any queries in this occurrence. These queries do not affect scoring
   * and are executed in filter context.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     mustNotDsl {
   *       document.category term "discontinued"
   *       document.status term Status.DELETED
   *     }
   *   }
   * }
   * ```
   *
   * @see QueryVariantDsl for available query types
   */
  fun mustNotDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.mustNot(Query(query)) }).apply(block)
  }

  /**
   * Adds queries to the `should` occurrence.
   *
   * Documents should match at least one query in this occurrence to boost the relevance score.
   * These queries are optional but affect scoring when matched.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     shouldDsl {
   *       document.brand term "Dell"
   *       document.tags.containsTerms("featured", "sale")
   *     }
   *   }
   * }
   * ```
   *
   * @see QueryVariantDsl for available query types
   */
  fun shouldDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.should(Query(query)) }).apply(block)
  }

  /**
   * Adds queries to the `filter` occurrence.
   *
   * Documents must match all queries in this occurrence. These queries do not affect scoring and
   * are executed in filter context for better performance.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     filterDsl {
   *       document.price.range(Range.closed(100.0, 500.0))
   *       document.inStock term true
   *     }
   *   }
   * }
   * ```
   *
   * @see QueryVariantDsl for available query types
   */
  fun filterDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.filter(Query(query)) }).apply(block)
  }

  /**
   * Operator syntax for adding queries to the `must` occurrence.
   *
   * This provides a more concise syntax using the `+` operator.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     must + {
   *       document.title match "laptop"
   *       document.status term Status.ACTIVE
   *     }
   *   }
   * }
   * ```
   *
   * @see mustDsl
   */
  operator fun Must.plus(block: QueryVariantDsl.() -> Unit) = mustDsl(block)

  /**
   * Operator syntax for adding queries to the `must_not` occurrence.
   *
   * This provides a more concise syntax using the `+` operator.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     mustNot + {
   *       document.category term "discontinued"
   *     }
   *   }
   * }
   * ```
   *
   * @see mustNotDsl
   */
  operator fun MustNot.plus(block: QueryVariantDsl.() -> Unit) = mustNotDsl(block)

  /**
   * Operator syntax for adding queries to the `should` occurrence.
   *
   * This provides a more concise syntax using the `+` operator.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     should + {
   *       document.brand term "Dell"
   *     }
   *   }
   * }
   * ```
   *
   * @see shouldDsl
   */
  operator fun Should.plus(block: QueryVariantDsl.() -> Unit) = shouldDsl(block)

  /**
   * Operator syntax for adding queries to the `filter` occurrence.
   *
   * This provides a more concise syntax using the `+` operator.
   *
   * ## Example
   *
   * ```kotlin
   * val document = Metamodels.product
   *
   * BoolQuery.of {
   *   boolQueryDsl {
   *     filter + {
   *       document.inStock term true
   *     }
   *   }
   * }
   * ```
   *
   * @see filterDsl
   */
  operator fun Filter.plus(block: QueryVariantDsl.() -> Unit) = filterDsl(block)
}
