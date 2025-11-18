/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.json.JsonpUtils
import org.springframework.data.elasticsearch.client.elc.NativeQuery

/**
 * Utility functions for testing Elasticsearch queries by converting them to JSON strings. This
 * allows for precise verification of the generated query structure.
 *
 * These utilities work in combination with JCV (JSON Content Validator) for advanced JSON testing
 * capabilities including strict matching, lenient matching, and template-based testing with
 * placeholders.
 */

/**
 * Converts a NativeQuery to its JSON representation for testing purposes. Removes the class name
 * prefix for cleaner output.
 */
fun NativeQuery.toJsonSearchRequest(): String =
  JsonpUtils.toString(toSearchRequest()).removePrefix("${SearchRequest::class.simpleName}:")

/** Converts a NativeQuery to a SearchRequest for JSON serialization. */
private fun NativeQuery.toSearchRequest(): SearchRequest =
  SearchRequest.of {
    it.postFilter(filter)
    it.query(query)
    it.aggregations(aggregations)
    it.sort(sortOptions)
  }

/**
 * Converts a Query to its JSON representation for testing purposes. Removes the class name prefix
 * for cleaner output compatible with JCV.
 */
fun Query.toJsonString(): String =
  JsonpUtils.toString(this).removePrefix("${Query::class.simpleName}:")
