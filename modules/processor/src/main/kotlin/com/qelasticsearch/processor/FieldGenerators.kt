package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.qelasticsearch.core.QDynamicField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/** Handles generation of field properties for the core classes. */
class FieldGenerators(
  private val logger: KSPLogger,
  private val fieldTypeMappings: Map<FieldType, FieldTypeMapping>,
  private val codeGenUtils: CodeGenerationUtils,
) {

  companion object {
    private const val GETTER_PREFIX_LENGTH = 3
    private const val BOOLEAN_PREFIX_LENGTH = 2
  }

  /** Context for field property generation. */
  data class FieldGenerationContext(
    val objectBuilder: TypeSpec.Builder,
    val property: KSPropertyDeclaration,
    val propertyName: String,
    val fieldType: ProcessedFieldType,
    val importContext: ImportContext,
    val typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
  )

  /** Processes a property and generates appropriate field property. */
  @Suppress("LongParameterList")
  fun processProperty(
    property: KSPropertyDeclaration,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    fieldTypeExtractor: FieldTypeExtractor,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val fieldAnnotation =
      property.findAnnotation(org.springframework.data.elasticsearch.annotations.Field::class)
    val multiFieldAnnotation = property.findAnnotation(MultiField::class)
    val qDynamicFieldAnnotation = property.findAnnotation(QDynamicField::class)

    val propertyName = property.simpleName.asString()

    // Log dynamic field detection
    if (qDynamicFieldAnnotation != null) {
      logger.info("Found @QDynamicField on property: $propertyName")
    }

    // Skip fields with no annotations at all
    if (
      fieldAnnotation == null && multiFieldAnnotation == null && qDynamicFieldAnnotation == null
    ) {
      logger.info("Property $propertyName has no annotations, ignoring field")
      return
    }

    when {
      qDynamicFieldAnnotation != null -> {
        generateDynamicFieldProperty(
          objectBuilder,
          property,
          propertyName,
          importContext,
          typeParameterResolver,
        )
      }

      multiFieldAnnotation != null -> {
        generateMultiFieldProperty(
          objectBuilder,
          property,
          propertyName,
          multiFieldAnnotation,
          importContext,
        )
      }

      fieldAnnotation != null -> {
        val fieldType = fieldTypeExtractor.determineFieldType(property, fieldAnnotation)

        // Check if this is an Object or Nested field type
        // Only generate object fields if the field is actually an object type
        // (not just annotated as Object/Nested but containing primitives/enums)
        if (fieldType.isObjectType) {
          objectFieldRegistry.generateObjectFieldProperty(
            objectBuilder,
            property,
            propertyName,
            fieldType,
            importContext,
          )
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

  /** Processes an annotated method and generates appropriate field property. */
  fun processAnnotatedMethod(
    method: KSFunctionDeclaration,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
  ) {
    val fieldAnnotation =
      method.findFunctionAnnotation(org.springframework.data.elasticsearch.annotations.Field::class)
    if (fieldAnnotation == null) {
      logger.info("Method ${method.simpleName.asString()} has no @Field annotation, ignoring")
      return
    }

    val methodName = method.simpleName.asString()
    val propertyName = extractPropertyNameFromMethod(methodName)

    logger.info("Processing annotated method: $methodName -> property: $propertyName")

    // Create a ProcessedFieldType from the method return type and annotation
    val fieldType = codeGenUtils.extractFieldTypeFromAnnotation(fieldAnnotation)
    val returnType =
      method.returnType
        ?: run {
          logger.warn("Method $methodName has no return type, skipping")
          return
        }

    val processedFieldType =
      ProcessedFieldType(
        elasticsearchType = fieldType,
        kotlinType = returnType,
        kotlinTypeName = codeGenUtils.getSimpleTypeName(returnType),
        isObjectType = false, // For now, assume getter methods return simple types
      )

    // Generate the field property using the same logic as regular properties
    val context =
      FieldGenerationContext(
        objectBuilder,
        // Create a synthetic property from the method for compatibility
        createSyntheticPropertyFromMethod(method, propertyName),
        propertyName,
        processedFieldType,
        importContext,
        typeParameterResolver,
      )
    generateSimpleFieldProperty(context)
  }

  /** Helper function to extract property name from method name by removing common prefixes. */
  private fun extractPropertyNameFromMethod(methodName: String): String {
    return when {
      methodName.startsWith("get") &&
        methodName.length > GETTER_PREFIX_LENGTH &&
        methodName[GETTER_PREFIX_LENGTH].isUpperCase() -> {
        methodName.removePrefix("get").replaceFirstChar { it.lowercase() }
      }
      methodName.startsWith("is") &&
        methodName.length > BOOLEAN_PREFIX_LENGTH &&
        methodName[BOOLEAN_PREFIX_LENGTH].isUpperCase() -> {
        methodName.removePrefix("is").replaceFirstChar { it.lowercase() }
      }
      else -> {
        // For any other method name, use as-is (convert first char to lowercase if needed)
        methodName.replaceFirstChar { it.lowercase() }
      }
    }
  }

  /** Creates a synthetic KSPropertyDeclaration from a getter method for compatibility. */
  private fun createSyntheticPropertyFromMethod(
    method: KSFunctionDeclaration,
    propertyName: String,
  ): KSPropertyDeclaration {
    // For now, we'll create a minimal wrapper that provides the essential information
    return SyntheticPropertyFromMethod(method, propertyName)
  }

  /**
   * Generates a simple field property (non-object/nested type). Also handles Object/Nested types
   * when they contain simple types like enums.
   */
  fun generateSimpleFieldProperty(context: FieldGenerationContext) {
    // Object/Nested fields should be handled by ObjectFieldRegistry only if they are actual object
    // types
    // Fields annotated as Object/Nested but containing simple types (like enums) are handled here
    require(
      context.fieldType.elasticsearchType != FieldType.Object || !context.fieldType.isObjectType
    ) {
      "Object fields with object types should be handled by ObjectFieldRegistry, not FieldGenerators"
    }

    // For Nested/Object types that are not actually object types (like enums),
    // treat them as keyword fields instead
    val (fieldClass, basicDelegate) =
      if (
        (context.fieldType.elasticsearchType == FieldType.Nested ||
          context.fieldType.elasticsearchType == FieldType.Object) &&
          !context.fieldType.isObjectType
      ) {
        "KeywordField" to "keyword()"
      } else {
        getFieldClass(context.fieldType.elasticsearchType) to
          getFieldDelegate(context.fieldType.elasticsearchType)
      }
    val methodName = basicDelegate.substringBefore("()")

    // Create KotlinPoet TypeName directly from KSType
    val kotlinType = context.fieldType.kotlinType.resolve()
    val typeParam = codeGenUtils.createKotlinPoetTypeName(kotlinType, context.typeParameterResolver)
    val delegateCall = "$methodName()"
    val finalTypeName = ClassName(CoreConstants.CORE_PACKAGE, fieldClass).parameterizedBy(typeParam)
    val kdoc = codeGenUtils.generateFieldKDoc(context.property, context.fieldType)

    context.objectBuilder.addProperty(
      PropertySpec.builder(context.propertyName, finalTypeName)
        .addKdoc(kdoc)
        .delegate(delegateCall)
        .build()
    )
  }

  /** Generates a multi-field property. */
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
          multiFieldAnnotation.arguments.find { it.name?.asString() == "otherFields" }?.value
      ) {
        is List<*> -> otherFieldsValue.filterIsInstance<KSAnnotation>()
        is KSAnnotation -> listOf(otherFieldsValue)
        else -> emptyList()
      }

    val mainFieldType = codeGenUtils.extractFieldTypeFromAnnotation(mainFieldAnnotation)

    generateComplexMultiField(
      objectBuilder,
      property,
      propertyName,
      mainFieldType,
      innerFields,
      importContext,
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
    // Generate multifield using new MultiField<T : Field> approach
    val multiFieldClassName = "${propertyName.replaceFirstChar { it.uppercase() }}MultiField"
    val mainFieldClass = getFieldClass(mainFieldType)
    val mainFieldTypeName =
      ClassName(CoreConstants.CORE_PACKAGE, mainFieldClass)
        .parameterizedBy(String::class.asTypeName())

    importContext.usedImports.add(mainFieldClass)
    importContext.usedImports.add("MultiField")

    // Create the nested multifield class that extends MultiField<MainFieldType>
    val multiFieldClass =
      TypeSpec.classBuilder(multiFieldClassName)
        .superclass(
          ClassName(CoreConstants.CORE_PACKAGE, "MultiField").parameterizedBy(mainFieldTypeName)
        )
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("parent", ClassName(CoreConstants.CORE_PACKAGE, "ObjectField"))
            .addParameter("path", String::class)
            .build()
        )
        .addSuperclassConstructorParameter("parent")
        .addSuperclassConstructorParameter("$mainFieldClass(parent, path)")

    // Add fields for each inner field
    innerFields.forEach { innerFieldAnnotation ->
      val suffix = innerFieldAnnotation.getArgumentValue<String>("suffix") ?: "unknown"
      val innerFieldType = codeGenUtils.extractFieldTypeFromAnnotation(innerFieldAnnotation)
      val innerFieldClass = getFieldClass(innerFieldType)
      val innerFieldDelegate = getFieldDelegate(innerFieldType)

      importContext.usedImports.add(innerFieldClass)

      val kdocForInnerField =
        """
                Elasticsearch inner field for suffix '$suffix'.

                **Original @InnerField:**
                - Suffix: `$suffix`
                - Elasticsearch Type: `${innerFieldType.name}`
                """
          .trimIndent()

      multiFieldClass.addProperty(
        PropertySpec.builder(
            suffix,
            ClassName(CoreConstants.CORE_PACKAGE, innerFieldClass)
              .parameterizedBy(String::class.asTypeName()),
          )
          .addKdoc(kdocForInnerField)
          .delegate("${innerFieldDelegate.substringBefore("()")}<String>()")
          .build()
      )
    }

    // Add the nested class to the main object
    objectBuilder.addType(multiFieldClass.build())

    // Generate the property that uses object instantiation
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
      PropertySpec.builder(propertyName, ClassName("", multiFieldClassName))
        .addKdoc(kdoc)
        .delegate("multiField()")
        .build()
    )

    // Track used delegation function
    importContext.usedDelegationFunctions.add("multiField")
  }

  /** Get the DSL delegate method name for a given field type. */
  private fun getFieldDelegate(fieldType: FieldType): String =
    fieldTypeMappings[fieldType]?.delegate ?: "keyword()"

  /** Get the DSL field class name for a given field type. */
  private fun getFieldClass(fieldType: FieldType): String =
    fieldTypeMappings[fieldType]?.className ?: "KeywordField"

  /** Generates a dynamic field property for @QDynamicField annotated fields. */
  fun generateDynamicFieldProperty(
    objectBuilder: TypeSpec.Builder,
    property: KSPropertyDeclaration,
    propertyName: String,
    importContext: ImportContext,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
  ) {
    // Create KotlinPoet TypeName directly from KSType
    val kotlinType = property.type.resolve()
    val typeParam = codeGenUtils.createKotlinPoetTypeName(kotlinType, typeParameterResolver)
    val finalTypeName =
      ClassName(CoreConstants.CORE_PACKAGE, "DynamicField").parameterizedBy(typeParam)

    val kdoc =
      """
        Dynamic field for property [${property.parentDeclaration?.qualifiedName?.asString()}.${propertyName}].

        **Original Property:**
        - Annotated with @QDynamicField
        - Kotlin Type: `${codeGenUtils.getSimpleTypeName(property.type)}`

        @see ${property.parentDeclaration?.qualifiedName?.asString()}.${propertyName}
        """
        .trimIndent()

    objectBuilder.addProperty(
      PropertySpec.builder(propertyName, finalTypeName)
        .addKdoc(kdoc)
        .delegate("dynamicField()")
        .build()
    )

    // Add DynamicField to imports
    importContext.usedImports.add("DynamicField")
    importContext.usedDelegationFunctions.add("dynamicField")
  }

  // Extension function to find annotations
  private fun KSPropertyDeclaration.findAnnotation(
    annotationClass: kotlin.reflect.KClass<*>
  ): KSAnnotation? =
    annotations.find {
      it.annotationType.resolve().declaration.qualifiedName?.asString() ==
        annotationClass.qualifiedName
    }

  // Extension function to get argument values
  private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? =
    arguments.find { it.name?.asString() == name }?.value as? T

  // Extension function to find annotations on functions
  private fun KSFunctionDeclaration.findFunctionAnnotation(
    annotationClass: kotlin.reflect.KClass<*>
  ): KSAnnotation? =
    annotations.find {
      it.annotationType.resolve().declaration.qualifiedName?.asString() ==
        annotationClass.qualifiedName
    }
}
