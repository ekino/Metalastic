package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/**
 * Handles generation of field properties for the DSL classes.
 */
class FieldGenerators(
    private val logger: KSPLogger,
    private val fieldTypeMappings: Map<FieldType, FieldTypeMapping>,
    private val codeGenUtils: CodeGenerationUtils,
) {
    /**
     * Context for field property generation.
     */
    data class FieldGenerationContext(
        val objectBuilder: TypeSpec.Builder,
        val property: KSPropertyDeclaration,
        val propertyName: String,
        val fieldType: ProcessedFieldType,
        val importContext: ImportContext,
        val typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    )

    /**
     * Processes a property and generates appropriate field property.
     */
    @Suppress("LongParameterList")
    fun processProperty(
        property: KSPropertyDeclaration,
        objectBuilder: TypeSpec.Builder,
        importContext: ImportContext,
        typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
        fieldTypeExtractor: FieldTypeExtractor,
        objectFieldRegistry: ObjectFieldRegistry,
    ) {
        val fieldAnnotation = property.findAnnotation(org.springframework.data.elasticsearch.annotations.Field::class)
        val multiFieldAnnotation = property.findAnnotation(MultiField::class)
        val idAnnotation = property.findAnnotation(org.springframework.data.annotation.Id::class)

        val propertyName = property.simpleName.asString()

        // Skip fields with no annotations at all
        if (fieldAnnotation == null && multiFieldAnnotation == null && idAnnotation == null) {
            logger.info("Property $propertyName has no annotations, ignoring field")
            return
        }

        when {
            multiFieldAnnotation != null -> {
                generateMultiFieldProperty(
                    objectBuilder,
                    property,
                    propertyName,
                    multiFieldAnnotation,
                    importContext,
                )
            }
            else -> {
                val fieldType = fieldTypeExtractor.determineFieldType(property, fieldAnnotation, idAnnotation)

                // Check if this is an Object or Nested field type
                // Only generate object fields if the field is actually an object type
                // (not just annotated as Object/Nested but containing primitives/enums)
                if (fieldType.isObjectType) {
                    objectFieldRegistry.generateObjectFieldProperty(objectBuilder, property, propertyName, fieldType)
                } else {
                    val context =
                        FieldGenerationContext(
                            objectBuilder,
                            property,
                            propertyName,
                            fieldType,
                            importContext,
                            typeParameterResolver,
                        )
                    generateSimpleFieldProperty(context)
                }
            }
        }
    }

    /**
     * Generates a simple field property (non-object/nested type).
     * Also handles Object/Nested types when they contain simple types like enums.
     */
    fun generateSimpleFieldProperty(context: FieldGenerationContext) {
        // Object/Nested fields should be handled by ObjectFieldRegistry only if they are actual object types
        // Fields annotated as Object/Nested but containing simple types (like enums) are handled here
        require(
            context.fieldType.elasticsearchType != FieldType.Object || !context.fieldType.isObjectType,
        ) {
            "Object fields with object types should be handled by ObjectFieldRegistry, not FieldGenerators"
        }

        // For Nested/Object types that are not actually object types (like enums),
        // treat them as keyword fields instead
        val (fieldClass, basicDelegate) =
            if (
                (context.fieldType.elasticsearchType == FieldType.Nested || context.fieldType.elasticsearchType == FieldType.Object) &&
                !context.fieldType.isObjectType
            ) {
                "KeywordField" to "keyword()"
            } else {
                getFieldClass(context.fieldType.elasticsearchType) to getFieldDelegate(context.fieldType.elasticsearchType)
            }
        val methodName = basicDelegate.substringBefore("()")

        // Create KotlinPoet TypeName directly from KSType
        val kotlinType = context.fieldType.kotlinType.resolve()
        val typeParam = codeGenUtils.createKotlinPoetTypeName(kotlinType, context.typeParameterResolver)
        val delegateCall =
            codeGenUtils.generateGenericDelegateCallForKSType(
                methodName,
                kotlinType,
                context.typeParameterResolver,
            )

        val finalTypeName = ClassName(DSLConstants.DSL_PACKAGE, fieldClass).parameterizedBy(typeParam)
        val kdoc = codeGenUtils.generateFieldKDoc(context.property, context.fieldType)

        context.objectBuilder.addProperty(
            PropertySpec
                .builder(context.propertyName, finalTypeName)
                .addKdoc(kdoc)
                .delegate(delegateCall)
                .build(),
        )
    }

    /**
     * Generates a multi-field property.
     */
    fun generateMultiFieldProperty(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        multiFieldAnnotation: KSAnnotation,
        importContext: ImportContext,
    ) {
        val mainFieldAnnotation = multiFieldAnnotation.getArgumentValue<KSAnnotation>("mainField")

        // Handle both single @InnerField and array of @InnerField
        val innerFields =
            when (
                val otherFieldsValue =
                    multiFieldAnnotation.arguments
                        .find {
                            it.name?.asString() == "otherFields"
                        }?.value
            ) {
                is List<*> -> otherFieldsValue.filterIsInstance<KSAnnotation>()
                is KSAnnotation -> listOf(otherFieldsValue)
                else -> emptyList()
            }

        val mainFieldType = codeGenUtils.extractFieldTypeFromAnnotation(mainFieldAnnotation)
        val mainFieldDelegate = getFieldDelegate(mainFieldType)

        if (innerFields.isEmpty()) {
            // Simple multi-field without inner fields
            generateSimpleMultiField(objectBuilder, property, propertyName, mainFieldType, mainFieldDelegate)
        } else {
            // Multi-field with inner fields
            generateComplexMultiField(
                objectBuilder,
                property,
                propertyName,
                mainFieldType,
                innerFields,
                importContext,
            )
        }
    }

    private fun generateSimpleMultiField(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        mainFieldType: FieldType,
        mainFieldDelegate: String,
    ) {
        val simpleFieldType =
            ProcessedFieldType(
                elasticsearchType = mainFieldType,
                kotlinType = property.type,
                kotlinTypeName = codeGenUtils.getSimpleTypeName(property.type),
                isObjectType = false,
            )
        val kdoc = codeGenUtils.generateFieldKDoc(property, simpleFieldType, listOf("@MultiField"))

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                .addKdoc(kdoc)
                .delegate(mainFieldDelegate)
                .build(),
        )
    }

    @Suppress("LongParameterList")
    private fun generateComplexMultiField(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        mainFieldType: FieldType,
        innerFields: List<KSAnnotation>,
        importContext: ImportContext,
    ) {
        val mainFieldClass = getFieldClass(mainFieldType)
        importContext.usedImports.add(mainFieldClass)
        importContext.usedImports.add(DSLConstants.MULTI_FIELD_PROXY)

        val innerFieldsCode =
            "\n        " +
                innerFields.joinToString(separator = "\n        ") { innerFieldAnnotation ->
                    val suffix = innerFieldAnnotation.getArgumentValue<String>("suffix") ?: "unknown"
                    val innerFieldType = codeGenUtils.extractFieldTypeFromAnnotation(innerFieldAnnotation)
                    val innerFieldClass = getFieldClass(innerFieldType)
                    importContext.usedImports.add(innerFieldClass)
                    "field(\"$suffix\") { $innerFieldClass<String>(\"$suffix\") }"
                } + "\n    "

        val complexFieldType =
            ProcessedFieldType(
                elasticsearchType = mainFieldType,
                kotlinType = property.type,
                kotlinTypeName = codeGenUtils.getSimpleTypeName(property.type),
                isObjectType = false,
            )
        val innerFieldsList = innerFields.map { it.getArgumentValue<String>("suffix") ?: "unknown" }
        val kdoc =
            codeGenUtils.generateFieldKDoc(
                property,
                complexFieldType,
                listOf("@MultiField", "inner fields: ${innerFieldsList.joinToString(", ")}"),
            )

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.MULTI_FIELD_PROXY))
                .addKdoc(kdoc)
                .delegate("multiFieldProxy($mainFieldClass<String>(\"$propertyName\")) {$innerFieldsCode}")
                .build(),
        )
    }

    /**
     * Get the DSL delegate method name for a given field type.
     */
    private fun getFieldDelegate(fieldType: FieldType): String = fieldTypeMappings[fieldType]?.delegate ?: "keyword()"

    /**
     * Get the DSL field class name for a given field type.
     */
    private fun getFieldClass(fieldType: FieldType): String = fieldTypeMappings[fieldType]?.className ?: "KeywordField"

    // Extension function to find annotations
    private fun KSPropertyDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? =
        annotations.find {
            it.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == annotationClass.qualifiedName
        }

    // Extension function to get argument values
    private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? = arguments.find { it.name?.asString() == name }?.value as? T
}
