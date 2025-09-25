package com.metalastic.processor.collecting

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.metalastic.processor.model.FieldModel
import com.metalastic.processor.model.InnerFieldModel
import com.metalastic.processor.model.MetalasticGraph
import com.metalastic.processor.model.MultiFieldModel
import com.metalastic.processor.model.ObjectFieldModel
import com.metalastic.processor.model.SimpleFieldModel
import com.metalastic.processor.report.reporter
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
private fun KSDeclaration.toFieldModel(qClassModel: MetalasticGraph.MetaClassModel): FieldModel {
  val name = simpleName.asString()
  val potentialQClass = extractPotentialQClass()
  val annotations = annotations.toList()
  val isMultiField = getAnnotationsByType(MultiField::class).any()
  val (elasticsearchFieldName, propertyName) = resolveFieldNames()

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
      elasticsearchFieldName = elasticsearchFieldName,
      name = propertyName,
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
        elasticsearchFieldName = elasticsearchFieldName,
        name = propertyName,
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

/** Extension function to check if a function has @Field annotation. */
private fun KSFunctionDeclaration.hasFieldAnnotation(): Boolean = isAnnotationPresent(Field::class)

/** Extension function to check if a function is a getter method. */
private fun KSFunctionDeclaration.isGetterMethod(): Boolean {
  return parameters.isEmpty() && returnType != null
}

/** Resolves the Elasticsearch field name and Kotlin property name from a declaration. */
private fun KSDeclaration.resolveFieldNames(): Pair<String, String> {
  val originalPropertyName = toFieldName()

  val annotationName =
    when {
      isAnnotationPresent(MultiField::class) -> {
        getAnnotationsByType(MultiField::class).first().mainField.name
      }
      isAnnotationPresent(Field::class) -> {
        getAnnotationsByType(Field::class).first().name
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
