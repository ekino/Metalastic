package com.metalastic.elasticsearch.dsl

@DslMarker annotation class ElasticsearchDsl

@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class VariantDsl
