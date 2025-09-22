package com.metalastic.core

/**
 * Interface for Elasticsearch document types. Provides access to the index name where the document
 * is stored.
 */
abstract class Document<T : Any>(
  parent: ObjectField<*>? = null,
  name: String = "",
  nested: Boolean = false,
) : ObjectField<T>(parent, name, nested) {
  /** Returns the name of the Elasticsearch index where this document type is stored. */
  abstract fun indexName(): String
}
