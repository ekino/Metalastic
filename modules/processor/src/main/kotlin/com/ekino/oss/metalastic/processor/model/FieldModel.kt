/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.model

import com.ekino.oss.metalastic.processor.CoreConstants
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Sealed class hierarchy for field models - eliminates impossible states and provides better type
 * safety than boolean flags.
 *
 * Enhanced with originalName for accurate KDoc generation from getter methods.
 */
sealed class FieldModel {
  abstract val parentModel: MetalasticGraph.MetaClassModel
  abstract val sourceDeclaration: KSDeclaration
  abstract val fieldType: FieldType
  abstract val annotations: List<KSAnnotation>
  abstract val elasticsearchFieldName: String
  abstract val name: String

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
  override val parentModel: MetalasticGraph.MetaClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType,
  override val annotations: List<KSAnnotation>,
  override val elasticsearchFieldName: String,
  override val name: String,
) : FieldModel()

/** Object field model for FieldType.Object fields that reference other classes */
data class ObjectFieldModel(
  override val parentModel: MetalasticGraph.MetaClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType,
  override val annotations: List<KSAnnotation>,
  override val elasticsearchFieldName: String,
  override val name: String,
  val targetModel: MetalasticGraph.MetaClassModel?,
) : FieldModel() {

  val nested: Boolean
    get() = fieldType == FieldType.Nested
}

/** Multi-field model for @MultiField annotated fields with multiple inner fields */
data class MultiFieldModel(
  override val parentModel: MetalasticGraph.MetaClassModel,
  override val sourceDeclaration: KSDeclaration,
  override val fieldType: FieldType, // Main field type
  override val annotations: List<KSAnnotation>,
  override val elasticsearchFieldName: String,
  override val name: String,
  val innerFields: List<InnerFieldModel>,
) : FieldModel() {

  val mainFieldType
    get() = fieldType

  val className = "${name.replaceFirstChar { it.uppercase() }}${CoreConstants.MULTIFIELD_POSTFIX}"
}

/** Model for @InnerField annotations in @MultiField scenarios */
data class InnerFieldModel(val suffix: String, val fieldType: FieldType)
