package com.qelasticsearch.core

/**
 * Base class for index definitions. Represents the root of an Elasticsearch index.
 *
 * @param indexName The name of the Elasticsearch index
 */
abstract class Index(
  private val indexName: String,
  parent: ObjectField? = null,
  fieldName: String = "",
  nested: Boolean = false,
) : ObjectField(parent, fieldName, nested) {
  @YellowColor fun indexName(): String = indexName
}
