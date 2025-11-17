/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl.utils

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonpUtils
import io.kotest.matchers.shouldBe

/** Common test utilities for elasticsearch-dsl tests */

/** Converts a Query to its JSON string representation */
fun Query.toJsonString(): String =
  JsonpUtils.toString(this).removePrefix("${Query::class.simpleName}:")

/** Converts a BoolQuery to its JSON string representation */
fun BoolQuery.toJsonString(): String =
  JsonpUtils.toString(this).removePrefix("${BoolQuery::class.simpleName}:")

/** Helper to create a Query from a BoolQuery */
fun BoolQuery.toQuery(): Query = Query(this)

/** Asserts that a BoolQuery has the expected structure */
fun BoolQuery.shouldHaveStructure(
  mustCount: Int = 0,
  mustNotCount: Int = 0,
  shouldCount: Int = 0,
  filterCount: Int = 0,
) {
  must().size shouldBe mustCount
  mustNot().size shouldBe mustNotCount
  should().size shouldBe shouldCount
  filter().size shouldBe filterCount
}

/** Creates a simple Query from a query variant builder */
fun buildQuery(block: BoolQuery.Builder.() -> Unit): Query {
  return Query(BoolQuery.Builder().apply(block).build())
}

/** Extension to check if BoolQuery is empty */
fun BoolQuery.isEmpty(): Boolean =
  must().isEmpty() && mustNot().isEmpty() && should().isEmpty() && filter().isEmpty()

/** Extension to check if BoolQuery is not empty */
fun BoolQuery.isNotEmpty(): Boolean = !isEmpty()
