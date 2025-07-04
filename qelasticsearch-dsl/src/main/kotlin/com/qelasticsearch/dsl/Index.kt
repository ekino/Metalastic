package com.qelasticsearch.dsl

/**
 * Base class for index definitions. Represents the root of an Elasticsearch index.
 *
 * @param indexName The name of the Elasticsearch index
 */
abstract class Index(
    val indexName: String,
) : ObjectFields() {
    /**
     * The path for the root index is always empty
     */
    val path: String = ""
}
