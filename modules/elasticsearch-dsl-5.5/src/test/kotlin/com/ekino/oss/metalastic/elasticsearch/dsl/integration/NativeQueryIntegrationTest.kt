/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl.integration

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.core.ObjectField
import com.ekino.oss.metalastic.core.TextField
import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl
import com.ekino.oss.metalastic.elasticsearch.dsl.toJsonSearchRequest
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import kotlin.reflect.typeOf
import org.springframework.data.elasticsearch.client.elc.NativeQuery

object SearchMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val title: TextField<String> = TextField(this, "title", typeOf<String>())
  val category: KeywordField<String> = KeywordField(this, "category", typeOf<String>())
  val published: KeywordField<Boolean> = KeywordField(this, "published", typeOf<Boolean>())
  val author: TextField<String> = TextField(this, "author", typeOf<String>())
}

class NativeQueryIntegrationTest :
  ShouldSpec({
    val metamodel = SearchMetamodel

    should("integrate with NativeQuery and produce correct search request JSON") {
      // Build the bool query using our DSL
      val boolBuilder = BoolQuery.Builder()

      boolBuilder.boolQueryDsl {
        must +
          {
            metamodel.category term "technology"
            metamodel.published.exist()
          }
        should +
          {
            metamodel.title match "kotlin"
            metamodel.author match "john"
          }
        mustNot + { metamodel.title match "deprecated" }
      }

      // Create NativeQuery with the bool query
      val nativeQuery =
        NativeQuery.builder().withQuery(Query(boolBuilder.build())).withMaxResults(10).build()

      // Convert to JSON and verify structure
      val json = nativeQuery.toJsonSearchRequest()

      // Verify the overall structure contains our query
      json shouldContain """"query":{"bool":{"""

      // Verify must clauses
      json shouldContain """"must":["""
      json shouldContain """"term":{"category":{"value":"technology"}}"""
      json shouldContain """"exists":{"field":"published"}"""

      // Verify should clauses
      json shouldContain """"should":["""
      json shouldContain """"match":{"title":{"query":"kotlin"}}"""
      json shouldContain """"match":{"author":{"query":"john"}}"""

      // Verify must_not clauses
      json shouldContain """"must_not":["""
      json shouldContain """"match":{"title":{"query":"deprecated"}}"""

      // Note: size parameter is not included in JSON when using toJsonSearchRequest()
      // The JSON structure should contain the query structure we built
    }

    should("work with simple single-clause queries") {
      val boolBuilder = BoolQuery.Builder()

      boolBuilder.boolQueryDsl { must + { metamodel.title match "elasticsearch" } }

      val nativeQuery = NativeQuery.builder().withQuery(Query(boolBuilder.build())).build()

      val json = nativeQuery.toJsonSearchRequest()

      json shouldContain
        """"query":{"bool":{"must":[{"match":{"title":{"query":"elasticsearch"}}}]}}"""
    }

    should("handle empty query gracefully") {
      val boolBuilder = BoolQuery.Builder()

      // Add queries that will be filtered out
      boolBuilder.boolQueryDsl {
        must +
          {
            val nullString: String? = null
            metamodel.title match nullString
            metamodel.category term ""
          }
      }

      val nativeQuery = NativeQuery.builder().withQuery(Query(boolBuilder.build())).build()

      val json = nativeQuery.toJsonSearchRequest()

      // Should create an empty bool query
      json shouldContain """"query":{"bool":{}}"""
    }

    should("demonstrate complex real-world query") {
      val boolBuilder = BoolQuery.Builder()

      boolBuilder.boolQueryDsl {
        // Main search criteria
        must +
          {
            metamodel.published.exist()
            metamodel.category term "tutorial"
          }
        // Boost relevant content
        should +
          {
            metamodel.title match "beginner"
            metamodel.title match "introduction"
            metamodel.author match "expert"
          }
        // Exclude unwanted content
        mustNot +
          {
            metamodel.title match "advanced"
            metamodel.title match "deprecated"
          }
        // Filter by additional criteria
        filter + { metamodel.category term "programming" }
      }

      val nativeQuery =
        NativeQuery.builder()
          .withQuery(Query(boolBuilder.build()))
          .withMaxResults(20)
          .withMinScore(0.5f)
          .build()

      val json = nativeQuery.toJsonSearchRequest()

      // Verify all sections are present
      json shouldContain """"must":["""
      json shouldContain """"should":["""
      json shouldContain """"must_not":["""
      json shouldContain """"filter":["""
      // Note: size and min_score are not included in toJsonSearchRequest() output
      // They are handled at the NativeQuery level, not in the query JSON structure

      // Verify specific queries
      json shouldContain """"exists":{"field":"published"}"""
      json shouldContain """"term":{"category":{"value":"tutorial"}}"""
      json shouldContain """"match":{"title":{"query":"beginner"}}"""
      json shouldContain """"match":{"title":{"query":"advanced"}}"""
      json shouldContain """"term":{"category":{"value":"programming"}}"""
    }
  })
