package com.ekino.oss.metalastic.elasticsearch.dsl.edgecases

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.core.ObjectField
import com.ekino.oss.metalastic.core.TextField
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.typeOf

object TestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val country: KeywordField<String> = KeywordField(this, "country", typeOf<String>())
  val active: KeywordField<Boolean> = KeywordField(this, "active", typeOf<Boolean>())
}

class FieldQueryVariantDslTest :
  ShouldSpec({
    val metamodel = TestMetamodel

    should("create must queries with receiver functions") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name match "test"
            metamodel.country term "France"
            metamodel.active.exist()
          }
      }

      val boolQuery = builder.build()
      boolQuery.must().size shouldBe 3
    }

    should("create mustNot queries with receiver functions") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        mustNot +
          {
            metamodel.name match "exclude"
            metamodel.active.exist()
          }
      }

      val boolQuery = builder.build()
      boolQuery.mustNot().size shouldBe 2
    }

    should("create combined must and mustNot queries") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must + { metamodel.country term "France" }
        mustNot + { metamodel.name match "exclude" }
      }

      val boolQuery = builder.build()
      boolQuery.must().size shouldBe 1
      boolQuery.mustNot().size shouldBe 1
    }

    should("handle null values gracefully") {
      val builder = BoolQuery.Builder()
      builder.boolQueryDsl {
        must +
          {
            val nullString: String? = null
            metamodel.name match nullString
            metamodel.country term nullString
          }
      }

      val boolQuery = builder.build()
      // Null values should not create queries
      boolQuery.must().size shouldBe 0
    }

    should("filter out blank strings") {
      val builder = BoolQuery.Builder()

      builder.boolQueryDsl {
        must +
          {
            metamodel.name match ""
            metamodel.name match "   "
            metamodel.country term "France" // Only this should create a query
          }
      }

      val boolQuery = builder.build()
      // Only the "France" term query should be created, blank strings filtered out
      boolQuery.must().size shouldBe 1
    }
  })
