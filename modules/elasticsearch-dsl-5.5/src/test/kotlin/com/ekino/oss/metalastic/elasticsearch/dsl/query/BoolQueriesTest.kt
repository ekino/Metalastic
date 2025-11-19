/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl.query

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.ComprehensiveTestMetamodel
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.TestStatus
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.jsonLenientMatcher
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.shouldHaveStructure
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.toJsonString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/** Tests for BoolQuery DSL - testing must, mustNot, should, filter occurrences */
class BoolQueriesTest :
  ShouldSpec({
    val meta = ComprehensiveTestMetamodel

    context("bool query occurrences") {
      should("support must occurrence with operator syntax") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.name match "John Doe"
              meta.country term "France"
              meta.active.exist()
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 3)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"must":["""
        json shouldContain """"match":{"name":{"query":"John Doe"}}"""
        json shouldContain """"term":{"country":{"value":"France"}}"""
        json shouldContain """"exists":{"field":"active"}"""
      }

      should("support mustNot occurrence with operator syntax") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          mustNot +
            {
              meta.status term TestStatus.INACTIVE
              meta.name match "excluded"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustNotCount = 2)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"must_not":["""
        json shouldContain """"term":{"status":{"value":"INACTIVE"}}"""
        json shouldContain """"match":{"name":{"query":"excluded"}}"""
      }

      should("support should occurrence with operator syntax") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          should +
            {
              meta.name match "John"
              meta.name match "Jane"
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(shouldCount = 2)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"should":["""
        json shouldContain """"match":{"name":{"query":"John"}}"""
        json shouldContain """"match":{"name":{"query":"Jane"}}"""
      }

      should("support filter occurrence with operator syntax") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          filter +
            {
              meta.country term "France"
              meta.age term 25
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(filterCount = 2)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"filter":["""
        json shouldContain """"term":{"country":{"value":"France"}}"""
        json shouldContain """"term":{"age":{"value":25}}"""
      }

      should("support combined must and mustNot occurrences") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.country term "France"
              meta.active.exist()
            }
          mustNot + { meta.status term TestStatus.DELETED }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 2, mustNotCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"must":["""
        json shouldContain """"must_not":["""
        json shouldContain """"term":{"country":{"value":"France"}}"""
        json shouldContain """"exists":{"field":"active"}"""
        json shouldContain """"term":{"status":{"value":"DELETED"}}"""
      }

      should("support all four occurrences combined") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must + { meta.country term "France" }
          should +
            {
              meta.name match "John"
              meta.name match "Jane"
            }
          mustNot + { meta.status term TestStatus.INACTIVE }
          filter + { meta.active term true }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(
          mustCount = 1,
          mustNotCount = 1,
          shouldCount = 2,
          filterCount = 1,
        )

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"bool":{"""
        json shouldContain """"must":["""
        json shouldContain """"should":["""
        json shouldContain """"must_not":["""
        json shouldContain """"filter":["""
      }
    }

    context("null and blank value filtering") {
      should("filter out null and blank values from queries") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullString: String? = null
              meta.name match nullString
              meta.name match ""
              meta.name match "   "
              meta.country term "France" // Only this should appear
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"term":{"country":{"value":"France"}}"""
        json shouldNotContain """"match":{"name"""
      }
    }

    context("nested bool queries") {
      should("support nested bool within must") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              bool {
                should +
                  {
                    meta.name match "John"
                    meta.name match "Jane"
                  }
              }
              meta.country term "France"
            }
        }

        val boolQuery = builder.build()
        val json = Query(boolQuery).toJsonString()

        // Should have nested bool structure
        json shouldContain """"must":[{"""
        json shouldContain """"bool":{"""
        json shouldContain """"should":["""
      }
    }

    context("shouldAtLeastOneOf") {
      should("create nested bool with should occurrence for each collection value") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.country term "France"
              shouldAtLeastOneOf(listOf("premium", "enterprise")) {
                meta.category term it
                meta.name term it
              }
            }
        }

        val boolQuery = builder.build()
        val json = Query(boolQuery).toJsonString()

        json should
          jsonLenientMatcher(
            """
          {
            "bool": {
              "must": [
                {"term": {"country": {"value": "France"}}},
                {
                  "bool": {
                    "should": [
                      {"term": {"category": {"value": "premium"}}},
                      {"term": {"name": {"value": "premium"}}},
                      {"term": {"category": {"value": "enterprise"}}},
                      {"term": {"name": {"value": "enterprise"}}}
                    ]
                  }
                }
              ]
            }
          }
        """
          )
      }

      should("do nothing with empty collection") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.country term "France"
              shouldAtLeastOneOf(emptyList<String>()) { meta.category term it }
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
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

      should("do nothing with null collection") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.country term "France"
              shouldAtLeastOneOf(null as List<String>?) { meta.category term it }
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
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
    }
  })
