package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import jakarta.annotation.Generated
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Refactored KSP-based annotation processor that generates type-safe Elasticsearch DSL classes from
 * Spring Data Elasticsearch @Document annotated classes.
 */
class QElasticsearchSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  private val fieldTypeMappings: Map<FieldType, FieldTypeMapping> by lazy {
    FieldTypeMappingBuilder(logger).build()
  }

  private val codeGenUtils = CodeGenerationUtils()
  private val fieldTypeExtractor = FieldTypeExtractor(logger, codeGenUtils)
  private val nestedClassProcessor = NestedClassProcessor(logger, codeGenUtils)
  private val fieldGenerators = FieldGenerators(logger, fieldTypeMappings, codeGenUtils)

  private val generatedFiles = mutableSetOf<String>()

  private val generatedAnnotation by lazy {
    AnnotationSpec.builder(Generated::class)
      .addMember(
        "%S, date=%S",
        "${QElasticsearchSymbolProcessor::class.qualifiedName}",
        DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now()),
      )
      .build()
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    runCatching {
        val documentClasses =
          resolver
            .getSymbolsWithAnnotation(Document::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (documentClasses.isEmpty()) {
          return emptyList()
        }

        logger.info("Processing ${documentClasses.size} document classes")

        // Collect all object fields
        documentClasses.forEach { documentClass ->
          nestedClassProcessor.collectObjectFields(documentClass, fieldTypeExtractor)
        }
        nestedClassProcessor.collectAllPossibleObjectFields(resolver)

        val globalObjectFields = nestedClassProcessor.getGlobalObjectFields()
        val objectFieldRegistry = ObjectFieldRegistry(logger, codeGenUtils, globalObjectFields)

        // Generate document classes
        documentClasses.forEach { documentClass ->
          processDocumentClass(documentClass, objectFieldRegistry)
        }

        // Generate object field classes
        generateAllObjectFields(globalObjectFields, objectFieldRegistry)
      }
      .getOrElse { error ->
        logger.error("Error processing documents: ${error.message}")
        logger.exception(error)
      }
    return emptyList()
  }

  private fun processDocumentClass(
    documentClass: KSClassDeclaration,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val documentAnnotation = documentClass.findAnnotation(Document::class)
    val indexName = documentAnnotation?.getArgumentValue<String>("indexName") ?: "unknown"
    val className = documentClass.simpleName.asString()
    val packageName = documentClass.packageName.asString()

    logger.info("Processing document class: $className")

    val qIndexClass =
      generateQIndexClass(documentClass, indexName, className, packageName, objectFieldRegistry)
    writeGeneratedFile(qIndexClass, packageName, "Q$className")
  }

  private fun generateQIndexClass(
    documentClass: KSClassDeclaration,
    indexName: String,
    className: String,
    packageName: String,
    objectFieldRegistry: ObjectFieldRegistry,
  ): FileSpec {
    val qIndexClassName = "${DSLConstants.Q_PREFIX}$className"
    val importContext = ImportContext()
    val typeParameterResolver = documentClass.typeParameters.toTypeParameterResolver()

    val objectBuilder =
      TypeSpec.objectBuilder(qIndexClassName)
        .addKdoc(createClassKDoc(documentClass, indexName))
        .addModifiers(KModifier.DATA)
        .addAnnotation(generatedAnnotation)
        .superclass(ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.INDEX_CLASS))
        .addSuperclassConstructorParameter("%S", indexName)
        .addOriginatingKSFile(documentClass.containingFile!!)

    // Add properties
    addPropertiesToObjectBuilder(
      documentClass,
      objectBuilder,
      importContext,
      typeParameterResolver,
      objectFieldRegistry,
    )

    return createFileSpec(packageName, qIndexClassName, objectBuilder, importContext)
  }

  private fun addPropertiesToObjectBuilder(
    documentClass: KSClassDeclaration,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val processedPropertyNames = mutableSetOf<String>()
    documentClass.getAllProperties().forEach { property ->
      val propertyName = property.simpleName.asString()
      if (propertyName !in processedPropertyNames) {
        fieldGenerators.processProperty(
          property,
          objectBuilder,
          importContext,
          typeParameterResolver,
          fieldTypeExtractor,
          objectFieldRegistry,
        )
        processedPropertyNames.add(propertyName)
      }
    }

    // Add nested objects
    nestedClassProcessor.addNestedObjectsToBuilder(
      documentClass,
      objectBuilder,
      fieldGenerators,
      fieldTypeExtractor,
      objectFieldRegistry,
      importContext,
    )
  }

  private fun generateAllObjectFields(
    globalObjectFields: Map<String, ObjectFieldInfo>,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val objectFieldsToGenerate = globalObjectFields.values.filter { it.parentDocumentClass == null }

    objectFieldsToGenerate.forEach { objectFieldInfo ->
      val fileSpec = generateObjectFieldsClassFromInfo(objectFieldInfo, objectFieldRegistry)
      writeGeneratedFile(fileSpec, objectFieldInfo.packageName, objectFieldInfo.className)
    }
  }

  private fun generateObjectFieldsClassFromInfo(
    objectFieldInfo: ObjectFieldInfo,
    objectFieldRegistry: ObjectFieldRegistry,
  ): FileSpec {
    val importContext = ImportContext()
    val typeParameterResolver =
      objectFieldInfo.classDeclaration.typeParameters.toTypeParameterResolver()

    val objectBuilder =
      TypeSpec.classBuilder(objectFieldInfo.className)
        .addKdoc(createObjectFieldKDoc(objectFieldInfo))
        .addAnnotation(generatedAnnotation)
        .superclass(ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.OBJECT_FIELDS_CLASS))
        .addSuperclassConstructorParameter("parent")
        .addSuperclassConstructorParameter("path")
        .addSuperclassConstructorParameter("nested")
        .primaryConstructor(
          com.squareup.kotlinpoet.FunSpec.constructorBuilder()
            .addParameter(
              "parent",
              ClassName(DSLConstants.DSL_PACKAGE, "ObjectField").copy(nullable = true),
            )
            .addParameter("path", String::class)
            .addParameter("nested", Boolean::class)
            .build()
        )
        .addOriginatingKSFile(objectFieldInfo.classDeclaration.containingFile!!)

    // Add properties
    val processedPropertyNames = mutableSetOf<String>()
    objectFieldInfo.classDeclaration.getAllProperties().forEach { property ->
      val propertyName = property.simpleName.asString()
      if (propertyName !in processedPropertyNames) {
        fieldGenerators.processProperty(
          property,
          objectBuilder,
          importContext,
          typeParameterResolver,
          fieldTypeExtractor,
          objectFieldRegistry,
        )
        processedPropertyNames.add(propertyName)
      }
    }

    // Process annotated getter methods for interfaces
    nestedClassProcessor.processAnnotatedGetterMethods(objectFieldInfo.classDeclaration) { method ->
      fieldGenerators.processAnnotatedMethod(
        method,
        objectBuilder,
        importContext,
        typeParameterResolver,
      )
    }

    // Add nested objects
    nestedClassProcessor.addNestedObjectsToBuilder(
      objectFieldInfo.classDeclaration,
      objectBuilder,
      fieldGenerators,
      fieldTypeExtractor,
      objectFieldRegistry,
      importContext,
    )

    return createObjectFieldFileSpec(objectFieldInfo, objectBuilder, importContext)
  }

  private fun createClassKDoc(documentClass: KSClassDeclaration, indexName: String): String =
    """
        Query DSL object for Elasticsearch index '$indexName'.

        This class was automatically generated by QElasticsearch annotation processor
        from the source class [${documentClass.qualifiedName?.asString()}].

        **Do not modify this file directly.** Any changes will be overwritten
        during the next compilation. To modify the DSL structure, update the
        annotations on the source document class.

        @see ${documentClass.qualifiedName?.asString()}
        @generated by QElasticsearch annotation processor
        """
      .trimIndent()

  private fun createObjectFieldKDoc(objectFieldInfo: ObjectFieldInfo): String =
    """
        Query DSL object fields for class [${objectFieldInfo.qualifiedName}].

        This class was automatically generated by QElasticsearch annotation processor
        from the source class [${objectFieldInfo.qualifiedName}].

        **Do not modify this file directly.** Any changes will be overwritten
        during the next compilation. To modify the DSL structure, update the
        annotations on the source class.

        @see ${objectFieldInfo.qualifiedName}
        @generated by QElasticsearch annotation processor
        """
      .trimIndent()

  private fun createFileSpec(
    packageName: String,
    qIndexClassName: String,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
  ): FileSpec {
    val fileBuilder =
      FileSpec.builder(packageName, qIndexClassName)
        .addType(objectBuilder.build())
        .addImport(DSLConstants.DSL_PACKAGE, DSLConstants.INDEX_CLASS)
        .addAnnotation(
          AnnotationSpec.builder(JvmName::class).addMember("%S", qIndexClassName).build()
        )
        .indent("    ")

    addImportsToFileBuilder(fileBuilder, importContext)
    return fileBuilder.build()
  }

  private fun createObjectFieldFileSpec(
    objectFieldInfo: ObjectFieldInfo,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
  ): FileSpec {
    val fileBuilder =
      FileSpec.builder(objectFieldInfo.packageName, objectFieldInfo.className)
        .addType(objectBuilder.build())
        .addImport(DSLConstants.DSL_PACKAGE, DSLConstants.OBJECT_FIELDS_CLASS)
        .indent("    ")

    addImportsToFileBuilder(fileBuilder, importContext)
    return fileBuilder.build()
  }

  private fun addImportsToFileBuilder(fileBuilder: FileSpec.Builder, importContext: ImportContext) {
    // Add DSL imports
    importContext.usedImports.forEach { className ->
      fileBuilder.addImport(DSLConstants.DSL_PACKAGE, className)
    }

    // Add delegate helper function imports - only the ones actually used
    importContext.usedDelegationFunctions.forEach { delegationFunction ->
      fileBuilder.addImport("${DSLConstants.DSL_PACKAGE}.delegation", delegationFunction)
    }

    // Add type imports
    importContext.typeImports.forEach { qualifiedName ->
      if (qualifiedName.contains('.')) {
        val parts = qualifiedName.split('.')
        val className = parts.last()
        val packageName = parts.dropLast(1).joinToString(".")
        fileBuilder.addImport(packageName, className)
      }
    }
  }

  private fun writeGeneratedFile(fileSpec: FileSpec, packageName: String, className: String) {
    val fileKey = "$packageName.$className"
    if (fileKey in generatedFiles) {
      logger.info("Skipping duplicate file generation for: $fileKey")
      return
    }

    val outputFile =
      codeGenerator.createNewFile(
        dependencies = Dependencies(false),
        packageName = packageName,
        fileName = className,
      )

    outputFile.bufferedWriter().use { writer -> fileSpec.writeTo(writer) }

    generatedFiles.add(fileKey)
    logger.info("Generated file: $packageName.$className")
  }

  // Extension function to find annotations
  private fun KSClassDeclaration.findAnnotation(
    annotationClass: kotlin.reflect.KClass<*>
  ): com.google.devtools.ksp.symbol.KSAnnotation? =
    annotations.find {
      it.annotationType.resolve().declaration.qualifiedName?.asString() ==
        annotationClass.qualifiedName
    }

  // Extension function to get argument values
  private inline fun <reified T> com.google.devtools.ksp.symbol.KSAnnotation.getArgumentValue(
    name: String
  ): T? = arguments.find { it.name?.asString() == name }?.value as? T
}
