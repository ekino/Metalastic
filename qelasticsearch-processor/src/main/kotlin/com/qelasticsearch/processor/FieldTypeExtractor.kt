package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Handles extraction and determination of field types.
 */
class FieldTypeExtractor(
    private val logger: KSPLogger,
    private val codeGenUtils: CodeGenerationUtils,
) {
    /**
     * Determines the field type for a property based on annotations.
     */
    fun determineFieldType(
        property: KSPropertyDeclaration,
        fieldAnnotation: KSAnnotation?,
        idAnnotation: KSAnnotation?,
    ): ProcessedFieldType {
        val propertyName = property.simpleName.asString()

        // If property has @Id annotation, treat as keyword by default
        if (idAnnotation != null) {
            logger.info("Property $propertyName has @Id annotation, using Keyword type")
            return ProcessedFieldType(
                elasticsearchType = FieldType.Keyword,
                kotlinType = property.type,
                kotlinTypeName = codeGenUtils.getSimpleTypeName(property.type),
                isObjectType = false,
            )
        }

        // If property has @Field annotation, use the specified type
        if (fieldAnnotation != null) {
            val fieldType = codeGenUtils.extractFieldTypeFromAnnotation(fieldAnnotation)
            logger.info("Property $propertyName has @Field annotation with type: $fieldType")

            // Handle nested/object types - including collections
            val isObjectType =
                when (fieldType) {
                    FieldType.Object, FieldType.Nested -> {
                        // For collections, check the element type
                        if (codeGenUtils.isCollectionType(codeGenUtils.getSimpleTypeName(property.type))) {
                            val elementType = codeGenUtils.getCollectionElementType(property)
                            elementType != null &&
                                !codeGenUtils.isStandardLibraryType(elementType.packageName.asString()) &&
                                (elementType.classKind == ClassKind.CLASS || elementType.classKind == ClassKind.INTERFACE)
                        } else {
                            // For single objects, check if it's a custom class or interface
                            val typeDeclaration = property.type.resolve().declaration
                            typeDeclaration is KSClassDeclaration &&
                                (typeDeclaration.classKind == ClassKind.CLASS || typeDeclaration.classKind == ClassKind.INTERFACE) &&
                                !codeGenUtils.isStandardLibraryType(typeDeclaration.packageName.asString())
                        }
                    }
                    else -> false
                }

            val kotlinTypeName =
                if (codeGenUtils.isCollectionType(codeGenUtils.getSimpleTypeName(property.type))) {
                    codeGenUtils.getCollectionElementType(property)?.simpleName?.asString()
                        ?: codeGenUtils.getSimpleTypeName(property.type)
                } else {
                    codeGenUtils.getSimpleTypeName(property.type)
                }

            return ProcessedFieldType(
                elasticsearchType = fieldType,
                kotlinType = property.type,
                kotlinTypeName = kotlinTypeName,
                isObjectType = isObjectType,
            )
        }

        // This should not happen since we check for annotations before calling this function
        error("Property $propertyName has neither @Field nor @Id annotation")
    }
}
