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

    context("terms query") {
      should("work with a Collection of Enum values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must + { meta.status terms listOf(TestStatus.ACTIVE, TestStatus.PENDING) }
        }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"terms": {"status": ["ACTIVE", "PENDING"]}}
            ]
          }
        }
      """
          )
      }

      should("work with a Collection of Enum values and a block") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.status.terms(listOf(TestStatus.ACTIVE, TestStatus.DRAFT)) {
                boost(2.0f)
                queryName("status-filter")
              }
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"terms":{"status":["ACTIVE","DRAFT"]"""
        json shouldContain """"boost":2.0"""
        json shouldContain """"_name":"status-filter""""
      }

      should("skip the query when the enum collection is null") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullList: List<TestStatus>? = null
              meta.status terms nullList
              meta.active term true
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }

      should("skip the query when the enum collection is empty") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.status terms emptyList<TestStatus>()
              meta.active term true
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }
    }

    context("containsTerms query") {
      should("work with a Collection of Enum values") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must + { meta.statuses containsTerms listOf(TestStatus.ACTIVE, TestStatus.PENDING) }
        }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"terms": {"statuses": ["ACTIVE", "PENDING"]}}
            ]
          }
        }
      """
          )
      }

      should("work with a Collection of Enum values and a block") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.statuses.containsTerms(listOf(TestStatus.ACTIVE, TestStatus.DRAFT)) {
                boost(2.0f)
                queryName("statuses-filter")
              }
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)

        val json = Query(boolQuery).toJsonString()
        json shouldContain """"terms":{"statuses":["ACTIVE","DRAFT"]"""
        json shouldContain """"boost":2.0"""
        json shouldContain """"_name":"statuses-filter""""
      }

      should("skip the query when the enum collection is null") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              val nullList: List<TestStatus>? = null
              meta.statuses containsTerms nullList
              meta.active term true
            }
        }

        val boolQuery = builder.build()
        boolQuery.shouldHaveStructure(mustCount = 1)
      }

      should("skip the query when the enum collection is empty") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.statuses containsTerms emptyList<TestStatus>()
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
