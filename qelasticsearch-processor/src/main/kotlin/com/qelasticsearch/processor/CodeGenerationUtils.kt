package com.qelasticsearch.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Utility functions for code generation.
 */
class CodeGenerationUtils {
    /**
     * Checks if a type is a collection type.
     */
    fun isCollectionType(typeName: String): Boolean = typeName in
        setOf(
            "List",
            "MutableList",
            "ArrayList",
            "LinkedList",
            "Set",
            "MutableSet",
            "HashSet",
            "LinkedHashSet",
            "Collection",
            "MutableCollection",
            "Array",
            "Map",
            "MutableMap",
            "HashMap",
            "LinkedHashMap",
        )

    /**
     * Checks if a package is a standard library type.
     */
    fun isStandardLibraryType(packageName: String): Boolean = packageName.startsWith("kotlin.") ||
        packageName.startsWith("java.") ||
        packageName.startsWith("javax.") ||
        packageName == "kotlin" ||
        packageName == "java"

    /**
     * Gets the simple type name from a KSTypeReference.
     */
    fun getSimpleTypeName(type: KSTypeReference): String = type
        .resolve()
        .declaration.simpleName
        .asString()

    /**
     * Gets the collection element type from a property.
     */
    fun getCollectionElementType(property: KSPropertyDeclaration): KSClassDeclaration? = runCatching {
        val type = property.type.resolve()
        val typeArguments = type.arguments
        if (typeArguments.isNotEmpty()) {
            val firstArg = typeArguments.first()
            val argType = firstArg.type?.resolve()
            argType?.declaration as? KSClassDeclaration
        } else {
            null
        }
    }.getOrNull()

    /**
     * Recursively builds a type string with generics.
     */
    private fun buildTypeString(type: KSType): String {
        val declaration = type.declaration
        val simpleName = declaration.simpleName.asString()

        val arguments = type.arguments
        return if (arguments.isNotEmpty()) {
            val argStrings =
                arguments.mapNotNull { arg ->
                    when (arg.variance) {
                        Variance.INVARIANT -> {
                            arg.type?.let { buildTypeString(it.resolve()) }
                        }

                        else -> "*"
                    }
                }
            "$simpleName<${argStrings.joinToString(", ")}>"
        } else {
            simpleName
        }
    }

    /**
     * Create a KotlinPoet TypeName directly from KSType.
     */
    fun createKotlinPoetTypeName(kotlinType: KSType, typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver): TypeName =
        kotlinType.toTypeName(typeParameterResolver)

    /**
     * Simplify TypeName to string without adding imports.
     */
    private fun simplifyTypeNameWithoutImports(typeName: TypeName): String = when (typeName) {
        is ClassName -> typeName.simpleName
        is ParameterizedTypeName -> {
            val baseSimpleName = simplifyTypeNameWithoutImports(typeName.rawType)
            val typeArgs =
                typeName.typeArguments.joinToString(", ") { arg ->
                    simplifyTypeNameWithoutImports(arg)
                }
            "$baseSimpleName<$typeArgs>"
        }

        else -> typeName.toString()
    }

    /**
     * Extract imports and simplify type name.
     */
    fun extractImportsAndSimplifyTypeName(typeName: TypeName, usedImports: MutableSet<String>): String = when (typeName) {
        is ClassName -> {
            if (typeName.packageName.isNotEmpty() &&
                !typeName.packageName.startsWith("kotlin") &&
                !typeName.packageName.startsWith("java.lang")
            ) {
                usedImports.add("${typeName.packageName}.${typeName.simpleName}")
            }
            typeName.simpleName
        }

        is ParameterizedTypeName -> {
            val baseSimpleName = extractImportsAndSimplifyTypeName(typeName.rawType, usedImports)
            val typeArgs =
                typeName.typeArguments.joinToString(", ") { arg ->
                    extractImportsAndSimplifyTypeName(arg, usedImports)
                }
            "$baseSimpleName<$typeArgs>"
        }

        else -> typeName.toString()
    }

    /**
     * Extract FieldType from annotation, with default fallback.
     */
    fun extractFieldTypeFromAnnotation(annotation: KSAnnotation?): FieldType = if (annotation != null) {
        extractFieldType(annotation) ?: FieldType.Text
    } else {
        FieldType.Text
    }

    /**
     * Generates KDoc documentation for a generated field property.
     * This is shared between FieldGenerators and ObjectFieldRegistry.
     */
    fun generateFieldKDoc(property: KSPropertyDeclaration, fieldType: ProcessedFieldType, annotations: List<String> = emptyList()): String {
        val containingClass = property.parentDeclaration as? KSClassDeclaration
        val containingClassName = containingClass?.qualifiedName?.asString() ?: "Unknown"
        val propertyName = property.simpleName.asString()
        val elasticsearchType = fieldType.elasticsearchType.name

        val annotationInfo =
            if (annotations.isNotEmpty()) {
                " with ${annotations.joinToString(", ")}"
            } else {
                ""
            }

        return """
            |Elasticsearch field for property [$containingClassName.$propertyName].
            |
            |**Original Property:**
            |- Elasticsearch Type: `$elasticsearchType`$annotationInfo
            |
            |@see $containingClassName.$propertyName
        """.trimMargin()
    }

    /**
     * Extract FieldType from KSAnnotation.
     */
    private fun extractFieldType(fieldAnnotation: KSAnnotation): FieldType? = runCatching {
        val typeArgument = fieldAnnotation.arguments.find { it.name?.asString() == "type" }
        val value = typeArgument?.value

        when (value) {
            is KSType -> {
                val enumName = value.declaration.simpleName.asString()
                FieldType.entries.find { it.name == enumName }
            }

            is String -> {
                FieldType.entries.find { it.name == value }
            }

            else -> {
                val valueStr = value.toString()
                val enumName =
                    when {
                        valueStr.contains("FieldType.") -> valueStr.substringAfter("FieldType.")
                        valueStr.contains(".") -> valueStr.substringAfterLast(".")
                        else -> valueStr
                    }
                FieldType.entries.find { it.name == enumName }
            }
        }
    }.getOrNull()
}
