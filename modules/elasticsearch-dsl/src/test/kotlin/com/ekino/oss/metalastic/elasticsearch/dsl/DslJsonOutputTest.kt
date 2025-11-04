package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.reflect.typeOf

object JsonTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val country: KeywordField<String> = KeywordField(this, "country", typeOf<String>())
  val status: KeywordField<String> = KeywordField(this, "status", typeOf<String>())
  val active: KeywordField<Boolean> = KeywordField(this, "active", typeOf<Boolean>())
  val age: KeywordField<Int> = KeywordField(this, "age", typeOf<Int>())
}

class DslJsonOutputTest :
  ShouldSpec({
    val metamodel = JsonTestMetamodel

    should("generate correct JSON for must queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name match "John Doe"
            metamodel.country term "France"
            metamodel.active.exist()
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      json shouldContain """"must":["""
      json shouldContain """"match":{"name":{"query":"John Doe"}}"""
      json shouldContain """"term":{"country":{"value":"France"}}"""
      json shouldContain """"exists":{"field":"active"}"""
    }

    should("generate correct JSON for mustNot queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        mustNot +
          {
            metamodel.status term "inactive"
            metamodel.name match "excluded"
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      json shouldContain """"must_not":["""
      json shouldContain """"term":{"status":{"value":"inactive"}}"""
      json shouldContain """"match":{"name":{"query":"excluded"}}"""
    }

    should("generate correct JSON for combined must and mustNot") {
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
      val json = query.toJsonString()

      json shouldContain """"must":["""
      json shouldContain """"must_not":["""
      json shouldContain """"term":{"country":{"value":"France"}}"""
      json shouldContain """"exists":{"field":"active"}"""
      json shouldContain """"term":{"status":{"value":"inactive"}}"""
    }

    should("generate correct JSON for should queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        should +
          {
            metamodel.name match "John"
            metamodel.name match "Jane"
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      json shouldContain """"should":["""
      json shouldContain """"match":{"name":{"query":"John"}}"""
      json shouldContain """"match":{"name":{"query":"Jane"}}"""
    }

    should("generate correct JSON for filter queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        filter +
          {
            metamodel.country term "France"
            metamodel.age term 25
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      json shouldContain """"filter":["""
      json shouldContain """"term":{"country":{"value":"France"}}"""
      json shouldContain """"term":{"age":{"value":25}}"""
    }

    should("handle null and blank values correctly in JSON") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            val nullString: String? = null
            metamodel.name match nullString
            metamodel.name match ""
            metamodel.name match "   "
            metamodel.country term "France" // Only this should appear
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      // Should only contain the France term query
      json shouldContain """"must":[{"term":{"country":{"value":"France"}}}]"""
      json shouldNotContain """"match":{"name"""
    }

    should("generate correct JSON for match phrase queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name matchPhrase "John Doe Smith"
            metamodel.country term "France"
          }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      json shouldContain """"match_phrase":{"name":{"query":"John Doe Smith"}}"""
      json shouldContain """"term":{"country":{"value":"France"}}"""
    }

    should("generate correct JSON for nested query structure") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must + { metamodel.country term "France" }
        should +
          {
            metamodel.name match "John"
            metamodel.name match "Jane"
          }
        mustNot + { metamodel.status term "inactive" }
      }

      val query = Query(builder.build())
      val json = query.toJsonString()

      // Verify the overall structure
      json shouldContain """"bool":{"""
      json shouldContain """"must":["""
      json shouldContain """"should":["""
      json shouldContain """"must_not":["""

      // Verify specific queries
      json shouldContain """"term":{"country":{"value":"France"}}"""
      json shouldContain """"match":{"name":{"query":"John"}}"""
      json shouldContain """"match":{"name":{"query":"Jane"}}"""
      json shouldContain """"term":{"status":{"value":"inactive"}}"""
    }
  })
