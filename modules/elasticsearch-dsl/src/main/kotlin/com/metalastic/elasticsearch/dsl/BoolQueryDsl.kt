package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

/** Creates a BoolQueryDsl context for building bool queries */
fun BoolQuery.Builder.boolQueryDsl(block: BoolQueryDsl.() -> Unit) {
  BoolQueryDsl(this).apply(block)
}

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

  fun mustDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.must(Query(query)) }).apply(block)
  }

  fun mustNotDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.mustNot(Query(query)) }).apply(block)
  }

  fun shouldDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.should(Query(query)) }).apply(block)
  }

  fun filterDsl(block: QueryVariantDsl.() -> Unit) {
    QueryVariantDsl({ query -> builder.filter(Query(query)) }).apply(block)
  }

  operator fun Must.plus(block: QueryVariantDsl.() -> Unit) = mustDsl(block)

  operator fun MustNot.plus(block: QueryVariantDsl.() -> Unit) = mustNotDsl(block)

  operator fun Should.plus(block: QueryVariantDsl.() -> Unit) = shouldDsl(block)

  operator fun Filter.plus(block: QueryVariantDsl.() -> Unit) = filterDsl(block)
}
