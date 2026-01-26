/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl

@DslMarker annotation class ElasticsearchDsl

@DslMarker @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY) annotation class VariantDsl
