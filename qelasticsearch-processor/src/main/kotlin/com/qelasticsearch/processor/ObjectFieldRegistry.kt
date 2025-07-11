package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Handles object field registration and generation.
 */
class ObjectFieldRegistry(
    private val logger: KSPLogger,
    private val codeGenUtils: CodeGenerationUtils,
    private val globalObjectFields: Map<String, ObjectFieldInfo>,
) {
    /**
     * Generates an object field property.
     */
    fun generateObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        fieldType: ProcessedFieldType,
    ) {
        val actualClassDeclaration = findActualClassDeclaration(fieldType)
        if (actualClassDeclaration == null) {
            logger.warn("Could not find class declaration for field type: ${fieldType.kotlinTypeName}")
            return
        }

        val objectFieldKey = generateObjectFieldKey(actualClassDeclaration)
        val objectFieldInfo = globalObjectFields[objectFieldKey]
        if (objectFieldInfo == null) {
            logger.warn("No ObjectField registered for key: $objectFieldKey (type: ${fieldType.kotlinTypeName})")
            return
        }

        val currentDocumentClass = findContainingDocumentClass(property)
        val isNestedInCurrentDocument = isNestedInCurrentDocument(objectFieldInfo, currentDocumentClass)

        val finalReferenceClassName = determineFinalReferenceClassName(objectFieldInfo, isNestedInCurrentDocument)
        val propertyTypeName = determinePropertyTypeName(objectFieldInfo, isNestedInCurrentDocument)

        val isNested = fieldType.elasticsearchType == FieldType.Nested
        val kdoc = codeGenUtils.generateFieldKDoc(property, fieldType, listOf("@${fieldType.elasticsearchType.name}"))

        val delegateCall =
            if (isNested) {
                "nestedField($finalReferenceClassName::class)"
            } else {
                "objectField($finalReferenceClassName::class)"
            }

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, propertyTypeName)
                .addKdoc(kdoc)
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun findActualClassDeclaration(fieldType: ProcessedFieldType): KSClassDeclaration? {
        val kotlinType = fieldType.kotlinType.resolve()

        return if (codeGenUtils.isCollectionType(codeGenUtils.getSimpleTypeName(fieldType.kotlinType))) {
            // For collections, get the element type
            val typeArguments = kotlinType.arguments
            if (typeArguments.isNotEmpty()) {
                val firstArg = typeArguments.first()
                firstArg.type?.resolve()?.declaration as? KSClassDeclaration
            } else {
                null
            }
        } else {
            // For direct object types
            kotlinType.declaration as? KSClassDeclaration
        }
    }

    private fun generateObjectFieldKey(classDeclaration: KSClassDeclaration): String =
        classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()

    private fun isNestedInCurrentDocument(
        objectFieldInfo: ObjectFieldInfo,
        currentDocumentClass: KSClassDeclaration?,
    ): Boolean =
        currentDocumentClass != null &&
            objectFieldInfo.parentDocumentClass == currentDocumentClass &&
            isActuallyNestedClass(objectFieldInfo.classDeclaration, currentDocumentClass) &&
            objectFieldInfo.packageName == currentDocumentClass.packageName.asString() &&
            objectFieldInfo.qualifiedName.contains("${currentDocumentClass.simpleName.asString()}.")

    private fun determineFinalReferenceClassName(
        objectFieldInfo: ObjectFieldInfo,
        isNestedInCurrentDocument: Boolean,
    ): String =
        if (isNestedInCurrentDocument) {
            // For nested classes within the same document, use the document's Q class name
            val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass!!)
            val parentQClassName = generateUniqueQClassName(rootDocumentClass)
            val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)
            "$parentQClassName.$nestedPath"
        } else if (objectFieldInfo.parentDocumentClass != null) {
            // For nested classes in other documents, use the correct parent Q class name
            val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass)
            val parentQClassName = generateUniqueQClassName(rootDocumentClass)
            val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)
            "$parentQClassName.$nestedPath"
        } else {
            // For standalone classes, use the Q-prefixed class name
            val simpleClassName = objectFieldInfo.classDeclaration.simpleName.asString()
            if (objectFieldInfo.className.startsWith("Q")) {
                objectFieldInfo.className
            } else {
                "Q$simpleClassName"
            }
        }

    private fun determinePropertyTypeName(
        objectFieldInfo: ObjectFieldInfo,
        isNestedInCurrentDocument: Boolean,
    ): com.squareup.kotlinpoet.TypeName =
        if (isNestedInCurrentDocument) {
            // For nested classes within the same document, use the nested path from the parent Q-class
            val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass!!)
            val parentQClassName = generateUniqueQClassName(rootDocumentClass)
            val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)

            // Build the nested class name step by step to avoid import issues
            val nestedParts = nestedPath.split(".")
            var currentClass = ClassName(objectFieldInfo.packageName, parentQClassName)
            for (part in nestedParts) {
                currentClass = currentClass.nestedClass(part)
            }
            currentClass
        } else if (objectFieldInfo.parentDocumentClass != null) {
            // For nested classes in other documents, use the parent Q-class as the outer class
            val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass)
            val parentQClassName = generateUniqueQClassName(rootDocumentClass)
            val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)

            // Build the nested class name step by step to avoid import issues
            val nestedParts = nestedPath.split(".")
            var currentClass = ClassName(objectFieldInfo.packageName, parentQClassName)
            for (part in nestedParts) {
                currentClass = currentClass.nestedClass(part)
            }
            currentClass
        } else {
            // For standalone classes, use the Q-prefixed class name
            val qClassName =
                if (objectFieldInfo.className.startsWith("Q")) {
                    objectFieldInfo.className
                } else {
                    "Q${objectFieldInfo.classDeclaration.simpleName.asString()}"
                }
            ClassName(objectFieldInfo.packageName, qClassName)
        }

    private fun generateUniqueQClassName(classDeclaration: KSClassDeclaration): String {
        val simpleName = classDeclaration.simpleName.asString()

        // Always use the simple Q class name to avoid naming conflicts
        return "Q$simpleName"
    }

    private fun extractNestedPath(
        objectFieldInfo: ObjectFieldInfo,
        rootDocumentClass: KSClassDeclaration,
    ): String {
        val qualifiedName = objectFieldInfo.qualifiedName
        val rootDocumentQualifiedName = rootDocumentClass.qualifiedName?.asString()

        return if (rootDocumentQualifiedName != null && qualifiedName.startsWith(rootDocumentQualifiedName)) {
            // Extract the path after the root document class
            val nestedPath = qualifiedName.substring(rootDocumentQualifiedName.length + 1)
            nestedPath
        } else {
            // Fallback to simple class name
            objectFieldInfo.classDeclaration.simpleName.asString()
        }
    }

    private fun findRootDocumentClass(classDeclaration: KSClassDeclaration): KSClassDeclaration {
        var currentClass = classDeclaration

        // Navigate up the hierarchy until we find the root document class
        while (currentClass.parentDeclaration is KSClassDeclaration) {
            val parentClass = currentClass.parentDeclaration as KSClassDeclaration

            // Check if parent has @Document annotation
            val hasDocumentAnnotation =
                parentClass.annotations.any { annotation ->
                    annotation.annotationType
                        .resolve()
                        .declaration.qualifiedName
                        ?.asString() == Document::class.qualifiedName
                }

            if (hasDocumentAnnotation) {
                return parentClass
            }

            currentClass = parentClass
        }

        // If we reach here, the current class itself should be the document class
        return currentClass
    }

    private fun isActuallyNestedClass(
        nestedClass: KSClassDeclaration,
        parentClass: KSClassDeclaration?,
    ): Boolean {
        if (parentClass == null) return false

        return parentClass.declarations.filterIsInstance<KSClassDeclaration>().any {
            it.qualifiedName?.asString() == nestedClass.qualifiedName?.asString()
        }
    }

    private fun findContainingDocumentClass(property: KSPropertyDeclaration): KSClassDeclaration? {
        var currentDeclaration: KSDeclaration? = property.parentDeclaration
        while (currentDeclaration != null) {
            if (currentDeclaration is KSClassDeclaration) {
                val hasDocumentAnnotation =
                    currentDeclaration.annotations.any { annotation ->
                        annotation.annotationType
                            .resolve()
                            .declaration.qualifiedName
                            ?.asString() == Document::class.qualifiedName
                    }
                if (hasDocumentAnnotation) {
                    return currentDeclaration
                }
            }
            currentDeclaration = currentDeclaration.parentDeclaration
        }
        return null
    }
}
