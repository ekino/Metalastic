package com.qelasticsearch.dsl

abstract class PathElement(
    protected val path: String,
) {
    fun path(): String = path
}
