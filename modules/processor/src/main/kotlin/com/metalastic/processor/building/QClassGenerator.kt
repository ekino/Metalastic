package com.metalastic.processor.building

import com.metalastic.processor.CoreConstants
import com.metalastic.processor.CoreConstants.DocumentClass.INDEX_NAME_CONSTANT
import com.metalastic.processor.CoreConstants.PRODUCT_NAME
import com.metalastic.processor.MetalasticSymbolProcessor
import com.metalastic.processor.collecting.fullyQualifiedName
import com.metalastic.processor.collecting.toSafeTypeName
import com.metalastic.processor.model.FieldModel
import com.metalastic.processor.model.InnerFieldModel
import com.metalastic.processor.model.MetalasticGraph
import com.metalastic.processor.model.MultiFieldModel
import com.metalastic.processor.model.ObjectFieldModel
import com.metalastic.processor.model.SimpleFieldModel
import com.metalastic.processor.options.ProcessorOptions
import com.metalastic.processor.report.reporter
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
import org.springframework.data.elasticsearch.annotations.FieldType

/** Pure Q-class TypeSpec generation. */
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
    val classBuilder =
      TypeSpec.classBuilder(model.qClassName)
        .addModifiers(KModifier.PUBLIC)
        .addGeneratedAnnotation()

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

    return PropertySpec.builder(field.name, typeName)
      .addModifiers(KModifier.PUBLIC)
      .initializer("%L(%S)", fieldTypeClass.helperMethodName, field.elasticsearchFieldName)
      .addKdoc(generateFieldKDoc(field))
      .withOptionalJavaCompatibility()
      .build()
  }

  /** Generates an object field property. */
  private fun generateObjectFieldProperty(field: ObjectFieldModel): PropertySpec {
    if (field.targetModel == null) {
      return generateTerminalObjectField(field)
    }
    val qClassName = field.targetModel.toClassName()
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
    if (field.nested) {
      initializer(
        "%T(this, %S, true, typeOf<%T>())",
        typeName,
        field.elasticsearchFieldName,
        field.type.toSafeTypeName(typeParameterResolver),
      )
    } else {
      initializer(
        "%T(this, %S, false, typeOf<%T>())",
        typeName,
        field.elasticsearchFieldName,
        field.type.toSafeTypeName(typeParameterResolver),
      )
    }
  }

  private fun generateTerminalObjectField(field: ObjectFieldModel): PropertySpec {
    val nested = field.fieldType == FieldType.Nested

    // Create ObjectField<*> type for terminal objects
    val objectFieldStarType = objectFieldClass.parameterizedBy(com.squareup.kotlinpoet.STAR)
    val sourceTypeName = field.type.toTypeName(typeParameterResolver)
    val objectFieldAnyType = objectFieldClass.parameterizedBy(sourceTypeName)

    return PropertySpec.builder(field.name, objectFieldStarType)
      .apply {
        if (field.nested) {
          initializer(
            "object : %T(parent = this, name = %S, nested = %L, fieldType = typeOf<%T>()) {}",
            objectFieldAnyType,
            field.elasticsearchFieldName,
            nested,
            sourceTypeName,
          )
        } else {
          initializer(
            "object : %T(parent = this, name = %S, nested = false, fieldType = typeOf<%T>()) {}",
            objectFieldAnyType,
            field.elasticsearchFieldName,
            sourceTypeName,
          )
        }
      }
      .addKdoc(generateFieldKDoc(field))
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

    // Build the ClassName for the Q-class
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
    return """
            Metamodel for Elasticsearch index `${document.indexName}`.

            This class was automatically generated by $PRODUCT_NAME annotation processor
            from the source class [${document.sourceClassDeclaration.fullyQualifiedName()}].

            **Do not modify this file directly.** Any changes will be overwritten
            during the next compilation. To modify the metamodel structure, update the
            annotations on the source document class.

            @see ${document.sourceClassDeclaration.fullyQualifiedName()}
            """
      .trimIndent()
  }

  private fun generateObjectFieldKdoc(objectModel: MetalasticGraph.MetaClassModel): String {
    val className = objectModel.fullyQualifiedName.substringAfterLast(".")
    return """
            Metamodel for class `$className`.

            This class was automatically generated by $PRODUCT_NAME annotation processor
            from the source class [${objectModel.sourceClassDeclaration.fullyQualifiedName()}].

            **Do not modify this file directly.** Any changes will be overwritten
            during the next compilation. To modify the metamodel structure, update the
            annotations on the source class.

            @see ${objectModel.sourceClassDeclaration.fullyQualifiedName()}
            """
      .trimIndent()
  }

  private fun generateCompanionPropertyKdoc(document: MetalasticGraph.DocumentClass): String {
    return """
            Static instance of the [${document.fullyQualifiedName}] metamodel.
            """
      .trimIndent()
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
  private fun generateFieldKDoc(field: FieldModel): String {
    val containingClass = field.parentModel.sourceClassDeclaration
    val containingClassName = containingClass.qualifiedName?.asString() ?: "Unknown"
    val propertyName = field.sourceDeclaration.simpleName.asString()
    val elasticsearchType = field.fieldType.name

    return """
        |
        |**Original Property:**
        |- [$containingClassName.$propertyName]
        |- Elasticsearch Type: `$elasticsearchType`
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
    val propertyName = field.name

    return """
        |
        |**Metamodel for multifield [${containingClassName}.${propertyName}].**
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
