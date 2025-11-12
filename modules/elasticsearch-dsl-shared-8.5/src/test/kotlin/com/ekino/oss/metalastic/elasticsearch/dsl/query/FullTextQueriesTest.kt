package com.ekino.oss.metalastic.elasticsearch.dsl.query

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.ComprehensiveTestMetamodel
import com.ekino.oss.metalastic.elasticsearch.dsl.toJsonString
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.jsonLenientMatcher
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.shouldHaveStructure
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * Tests for full-text queries: match, matchPhrase, matchPhrasePrefix, multiMatch Migrated from
 * BasicEnhancedDslTest.kt and DslJsonOutputTest.kt
 */
class FullTextQueriesTest :
  ShouldSpec({
    val meta = ComprehensiveTestMetamodel

    context("match query") {
      should("work with String infix operator") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.name match "test" } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"match": {"name": {"query": "test"}}}
            ]
          }
        }
      """
          )
      }

      should("work with String function syntax") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.name.match("John Doe") } }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"match":{"name":{"query":"John Doe"}}"""
      }

      should("filter null values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullString: String? = null
              meta.name match nullString
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"term":{"country":{"value":"France"}}"""
        json.contains(""""match":{"name"""") shouldBe false
      }

      should("filter blank values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.name match ""
              meta.name match "   "
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }

    context("matchPhrase query") {
      should("work with String infix operator") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl { must + { meta.name matchPhrase "John Doe Smith" } }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"match_phrase": {"name": {"query": "John Doe Smith"}}}
            ]
          }
        }
      """
          )
      }

      should("filter null and blank values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullString: String? = null
              meta.name matchPhrase nullString
              meta.name matchPhrase ""
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }
  })
