/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl.query

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.fixtures.ComprehensiveTestMetamodel
import com.ekino.oss.metalastic.elasticsearch.dsl.toJsonString
import com.ekino.oss.metalastic.elasticsearch.dsl.utils.jsonLenientMatcher
import io.kotest.core.spec.style.ShouldSpec

/** Tests for nested queries Migrated from EnhancedDslCompositionTest.kt */
class NestedQueriesTest :
  ShouldSpec({
    val meta = ComprehensiveTestMetamodel

    context("nested query") {
      should("create nested query with bool composition") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.reviews.nested {
                must +
                  {
                    meta.reviews.author term "John"
                    meta.reviews.score greaterThanEqualTo 4.0
                  }
              }
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
              {
                "nested": {
                  "path": "reviews",
                  "query": {
                    "bool": {
                      "must": [
                        {"term": {"reviews.author": {"value": "John"}}},
                        {"range": {"reviews.score": {"gte": 4.0}}}
                      ]
                    }
                  }
                }
              }
            ]
          }
        }
      """
          )
      }

      should("support nested query within complex bool") {
        val builder = BoolQuery.Builder()

        builder.boolQueryDsl {
          must +
            {
              meta.name match "test"
              meta.reviews.nested { must + { meta.reviews.verified term true } }
            }
          mustNot + { meta.country term "deleted" }
        }

        val query = Query(builder.build())
        val json = query.toJsonString()

        json should
          jsonLenientMatcher(
            """
        {
          "bool": {
            "must": [
              {"match": {"name": {"query": "test"}}},
              {
                "nested": {
                  "path": "reviews",
                  "query": {
                    "bool": {
                      "must": [
                        {"term": {"reviews.verified": {"value": true}}}
                      ]
                    }
                  }
                }
              }
            ],
            "must_not": [
              {"term": {"country": {"value": "deleted"}}}
            ]
          }
        }
      """
          )
      }
    }
  })
