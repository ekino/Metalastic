/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.processor.collecting

import com.ekino.oss.metalastic.processor.model.FieldModel
import com.ekino.oss.metalastic.processor.model.InnerFieldModel
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.model.MultiFieldModel
import com.ekino.oss.metalastic.processor.model.ObjectFieldModel
import com.ekino.oss.metalastic.processor.model.SimpleFieldModel
import com.ekino.oss.metalastic.processor.report.reporter
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/**
 * Field collector that works with the new MetalasticModel architecture.
 *
 * Key enhancements:
 * - Supports originalName property for accurate KDoc generation
 * - Integrates with recursive type exploration
 * - Handles collection types properly
 */

/** Extracts all @Field annotated properties and getter methods from a class declaration. */
fun MetalasticGraph.MetaClassModel.collectFields(): List<FieldModel> {
  val fields = mutableMapOf<String, FieldModel>()

  reporter.debug { "Collecting fields for Class: ${sourceClassDeclaration.fullyQualifiedName()}" }

  // Extract fields from properties
  sourceClassDeclaration.getAllProperties().forEach { property ->
    if (property.hasFieldAnnotation()) {
      runCatching { property.toFieldModel(this) }
        .onSuccess { fieldModel -> fields.putIfAbsent(fieldModel.name, fieldModel) }
        .onFailure { e ->
          reporter.exception(e) { "Failed to process property ${property.simpleName.asString()}" }
        }
    }
  }

  // Java records: KSP2 does not expose record components as properties. At the source-model
  // level KSP surfaces the record-component annotation on the canonical constructor's parameter
  // (even though @Field's @Target is FIELD/METHOD, not PARAMETER — the annotation only survives
  // on the constructor parameter symbol before javac-level target filtering). The matching
  // accessor function then provides the correct return type. Pair them up to build fields.
  sourceClassDeclaration.collectRecordComponents(this, fields)

  // Extract fields from getter methods (for interfaces)
  // Prioritize properties over getters to avoid duplicates
  sourceClassDeclaration
    .getAllFunctions()
    .filterNot { fields.containsKey(it.toFieldName()) }
    .forEach { function ->
      if (function.hasFieldAnnotation() && function.isGetterMethod()) {
        runCatching { function.toFieldModel(this) }
          .onSuccess { fieldModel ->
            fields.putIfAbsent(fieldModel.name, fieldModel)
            reporter.debug { "Found field getter: ${function.simpleName.asString()}" }
          }
          .onFailure { e ->
            reporter.exception(e) { "Failed to process getter ${function.simpleName.asString()}" }
          }
      }
    }

  return fields.values.toList()
}

private fun KSClassDeclaration.collectRecordComponents(
  qClassModel: MetalasticGraph.MetaClassModel,
  fields: MutableMap<String, FieldModel>,
) {
  val annotatedParams =
    getConstructors()
      .flatMap { it.parameters.asSequence() }
      .filter { it.hasFieldAnnotation() }
      .toList()
  if (annotatedParams.isEmpty()) return
  val paramNames = annotatedParams.mapNotNullTo(mutableSetOf()) { it.name?.asString() }
  // Only pair accessors whose name matches a @Field-annotated ctor param — this guards against
  // inherited zero-arg methods (toString, hashCode, interface defaults) leaking in.
  val accessorsByName =
    getAllFunctions()
      .filter { it.parameters.isEmpty() && it.returnType != null }
      .filter { it.simpleName.asString() in paramNames }
      .associateBy { it.simpleName.asString() }
  annotatedParams.forEach { param ->
    val paramName = param.name?.asString() ?: return@forEach
    if (fields.containsKey(paramName)) return@forEach
    val accessor = accessorsByName[paramName] ?: return@forEach
    runCatching { accessor.toFieldModel(qClassModel, annotationSource = param) }
      .onSuccess { fieldModel -> fields.putIfAbsent(fieldModel.name, fieldModel) }
      .onFailure { e -> reporter.exception(e) { "Failed to process record component $paramName" } }
  }
}

