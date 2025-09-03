package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.springframework.data.elasticsearch.annotations.FieldType

/** Handles extraction and determination of field types. */
class FieldTypeExtractor(
  private val logger: KSPLogger,
) {
  /** Determines the field type for a property based on annotations. */
  fun determineFieldType(
    property: KSPropertyDeclaration,
    fieldAnnotation: KSAnnotation,
  ): ProcessedFieldType {
    val propertyName = property.simpleName.asString()

    // Extract field type from @Field annotation
    val fieldType = CodeGenerationUtils.extractFieldTypeFromAnnotation(fieldAnnotation)
    logger.info("Property $propertyName has @Field annotation with type: $fieldType")

    // Handle nested/object types - including collections
    val isObjectType =
      when (fieldType) {
        FieldType.Object,
        FieldType.Nested -> {
          // For collections, check the element type
          if (CodeGenerationUtils.isCollectionType(CodeGenerationUtils.getSimpleTypeName(property.type))) {
            val elementType = CodeGenerationUtils.getCollectionElementType(property)
            elementType != null &&
              !CodeGenerationUtils.isStandardLibraryType(elementType.packageName.asString()) &&
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
              !CodeGenerationUtils.isStandardLibraryType(typeDeclaration.packageName.asString())
          }
        }
        else -> false
      }

    val kotlinTypeName =
      if (CodeGenerationUtils.isCollectionType(CodeGenerationUtils.getSimpleTypeName(property.type))) {
        CodeGenerationUtils.getCollectionElementType(property)?.simpleName?.asString()
          ?: CodeGenerationUtils.getSimpleTypeName(property.type)
      } else {
        CodeGenerationUtils.getSimpleTypeName(property.type)
      }

    return ProcessedFieldType(
      elasticsearchType = fieldType,
      kotlinType = property.type,
      kotlinTypeName = kotlinTypeName,
      isObjectType = isObjectType,
    )
  }
}
