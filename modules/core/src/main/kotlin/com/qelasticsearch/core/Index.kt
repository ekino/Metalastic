package com.qelasticsearch.core

/**
 * Base class for index definitions. Represents the root of an Elasticsearch index.
 *
 * @param indexName The name of the Elasticsearch index
 */
abstract class Index(val indexName: String) : ObjectField()
