package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import kotlin.reflect.typeOf
import org.springframework.data.elasticsearch.client.elc.NativeQuery

object JcvTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val country: KeywordField<String> = KeywordField(this, "country", typeOf<String>())
  val status: KeywordField<String> = KeywordField(this, "status", typeOf<String>())
  val category: KeywordField<String> = KeywordField(this, "category", typeOf<String>())
  val active: KeywordField<Boolean> = KeywordField(this, "active", typeOf<Boolean>())
}

class JcvAdvancedJsonTest :
  ShouldSpec({
    val metamodel = JcvTestMetamodel

    should("validate exact JSON structure with strict matching") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name match "John"
            metamodel.country term "France"
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      val expectedJson =
        """
        {
          "bool": {
            "must": [
              {"match": {"name": {"query": "John"}}},
              {"term": {"country": {"value": "France"}}}
            ]
          }
        }
        """
          .trimIndent()

      actualJson should jsonStrictMatcher(expectedJson)
    }

    should("validate JSON structure with lenient matching ignoring field order") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.country term "France"
            metamodel.active.exist()
          }
        mustNot + { metamodel.status term "inactive" }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      // Note: This expected JSON has different field order than actual
      val expectedJson =
        """
        {
          "bool": {
            "must_not": [
              {"term": {"status": {"value": "inactive"}}}
            ],
            "must": [
              {"exists": {"field": "active"}},
              {"term": {"country": {"value": "France"}}}
            ]
          }
        }
        """
          .trimIndent()

      actualJson should jsonLenientMatcher(expectedJson)
    }

    should("use template-based testing for complex queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.category term "tech"
            metamodel.active.exist()
          }
        should +
          {
            metamodel.name match "kotlin"
            metamodel.name match "java"
          }
        mustNot + { metamodel.status term "deprecated" }
        filter + { metamodel.country term "France" }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      val expectedTemplate =
        elasticsearchQueryTemplate(
          mustQueries =
            listOf(
              """{"term": {"category": {"value": "tech"}}}""",
              """{"exists": {"field": "active"}}""",
            ),
          shouldQueries =
            listOf(
              """{"match": {"name": {"query": "kotlin"}}}""",
              """{"match": {"name": {"query": "java"}}}""",
            ),
          mustNotQueries = listOf("""{"term": {"status": {"value": "deprecated"}}}"""),
          filterQueries = listOf("""{"term": {"country": {"value": "France"}}}"""),
        )

      actualJson should jsonLenientMatcher(expectedTemplate)
    }

    should("validate NativeQuery integration with JCV") {
      val boolBuilder = BoolQuery.Builder()

      boolBuilder.boolQueryDsl {
        must +
          {
            metamodel.category term "tutorial"
            metamodel.active.exist()
          }
      }

      val nativeQuery =
        NativeQuery.builder().withQuery(Query(boolBuilder.build())).withMaxResults(10).build()

      val actualJson = nativeQuery.toJsonSearchRequest()

      val expectedJson =
        """
        {
          "aggregations": {},
          "query": {
            "bool": {
              "must": [
                {"term": {"category": {"value": "tutorial"}}},
                {"exists": {"field": "active"}}
              ]
            }
          },
          "sort": []
        }
        """
          .trimIndent()

      actualJson should jsonLenientMatcher(expectedJson)
    }

    should("demonstrate JCV placeholders for dynamic values") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.name match "dynamic-value" } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      // Using JCV placeholder to ignore the actual value
      val expectedWithPlaceholder =
        """
        {
          "bool": {
            "must": [
              {"match": {"name": {"query":  "{#string_type#}"}}}
            ]
          }
        }
        """
          .trimIndent()

      actualJson should jsonMatcher(expectedWithPlaceholder)
    }

    should("validate partial JSON matching with non-extensible mode") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.country term "France"
            metamodel.name match "test"
          }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      // This will match even if actual JSON has the queries in different order
      // but will fail if there are extra fields at the same level
      val expectedJson =
        """
        {
          "bool": {
            "must": [
              {"term": {"country": {"value": "France"}}},
              {"match": {"name": {"query": "test"}}}
            ]
          }
        }
        """
          .trimIndent()

      actualJson should jsonNonExtensibleMatcher(expectedJson)
    }

    should("combine multiple query types with precise JCV validation") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must + { metamodel.category term "programming" }
        should +
          {
            metamodel.name match "kotlin"
            metamodel.name match "scala"
          }
        mustNot + { metamodel.status term "obsolete" }
      }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      val expectedTemplate =
        elasticsearchQueryTemplate(
          mustQueries = listOf("""{"term": {"category": {"value": "programming"}}}"""),
          shouldQueries =
            listOf(
              """{"match": {"name": {"query": "kotlin"}}}""",
              """{"match": {"name": {"query": "scala"}}}""",
            ),
          mustNotQueries = listOf("""{"term": {"status": {"value": "obsolete"}}}"""),
        )

      // Use lenient matching to allow field reordering
      actualJson should jsonLenientMatcher(expectedTemplate)
    }
  })
