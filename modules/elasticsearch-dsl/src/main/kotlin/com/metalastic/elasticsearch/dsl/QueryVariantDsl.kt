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
import com.metalastic.core.DateField
import com.metalastic.core.Metamodel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.Temporal
import java.util.Date
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf
import org.springframework.data.elasticsearch.annotations.DateFormat

@Suppress("TooManyFunctions")
@ElasticsearchDsl
class QueryVariantDsl(private val add: (queryVariant: QueryVariant) -> Unit) {

  operator fun <T : QueryVariant> T.unaryPlus(): T {
    add(this)
    return this
  }

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
              nonEmptyValues.forEach { value -> QueryVariantDsl({ query -> +query }).block(value) }
            }
        }
      }
  }

  @VariantDsl
  fun disMax(
    disMax: DisMaxQuery.Builder.() -> Unit = {},
    block: QueryVariantDsl.() -> Unit,
  ): DisMaxQuery? {
    val queryVariants = mutableListOf<QueryVariant>()
    QueryVariantDsl({ queryVariants += it }).apply(block)
    return queryVariants
      .takeUnless { it.isEmpty() }
      ?.let { +DisMaxQuery.of { it.apply(disMax).queries(queryVariants.map(::Query)) } }
  }

  @VariantDsl
  fun Collection<Metamodel<*>>.combinedFields(
    value: String?,
    block: CombinedFieldsQuery.Builder.() -> Unit = {},
  ) {
    value?.also {
      +CombinedFieldsQuery.of { it.fields(map(Metamodel<*>::path)).query(value).apply(block) }
    }
  }

  @VariantDsl
  fun Metamodel<*>.commonTerms(value: String?, block: CommonTermsQuery.Builder.() -> Unit = {}) {
    value?.also { +CommonTermsQuery.of { it.field(path()).query(value).apply(block) } }
  }

  @VariantDsl fun Metamodel<*>.exist(): ExistsQuery = +ExistsQuery.of { it.field(path()) }

  @VariantDsl infix fun Metamodel<*>.fuzzy(value: FieldValue?): FuzzyQuery? = fuzzy(value) {}

  @VariantDsl
  fun Metamodel<*>.fuzzy(
    value: FieldValue?,
    block: FuzzyQuery.Builder.() -> Unit = {},
  ): FuzzyQuery? = value?.let { +FuzzyQuery.of { it.field(path()).value(value).apply(block) } }

  @VariantDsl
  fun Metamodel<*>.geoDistance(
    latitude: Double,
    longitude: Double,
    distance: Double,
    unit: DistanceUnit = DistanceUnit.Kilometers,
    block: GeoDistanceQuery.Builder.() -> Unit = {},
  ): GeoDistanceQuery =
    +GeoDistanceQuery.of {
      it
        .field(path())
        .distance("${distance}${unit.jsonValue()}")
        .location { loc -> loc.latlon { ll -> ll.lat(latitude).lon(longitude) } }
        .apply(block)
    }

  @VariantDsl
  fun idsQuery(ids: List<String>?): IdsQuery? =
    ids?.takeUnless { it.isEmpty() }?.let { +IdsQuery.of { it.values(ids) } }

  @VariantDsl fun matchAll(): MatchAllQuery = +MatchAllQuery.Builder().build()

  @VariantDsl fun matchNone(): MatchNoneQuery = +MatchNoneQuery.Builder().build()

  @VariantDsl
  infix fun Metamodel<*>.matchPhrasePrefix(value: String?): MatchPhrasePrefixQuery? =
    matchPhrasePrefix(value) {}

  @VariantDsl
  fun Metamodel<*>.matchPhrasePrefix(
    value: String?,
    block: MatchPhrasePrefixQuery.Builder.() -> Unit = {},
  ): MatchPhrasePrefixQuery? =
    value?.let { +MatchPhrasePrefixQuery.of { it.field(path()).query(value).apply(block) } }

  @VariantDsl infix fun <T : Any> Metamodel<T>.matchPhrase(value: String?) = matchPhrase(value) {}

  @VariantDsl
  fun Metamodel<*>.matchPhrase(
    value: String?,
    block: MatchPhraseQuery.Builder.() -> Unit = {},
  ): MatchPhraseQuery? =
    value?.let { +MatchPhraseQuery.of { it.field(path()).query(value).apply(block) } }

  // MATCH QUERIES - Infix operators

  @VariantDsl infix fun Metamodel<*>.match(value: FieldValue?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<String>.match(value: String?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<Int>.match(value: Int?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<Long>.match(value: Long?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<Float>.match(value: Float?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<Double>.match(value: Double?): MatchQuery? = match(value) {}

  @VariantDsl infix fun Metamodel<Boolean>.match(value: Boolean?): MatchQuery? = match(value) {}

  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<T>.match(value: Enum<T>?): MatchQuery? = match(value) {}

  @VariantDsl infix fun DateField<Instant>.match(value: Instant?): MatchQuery? = match(value) {}

  @VariantDsl infix fun DateField<LocalDate>.match(value: LocalDate?): MatchQuery? = match(value) {}

  @VariantDsl
  infix fun DateField<LocalDateTime>.match(value: LocalDateTime?): MatchQuery? = match(value) {}

  @VariantDsl
  infix fun DateField<ZonedDateTime>.match(value: ZonedDateTime?): MatchQuery? = match(value) {}

  @VariantDsl
  infix fun DateField<OffsetDateTime>.match(value: OffsetDateTime?): MatchQuery? = match(value) {}

  @VariantDsl infix fun DateField<Date>.match(value: Date?): MatchQuery? = match(value) {}

  // MATCH QUERIES - Full functions with block parameter

  @VariantDsl
  fun Metamodel<*>.match(
    value: FieldValue?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<String>.match(
    value: String?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Int>.match(value: Int?, block: MatchQuery.Builder.() -> Unit = {}): MatchQuery? =
    matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Long>.match(value: Long?, block: MatchQuery.Builder.() -> Unit = {}): MatchQuery? =
    matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Float>.match(
    value: Float?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Double>.match(
    value: Double?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Boolean>.match(
    value: Boolean?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.match(
    value: Enum<T>?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<Instant>.match(
    value: Instant?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<LocalDate>.match(
    value: LocalDate?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<LocalDateTime>.match(
    value: LocalDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<ZonedDateTime>.match(
    value: ZonedDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<OffsetDateTime>.match(
    value: OffsetDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<Date>.match(value: Date?, block: MatchQuery.Builder.() -> Unit = {}): MatchQuery? =
    matchUnchecked(value, block)

  private fun <T : Any> Metamodel<*>.matchUnchecked(
    value: T?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? =
    toFieldValue(value)?.let { fieldValue ->
      +MatchQuery.of { it.field(path()).query(fieldValue).apply(block) }
    }

  // MATCH QUERIES FOR COLLECTIONS - Infix operators

  @VariantDsl
  infix fun Metamodel<out Collection<*>>.containsMatch(value: FieldValue?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<String>>.containsMatch(value: String?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Int>>.containsMatch(value: Int?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Long>>.containsMatch(value: Long?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Float>>.containsMatch(value: Float?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Double>>.containsMatch(value: Double?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Boolean>>.containsMatch(value: Boolean?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<out Collection<T>>.containsMatch(value: Enum<T>?): MatchQuery? =
    matchUnchecked(value) {}

  @VariantDsl
  infix fun DateField<out Collection<Instant>>.containsMatch(value: Instant?): MatchQuery? =
    containsMatch(value) {}

  @VariantDsl
  infix fun DateField<out Collection<LocalDate>>.containsMatch(value: LocalDate?): MatchQuery? =
    containsMatch(value) {}

  @VariantDsl
  infix fun DateField<out Collection<LocalDateTime>>.containsMatch(
    value: LocalDateTime?
  ): MatchQuery? = containsMatch(value) {}

  @VariantDsl
  infix fun DateField<out Collection<ZonedDateTime>>.containsMatch(
    value: ZonedDateTime?
  ): MatchQuery? = containsMatch(value) {}

  @VariantDsl
  infix fun DateField<out Collection<OffsetDateTime>>.containsMatch(
    value: OffsetDateTime?
  ): MatchQuery? = containsMatch(value) {}

  @VariantDsl
  infix fun DateField<out Collection<Date>>.containsMatch(value: Date?): MatchQuery? =
    containsMatch(value) {}

  // MATCH QUERIES FOR COLLECTIONS - Full functions with block parameter

  @VariantDsl
  fun Metamodel<out Collection<*>>.containsMatch(
    value: FieldValue?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<String>>.containsMatch(
    value: String?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsMatch(
    value: Int?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsMatch(
    value: Long?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsMatch(
    value: Float?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsMatch(
    value: Double?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsMatch(
    value: Boolean?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsMatch(
    value: Enum<T>?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<Instant>>.containsMatch(
    value: Instant?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsMatch(
    value: LocalDate?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsMatch(
    value: LocalDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsMatch(
    value: ZonedDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsMatch(
    value: OffsetDateTime?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<Date>>.containsMatch(
    value: Date?,
    block: MatchQuery.Builder.() -> Unit = {},
  ): MatchQuery? = matchUnchecked(value, block)

  @VariantDsl
  fun Collection<Metamodel<*>>.moreLikeThis(
    block: MoreLikeThisQuery.Builder.() -> Unit = {}
  ): MoreLikeThisQuery = +MoreLikeThisQuery.of { it.fields(map(Metamodel<*>::path)).apply(block) }

  @VariantDsl
  infix fun Collection<Metamodel<*>>.multiMatch(value: String?): MultiMatchQuery? =
    multiMatch(value) {}

  @VariantDsl
  fun Collection<Metamodel<*>>.multiMatch(
    value: String?,
    block: MultiMatchQuery.Builder.() -> Unit = {},
  ): MultiMatchQuery? =
    value?.let {
      +MultiMatchQuery.of { it.fields(map(Metamodel<*>::path)).query(value).apply(block) }
    }

  @VariantDsl
  fun Metamodel<*>.nested(
    setupBlock: NestedQuery.Builder.() -> Unit = {},
    block: BoolQueryDsl.() -> Unit,
  ) {
    val boolQuery = BoolQuery.Builder().apply { BoolQueryDsl(this).apply(block) }.build()
    if (!boolQuery.isEmpty()) {
      +NestedQuery.of { it.path(path()).query(Query(boolQuery)).apply(setupBlock) }
    }
  }

  @VariantDsl infix fun Metamodel<*>.prefix(value: String?): PrefixQuery? = prefix(value) {}

  @VariantDsl
  fun Metamodel<*>.prefix(
    value: String?,
    block: PrefixQuery.Builder.() -> Unit = {},
  ): PrefixQuery? = value?.let { +PrefixQuery.of { it.field(path()).value(value).apply(block) } }

  @VariantDsl
  fun Metamodel<*>.regexp(block: RegexpQuery.Builder.() -> Unit = {}): RegexpQuery =
    +RegexpQuery.of { it.field(path()).apply(block) }

  // TERM QUERIES

  @VariantDsl infix fun Metamodel<*>.term(value: FieldValue?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<String>.term(value: String?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<Int>.term(value: Int?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<Long>.term(value: Long?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<Float>.term(value: Float?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<Double>.term(value: Double?): TermQuery? = term(value) {}

  @VariantDsl infix fun DateField<Instant>.term(value: Instant?): TermQuery? = term(value) {}

  @VariantDsl infix fun DateField<LocalDate>.term(value: LocalDate?): TermQuery? = term(value) {}

  @VariantDsl
  infix fun DateField<LocalDateTime>.term(value: LocalDateTime?): TermQuery? = term(value) {}

  @VariantDsl
  infix fun DateField<ZonedDateTime>.term(value: ZonedDateTime?): TermQuery? = term(value) {}

  @VariantDsl
  infix fun DateField<OffsetDateTime>.term(value: OffsetDateTime?): TermQuery? = term(value) {}

  @VariantDsl infix fun DateField<Date>.term(value: Date?): TermQuery? = term(value) {}

  @VariantDsl infix fun Metamodel<Boolean>.term(value: Boolean?): TermQuery? = term(value) {}

  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<T>.term(value: Enum<T>?): TermQuery? = term(value) {}

  @VariantDsl
  fun Metamodel<*>.term(value: FieldValue?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<String>.term(value: String?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Int>.term(value: Int?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Long>.term(value: Long?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Float>.term(value: Float?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Double>.term(value: Double?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun DateField<Instant>.term(
    value: Instant?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<LocalDate>.term(
    value: LocalDate?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<LocalDateTime>.term(
    value: LocalDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<ZonedDateTime>.term(
    value: ZonedDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<OffsetDateTime>.term(
    value: OffsetDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<Date>.term(value: Date?, block: TermQuery.Builder.() -> Unit = {}): TermQuery? =
    termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<Boolean>.term(
    value: Boolean?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.term(
    value: Enum<T>?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  infix fun <T : Any> Metamodel<out Collection<*>>.containsTerm(value: FieldValue?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<String>>.containsTerm(value: String?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Int>>.containsTerm(value: Int?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Long>>.containsTerm(value: Long?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Float>>.containsTerm(value: Float?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Double>>.containsTerm(value: Double?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun Metamodel<out Collection<Boolean>>.containsTerm(value: Boolean?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerm(value: Enum<T>?): TermQuery? =
    termUnchecked(value) {}

  @VariantDsl
  infix fun DateField<out Collection<Instant>>.containsTerm(value: Instant?): TermQuery? =
    containsTerm(value) {}

  @VariantDsl
  infix fun DateField<out Collection<LocalDate>>.containsTerm(value: LocalDate?): TermQuery? =
    containsTerm(value) {}

  @VariantDsl
  infix fun DateField<out Collection<LocalDateTime>>.containsTerm(
    value: LocalDateTime?
  ): TermQuery? = containsTerm(value) {}

  @VariantDsl
  infix fun DateField<out Collection<ZonedDateTime>>.containsTerm(
    value: ZonedDateTime?
  ): TermQuery? = containsTerm(value) {}

  @VariantDsl
  infix fun DateField<out Collection<OffsetDateTime>>.containsTerm(
    value: OffsetDateTime?
  ): TermQuery? = containsTerm(value) {}

  @VariantDsl
  infix fun DateField<out Collection<Date>>.containsTerm(value: Date?): TermQuery? =
    containsTerm(value) {}

  @VariantDsl
  fun <T : Any> Metamodel<out Collection<*>>.containsTerm(
    value: FieldValue?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerm(
    value: String?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerm(
    value: Int?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerm(
    value: Long?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerm(
    value: Float?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerm(
    value: Double?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerm(
    value: Boolean?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerm(
    value: Enum<T>?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerm(
    value: Instant?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerm(
    value: LocalDate?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerm(
    value: LocalDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerm(
    value: ZonedDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerm(
    value: OffsetDateTime?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerm(
    value: Date?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? = termUnchecked(value, block)

  private fun <T : Any> Metamodel<*>.termUnchecked(
    value: T?,
    block: TermQuery.Builder.() -> Unit = {},
  ): TermQuery? =
    toFieldValue(value)?.let { fieldValue ->
      +TermQuery.of { it.field(path()).value(fieldValue).apply(block) }
    }

  // TERMS QUERIES - Vararg overloads

  @VariantDsl
  fun Metamodel<String>.terms(vararg terms: String): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<String>.terms(
    vararg terms: String,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<Int>.terms(vararg terms: Int): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Int>.terms(
    vararg terms: Int,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<Long>.terms(vararg terms: Long): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Long>.terms(
    vararg terms: Long,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<Float>.terms(vararg terms: Float): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Float>.terms(
    vararg terms: Float,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<Double>.terms(vararg terms: Double): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Double>.terms(
    vararg terms: Double,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<Boolean>.terms(vararg terms: Boolean): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<Boolean>.terms(
    vararg terms: Boolean,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.terms(vararg terms: T): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun <T : Enum<T>> Metamodel<T>.terms(
    vararg terms: T,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<Instant>.terms(vararg terms: Instant): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<Instant>.terms(
    vararg terms: Instant,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<LocalDate>.terms(vararg terms: LocalDate): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<LocalDate>.terms(
    vararg terms: LocalDate,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<LocalDateTime>.terms(vararg terms: LocalDateTime): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<LocalDateTime>.terms(
    vararg terms: LocalDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<ZonedDateTime>.terms(vararg terms: ZonedDateTime): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<ZonedDateTime>.terms(
    vararg terms: ZonedDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<OffsetDateTime>.terms(vararg terms: OffsetDateTime): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<OffsetDateTime>.terms(
    vararg terms: OffsetDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<Date>.terms(vararg terms: Date): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<Date>.terms(
    vararg terms: Date,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  // TERMS QUERIES - Collection overloads (fallback)

  @VariantDsl
  infix fun Metamodel<*>.terms(terms: Collection<FieldValue>?): TermsQuery? =
    termsUnchecked(terms) {}

  @VariantDsl
  fun Metamodel<*>.terms(
    terms: Collection<FieldValue>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms, block)

  // CONTAINS TERMS QUERIES - Vararg overloads

  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerms(vararg terms: String): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<String>>.containsTerms(
    vararg terms: String,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerms(vararg terms: Int): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<Int>>.containsTerms(
    vararg terms: Int,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerms(vararg terms: Long): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<Long>>.containsTerms(
    vararg terms: Long,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerms(vararg terms: Float): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<Float>>.containsTerms(
    vararg terms: Float,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerms(vararg terms: Double): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<Double>>.containsTerms(
    vararg terms: Double,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerms(vararg terms: Boolean): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun Metamodel<out Collection<Boolean>>.containsTerms(
    vararg terms: Boolean,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerms(vararg terms: T): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun <T : Enum<T>> Metamodel<out Collection<T>>.containsTerms(
    vararg terms: T,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerms(vararg terms: Instant): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<Instant>>.containsTerms(
    vararg terms: Instant,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerms(vararg terms: LocalDate): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<LocalDate>>.containsTerms(
    vararg terms: LocalDate,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerms(
    vararg terms: LocalDateTime
  ): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<LocalDateTime>>.containsTerms(
    vararg terms: LocalDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerms(
    vararg terms: ZonedDateTime
  ): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<ZonedDateTime>>.containsTerms(
    vararg terms: ZonedDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerms(
    vararg terms: OffsetDateTime
  ): TermsQuery? = termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<OffsetDateTime>>.containsTerms(
    vararg terms: OffsetDateTime,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerms(vararg terms: Date): TermsQuery? =
    termsUnchecked(terms.toList()) {}

  @VariantDsl
  fun DateField<out Collection<Date>>.containsTerms(
    vararg terms: Date,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms.toList(), block)

  // CONTAINS TERMS QUERIES - Collection overloads (fallback)

  @VariantDsl
  infix fun Metamodel<out Collection<*>>.containsTerms(
    terms: Collection<FieldValue>?
  ): TermsQuery? = termsUnchecked(terms) {}

  @VariantDsl
  fun Metamodel<out Collection<*>>.containsTerms(
    terms: Collection<FieldValue>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? = termsUnchecked(terms, block)

  private fun <T : Any> Metamodel<*>.termsUnchecked(
    terms: Collection<T>?,
    block: TermsQuery.Builder.() -> Unit = {},
  ): TermsQuery? =
    terms
      ?.takeUnless { it.isEmpty() }
      ?.let {
        +TermsQuery.of {
          it
            .field(path())
            .terms { tb -> tb.value(terms.mapNotNull { term -> toFieldValue(term) }) }
            .apply(block)
        }
      }

  @VariantDsl
  infix fun Metamodel<*>.termsSet(terms: Collection<String>?): TermsSetQuery? = termsSet(terms) {}

  @VariantDsl
  fun Metamodel<*>.termsSet(
    terms: Collection<String>?,
    block: TermsSetQuery.Builder.() -> Unit = {},
  ): TermsSetQuery? =
    terms
      ?.takeUnless { it.isEmpty() }
      ?.let { +TermsSetQuery.of { it.field(path()).terms(terms.toList()).apply(block) } }

  @VariantDsl infix fun Metamodel<*>.wildCard(value: String?): WildcardQuery? = wildCard(value) {}

  @VariantDsl
  fun Metamodel<*>.wildCard(
    value: String?,
    block: WildcardQuery.Builder.() -> Unit = {},
  ): WildcardQuery? =
    value?.let { +WildcardQuery.of { it.field(path()).value(value).apply(block) } }

  /**
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

  @VariantDsl
  infix fun <T : Any> Metamodel<T>.range(range: Range<out Comparable<T>>?): QueryVariant? =
    when (range) {
      null -> null
      range if (!range.hasLowerBound() && !range.hasUpperBound()) -> +MatchNoneQuery.of { it }
      else -> +toRangeQuery(range)
    }

  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.greaterThanEqualTo(value: T?): QueryVariant? =
    value?.let { range(Range.atLeast(it)) }

  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.greaterThan(value: T?): QueryVariant? =
    value?.let { range(Range.greaterThan(it)) }

  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.lowerThanEqualTo(value: T?): QueryVariant? =
    value?.let { range(Range.atMost(it)) }

  @VariantDsl
  infix fun <T : Comparable<T>> Metamodel<T>.lowerThan(value: T?): QueryVariant? =
    value?.let { range(Range.lessThan(it)) }
}

private fun <T> Metamodel<*>.toRangeQuery(range: Range<out Comparable<T>>) =
  RangeQuery.of { rangeQuery ->
    rangeQuery.field(path())
    if (range.hasLowerBound()) {
      val operator = range.toLowerOperator()
      rangeQuery.withBound(range.lowerEndpoint(), this@toRangeQuery, operator)
    }
    if (range.hasUpperBound()) {
      val operator = range.toUpperOperator()
      rangeQuery.withBound(range.upperEndpoint(), this@toRangeQuery, operator)
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
  field: Metamodel<*>,
  operator: (RangeQuery.Builder, JsonData) -> RangeQuery.Builder,
) {
  if (field.fieldType().isSubtypeOf(typeOf<Temporal>())) {
    (value as? Temporal).toJsonDataDate()?.also { json ->
      operator.invoke(this, json)
      // TODO(any): the use of format will be removable when we upgrade to Elasticsearch 8.x
      format(DateFormat.epoch_millis.pattern)
    }
  } else {
    value.toJsonData()?.also { jsonData -> operator.invoke(this, jsonData) }
  }
}

private fun <T> T?.toJsonData() = this?.let(JsonData::of)

private fun BoolQuery.isEmpty(): Boolean =
  must().isEmpty() && mustNot().isEmpty() && should().isEmpty() && filter().isEmpty()
