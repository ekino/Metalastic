package com.qelasticsearch.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import org.springframework.data.elasticsearch.annotations.FieldType

/** Context for import management during code generation. */
data class ImportContext(val usedImports: MutableSet<String> = mutableSetOf())

/** Field type mapping for generating DSL classes. */
data class FieldTypeMapping(val className: String)

/** Processed field type information. */
data class ProcessedFieldType(
  val elasticsearchType: FieldType,
  val kotlinType: KSTypeReference,
  val kotlinTypeName: String,
  val isObjectType: Boolean,
)

/** Information about an object field that needs Q-class generation. */
data class ObjectFieldInfo(
  val className: String,
  val packageName: String,
  val classDeclaration: KSClassDeclaration,
  val qualifiedName: String,
  val parentDocumentClass: KSClassDeclaration? = null,
)

/** Constants for Core logic generation. */
object CoreConstants {
  const val CORE_PACKAGE = "com.qelasticsearch.core"
  const val Q_PREFIX = "Q"
  const val INDEX_CLASS = "Index"
  const val OBJECT_FIELDS_CLASS = "ObjectField"
}
