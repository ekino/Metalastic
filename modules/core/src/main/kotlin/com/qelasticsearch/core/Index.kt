package com.qelasticsearch.core

/**
 * Base class for index definitions. Represents the root of an Elasticsearch index.
 *
 * @param indexName The name of the Elasticsearch index
 */
abstract class Index(private val indexName: String) : ObjectField() {
  @YellowColor fun indexName(): String = indexName
}
