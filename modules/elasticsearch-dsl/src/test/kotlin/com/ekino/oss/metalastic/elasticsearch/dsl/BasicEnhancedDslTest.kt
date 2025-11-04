package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.typeOf

object BasicTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val status: KeywordField<String> = KeywordField(this, "status", typeOf<String>())
}

class BasicEnhancedDslTest :
  ShouldSpec({
    val metamodel = BasicTestMetamodel

    should("verify basic term query works") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.status.term("active") } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
        jsonLenientMatcher(
          """
            {
              "bool": {
                "must": [
                  {"term": {"status": {"value": "active"}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("verify shouldAtLeastOneOf functionality") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.status.term("active")
            shouldAtLeastOneOf(listOf("premium", "enterprise")) {
              metamodel.status.term(it)
              metamodel.name.term(it)
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
                  {"term": {"status": {"value": "active"}}},
                  {
                    "bool": {
                      "should": [
                        {"term": {"status": {"value": "premium"}}},
                        {"term": {"name": {"value": "premium"}}},
                        {"term": {"status": {"value": "enterprise"}}},
                        {"term": {"name": {"value": "enterprise"}}}
                      ]
                    }
                  }
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("verify shouldAtLeastOneOf with empty collection does nothing") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.status.term("active")
            shouldAtLeastOneOf(emptyList<String>()) { metamodel.status.term(it) }
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
                  {"term": {"status": {"value": "active"}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("verify shouldAtLeastOneOf with null collection does nothing") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.status.term("active")
            shouldAtLeastOneOf(null as List<String>?) { metamodel.status.term(it) }
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
                  {"term": {"status": {"value": "active"}}}
                ]
              }
            }
          """
            .trimIndent()
        )
    }

    should("verify basic match query works") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.name.match("test") } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      actualJson should
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
            .trimIndent()
        )
    }

    should("verify query return types work") {
      val termQuery = QueryVariantDsl {}.run { metamodel.status.term("active") }
      val matchQuery = QueryVariantDsl {}.run { metamodel.name.match("test") }

      termQuery shouldNotBe null
      matchQuery shouldNotBe null
    }
  })
