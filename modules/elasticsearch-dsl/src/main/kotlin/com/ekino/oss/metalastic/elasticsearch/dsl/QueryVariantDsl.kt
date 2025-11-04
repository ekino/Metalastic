package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.DistanceUnit
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.CommonTermsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.DisMaxQuery
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery
import co.elastic.clients.elasticsearch._types.query_dsl.GeoDistanceQuery
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchNoneQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhrasePrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryVariant
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.RegexpQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermsSetQuery
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery
import co.elastic.clients.json.JsonData
import com.google.common.collect.BoundType
import com.google.common.collect.Range
import com.metalastic.core.Container
import com.metalastic.core.DateField
import com.metalastic.core.Metamodel
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.Date

/**
 * Type-safe DSL for building Elasticsearch queries using metamodel-based field references.
 *
 * This DSL provides a fluent, Kotlin-idiomatic API for constructing Elasticsearch queries with
 * compile-time type safety. Instead of using string-based field names, queries are built using
 * generated metamodel field references, preventing runtime errors from typos or incorrect field
 * paths.
 *
 * ## Key Features
 * - **Type-safe field references**: Use metamodel properties instead of string literals
 * - **Fluent query composition**: Chain query builders with infix operators
 * - **Nested and object field support**: Navigate complex document structures with dot notation
 * - **Comprehensive query coverage**: Support for all major Elasticsearch query types
 *
 * ## Supported Query Types
 * - **Full-text queries**: [match], [multiMatch], [matchPhrase], [matchPhrasePrefix]
 * - **Term-level queries**: [term], [terms], [termsSet], [wildCard], [prefix], [regexp]
 * - **Boolean queries**: [bool], [shouldAtLeastOneOf], [disMax]
 * - **Range queries**: [range], [greaterThan], [lowerThan], [mustBeBetween]
 * - **Nested queries**: [nested]
 * - **Specialized queries**: [fuzzy], [exist], [geoDistance], [moreLikeThis]
 *
 * [Elasticsearch Query dsl
 * documentation](https://www.elastic.co/docs/explore-analyze/query-filter/languages/querydsl)
 *
 * @see BoolQueryDsl for boolean query composition
 */
@Suppress("TooManyFunctions")
@ElasticsearchDsl
class QueryVariantDsl(private val add: (queryVariant: QueryVariant) -> Unit) {

  companion object {
    private val logger = KotlinLogging.logger {}
  }

  operator fun <T : QueryVariant> T.unaryPlus(): T {
    add(this)
    return this
  }

  /**
   * Creates a nested [BoolQuery] inside the current
   * [typed occurrence](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query)
   * of the [BoolQueryDsl]
   * - [BoolQueryDsl.must]
   * - [BoolQueryDsl.mustNot]
   * - [BoolQueryDsl.should]
   * - [BoolQueryDsl.filter]
   */
  @VariantDsl
  fun bool(block: BoolQueryDsl.() -> Unit) {
    val boolQuery = BoolQuery.Builder().apply { BoolQueryDsl(this).apply(block) }.build()
    if (!boolQuery.isEmpty()) {
      +boolQuery
    }
  }

  /**
   * Creates a nested [BoolQuery] inside the current
   * [typed occurrence](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query)
   * of the [BoolQueryDsl]
   * - [BoolQueryDsl.must]
   * - [BoolQueryDsl.mustNot]
   * - [BoolQueryDsl.should]
   * - [BoolQueryDsl.filter]
   *
   * If the collection is empty or null, current [BoolQuery] is not modified.
   *
   * For each non-empty value in the collection [values], offers a [QueryVariantDsl] callback
   * allowing to add [QueryVariant] to the `should` occurrence of the nested [BoolQuery].
   *
   * `should` query-variants are added in a nested [BoolQuery] to ensure that at least one of the
   * [QueryVariant] is a match.
   *
   * [see Using&nbsp;
   * minimum_should_match](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-bool-query#bool-min-should-match)
   */
  @VariantDsl
  fun <T> shouldAtLeastOneOf(values: Collection<T>?, block: QueryVariantDsl.(T) -> Unit) {
    values
      ?.takeUnless { it.isEmpty() }
      ?.distinct()
      ?.also { nonEmptyValues ->
        bool {
          should +
            {
              nonEmptyValues.forEach { value -> QueryVariantDsl { query -> +query }.block(value) }
            }
        }
      }
  }

