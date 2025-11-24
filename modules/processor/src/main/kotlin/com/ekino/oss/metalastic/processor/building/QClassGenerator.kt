/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.building

import com.ekino.oss.metalastic.processor.CoreConstants
import com.ekino.oss.metalastic.processor.CoreConstants.DocumentClass.INDEX_NAME_CONSTANT
import com.ekino.oss.metalastic.processor.CoreConstants.PRODUCT_NAME
import com.ekino.oss.metalastic.processor.CoreConstants.SPRING_DATA_ELASTICSEARCH_PACKAGE
import com.ekino.oss.metalastic.processor.MetalasticSymbolProcessor
import com.ekino.oss.metalastic.processor.collecting.fullyQualifiedName
import com.ekino.oss.metalastic.processor.collecting.toSafeTypeName
import com.ekino.oss.metalastic.processor.model.FieldModel
import com.ekino.oss.metalastic.processor.model.InnerFieldModel
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.model.MultiFieldModel
import com.ekino.oss.metalastic.processor.model.ObjectFieldModel
import com.ekino.oss.metalastic.processor.model.SimpleFieldModel
import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.ekino.oss.metalastic.processor.report.reporter
import com.google.devtools.ksp.getAnnotationsByType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import java.util.Date
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

/** Pure Meta-class TypeSpec generation. */
class QClassGenerator(
  private val rootModel: MetalasticGraph.MetaClassModel,
  private val options: ProcessorOptions,
) {
  val typeParameterResolver =
    rootModel.sourceClassDeclaration.typeParameters.toTypeParameterResolver()

  companion object {
    val SYMBOL_PROCESSOR_FQN = MetalasticSymbolProcessor::class.qualifiedName!!
    val documentClass =
      ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.DocumentClass.SIMPLE_NAME)
    val objectFieldClass =
      ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.ObjectFieldClass.SIMPLE_NAME)
    val multifieldClass =
      ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.MultiFieldClass.SIMPLE_NAME)
    val unModellableObjectClass =
      ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.UnModellableObjectClass.SIMPLE_NAME)
    val selfReferencingObjectClass =
      ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.SelfReferencingObjectClass.SIMPLE_NAME)
    val typeParameterTAny = TypeVariableName("T : Any?")
    val typeParameterT = TypeVariableName("T")
    val kType = ClassName("kotlin.reflect", "KType")
  }

  /** Entry point to build TypeSpec for the root model and its inner classes */
  fun buildFieldSpec(): FileSpec =
    FileSpec.builder(rootModel.packageName, rootModel.qClassName)
      .addType(buildTypeSpec(rootModel))
      .addImport("kotlin.reflect", "typeOf")
      .build()

  private fun buildTypeSpec(model: MetalasticGraph.MetaClassModel): TypeSpec {
    reporter.debug { "Generating TypeSpec for ${model::class.simpleName}: ${model.qClassName}" }
    val classBuilder = TypeSpec.classBuilder(model.qClassName).addModifiers(KModifier.PUBLIC)

    if (!model.isNested) {
      classBuilder.addGeneratedAnnotation()
    }

    // Setup based on model type
    when (model) {
      is MetalasticGraph.DocumentClass -> {
        // Add generic type parameter T

        classBuilder.addTypeVariable(typeParameterTAny)
        val parameterizedDocumentClass = documentClass.parameterizedBy(typeParameterT)

        classBuilder
          .superclass(parameterizedDocumentClass)
          .primaryConstructor(buildDocumentConstructor())
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.PARENT_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.NAME_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.NESTED_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.FIELD_TYPE_PROPERTY.namedArgument
          )
          .addFunction(buildIndexNameFunction())
          .addKdoc(generateDocumentKdoc(model))
      }

      is MetalasticGraph.ObjectClass -> {
        // Add generic type parameter T:Any
        classBuilder.addTypeVariable(typeParameterTAny)

        // Create parameterized ObjectField type
        val parameterizedObjectField = objectFieldClass.parameterizedBy(typeParameterT)

        classBuilder
          .superclass(parameterizedObjectField)
          .primaryConstructor(buildObjectFieldConstructor())
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.PARENT_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.NAME_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.NESTED_PROPERTY.namedArgument
          )
          .addSuperclassConstructorParameter(
            CoreConstants.ObjectFieldClass.FIELD_TYPE_PROPERTY.namedArgument
          )
          .addKdoc(generateObjectFieldKdoc(model))
      }
    }

    // Add field properties (common for all models)
    model.fields.forEach { field ->
      val fieldProperty = generateFieldProperty(field)
      classBuilder.addProperty(fieldProperty)
    }

    // Add MultiField inner classes (common for all models)
    model.fields.filterIsInstance<MultiFieldModel>().forEach { multiField ->
      val multiFieldClass = generateMultiFieldClass(multiField)
      classBuilder.addType(multiFieldClass)
    }

    // Add nested classes recursively (common for all models)
    model.nestedClasses().forEach { nestedModel ->
      reporter.debug {
        "Generating nested class ${nestedModel.qClassName} for parent ${model.qClassName}"
      }
      val nestedTypeSpec = buildTypeSpec(nestedModel) // RECURSIVE!
      classBuilder.addType(nestedTypeSpec)
    }

    // Add companion object only for documents
    if (model is MetalasticGraph.DocumentClass) {
      val companionObject = generateCompanionObject(model)
      classBuilder.addType(companionObject)
    }

    return classBuilder.build()
  }

  /** Generates a field property based on the field model type. */
  private fun generateFieldProperty(field: FieldModel): PropertySpec {
    return when (field) {
      is SimpleFieldModel -> generateSimpleFieldProperty(field)
      is ObjectFieldModel -> generateObjectFieldProperty(field)
      is MultiFieldModel -> generateMultiFieldProperty(field)
    }
  }

  /** Generates a simple field property (Text, Keyword, Date, etc.). */
  private fun generateSimpleFieldProperty(field: SimpleFieldModel): PropertySpec {
    val fieldTypeClass = FieldTypeMappings.classOf(field.fieldType)
    val sourceTypeName = field.type.toSafeTypeName(typeParameterResolver)
    val typeName = fieldTypeClass.className.parameterizedBy(sourceTypeName)

    if (field.fieldType == FieldType.Date) {
      val formats =
        field.sourceDeclaration.getAnnotationsByType(Field::class).first().format.toList()

      val formatArgs =
        if (formats.isNotEmpty()) {
          formats.map { ClassName(SPRING_DATA_ELASTICSEARCH_PACKAGE, "DateFormat") to it.name }
        } else {
          emptyList()
        }

      val formatString = buildString {
        append("%L(%S")
        if (formatArgs.isNotEmpty()) {
          append(", listOf(")
          append(formatArgs.joinToString(", ") { "%T.%L" })
          append(")")
        }
        append(")")
      }

      val initializerArgs = buildList {
        add(fieldTypeClass.helperMethodName)
        add(field.elasticsearchFieldName)
        formatArgs.forEach { (className, formatName) ->
          add(className)
          add(formatName)
        }
      }

      @Suppress("SpreadOperator") // KotlinPoet requires varargs
      return PropertySpec.builder(field.name, typeName)
        .addModifiers(KModifier.PUBLIC)
        .initializer(formatString, *initializerArgs.toTypedArray())
        .addKdoc(generateFieldKDoc(field))
        .withOptionalJavaCompatibility()
        .build()
    }

    return PropertySpec.builder(field.name, typeName)
      .addModifiers(KModifier.PUBLIC)
      .initializer("%L(%S)", fieldTypeClass.helperMethodName, field.elasticsearchFieldName)
      .addKdoc(generateFieldKDoc(field))
      .withOptionalJavaCompatibility()
      .build()
  }

  /** Generates an object field property. */
  private fun generateObjectFieldProperty(field: ObjectFieldModel): PropertySpec {
    return when {
      field.targetModel == null -> generateUnModellableObjectField(field)
      field.parentModel.fullyQualifiedName == field.targetModel.fullyQualifiedName ->
        generateSelfReferencingObjectField(field)
      else -> generateRegularObjectField(field)
    }
  }

  /** Generates a regular object field property with a target model. */
  private fun generateRegularObjectField(field: ObjectFieldModel): PropertySpec {
    val qClassName = field.targetModel!!.toClassName()
    val sourceTypeName = field.type.toSafeTypeName(typeParameterResolver)

    val parameterizedTypeName = qClassName.parameterizedBy(sourceTypeName)
    return PropertySpec.builder(field.name, parameterizedTypeName)
      .addModifiers(KModifier.PUBLIC)
      .objectFieldInitializer(field, qClassName)
      .addKdoc(generateFieldKDoc(field))
      .withOptionalJavaCompatibility()
      .build()
  }

  private fun PropertySpec.Builder.objectFieldInitializer(
    field: ObjectFieldModel,
    typeName: TypeName,
  ) = apply {
    initializer(
      "%T(this, %S, %L, typeOf<%T>())",
      typeName,
      field.elasticsearchFieldName,
      field.nested,
      field.type.toSafeTypeName(typeParameterResolver),
    )
  }

  /**
   * generates an object field property for terminal objects without a target model or
   * self-referencing fields
   */
  private fun generateUnModellableObjectField(field: ObjectFieldModel): PropertySpec {
    // Create ObjectField<*> type for terminal objects
    val sourceTypeName = field.type.toSafeTypeName(typeParameterResolver)
    val objectFieldType = unModellableObjectClass.parameterizedBy(sourceTypeName)

    val kdocDescription =
      """
        |**🚫 UN-HANDLED collection or generic type**
        |
        |`${field.name}` with original type `$sourceTypeName` cannot be modeled.
        |
        """
        .trimMargin()

    return PropertySpec.builder(field.name, objectFieldType)
      .apply {
        initializer(
          "%T(this, %S, %L, typeOf<%T>())",
          objectFieldType,
          field.elasticsearchFieldName,
          field.nested,
          sourceTypeName,
        )
      }
      .addKdoc(generateFieldKDoc(field, kdocDescription))
      .withOptionalJavaCompatibility()
      .build()
  }

  private fun generateSelfReferencingObjectField(field: ObjectFieldModel): PropertySpec {
    // Create ObjectField<*> type for terminal objects
    val sourceTypeName = field.type.toSafeTypeName(typeParameterResolver)
    val objectFieldType = selfReferencingObjectClass.parameterizedBy(sourceTypeName)

    val kdocDescription =
      """
        | **∞ SELF-REFERENCING FIELD**
        |
        |`${field.name}` is a self reference to [${field.targetModel?.sourceClassDeclaration?.fullyQualifiedName()}].
        |
        |Using a terminal object to avoid infinite recursion.
        |
        """
        .trimMargin()

    return PropertySpec.builder(field.name, objectFieldType)
      .apply {
        initializer(
          "%T(this, %S, %L, typeOf<%T>())",
          objectFieldType,
          field.elasticsearchFieldName,
          field.nested,
          sourceTypeName,
        )
      }
      .addKdoc(generateFieldKDoc(field, kdocDescription))
      .withOptionalJavaCompatibility()
      .build()
  }

  /** Generates a multi-field property with custom inner class. */
  private fun generateMultiFieldProperty(field: MultiFieldModel): PropertySpec {
    val multifieldClassName = ClassName("", field.className)

    // Create parameterized MultiField class type
    val parameterizedMultiFieldClassName = multifieldClassName

    return PropertySpec.builder(field.name, parameterizedMultiFieldClassName)
      .addModifiers(KModifier.PUBLIC)
      .initializer("%L(this, %S)", multifieldClassName, field.elasticsearchFieldName)
      .addKdoc(generateFieldKDoc(field))
      .withOptionalJavaCompatibility()
      .build()
  }

  /** Generates a MultiField inner class with main field and inner field properties. */
  private fun generateMultiFieldClass(field: MultiFieldModel): TypeSpec {
    val className = field.className

    // Determine main field type
    val fieldTypeClass = FieldTypeMappings.classOf(field.mainFieldType)

    // Get the value type from the source field type (e.g., String, List<String>)
    val valueTypeName = field.type.toTypeName(typeParameterResolver)

    // Create the main field type (e.g., TextField<String>)
    val mainFieldType = fieldTypeClass.className.parameterizedBy(valueTypeName)

    // MultiField uses dual generics: MultiField<ValueType, MainFieldType>
    val parameterizedMultiFieldType = multifieldClass.parameterizedBy(valueTypeName, mainFieldType)

    val classBuilder =
      TypeSpec.classBuilder(className)
        .addModifiers(KModifier.PUBLIC, KModifier.INNER)
        .superclass(parameterizedMultiFieldType)
        .primaryConstructor(buildMultiFieldConstructor())
        .addSuperclassConstructorParameter(CoreConstants.MultiFieldClass.PARENT_PROPERTY.name)
        .addSuperclassConstructorParameter(
          "%T(${CoreConstants.MultiFieldClass.PARENT_PROPERTY.name}, ${CoreConstants.MultiFieldClass.MAIN_FIELD_PROPERTY}, typeOf<%T>())",
          fieldTypeClass.className,
          field.type.toSafeTypeName(typeParameterResolver),
        )
        .addSuperclassConstructorParameter(
          "typeOf<%T>()",
          field.type.toSafeTypeName(typeParameterResolver),
        )
        .addKdoc(generateMultiFieldClassKDoc(field))

    // Add inner field properties for each @InnerField
    field.innerFields.forEach { innerField ->
      val innerProperty = generateInnerFieldProperty(innerField)
      classBuilder.addProperty(innerProperty)
    }

    return classBuilder.build()
  }

  /** Builds constructor for MultiField classes. */
  private fun buildMultiFieldConstructor(): FunSpec {
    return FunSpec.constructorBuilder()
      .addParameter(
        CoreConstants.MultiFieldClass.PARENT_PROPERTY.name,
        objectFieldClass.parameterizedBy(com.squareup.kotlinpoet.STAR),
      )
      .addParameter(CoreConstants.MultiFieldClass.MAIN_FIELD_PROPERTY, String::class)
      .build()
  }

  /** Generates an inner field property for @InnerField annotations. */
  private fun generateInnerFieldProperty(innerField: InnerFieldModel): PropertySpec {
    val fieldTypeClass = FieldTypeMappings.classOf(innerField.fieldType)

    val typeParameter = getKotlinTypeForInnerField(innerField.fieldType)
    val fieldType = fieldTypeClass.className.parameterizedBy(typeParameter)

    return PropertySpec.builder(innerField.suffix, fieldType)
      .addModifiers(KModifier.PUBLIC)
      .initializer("%L<%T>(%S)", fieldTypeClass.helperMethodName, typeParameter, innerField.suffix)
      .addKdoc(generateInnerFieldKDoc(innerField))
      .withOptionalJavaCompatibility()
      .build()
  }

  /** Builds constructor for @Document classes. */
  private fun buildDocumentConstructor(): FunSpec {
    return FunSpec.constructorBuilder()
      .addParameter(
        ParameterSpec.builder(
            CoreConstants.ObjectFieldClass.PARENT_PROPERTY.name,
            objectFieldClass.parameterizedBy(com.squareup.kotlinpoet.STAR).copy(nullable = true),
          )
          .defaultValue("null")
          .build()
      )
      .addParameter(
        ParameterSpec.builder(CoreConstants.ObjectFieldClass.NAME_PROPERTY.name, String::class)
          .defaultValue("\"\"")
          .build()
      )
      .addParameter(
        ParameterSpec.builder(CoreConstants.ObjectFieldClass.NESTED_PROPERTY.name, Boolean::class)
          .defaultValue("false")
          .build()
      )
      .addParameter(
        ParameterSpec.builder(CoreConstants.ObjectFieldClass.FIELD_TYPE_PROPERTY.name, kType)
          .build()
      )
      .build()
  }

  /** Builds constructor for ObjectField classes. */
  private fun buildObjectFieldConstructor(): FunSpec {
    return FunSpec.constructorBuilder()
      .addParameter(
        CoreConstants.ObjectFieldClass.PARENT_PROPERTY.name,
        objectFieldClass.parameterizedBy(com.squareup.kotlinpoet.STAR).copy(nullable = true),
      )
      .addParameter(CoreConstants.ObjectFieldClass.NAME_PROPERTY.name, String::class)
      .addParameter(
        ParameterSpec.builder(CoreConstants.ObjectFieldClass.NESTED_PROPERTY.name, Boolean::class)
          .defaultValue("false")
          .build()
      )
      .addParameter(
        ParameterSpec.builder(CoreConstants.ObjectFieldClass.FIELD_TYPE_PROPERTY.name, kType)
          .build()
      )
      .build()
  }

  /** Builds indexName function for Document interface implementation. */
  private fun buildIndexNameFunction(): FunSpec {
    return FunSpec.builder(CoreConstants.DocumentClass.INDEX_NAME_FUNCTION)
      .addModifiers(KModifier.OVERRIDE)
      .returns(String::class)
      .addStatement("return %L", INDEX_NAME_CONSTANT)
      .build()
  }

  /** Generates companion object for Metamodels registry access. */
  private fun generateCompanionObject(document: MetalasticGraph.DocumentClass): TypeSpec {
    val companionBuilder = TypeSpec.companionObjectBuilder()

    // Build the ClassName for the Meta-class
    val qClassName = document.toClassName()
    // Use the source class type for the generic parameter
    val sourceTypeName =
      document.sourceClassDeclaration.asStarProjectedType().toSafeTypeName(typeParameterResolver)
    val parameterizedTypeName = qClassName.parameterizedBy(sourceTypeName)

    val indexNameConstant =
      PropertySpec.builder(INDEX_NAME_CONSTANT, String::class)
        .addModifiers(KModifier.CONST)
        .initializer("%S", document.indexName)
        .addKdoc(generateCompanionPropertyKdoc(document))
        .build()

    val property =
      PropertySpec.builder(document.companionPropertyName, parameterizedTypeName)
        .addModifiers(KModifier.PUBLIC)
        .initializer("%T(fieldType = typeOf<%T>())", qClassName, sourceTypeName)
        .addKdoc(generateCompanionPropertyKdoc(document))
        .withOptionalJavaCompatibility()
        .build()

    companionBuilder.addProperty(indexNameConstant)
    companionBuilder.addProperty(property)
    return companionBuilder.build()
  }

  private fun generateDocumentKdoc(document: MetalasticGraph.DocumentClass): String {
    val hierarchy = buildFieldHierarchy(document)
    return buildString {
      appendLine("Metamodel for Elasticsearch index `${document.indexName}`.")
      appendLine()
      appendLine("This class was automatically generated by $PRODUCT_NAME annotation processor")
      appendLine("from the source class [${document.sourceClassDeclaration.fullyQualifiedName()}].")
      appendLine()
      appendLine("## Field Hierarchy")
      appendLine("```")
      appendLine("${document.qClassName}<T>")
      if (hierarchy.isNotEmpty()) {
        appendLine(hierarchy)
      }
      appendLine("```")
      appendLine()
      appendLine("**Do not modify this file directly.** Any changes will be overwritten")
      appendLine("during the next compilation. To modify the metamodel structure, update the")
      appendLine("annotations on the source document class.")
      appendLine()
      append("@see ${document.sourceClassDeclaration.fullyQualifiedName()}")
    }
  }

  private fun generateObjectFieldKdoc(objectModel: MetalasticGraph.MetaClassModel): String {
    val className = objectModel.fullyQualifiedName.substringAfterLast(".")
    val hierarchy = buildFieldHierarchy(objectModel)
    return buildString {
      appendLine("Metamodel for class `$className`.")
      appendLine()
      appendLine("This class was automatically generated by $PRODUCT_NAME annotation processor")
      appendLine(
        "from the source class [${objectModel.sourceClassDeclaration.fullyQualifiedName()}]."
      )
      appendLine()
      appendLine("## Field Hierarchy")
      appendLine("```")
      appendLine("${objectModel.qClassName}<T>")
      if (hierarchy.isNotEmpty()) {
        appendLine(hierarchy)
      }
      appendLine("```")
      appendLine()
      appendLine("**Do not modify this file directly.** Any changes will be overwritten")
      appendLine("during the next compilation. To modify the metamodel structure, update the")
      appendLine("annotations on the source class.")
      appendLine()
      append("@see ${objectModel.sourceClassDeclaration.fullyQualifiedName()}")
    }
  }

  private fun generateCompanionPropertyKdoc(document: MetalasticGraph.DocumentClass): String {
    return """
            Static instance of the [${document.fullyQualifiedName}] metamodel.
            """
      .trimIndent()
  }

  /**
   * Builds a visual tree representation of the field hierarchy for KDoc.
   *
   * Renders fields with tree characters (├──, └──) showing:
   * - Simple fields with their type
   * - MultiFields with their inner fields
   * - Object/Nested fields with their target type
   * - Terminal objects (SelfReferencing, UnModellable) with indicators
   */
  private fun buildFieldHierarchy(model: MetalasticGraph.MetaClassModel): String {
    if (model.fields.isEmpty()) {
      return ""
    }

    val lines = mutableListOf<String>()
    model.fields.forEachIndexed { index, field ->
      val isLast = index == model.fields.lastIndex
      val prefix = if (isLast) "└── " else "├── "
      val connector = if (isLast) "    " else "│   "

      lines.add(prefix + formatFieldLine(field))

      // Add inner fields for MultiField
      if (field is MultiFieldModel) {
        field.innerFields.forEachIndexed { innerIndex, innerField ->
          val isLastInner = innerIndex == field.innerFields.lastIndex
          val innerPrefix = if (isLastInner) "└── " else "├── "
          lines.add(
            "$connector$innerPrefix${innerField.suffix}: ${formatFieldType(innerField.fieldType)}"
          )
        }
      }

      // Add nested structure indicator for ObjectField
      if (field is ObjectFieldModel && field.targetModel != null) {
        val nestedIndicator = if (field.nested) " (nested)" else ""
        lines.add("$connector    └── ...${field.targetModel.qClassName} structure$nestedIndicator")
      }
    }

    return lines.joinToString("\n")
  }

  /** Formats a single field line for the hierarchy tree. */
  private fun formatFieldLine(field: FieldModel): String {
    val fieldName = field.name

    return when (field) {
      is SimpleFieldModel -> {
        val fieldTypeClass = FieldTypeMappings.classOf(field.fieldType)
        "$fieldName: ${fieldTypeClass.className.simpleName}"
      }

      is MultiFieldModel -> {
        val mainFieldTypeClass = FieldTypeMappings.classOf(field.mainFieldType)
        "$fieldName: ${mainFieldTypeClass.className.simpleName} MultiField"
      }

      is ObjectFieldModel -> {
        when {
          field.targetModel == null -> {
            "$fieldName: UnModellableObject 🚫"
          }
          field.parentModel.fullyQualifiedName == field.targetModel.fullyQualifiedName -> {
            "$fieldName: SelfReferencingObject ∞"
          }
          else -> {
            val nestedIndicator = if (field.nested) " (nested)" else ""
            "$fieldName: ${field.targetModel.qClassName}$nestedIndicator"
          }
        }
      }
    }
  }

  /** Formats a field type name for display in the hierarchy. */
  private fun formatFieldType(fieldType: FieldType): String {
    val fieldTypeClass = FieldTypeMappings.classOf(fieldType)
    return fieldTypeClass.className.simpleName
  }

  private fun getKotlinTypeForInnerField(fieldType: FieldType): TypeName {
    return when (fieldType) {
      FieldType.Auto,
      FieldType.Text,
      FieldType.Keyword -> String::class.asTypeName()

      FieldType.Long -> Long::class.asTypeName()
      FieldType.Integer -> Int::class.asTypeName()
      FieldType.Short -> Short::class.asTypeName()
      FieldType.Byte -> Byte::class.asTypeName()
      FieldType.Double -> Double::class.asTypeName()
      FieldType.Float -> Float::class.asTypeName()
      FieldType.Boolean -> Boolean::class.asTypeName()
      FieldType.Date -> Date::class.asTypeName()
      else -> String::class.asTypeName() // fallback for other types
    }
  }

  /** Generates field KDoc. */
  private fun generateFieldKDoc(field: FieldModel, description: String? = null): String {
    val containingClass = field.parentModel.sourceClassDeclaration
    val containingClassName = containingClass.qualifiedName?.asString() ?: "Unknown"
    val propertyName = field.sourceDeclaration.simpleName.asString()
    val elasticsearchType = field.fieldType.name

    val description2 = description?.let { "\n$it\n" } ?: ""
    return """
        | $description2
        |**Original Property:**
        |- [$containingClassName.$propertyName]
        |- Elasticsearch Type: `$elasticsearchType`
        |- Elasticsearch Path: `${field.elasticsearchFieldName}`
        |
      """
      .trimMargin()
  }

  /** Generates KDoc for inner field properties in MultiField classes. */
  private fun generateInnerFieldKDoc(innerField: InnerFieldModel): String {
    return """
        |
        |**Elasticsearch inner field for suffix '${innerField.suffix}'.**
        |
        |**Original @InnerField:**
        |- Suffix: `${innerField.suffix}`
        |- Elasticsearch Type: `${innerField.fieldType.name}`
        |
      """
      .trimMargin()
  }

  /** Generates KDoc for MultiField inner classes. */
  private fun generateMultiFieldClassKDoc(field: MultiFieldModel): String {
    val containingClass = field.parentModel.sourceClassDeclaration
    val containingClassName = containingClass.qualifiedName?.asString() ?: "Unknown"
    val propertyName = field.sourceDeclaration.simpleName.asString()
    return """
        |
        |**Metamodel for multifield.**
        | - [${containingClassName}.${propertyName}]
        |
      """
      .trimMargin()
  }

  private fun PropertySpec.Builder.withOptionalJavaCompatibility() = apply {
    if (options.generateJavaCompatibility) {
      addAnnotation(AnnotationSpec.builder(JvmField::class).build())
    }
  }
}

/** Extension function to create ClassName directly from MetaClassModel. */
private fun MetalasticGraph.MetaClassModel.toClassName(): ClassName =
  ClassName(packageName, qualifier.split("."))
