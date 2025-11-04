package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.core.ObjectField
import com.ekino.oss.metalastic.core.TextField
import com.google.common.collect.Range
import io.kotest.core.spec.style.ShouldSpec
import kotlin.reflect.typeOf

object EnhancedTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val status: KeywordField<String> = KeywordField(this, "status", typeOf<String>())
  val priority: KeywordField<Int> = KeywordField(this, "priority", typeOf<Int>())
  val nested: NestedTestField = NestedTestField(this, "nested", true)
}

class NestedTestField(parent: ObjectField<*>?, fieldName: String, nested: Boolean) :
  ObjectField<Any>(parent, fieldName, nested, typeOf<Any>()) {
  val category: KeywordField<String> = KeywordField(this, "category", typeOf<String>())
  val score: KeywordField<Double> = KeywordField(this, "score", typeOf<Double>())
}

class EnhancedDslCompositionTest :
  ShouldSpec({
    val metamodel = EnhancedTestMetamodel

    should("demonstrate QueryVariant return types for composition") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name.match("test")
            metamodel.status.term("active")
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"match": {"name": {"query": "test"}}},
                  {"term": {"status": {"value": "active"}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate range query functionality") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.priority.greaterThanEqualTo(5)
            metamodel.priority.lowerThanEqualTo(10)
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"range": {"priority": {"gte": 5}}},
                  {"range": {"priority": {"lte": 10}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate advanced range query with bounds") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.priority range Range.closedOpen(1, 100) } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"range": {"priority": {"gte": 1, "lt": 100}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate nested query composition") {
      val builder = BoolQuery.Builder()
      builder.boolQueryDsl {
        must +
          {
            metamodel.nested.nested {
              must +
                {
                  metamodel.nested.category term "important"
                  metamodel.nested.score.greaterThanEqualTo(0.8)
                }
            }
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {
                    "nested": {
                      "path": "nested",
                      "query": {
                        "bool": {
                          "must": [
                            {"term": {"nested.category": {"value": "important"}}},
                            {"range": {"nested.score": {"gte": 0.8}}}
                          ]
                        }
                      }
                    }
                  }
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate enhanced bool query DSL") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name.match("test")
            metamodel.status.exist()
          }
        mustNot + { metamodel.status.term("deleted") }
        should +
          {
            metamodel.priority.greaterThanEqualTo(5)
            metamodel.priority.lowerThanEqualTo(1)
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"match": {"name": {"query": "test"}}},
                  {"exists": {"field": "status"}}
                ],
                "must_not": [
                  {"term": {"status": {"value": "deleted"}}}
                ],
                "should": [
                  {"range": {"priority": {"gte": 5}}},
                  {"range": {"priority": {"lte": 1}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate multiple query clauses") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name.match("test")
            metamodel.status.term("active")
          }
        should +
          {
            metamodel.priority.greaterThanEqualTo(5)
            metamodel.priority.lowerThanEqualTo(1)
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"match": {"name": {"query": "test"}}},
                  {"term": {"status": {"value": "active"}}}
                ],
                "should": [
                  {"range": {"priority": {"gte": 5}}},
                  {"range": {"priority": {"lte": 1}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate infix operators for natural syntax") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name match "test"
            metamodel.status term "active"
            metamodel.priority greaterThanEqualTo 5
            metamodel.priority lowerThanEqualTo 10
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"match": {"name": {"query": "test"}}},
                  {"term": {"status": {"value": "active"}}},
                  {"range": {"priority": {"gte": 5}}},
                  {"range": {"priority": {"lte": 10}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("demonstrate query reuse and composition") {
      val query1 =
        BoolQuery.Builder()
          .apply {
            boolQueryDsl {
              must +
                {
                  metamodel.status.term("active")
                  metamodel.name.match("test")
                }
            }
          }
          .build()

      val query2 =
        BoolQuery.Builder()
          .apply {
            boolQueryDsl {
              must +
                {
                  metamodel.status.term("active")
                  metamodel.priority range Range.atLeast(5)
                }
            }
          }
          .build()

      // Verify both queries work
      val actualJson1 = Query(query1).toJsonString()
      val actualJson2 = Query(query2).toJsonString()

      actualJson1 should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"term": {"status": {"value": "active"}}},
                  {"match": {"name": {"query": "test"}}}
                ]
              }
            }
          """
            .trimIndent()
        )

      actualJson2 should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"term": {"status": {"value": "active"}}},
                  {"range": {"priority": {"gte": 5}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }
  })
