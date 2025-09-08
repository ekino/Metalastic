package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.qelasticsearch.processor.CoreConstants.DOCUMENT_ANNOTATION
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
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

  private val fieldTypeExtractor = FieldTypeExtractor(logger)
  private val nestedClassProcessor = NestedClassProcessor(logger)
  private val fieldGenerators = FieldGenerators(logger, fieldTypeMappings)

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
            .getSymbolsWithAnnotation(DOCUMENT_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (documentClasses.isEmpty()) {
          return emptyList()
        }

        logger.info("Processing ${documentClasses.size} document classes")

        // Collect object fields from document classes (recursive)
        documentClasses.forEach { documentClass ->
          nestedClassProcessor.collectObjectFields(documentClass, fieldTypeExtractor)
        }

        val globalObjectFields = nestedClassProcessor.getGlobalObjectFields()
        val objectFieldRegistry = ObjectFieldRegistry(logger, globalObjectFields)

        // Generate document classes
        documentClasses.forEach { documentClass ->
          processDocumentClass(documentClass, objectFieldRegistry)
        }

        // Generate object field classes
        generateAllObjectFields(globalObjectFields, objectFieldRegistry)

        // Generate Metamodels.kt with all Q-class instances
        generateMetamodelsFile(documentClasses)
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
    writeGeneratedFile(qIndexClass, packageName, "${CoreConstants.Q_PREFIX}$className")
  }

  private fun generateQIndexClass(
    documentClass: KSClassDeclaration,
    indexName: String,
    className: String,
    packageName: String,
    objectFieldRegistry: ObjectFieldRegistry,
  ): FileSpec {
    val qIndexClassName = "${CoreConstants.Q_PREFIX}$className"
    val importContext = ImportContext(packageName)
    val typeParameterResolver = documentClass.typeParameters.toTypeParameterResolver()

    val objectBuilder =
      TypeSpec.classBuilder(qIndexClassName)
        .addKdoc(createClassKDoc(documentClass, indexName))
        .addAnnotation(generatedAnnotation)
        .superclass(ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.INDEX_CLASS))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter(
              ParameterSpec.builder(
                  "parent",
                  ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.OBJECT_FIELDS_CLASS)
                    .copy(nullable = true),
                )
                .defaultValue("null")
                .build()
            )
            .addParameter(
              ParameterSpec.builder("fieldName", String::class).defaultValue("%S", "").build()
            )
            .addParameter(
              ParameterSpec.builder("nested", Boolean::class).defaultValue("false").build()
            )
            .build()
        )
        .addSuperclassConstructorParameter("%S", indexName)
        .addSuperclassConstructorParameter("parent")
        .addSuperclassConstructorParameter("fieldName")
        .addSuperclassConstructorParameter("nested")
        .addOriginatingKSFile(documentClass.containingFile!!)

    // Phase 1: Collect all types that will be used
    collectDocumentTypes(documentClass, importContext, objectFieldRegistry)

    // Phase 2: Finalize import decisions based on conflicts and proximity
    importContext.finalizeImportDecisions()

    // Phase 3: Generate properties with optimal type names
    addPropertiesToObjectBuilder(
      documentClass,
      objectBuilder,
      importContext,
      typeParameterResolver,
      objectFieldRegistry,
    )

    return createFileSpec(packageName, qIndexClassName, objectBuilder, importContext)
  }

  private fun collectDocumentTypes(
    documentClass: KSClassDeclaration,
    importContext: ImportContext,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    // Register locally defined nested classes that will be generated in this document
    nestedClassProcessor.getNestedClassesForDocument(documentClass).forEach { objectFieldInfo ->
      // Register both the source class name and the generated Q-class name
      importContext.registerLocallyDefinedNestedClass(objectFieldInfo.qualifiedName)

      // Also register the generated Q-class qualified name
      val qClassName = "${CoreConstants.Q_PREFIX}${documentClass.simpleName.asString()}"
      val packageName = documentClass.packageName.asString()
      val nestedClassName = objectFieldInfo.classDeclaration.simpleName.asString()
      val qClassQualifiedName = "$packageName.$qClassName.$nestedClassName"
      importContext.registerLocallyDefinedNestedClass(qClassQualifiedName)
    }

    val processedPropertyNames = mutableSetOf<String>()
    documentClass.getAllProperties().forEach { property ->
      val propertyName = property.simpleName.asString()
      if (propertyName !in processedPropertyNames) {
        fieldGenerators.collectPropertyTypes(property, importContext, objectFieldRegistry)
        processedPropertyNames.add(propertyName)
      }
    }
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
    val importContext = ImportContext(objectFieldInfo.packageName)
    val typeParameterResolver =
      objectFieldInfo.classDeclaration.typeParameters.toTypeParameterResolver()

    val objectBuilder =
      TypeSpec.classBuilder(objectFieldInfo.className)
        .addKdoc(createObjectFieldKDoc(objectFieldInfo))
        .addAnnotation(generatedAnnotation)
        .superclass(ClassName(CoreConstants.CORE_PACKAGE, CoreConstants.OBJECT_FIELDS_CLASS))
        .addSuperclassConstructorParameter("parent")
        .addSuperclassConstructorParameter("path")
        .addSuperclassConstructorParameter("nested")
        .primaryConstructor(
          com.squareup.kotlinpoet.FunSpec.constructorBuilder()
            .addParameter(
              "parent",
              ClassName(CoreConstants.CORE_PACKAGE, "ObjectField").copy(nullable = true),
            )
            .addParameter("path", String::class)
            .addParameter("nested", Boolean::class)
            .build()
        )
        .addOriginatingKSFile(objectFieldInfo.classDeclaration.containingFile!!)

    // Phase 1: Collect all types that will be used
    collectObjectFieldTypes(objectFieldInfo, importContext, objectFieldRegistry)

    // Phase 2: Finalize import decisions based on conflicts and proximity
    importContext.finalizeImportDecisions()

    // Phase 3: Generate properties with optimal type names
    addPropertiesToObjectBuilder(
      objectFieldInfo,
      objectBuilder,
      importContext,
      typeParameterResolver,
      objectFieldRegistry,
    )

    return createObjectFieldFileSpec(objectFieldInfo, objectBuilder, importContext)
  }

  private fun collectObjectFieldTypes(
    objectFieldInfo: ObjectFieldInfo,
    importContext: ImportContext,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
    val processedPropertyNames = mutableSetOf<String>()
    objectFieldInfo.classDeclaration.getAllProperties().forEach { property ->
      val propertyName = property.simpleName.asString()
      if (propertyName !in processedPropertyNames) {
        fieldGenerators.collectPropertyTypes(property, importContext, objectFieldRegistry)
        processedPropertyNames.add(propertyName)
      }
    }

    nestedClassProcessor.collectAnnotatedGetterMethodTypes(objectFieldInfo.classDeclaration)
    nestedClassProcessor.collectNestedObjectTypes(
      objectFieldInfo.classDeclaration,
      importContext,
      fieldGenerators,
      objectFieldRegistry,
    )
  }

  private fun addPropertiesToObjectBuilder(
    objectFieldInfo: ObjectFieldInfo,
    objectBuilder: TypeSpec.Builder,
    importContext: ImportContext,
    typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    objectFieldRegistry: ObjectFieldRegistry,
  ) {
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

    nestedClassProcessor.processAnnotatedGetterMethods(objectFieldInfo.classDeclaration) { method ->
      fieldGenerators.processAnnotatedMethod(
        method,
        objectBuilder,
        importContext,
        typeParameterResolver,
      )
    }

    nestedClassProcessor.addNestedObjectsToBuilder(
      objectFieldInfo.classDeclaration,
      objectBuilder,
      fieldGenerators,
      fieldTypeExtractor,
      objectFieldRegistry,
      importContext,
    )
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
        .addImport(CoreConstants.CORE_PACKAGE, CoreConstants.INDEX_CLASS)
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
        .addImport(CoreConstants.CORE_PACKAGE, CoreConstants.OBJECT_FIELDS_CLASS)
        .indent("    ")

    addImportsToFileBuilder(fileBuilder, importContext)
    return fileBuilder.build()
  }

  private fun addImportsToFileBuilder(fileBuilder: FileSpec.Builder, importContext: ImportContext) {
    importContext.usedImports.forEach { import ->
      when {
        import.contains(".Companion.") -> addCompanionImport(fileBuilder, import)
        import.startsWith(CoreConstants.CORE_PACKAGE) -> addCorePackageImport(fileBuilder, import)
        else -> addDefaultCoreImport(fileBuilder, import)
      }
    }
  }

  private fun addCompanionImport(fileBuilder: FileSpec.Builder, import: String) {
    val parts = import.split(".Companion.")
    if (parts.size != 2) return

    val (classPath, functionName) = parts
    val lastDotIndex = classPath.lastIndexOf(".")
    if (lastDotIndex == -1) return

    val packageName = classPath.substring(0, lastDotIndex)
    val className = classPath.substring(lastDotIndex + 1)
    fileBuilder.addImport("$packageName.$className.Companion", functionName)
  }

  private fun addCorePackageImport(fileBuilder: FileSpec.Builder, import: String) {
    fileBuilder.addImport(
      CoreConstants.CORE_PACKAGE,
      import.substringAfter("${CoreConstants.CORE_PACKAGE}."),
    )
  }

  private fun addDefaultCoreImport(fileBuilder: FileSpec.Builder, import: String) {
    val lastDotIndex = import.lastIndexOf(".")
    if (lastDotIndex != -1) {
      val packageName = import.substring(0, lastDotIndex)
      val className = import.substring(lastDotIndex + 1)
      fileBuilder.addImport(packageName, className)
    } else {
      // Fallback for imports without package (shouldn't happen normally)
      fileBuilder.addImport(CoreConstants.CORE_PACKAGE, import)
    }
  }

  private fun writeGeneratedFile(fileSpec: FileSpec, packageName: String, className: String) {
    val fileKey = "$packageName.$className"
    if (fileKey in generatedFiles) {
      logger.info("Skipping duplicate file generation for:  $fileKey")
      return
    }

    val outputFile =
      codeGenerator.createNewFile(
        dependencies = Dependencies(false),
        packageName = packageName,
        fileName = className,
      )

    // Replacing redundant 'public' modifiers for cleaner output
    outputFile.bufferedWriter().use { writer ->
      writer.write(fileSpec.toString().replace("public ", ""))
    }
    generatedFiles.add(fileKey)
    logger.info("Generated file: $packageName.$className")
  }

  /**
   * Generates [CoreConstants.METAMODELS_CLASS_NAME].kt file containing a data object with instances
   * of all Q-classes for convenient access to all metamodels.
   */
  private fun generateMetamodelsFile(documentClasses: List<KSClassDeclaration>) {
    logger.info(
      "Generating ${CoreConstants.METAMODELS_CLASS_NAME}.kt with ${documentClasses.size} Q-class instances"
    )

    val metamodelsBuilder =
      TypeSpec.objectBuilder(CoreConstants.METAMODELS_CLASS_NAME)
        .addModifiers(KModifier.DATA)
        .addAnnotation(generatedAnnotation)
        .addKdoc(
          """
        Central registry of all generated Q-classes for convenient access.

        This provides singleton-like access to all document metamodels:

        Generated automatically from all [$DOCUMENT_ANNOTATION] annotated classes.
      """
            .trimIndent()
        )

    val importContext = ImportContext("com.qelasticsearch")

    // Add property for each document class
    documentClasses.forEach { documentClass ->
      val className = documentClass.simpleName.asString()
      val qClassName = "${CoreConstants.Q_PREFIX}$className"
      val packageName = documentClass.packageName.asString()
      val propertyName = className.replaceFirstChar { it.lowercase() }

      // Register the Q-class for import
      val qualifiedQClassName = "$packageName.$qClassName"
      importContext.registerTypeUsage(qualifiedQClassName)

      metamodelsBuilder.addProperty(
        com.squareup.kotlinpoet.PropertySpec.builder(
            propertyName,
            ClassName(packageName, qClassName),
          )
          .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
          .initializer("$qClassName()")
          .addKdoc("Metamodel for [@Document] class [${documentClass.qualifiedName?.asString()}]")
          .build()
      )
    }

    // Finalize imports
    importContext.finalizeImportDecisions()

    val fileSpec =
      FileSpec.builder("com.qelasticsearch", CoreConstants.METAMODELS_CLASS_NAME)
        .addType(metamodelsBuilder.build())
        .apply {
          importContext.usedImports.forEach { import ->
            val packageName = import.substringBeforeLast('.')
            val className = import.substringAfterLast('.')
            addImport(packageName, className)
          }
        }
        .addAnnotation(
          AnnotationSpec.builder(JvmName::class)
            .addMember("%S", CoreConstants.METAMODELS_CLASS_NAME)
            .build()
        )
        .indent("    ")
        .build()

    writeGeneratedFile(fileSpec, "com.qelasticsearch", CoreConstants.METAMODELS_CLASS_NAME)
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
