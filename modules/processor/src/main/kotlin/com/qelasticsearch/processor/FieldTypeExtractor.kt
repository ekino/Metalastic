package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.springframework.data.elasticsearch.annotations.FieldType

/** Handles extraction and determination of field types. */
class FieldTypeExtractor(private val logger: KSPLogger, private val debugLogging: Boolean = false) {
  /** Determines the field type for a property based on annotations. */
  fun determineFieldType(
    property: KSPropertyDeclaration,
    fieldAnnotation: KSAnnotation,
  ): ProcessedFieldType {
    val propertyName = property.simpleName.asString()

    if (debugLogging) {
      logger.info("[DEBUG:FieldTypeExtractor] Determining field type for property: $propertyName")
    }

    // Extract field type from @Field annotation
    val fieldType = extractFieldTypeFromAnnotation(fieldAnnotation)
    logger.info("Property $propertyName has @Field annotation with type: $fieldType")

    // Handle nested/object types - including collections
    val isObjectType = determineIfObjectType(fieldType, property)

    val kotlinTypeName =
      if (isCollectionType(getSimpleTypeName(property.type))) {
        getCollectionElementType(property)?.simpleName?.asString()
          ?: getSimpleTypeName(property.type)
      } else {
        getSimpleTypeName(property.type)
      }

    return ProcessedFieldType(
      elasticsearchType = fieldType,
      kotlinType = property.type,
      kotlinTypeName = kotlinTypeName,
      isObjectType = isObjectType,
    )
  }

  /**
   * Determines if a field type represents an object type (Object or Nested) that should be treated
   * as a custom object field.
   */
  private fun determineIfObjectType(fieldType: FieldType, property: KSPropertyDeclaration): Boolean {
    return when (fieldType) {
      FieldType.Object, FieldType.Nested -> {
        if (isCollectionType(getSimpleTypeName(property.type))) {
          isValidCollectionObjectType(property)
        } else {
          isValidSingleObjectType(property)
        }
      }
      else -> false
    }
  }

  /**
   * Checks if a collection property contains valid object types (not standard library types or
   * enums).
   */
  private fun isValidCollectionObjectType(property: KSPropertyDeclaration): Boolean {
    val elementType = getCollectionElementType(property) ?: return false
    return isValidCustomClass(elementType)
  }

  /** Checks if a single property represents a valid object type (custom class or interface). */
  private fun isValidSingleObjectType(property: KSPropertyDeclaration): Boolean {
    val typeDeclaration = property.type.resolve().declaration
    return typeDeclaration is KSClassDeclaration &&
      isValidCustomClass(typeDeclaration) &&
      !isStandardLibraryType(typeDeclaration.packageName.asString())
  }

  /** Validates that a class declaration is a custom class or interface (not enum). */
  private fun isValidCustomClass(classDeclaration: KSClassDeclaration): Boolean {
    return !isStandardLibraryType(classDeclaration.packageName.asString()) &&
      classDeclaration.classKind != ClassKind.ENUM_CLASS &&
      (classDeclaration.classKind == ClassKind.CLASS ||
        classDeclaration.classKind == ClassKind.INTERFACE)
  }
}
