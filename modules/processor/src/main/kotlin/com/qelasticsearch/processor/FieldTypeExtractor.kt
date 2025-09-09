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
    val isObjectType =
      when (fieldType) {
        FieldType.Object,
        FieldType.Nested -> {
          // For collections, check the element type
          if (isCollectionType(getSimpleTypeName(property.type))) {
            val elementType = getCollectionElementType(property)
            elementType != null &&
              !isStandardLibraryType(elementType.packageName.asString()) &&
              elementType.classKind != ClassKind.ENUM_CLASS &&
              (elementType.classKind == ClassKind.CLASS ||
                elementType.classKind == ClassKind.INTERFACE)
          } else {
            // For single objects, check if it's a custom class or interface
            val typeDeclaration = property.type.resolve().declaration
            typeDeclaration is KSClassDeclaration &&
              typeDeclaration.classKind != ClassKind.ENUM_CLASS &&
              (typeDeclaration.classKind == ClassKind.CLASS ||
                typeDeclaration.classKind == ClassKind.INTERFACE) &&
              !isStandardLibraryType(typeDeclaration.packageName.asString())
          }
        }
        else -> false
      }

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
}
