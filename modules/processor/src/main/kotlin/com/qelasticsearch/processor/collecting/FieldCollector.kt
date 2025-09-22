package com.qelasticsearch.processor.collecting

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.qelasticsearch.processor.model.ElasticsearchGraph
import com.qelasticsearch.processor.model.FieldModel
import com.qelasticsearch.processor.model.InnerFieldModel
import com.qelasticsearch.processor.model.MultiFieldModel
import com.qelasticsearch.processor.model.ObjectFieldModel
import com.qelasticsearch.processor.model.SimpleFieldModel
import com.qelasticsearch.processor.report.reporter
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/**
 * Field collector that works with the new QElasticsearchModel architecture.
 *
 * Key enhancements:
 * - Supports originalName property for accurate KDoc generation
 * - Integrates with recursive type exploration
 * - Handles collection types properly
 */

/** Extracts all @Field annotated properties and getter methods from a class declaration. */
fun ElasticsearchGraph.QClassModel.collectFields(): List<FieldModel> {
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

/**
 * Creates FieldModel from a @Field annotated property. Includes originalName and proper collection
 * type handling.
 */
private fun KSDeclaration.toFieldModel(qClassModel: ElasticsearchGraph.QClassModel): FieldModel {
  val name = simpleName.asString()
  val potentialQClass = extractPotentialQClass()
  val annotations = annotations.toList()
  val isMultiField = getAnnotationsByType(MultiField::class).any()

  // Handle @MultiField properties (extract field type from mainField)
  if (isMultiField) {
    val multiFieldAnnotation = getAnnotationsByType(MultiField::class).first()
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
      innerFields = innerFields,
    )
  }

  // Handle regular @Field properties
  val fieldAnnotation = getAnnotationsByType(Field::class).first()
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
        targetModel = qClassModel.graph.getModel(potentialQClass),
        nested = fieldType == FieldType.Nested,
      )
    }

    else -> {
      SimpleFieldModel(
        parentModel = qClassModel,
        sourceDeclaration = this,
        fieldType = fieldType,
        annotations = annotations,
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

/** Extension function to check if a function has @Field annotation. */
private fun KSFunctionDeclaration.hasFieldAnnotation(): Boolean = isAnnotationPresent(Field::class)

/** Extension function to check if a function is a getter method. */
private fun KSFunctionDeclaration.isGetterMethod(): Boolean {
  return parameters.isEmpty() && returnType != null
}