/**
 * Creates FieldModel from a @Field annotated property. Includes originalName and proper collection
 * type handling.
 *
 * [annotationSource] defaults to the declaration itself but can be overridden for Java records
 * where annotations live on the canonical constructor parameter rather than on the accessor.
 */
private fun KSDeclaration.toFieldModel(
  qClassModel: MetalasticGraph.MetaClassModel,
  annotationSource: KSAnnotated = this,
): FieldModel {
  val name = simpleName.asString()
  val potentialQClass =
    (annotationSource as? KSValueParameter)?.extractPotentialQClass() ?: extractPotentialQClass()
  val annotations: List<KSAnnotation> = annotationSource.annotations.toList()
  val isMultiField = annotationSource.getAnnotationsByType(MultiField::class).any()
  val (elasticsearchFieldName, propertyName) = resolveFieldNames(annotationSource)

  // Handle @MultiField properties (extract field type from mainField)
  if (isMultiField) {
    val multiFieldAnnotation = annotationSource.getAnnotationsByType(MultiField::class).first()
    // Extract field type from mainField property of @MultiField
    val mainField = multiFieldAnnotation.mainField
    val fieldType = mainField.type

    reporter.debug { "Processing property '$name' with @MultiField(mainField.type = $fieldType)" }

    val innerFields = extractInnerFields(multiFieldAnnotation)
    return MultiFieldModel(
      parentModel = qClassModel,
      sourceDeclaration = this,
      fieldType = fieldType,
      annotations = annotations,
      elasticsearchFieldName = elasticsearchFieldName,
      name = propertyName,
      innerFields = innerFields,
    )
  }

  // Handle regular @Field properties
  val fieldAnnotation = annotationSource.getAnnotationsByType(Field::class).first()
  val fieldType = fieldAnnotation.type

  reporter.debug { "Processing property '$name' with @Field(type = $fieldType)" }

  return when (fieldType) {
    FieldType.Nested,
    FieldType.Object -> {
      ObjectFieldModel(
        parentModel = qClassModel,
        sourceDeclaration = this,
        annotations = annotations,
        fieldType = fieldType,
        elasticsearchFieldName = elasticsearchFieldName,
        name = propertyName,
        targetModel = qClassModel.graph.getModel(potentialQClass),
      )
    }

    else -> {
      SimpleFieldModel(
        parentModel = qClassModel,
        sourceDeclaration = this,
        fieldType = fieldType,
        annotations = annotations,
        elasticsearchFieldName = elasticsearchFieldName,
        name = propertyName,
      )
    }
  }
}

/** Extracts @InnerField annotations from a @MultiField annotation. */
private fun extractInnerFields(multiField: MultiField): List<InnerFieldModel> {
  return multiField.otherFields.map { innerField ->
    InnerFieldModel(
      suffix = innerField.suffix,
      fieldType = innerField.type,
      // InnerField doesn't have a value property
    )
  }
}

/** Extension function to check if a function is a getter method. */
private fun KSFunctionDeclaration.isGetterMethod(): Boolean {
  return parameters.isEmpty() && returnType != null
}

/** Resolves the Elasticsearch field name and Kotlin property name from a declaration. */
private fun KSDeclaration.resolveFieldNames(annotationSource: KSAnnotated): Pair<String, String> {
  val originalPropertyName = toFieldName()

  val annotationName =
    when {
      annotationSource.isAnnotationPresent(MultiField::class) -> {
        annotationSource.getAnnotationsByType(MultiField::class).first().mainField.name
      }
      annotationSource.isAnnotationPresent(Field::class) -> {
        annotationSource.getAnnotationsByType(Field::class).first().name
      }
      else -> ""
    }

  return if (annotationName.isNotBlank()) {
    val propertyName =
      if (annotationName.isValidKotlinIdentifier()) {
        annotationName // Use annotation name if it's a valid identifier
      } else {
        originalPropertyName // Fall back to original property name
      }
    annotationName to propertyName
  } else {
    originalPropertyName to originalPropertyName
  }
}
