package com.ekino.oss.metalastic.elasticsearch.dsl.query

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.ComprehensiveTestMetamodel
import com.ekino.oss.metalastic.elasticsearch.dsl.toJsonString
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.jsonLenientMatcher
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.shouldHaveStructure
import com.google.common.collect.Range
import io.kotest.core.spec.style.ShouldSpec

/**
 * Tests for range queries: range, greaterThan, lowerThan, greaterThanEqualTo, lowerThanEqualTo
 * Migrated from EnhancedDslCompositionTest.kt
 */
class RangeQueriesTest :
  ShouldSpec({
    val meta = ComprehensiveTestMetamodel

    context("greaterThanEqualTo query") {
      should("create range query with gte") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.age greaterThanEqualTo 5 } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"range": {"age": {"gte": 5}}}
            ]
          }
        }
      """
          )
      }

      should("filter null values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullInt: Int? = null
              meta.age greaterThanEqualTo nullInt
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }

    context("lowerThanEqualTo query") {
      should("create range query with lte") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.age lowerThanEqualTo 10 } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"range": {"age": {"lte": 10}}}
            ]
          }
        }
      """
          )
      }

      should("filter null values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullInt: Int? = null
              meta.age lowerThanEqualTo nullInt
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }

    context("range with Range object") {
      should("work with closedOpen range") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.age range Range.closedOpen(1, 100) } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"range": {"age": {"gte": 1, "lt": 100}}}
            ]
          }
        }
      """
          )
      }

      should("work with closed range") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.age range Range.closed(5, 10) } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"range": {"age": {"gte": 5, "lte": 10}}}
            ]
          }
        }
      """
          )
      }
    }

    context("combined range queries") {
      should("support multiple range conditions") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.age.greaterThanEqualTo(5)
              meta.age.lowerThanEqualTo(10)
            }
        }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"range": {"age": {"gte": 5}}},
              {"range": {"age": {"lte": 10}}}
            ]
          }
        }
      """
          )
      }
    }
  })
