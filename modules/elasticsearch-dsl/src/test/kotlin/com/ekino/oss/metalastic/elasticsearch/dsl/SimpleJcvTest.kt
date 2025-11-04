package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import kotlin.reflect.typeOf

object SimpleJcvMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val country: KeywordField<String> = KeywordField(this, "country", typeOf<String>())
}

class SimpleJcvTest :
  ShouldSpec({
    val metamodel = SimpleJcvMetamodel

    should("demonstrate basic JCV JSON matching capabilities") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.name match "John" } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      println("=== ACTUAL JSON ===")
      println(actualJson)
      println("=== END JSON ===")

      // Simple expected JSON - this should work
      val expectedJson = """{"bool":{"must":[{"match":{"name":{"query":"John"}}}]}}"""

      actualJson should jsonLenientMatcher(expectedJson)
    }

    should("demonstrate JCV with placeholders") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl { must + { metamodel.country term "France" } }

      val query = Query(builder.build())
      val actualJson = query.toJsonString()

      // First verify the structure works without placeholders
      val expectedExact = """{"bool":{"must":[{"term":{"country":{"value":"France"}}}]}}"""
      actualJson should jsonLenientMatcher(expectedExact)
    }
  })