  /**
   * creates
   * [Disjunction max query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-dis-max-query)
   */
  @VariantDsl
  fun disMax(disMax: DisMaxQuery.Builder.() -> Unit = {}, block: QueryVariantDsl.() -> Unit) {
    val queryVariants = mutableListOf<QueryVariant>()
    QueryVariantDsl { queryVariants += it }.apply(block)
    queryVariants
      .takeUnless { it.isEmpty() }
      ?.also { +DisMaxQuery.of { it.apply(disMax).queries(queryVariants.map(::Query)) } }
  }

  /**
   * creates
   * [Combined fields query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-combined-fields-query)
   */
  @VariantDsl
  fun Collection<Metamodel<*>>.combinedFields(
    value: String?,
    block: CombinedFieldsQuery.Builder.() -> Unit = {},
  ) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also {
        +CombinedFieldsQuery.of { it.fields(map(Metamodel<*>::path)).query(value).apply(block) }
      }
  }

  /** creates `Common terms query` */
  @VariantDsl
  fun Metamodel<*>.commonTerms(value: String?, block: CommonTermsQuery.Builder.() -> Unit = {}) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also { +CommonTermsQuery.of { it.field(path()).query(value).apply(block) } }
  }

  /**
   * creates
   * [Exists query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-exists-query)
   */
  @VariantDsl
  fun Metamodel<*>.exist() {
    +ExistsQuery.of { it.field(path()) }
  }

  /**
   * creates
   * [Fuzzy query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-fuzzy-query)
   */
  @VariantDsl infix fun Metamodel<*>.fuzzy(value: FieldValue?) = fuzzy(value) {}

  /**
   * creates
   * [Fuzzy query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-fuzzy-query)
   */
  @VariantDsl
  fun Metamodel<*>.fuzzy(value: FieldValue?, block: FuzzyQuery.Builder.() -> Unit = {}) {
    value?.also { +FuzzyQuery.of { it.field(path()).value(value).apply(block) } }
  }

  /**
   * creates
   * [Geo-distance query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-geo-distance-query)
   */
  @VariantDsl
  fun Metamodel<*>.geoDistance(
    latitude: Double,
    longitude: Double,
    distance: Double,
    unit: DistanceUnit = DistanceUnit.Kilometers,
    block: GeoDistanceQuery.Builder.() -> Unit = {},
  ) {
    +GeoDistanceQuery.of {
      it
        .field(path())
        .distance("${distance}${unit.jsonValue()}")
        .location { loc -> loc.latlon { ll -> ll.lat(latitude).lon(longitude) } }
        .apply(block)
    }
  }

  /**
   * creates
   * [IDs query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-ids-query)
   */
  @VariantDsl
  fun idsQuery(ids: List<String>?) {
    ids?.takeUnless { it.isEmpty() }?.also { +IdsQuery.of { it.values(ids) } }
  }

  /**
   * creates
   * [Match all query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-all-query)
   */
  @VariantDsl
  fun matchAll() {
    +MatchAllQuery.Builder().build()
  }

  /**
   * creates
   * [Match none query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-all-query#query-dsl-match-none-query)
   */
  @VariantDsl
  fun matchNone() {
    +MatchNoneQuery.Builder().build()
  }

  /**
   * creates
   * [Match phrase prefix query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query-phrase-prefix)
   */
  @VariantDsl infix fun Metamodel<*>.matchPhrasePrefix(value: String?) = matchPhrasePrefix(value) {}

  /**
   * creates
   * [Match phrase prefix query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query-phrase-prefix)
   */
  @VariantDsl
  fun Metamodel<*>.matchPhrasePrefix(
    value: String?,
    block: MatchPhrasePrefixQuery.Builder.() -> Unit = {},
  ) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also { +MatchPhrasePrefixQuery.of { it.field(path()).query(value).apply(block) } }
  }

  /**
   * creates
   * [Match phrase query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query-phrase)
   */
  @VariantDsl infix fun <T : Any> Metamodel<T>.matchPhrase(value: String?) = matchPhrase(value) {}

  /**
   * creates
   * [Match phrase query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query-phrase)
   */
  @VariantDsl
  fun Metamodel<*>.matchPhrase(value: String?, block: MatchPhraseQuery.Builder.() -> Unit = {}) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also { +MatchPhraseQuery.of { it.field(path()).query(value).apply(block) } }
  }

  // MATCH QUERIES - Infix operators

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<*>.match(value: FieldValue?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<String>.match(value: String?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<Int>.match(value: Int?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<Long>.match(value: Long?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<Float>.match(value: Float?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<Double>.match(value: Double?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun Metamodel<Boolean>.match(value: Boolean?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun <T : Enum<T>> Metamodel<T>.match(value: Enum<T>?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<Instant>.match(value: Instant?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<LocalDate>.match(value: LocalDate?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<LocalDateTime>.match(value: LocalDateTime?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<ZonedDateTime>.match(value: ZonedDateTime?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<OffsetDateTime>.match(value: OffsetDateTime?) = match(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl infix fun DateField<Date>.match(value: Date?) = match(value) {}

  // MATCH QUERIES - Full functions with block parameter

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<*>.match(value: FieldValue?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<String>.match(value: String?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<Int>.match(value: Int?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<Long>.match(value: Long?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<Float>.match(value: Float?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<Double>.match(value: Double?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<Boolean>.match(value: Boolean?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.match(value: Enum<T>?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<Instant>.match(value: Instant?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<LocalDate>.match(value: LocalDate?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<LocalDateTime>.match(
    value: LocalDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<ZonedDateTime>.match(
    value: ZonedDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<OffsetDateTime>.match(
    value: OffsetDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<Date>.match(value: Date?, block: MatchQuery.Builder.() -> Unit = {}) =
    matchUnchecked(value, block)

  private fun <T : Any> Metamodel<*>.matchUnchecked(
    value: T?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) {
    toFieldValue(value)?.also { fieldValue ->
      +MatchQuery.of { it.field(path()).query(fieldValue).apply(block) }
    }
  }

  // MATCH QUERIES FOR COLLECTIONS - Infix operators

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<*>>.containsMatch(value: FieldValue?) =
    matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<String>>.containsMatch(value: String?) =
    matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Int>>.containsMatch(value: Int?) = matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Long>>.containsMatch(value: Long?) = matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Float>>.containsMatch(value: Float?) = matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Double>>.containsMatch(value: Double?) =
    matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Boolean>>.containsMatch(value: Boolean?) =
    matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<out Collection<T>>.containsMatch(value: Enum<T>?) =
    matchUnchecked(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<Instant>>.containsMatch(value: Instant?) =
    containsMatch(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<LocalDate>>.containsMatch(value: LocalDate?) =
    containsMatch(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<LocalDateTime>>.containsMatch(value: LocalDateTime?) =
    containsMatch(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<ZonedDateTime>>.containsMatch(value: ZonedDateTime?) =
    containsMatch(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<OffsetDateTime>>.containsMatch(value: OffsetDateTime?) =
    containsMatch(value) {}

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<Date>>.containsMatch(value: Date?) = containsMatch(value) {}

  // MATCH QUERIES FOR COLLECTIONS - Full functions with block parameter

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<*>>.containsMatch(
    value: FieldValue?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<String>>.containsMatch(
    value: String?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsMatch(
    value: Int?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsMatch(
    value: Long?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsMatch(
    value: Float?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsMatch(
    value: Double?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsMatch(
    value: Boolean?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsMatch(
    value: Enum<T>?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<Instant>>.containsMatch(
    value: Instant?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsMatch(
    value: LocalDate?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsMatch(
    value: LocalDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsMatch(
    value: ZonedDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsMatch(
    value: OffsetDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [Match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-match-query)
   */
  @VariantDsl
  fun DateField<out Collection<Date>>.containsMatch(
    value: Date?,
    block: MatchQuery.Builder.() -> Unit = {},
  ) = matchUnchecked(value, block)

  /**
   * creates
   * [More like this query](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-specialized-queries.html)
   */
  @VariantDsl
  fun Collection<Metamodel<*>>.moreLikeThis(block: MoreLikeThisQuery.Builder.() -> Unit = {}) {
    +MoreLikeThisQuery.of { it.fields(map(Metamodel<*>::path)).apply(block) }
  }

  /**
   * creates
   * [Multi-match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-multi-match-query)
   */
  @VariantDsl infix fun Collection<Metamodel<*>>.multiMatch(value: String?) = multiMatch(value) {}

  /**
   * creates
   * [Multi-match query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-multi-match-query)
   */
  @VariantDsl
  fun Collection<Metamodel<*>>.multiMatch(
    value: String?,
    block: MultiMatchQuery.Builder.() -> Unit = {},
  ) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also {
        +MultiMatchQuery.of { it.fields(map(Metamodel<*>::path)).query(value).apply(block) }
      }
  }

  /**
   * creates
   * [Nested query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-nested-query)
   */
  @VariantDsl
  fun Container<*>.nested(
    setupBlock: NestedQuery.Builder.() -> Unit = {},
    block: BoolQueryDsl.() -> Unit,
  ) {
    val boolQuery = BoolQuery.Builder().apply { BoolQueryDsl(this).apply(block) }.build()
    if (!boolQuery.isEmpty()) {
      if (isNested()) {
        +NestedQuery.of { it.path(path()).query(Query(boolQuery)).apply(setupBlock) }
      } else {
        logger.warn {
          "Nested query used on non-nested field '${path()}'. " +
            "The field should be marked with @Field(type = FieldType.Nested) in the Elasticsearch mapping. " +
            "The query will be applied as a regular bool query instead."
        }
        +boolQuery
      }
    }
  }

  /**
   * creates
   * [Prefix query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-prefix-query)
   */
  @VariantDsl infix fun Metamodel<*>.prefix(value: String?) = prefix(value) {}

  /**
   * creates
   * [Prefix query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-prefix-query)
   */
  @VariantDsl
  fun Metamodel<*>.prefix(value: String?, block: PrefixQuery.Builder.() -> Unit = {}) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also { +PrefixQuery.of { it.field(path()).value(value).apply(block) } }
  }

  /**
   * creates
   * [Regexp query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-regexp-query)
   */
  @VariantDsl
  fun Metamodel<*>.regexp(block: RegexpQuery.Builder.() -> Unit = {}) {
    +RegexpQuery.of { it.field(path()).apply(block) }
  }

  // TERM QUERIES

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<*>.term(value: FieldValue?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<String>.term(value: String?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<Int>.term(value: Int?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<Long>.term(value: Long?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<Float>.term(value: Float?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<Double>.term(value: Double?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<Instant>.term(value: Instant?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<LocalDate>.term(value: LocalDate?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<LocalDateTime>.term(value: LocalDateTime?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<ZonedDateTime>.term(value: ZonedDateTime?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<OffsetDateTime>.term(value: OffsetDateTime?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun DateField<Date>.term(value: Date?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun Metamodel<Boolean>.term(value: Boolean?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl infix fun <T : Enum<T>> Metamodel<T>.term(value: Enum<T>?) = term(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<*>.term(value: FieldValue?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<String>.term(value: String?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<Int>.term(value: Int?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<Long>.term(value: Long?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<Float>.term(value: Float?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<Double>.term(value: Double?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<Instant>.term(value: Instant?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<LocalDate>.term(value: LocalDate?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<LocalDateTime>.term(
    value: LocalDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<ZonedDateTime>.term(
    value: ZonedDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<OffsetDateTime>.term(
    value: OffsetDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<Date>.term(value: Date?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<Boolean>.term(value: Boolean?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.term(value: Enum<T>?, block: TermQuery.Builder.() -> Unit = {}) =
    termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<*>>.containsTerm(value: FieldValue?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<String>>.containsTerm(value: String?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Int>>.containsTerm(value: Int?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Long>>.containsTerm(value: Long?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Float>>.containsTerm(value: Float?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Double>>.containsTerm(value: Double?) = termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<Boolean>>.containsTerm(value: Boolean?) =
    termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerm(value: Enum<T>?) =
    termUnchecked(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<Instant>>.containsTerm(value: Instant?) =
    containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<LocalDate>>.containsTerm(value: LocalDate?) =
    containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<LocalDateTime>>.containsTerm(value: LocalDateTime?) =
    containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<ZonedDateTime>>.containsTerm(value: ZonedDateTime?) =
    containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<OffsetDateTime>>.containsTerm(value: OffsetDateTime?) =
    containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  infix fun DateField<out Collection<Date>>.containsTerm(value: Date?) = containsTerm(value) {}

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<*>>.containsTerm(
    value: FieldValue?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerm(
    value: String?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerm(
    value: Int?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerm(
    value: Long?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerm(
    value: Float?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerm(
    value: Double?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerm(
    value: Boolean?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerm(
    value: Enum<T>?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerm(
    value: Instant?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerm(
    value: LocalDate?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerm(
    value: LocalDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerm(
    value: ZonedDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerm(
    value: OffsetDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  /**
   * creates
   * [Term query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-term-query)
   */
  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerm(
    value: Date?,
    block: TermQuery.Builder.() -> Unit = {},
  ) = termUnchecked(value, block)

  private fun <T : Any> Metamodel<*>.termUnchecked(
    value: T?,
    block: TermQuery.Builder.() -> Unit = {},
  ) {
    toFieldValue(value)?.also { fieldValue ->
      +TermQuery.of { it.field(path()).value(fieldValue).apply(block) }
    }
  }

  // TERMS QUERIES - Vararg overloads

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun Metamodel<String>.terms(vararg terms: String) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<String>.terms(vararg terms: String, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun Metamodel<Int>.terms(vararg terms: Int) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<Int>.terms(vararg terms: Int, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun Metamodel<Long>.terms(vararg terms: Long) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<Long>.terms(vararg terms: Long, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun Metamodel<Float>.terms(vararg terms: Float) = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Float>.terms(vararg terms: Float, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun Metamodel<Double>.terms(vararg terms: Double) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<Double>.terms(vararg terms: Double, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<Boolean>.terms(vararg terms: Boolean) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<Boolean>.terms(vararg terms: Boolean, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.terms(vararg terms: T) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.terms(vararg terms: T, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<Instant>.terms(vararg terms: Instant) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<Instant>.terms(vararg terms: Instant, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<LocalDate>.terms(vararg terms: LocalDate) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<LocalDate>.terms(
    vararg terms: LocalDate,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<LocalDateTime>.terms(vararg terms: LocalDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<LocalDateTime>.terms(
    vararg terms: LocalDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<ZonedDateTime>.terms(vararg terms: ZonedDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<ZonedDateTime>.terms(
    vararg terms: ZonedDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<OffsetDateTime>.terms(vararg terms: OffsetDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<OffsetDateTime>.terms(
    vararg terms: OffsetDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl fun DateField<Date>.terms(vararg terms: Date) = termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<Date>.terms(vararg terms: Date, block: TermsQuery.Builder.() -> Unit = {}) =
    termsUnchecked(terms.toList(), block)

  // TERMS QUERIES - Collection overloads (fallback)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  infix fun Metamodel<*>.terms(terms: Collection<FieldValue>?) = termsUnchecked(terms) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<*>.terms(
    terms: Collection<FieldValue>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms, block)

  // CONTAINS TERMS QUERIES - Vararg overloads

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerms(vararg terms: String) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerms(
    vararg terms: String,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerms(vararg terms: Int) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerms(
    vararg terms: Int,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerms(vararg terms: Long) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerms(
    vararg terms: Long,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerms(vararg terms: Float) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerms(
    vararg terms: Float,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerms(vararg terms: Double) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerms(
    vararg terms: Double,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerms(vararg terms: Boolean) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerms(
    vararg terms: Boolean,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerms(vararg terms: T) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerms(
    vararg terms: T,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerms(vararg terms: Instant) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerms(
    vararg terms: Instant,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerms(vararg terms: LocalDate) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerms(
    vararg terms: LocalDate,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerms(vararg terms: LocalDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerms(
    vararg terms: LocalDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerms(vararg terms: ZonedDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerms(
    vararg terms: ZonedDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerms(vararg terms: OffsetDateTime) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerms(
    vararg terms: OffsetDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerms(vararg terms: Date) =
    termsUnchecked(terms.toList()) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerms(
    vararg terms: Date,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms.toList(), block)

  // CONTAINS TERMS QUERIES - Collection overloads (fallback)

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  infix fun Metamodel<out Collection<*>>.containsTerms(terms: Collection<FieldValue>?) =
    termsUnchecked(terms) {}

  /**
   * creates
   * [Terms query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-query)
   */
  @VariantDsl
  fun Metamodel<out Collection<*>>.containsTerms(
    terms: Collection<FieldValue>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ) = termsUnchecked(terms, block)

  private fun <T : Any> Metamodel<*>.termsUnchecked(
    terms: Collection<T>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ) {
    terms
      ?.takeUnless { it.isEmpty() }
      ?.also {
        +TermsQuery.of {
          it
            .field(path())
            .terms { tb -> tb.value(terms.mapNotNull { term -> toFieldValue(term) }) }
            .apply(block)
        }
      }
  }

  /**
   * creates
   * [Terms set query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-set-query)
   */
  @VariantDsl infix fun Metamodel<*>.termsSet(terms: Collection<String>?) = termsSet(terms) {}

  /**
   * creates
   * [Terms set query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-terms-set-query)
   */
  @VariantDsl
  fun Metamodel<*>.termsSet(
    terms: Collection<String>?,
    block: TermsSetQuery.Builder.() -> Unit = {},
  ) {
    terms
      ?.takeUnless { it.isEmpty() }
      ?.also { +TermsSetQuery.of { it.field(path()).terms(terms.toList()).apply(block) } }
  }

  /**
   * creates
   * [Wildcard query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-wildcard-query)
   */
  @VariantDsl infix fun Metamodel<*>.wildCard(value: String?) = wildCard(value) {}

  /**
   * creates
   * [Wildcard query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-wildcard-query)
   */
  @VariantDsl
  fun Metamodel<*>.wildCard(value: String?, block: WildcardQuery.Builder.() -> Unit = {}) {
    value
      .takeUnless { it.isNullOrBlank() }
      ?.also { +WildcardQuery.of { it.field(path()).value(value).apply(block) } }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   *
   * When [this] is not null:
   *
   * Adds a range query to ensure the field [from] is less than or equal to [this] and the field
   * [to] is greater than or equal to [this]
   */
  fun <T : Comparable<T>> T?.mustBeBetween(from: Metamodel<T>, to: Metamodel<T>) {
    this?.also {
      +from.toRangeQuery(Range.atMost(this))
      +to.toRangeQuery(Range.atLeast(this))
    }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   */
  @VariantDsl
  infix fun <T : Any> Metamodel<T>.range(range: Range<out Comparable<T>>?) {
    when (range) {
      null -> Unit
      range if (!range.hasLowerBound() && !range.hasUpperBound()) -> +MatchNoneQuery.of { it }
      else -> +toRangeQuery(range)
    }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   */
  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.greaterThanEqualTo(value: T?) {
    value?.also { range(Range.atLeast(it)) }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   */
  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.greaterThan(value: T?) {
    value?.also { range(Range.greaterThan(it)) }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   */
  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.lowerThanEqualTo(value: T?) {
    value?.also { range(Range.atMost(it)) }
  }

  /**
   * creates
   * [Range query](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query)
   */
  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.lowerThan(value: T?) {
    value?.also { range(Range.lessThan(it)) }
  }
}

private fun <T> Metamodel<*>.toRangeQuery(range: Range<out Comparable<T>>) =
  RangeQuery.of { rangeQuery ->
    rangeQuery.field(path())
    if (range.hasLowerBound()) {
      val operator = range.toLowerOperator()
      rangeQuery.withBound(range.lowerEndpoint(), operator)
    }
    if (range.hasUpperBound()) {
      val operator = range.toUpperOperator()
      rangeQuery.withBound(range.upperEndpoint(), operator)
    }
    rangeQuery
  }

private fun Range<*>.toLowerOperator(): (RangeQuery.Builder, JsonData) -> RangeQuery.Builder =
  when (lowerBoundType()) {
    BoundType.CLOSED -> RangeQuery.Builder::gte
    BoundType.OPEN -> RangeQuery.Builder::gt
  }

private fun Range<*>.toUpperOperator(): (RangeQuery.Builder, JsonData) -> RangeQuery.Builder =
  when (upperBoundType()) {
    BoundType.CLOSED -> RangeQuery.Builder::lte
    BoundType.OPEN -> RangeQuery.Builder::lt
  }

private fun <T> RangeQuery.Builder.withBound(
  value: Comparable<T>,
  operator: (RangeQuery.Builder, JsonData) -> RangeQuery.Builder,
) {
  toJsonData(value)?.let { (jsonData, dateFormat) ->
    operator.invoke(this, jsonData)
    dateFormat?.also { format(dateFormat.pattern) }
  }
}

private fun BoolQuery.isEmpty(): Boolean =
  must().isEmpty() && mustNot().isEmpty() && should().isEmpty() && filter().isEmpty()
