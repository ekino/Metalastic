package com.qelasticsearch.dsl

/**
 * Annotation to mark fields that should be included in the generated Q-index as DynamicField<T>.
 *
 * Unlike @Field annotations that map to specific Elasticsearch field types, @QDynamicField creates
 * generic field references that can be used for runtime queries without being tied to a specific
 * Elasticsearch mapping.
 *
 * Supports primitive types and collections but not nested objects.
 *
 * Usage:
 * ```kotlin
 * @Document(indexName = "product")
 * class ProductDocument {
 *     @Field(type = FieldType.Keyword)
 *     val id: String = ""
 *
 *     @QDynamicField
 *     val runtimeScore: Double = 0.0
 *
 *     @QDynamicField
 *     val tags: List<String> = emptyList()
 * }
 * ```
 *
 * Generated Q-class:
 * ```kotlin
 * object QProductDocument : Index("product") {
 *     val id by keyword<String>()
 *     val runtimeScore by dynamicField<Double>()
 *     val tags by dynamicField<List<String>>()
 * }
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class QDynamicField
