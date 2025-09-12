package com.qelasticsearch.processor.model

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.qelasticsearch.processor.CoreConstants
import com.qelasticsearch.processor.collecting.toFieldName
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Sealed class hierarchy for field models - eliminates impossible states and provides better type
 * safety than boolean flags.
 *
 * Enhanced with originalName for accurate KDoc generation from getter methods.
 */
sealed class FieldModel {
  abstract val parentModel: ElasticsearchGraph.QClassModel
  abstract val sourceDeclaration: KSDeclaration
  abstract val fieldType: FieldType
  abstract val annotations: List<KSAnnotation>

  val name
    get() = sourceDeclaration.toFieldName()

  val type: KSType by lazy {
    when (val declaration = sourceDeclaration) {
      is KSPropertyDeclaration -> declaration.type
      is KSFunctionDeclaration -> declaration.returnType
      else -> null
    }?.resolve() ?: error("Unsupported declaration type for field $name")
  }
}

/** Regular field model for simple field types (Text, Keyword, Date, etc.) */
data class SimpleFieldModel(
  override val parentModel: ElasticsearchGraph.QClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType,
  override val annotations: List<KSAnnotation>,
) : FieldModel()

/** Object field model for FieldType.Object fields that reference other classes */
data class ObjectFieldModel(
  override val parentModel: ElasticsearchGraph.QClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType,
  override val annotations: List<KSAnnotation>,
  val targetModel: ElasticsearchGraph.QClassModel?,
  val nested: Boolean,
) : FieldModel()

/** Multi-field model for @MultiField annotated fields with multiple inner fields */
data class MultiFieldModel(
  override val parentModel: ElasticsearchGraph.QClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType, // Main field type
  override val annotations: List<KSAnnotation>,
  val innerFields: List<InnerFieldModel>,
) : FieldModel() {

  val mainFieldType
    get() = fieldType

  val className = "${name.replaceFirstChar { it.uppercase() }}${CoreConstants.MULTIFIELD_POSTFIX}"
}

/** Model for @InnerField annotations in @MultiField scenarios */
data class InnerFieldModel(val suffix: String, val fieldType: FieldType)
