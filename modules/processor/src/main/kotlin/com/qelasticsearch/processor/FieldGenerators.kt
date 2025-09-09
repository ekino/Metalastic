package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.qelasticsearch.core.QDynamicField
import com.squareup.kotlinpoet.AnnotationSpec
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

  /** Collects all types that will be used by a property for import optimization. */
  fun collectPropertyTypes(
    property: KSPropertyDeclaration,
    importContext: ImportContext,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val fieldAnnotation =
      property.findAnnotation(org.springframework.data.elasticsearch.annotations.Field::class)
    val multiFieldAnnotation = property.findAnnotation(MultiField::class)
    val qDynamicFieldAnnotation = property.findAnnotation(QDynamicField::class)

    when {
      multiFieldAnnotation != null -> collectMultiFieldTypes(multiFieldAnnotation, importContext)
      qDynamicFieldAnnotation != null -> collectDynamicFieldTypes(importContext)
      fieldAnnotation != null ->
        collectBasicFieldTypes(property, fieldAnnotation, importContext, objectFieldRegistry)
      else ->
        logger.warn(
          "Property ${property.simpleName.asString()} has no @Field, @MultiField, or @QDynamicField annotation"
        )
    }
  }

  /** Processes a property and generates appropriate field property. */
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
        generateDynamicFieldProperty(objectBuilder, property, propertyName, typeParameterResolver)
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
    val fieldType = extractFieldTypeFromAnnotation(fieldAnnotation)
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
        kotlinTypeName = getSimpleTypeName(returnType),
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
    val fieldClass =
      if (
        (context.fieldType.elasticsearchType == FieldType.Nested ||
          context.fieldType.elasticsearchType == FieldType.Object) &&
          !context.fieldType.isObjectType
      ) {
        "KeywordField"
      } else {
        getFieldClass(context.fieldType.elasticsearchType)
      }

    // Create KotlinPoet TypeName directly from KSType
    val kotlinType = context.fieldType.kotlinType.resolve()
    val typeParam = createKotlinPoetTypeName(kotlinType, context.typeParameterResolver)
    val finalTypeName = ClassName(CoreConstants.CORE_PACKAGE, fieldClass).parameterizedBy(typeParam)
    val kdoc = generateFieldKDoc(context.property, context.fieldType)

    // Use ObjectField helper method for cleaner generated code
    val helperMethodName = getHelperMethodName(context.fieldType.elasticsearchType)
    val initializer = "${helperMethodName}(%S)"

    context.objectBuilder.addProperty(
      PropertySpec.builder(context.propertyName, finalTypeName)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .addKdoc(kdoc)
        .initializer(initializer, context.propertyName)
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

    val mainFieldType = extractFieldTypeFromAnnotation(mainFieldAnnotation)

    generateComplexMultiField(
      objectBuilder,
      property,
      propertyName,
      mainFieldType,
      innerFields,
      importContext,
    )
  }

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
      val innerFieldType = extractFieldTypeFromAnnotation(innerFieldAnnotation)
      val innerFieldClass = getFieldClass(innerFieldType)

      importContext.usedImports.add(innerFieldClass)

      val kdocForInnerField =
        """
                Elasticsearch inner field for suffix '$suffix'.

                **Original @InnerField:**
                - Suffix: `$suffix`
                - Elasticsearch Type: `${innerFieldType.name}`
                """
          .trimIndent()

      val helperMethodName = getHelperMethodName(innerFieldType)
      val typeArguments = if (needsTypeArgument(innerFieldType)) "<String>" else ""
      val initializer = "${helperMethodName}${typeArguments}(%S)"

      multiFieldClass.addProperty(
        PropertySpec.builder(
            suffix,
            ClassName(CoreConstants.CORE_PACKAGE, innerFieldClass)
              .parameterizedBy(String::class.asTypeName()),
          )
          .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
          .addKdoc(kdocForInnerField)
          .initializer(initializer, suffix)
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
        kotlinTypeName = getSimpleTypeName(property.type),
        isObjectType = false,
      )
    val innerFieldsList = innerFields.map { it.getArgumentValue<String>("suffix") ?: "unknown" }
    val kdoc =
      generateFieldKDoc(
        property,
        complexFieldType,
        listOf("@MultiField", "inner fields: ${innerFieldsList.joinToString(", ")}"),
      )

    // Direct initialization instead of delegation for Java interop
    val multiFieldTypeName = ClassName("", multiFieldClassName)
    val initializer = "%T(this, %S)"

    objectBuilder.addProperty(
      PropertySpec.builder(propertyName, multiFieldTypeName)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .addKdoc(kdoc)
        .initializer(initializer, multiFieldTypeName, propertyName)
        .build()
    )
  }

  /** Get the DSL field class name for a given field type. */
  private fun getFieldClass(fieldType: FieldType): String =
    fieldTypeMappings[fieldType]?.className ?: "KeywordField"

  /** Get the helper method name for a given field type. */
  @Suppress("CyclomaticComplexMethod")
  private fun getHelperMethodName(fieldType: FieldType): String =
    when (fieldType) {
      FieldType.Text -> "textField"
      FieldType.Keyword -> "keywordField"
      FieldType.Long -> "longField"
      FieldType.Integer -> "integerField"
      FieldType.Short -> "shortField"
      FieldType.Byte -> "byteField"
      FieldType.Double -> "doubleField"
      FieldType.Float -> "floatField"
      FieldType.Boolean -> "booleanField"
      FieldType.Binary -> "binaryField"
      FieldType.Date -> "dateField"
      FieldType.Object -> "keywordField" // Object fields handled separately
      FieldType.Nested -> "keywordField" // Nested fields handled separately
      else -> {
        // For advanced field types that may not exist in all versions, use safe access
        when (fieldType.name) {
          "Half_Float" -> "halfFloatField"
          "Scaled_Float" -> "scaledFloatField"
          "Date_Nanos" -> "dateNanosField"
          "Ip" -> "ipField"
          "Geo_Point" -> "geoPointField"
          "Geo_Shape" -> "geoShapeField"
          "Completion" -> "completionField"
          "TokenCount" -> "tokenCountField"
          "Percolator" -> "percolatorField"
          "Rank_Feature" -> "rankFeatureField"
          "Rank_Features" -> "rankFeaturesField"
          "Flattened" -> "flattenedField"
          "Wildcard" -> "wildcardField"
          "Constant_Keyword" -> "constantKeywordField"
          "Integer_Range" -> "integerRangeField"
          "Float_Range" -> "floatRangeField"
          "Long_Range" -> "longRangeField"
          "Double_Range" -> "doubleRangeField"
          "Date_Range" -> "dateRangeField"
          "Ip_Range" -> "ipRangeField"
          else -> "keywordField" // fallback to keyword
        }
      }
    }

  /** Check if the field type needs a type argument in the helper method call. */
  private fun needsTypeArgument(fieldType: FieldType): Boolean =
    when (fieldType) {
      FieldType.Text,
      FieldType.Keyword,
      FieldType.Long,
      FieldType.Integer,
      FieldType.Short,
      FieldType.Byte,
      FieldType.Double,
      FieldType.Float,
      FieldType.Date,
      FieldType.Boolean -> true
      else -> false
    }

  /** Generates a dynamic field property for @QDynamicField annotated fields. */
  fun generateDynamicFieldProperty(
    objectBuilder: TypeSpec.Builder,
    property: KSPropertyDeclaration,
    propertyName: String,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
  ) {
    // Create KotlinPoet TypeName directly from KSType
    val kotlinType = property.type.resolve()
    val typeParam = createKotlinPoetTypeName(kotlinType, typeParameterResolver)
    val finalTypeName =
      ClassName(CoreConstants.CORE_PACKAGE, "DynamicField").parameterizedBy(typeParam)

    val kdoc =
      """
        Dynamic field for property [${property.parentDeclaration?.qualifiedName?.asString()}.${propertyName}].

        **Original Property:**
        - Annotated with @QDynamicField
        - Kotlin Type: `${getSimpleTypeName(property.type)}`

        @see ${property.parentDeclaration?.qualifiedName?.asString()}.${propertyName}
        """
        .trimIndent()

    // Use helper method for cleaner generated code - type is inferred from reified inline function
    val initializer = "dynamicField(%S)"

    objectBuilder.addProperty(
      PropertySpec.builder(propertyName, finalTypeName)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .addKdoc(kdoc)
        .initializer(initializer, propertyName)
        .build()
    )
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

  // ================== Type Collection Methods for Import Optimization ==================

  private fun collectBasicFieldTypes(
    property: KSPropertyDeclaration,
    fieldAnnotation: KSAnnotation,
    importContext: ImportContext,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val fieldTypeExtractor = FieldTypeExtractor(logger)
    val fieldType = fieldTypeExtractor.determineFieldType(property, fieldAnnotation)

    if (fieldType.isObjectType) {
      // For object/nested types, let ObjectFieldRegistry handle the type collection
      objectFieldRegistry.collectObjectFieldType(property, fieldType, importContext)
    } else {
      // For basic field types, register the field class
      val fieldClass = getFieldClass(fieldType.elasticsearchType)
      importContext.registerTypeUsage("${CoreConstants.CORE_PACKAGE}.$fieldClass")
    }
  }

  private fun collectMultiFieldTypes(
    multiFieldAnnotation: KSAnnotation,
    importContext: ImportContext,
  ) {
    // Register MultiField type
    importContext.registerTypeUsage("${CoreConstants.CORE_PACKAGE}.MultiField")

    // Collect main field type
    val mainField =
      multiFieldAnnotation.arguments.find { it.name?.asString() == "mainField" }?.value
        as? KSAnnotation
    if (mainField != null) {
      val mainFieldType = extractFieldTypeFromAnnotation(mainField)
      val mainFieldClass = getFieldClass(mainFieldType)
      importContext.registerTypeUsage("${CoreConstants.CORE_PACKAGE}.$mainFieldClass")
    }

    // Collect inner field types
    val otherFields =
      multiFieldAnnotation.arguments.find { it.name?.asString() == "otherFields" }?.value
        as? List<*>
    otherFields?.forEach { otherField ->
      if (otherField is KSAnnotation) {
        val innerFieldType = extractFieldTypeFromAnnotation(otherField)
        val innerFieldClass = getFieldClass(innerFieldType)
        importContext.registerTypeUsage("${CoreConstants.CORE_PACKAGE}.$innerFieldClass")
      }
    }
  }

  private fun collectDynamicFieldTypes(importContext: ImportContext) {
    importContext.registerTypeUsage("${CoreConstants.CORE_PACKAGE}.DynamicField")
  }
}
