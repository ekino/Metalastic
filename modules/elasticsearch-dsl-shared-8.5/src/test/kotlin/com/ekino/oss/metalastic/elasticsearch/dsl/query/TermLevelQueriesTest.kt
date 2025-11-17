/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl.query

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.ComprehensiveTestMetamodel
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.TestStatus
import com.ekino.oss.metalastic.elasticsearch.dsl.toJsonString
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.jsonLenientMatcher
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.shouldHaveStructure
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain

/**
 * Tests for term-level queries: term, terms, exist Migrated from BasicEnhancedDslTest.kt and
 * DslJsonOutputTest.kt
 */
class TermLevelQueriesTest :
  ShouldSpec({
    val meta = ComprehensiveTestMetamodel

    context("term query") {
      should("work with String values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.country term "France" } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"term": {"country": {"value": "France"}}}
            ]
          }
        }
      """
          )
      }

      should("work with Int values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.age term 25 } }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"term":{"age":{"value":25}}"""
      }

      should("work with Boolean values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.active term true } }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"term":{"active":{"value":true}}"""
      }

      should("work with Enum values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.status term TestStatus.ACTIVE } }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"term":{"status":{"value":"ACTIVE"}}"""
      }

      should("filter null values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullString: String? = null
              meta.country term nullString
              meta.active term true
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }

    context("exist query") {
      should("create exists query for field") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.active.exist() } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"exists": {"field": "active"}}
            ]
          }
        }
      """
          )
      }
    }
  })
