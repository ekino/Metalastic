package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field

/** Handles processing of nested classes and object field registration. */
class NestedClassProcessor(private val logger: KSPLogger) {
  private val globalObjectFields = mutableMapOf<String, ObjectFieldInfo>()

  /** Collects all object fields from a document class. */
  fun collectObjectFields(
    documentClass: KSClassDeclaration,
    fieldTypeExtractor: FieldTypeExtractor,
  ) {
    // First, collect all nested classes defined within the document class
    val directNestedClasses = mutableListOf<KSClassDeclaration>()
    collectNestedClasses(documentClass, directNestedClasses)

    // Register all nested classes found within the document class
    directNestedClasses.forEach { nestedClass -> registerObjectField(nestedClass, documentClass) }

    // Then process properties to find additional referenced classes
    processClassProperties(documentClass, fieldTypeExtractor) { nestedClass ->
      registerObjectField(nestedClass, documentClass)
      collectObjectFieldsFromClass(nestedClass, fieldTypeExtractor)
    }

    // Finally, process properties of all nested classes to find their referenced types
    directNestedClasses.forEach { nestedClass ->
      processClassProperties(nestedClass, fieldTypeExtractor) { referencedClass ->
        registerObjectField(referencedClass, documentClass)
        collectObjectFieldsFromClass(referencedClass, fieldTypeExtractor)
      }
    }
  }

  /** Gets nested classes that should be included in a document Q-class. */
  fun getNestedClassesForDocument(documentClass: KSClassDeclaration): List<ObjectFieldInfo> =
    globalObjectFields.values.filter { objectFieldInfo ->
      objectFieldInfo.parentDocumentClass == documentClass &&
        isDirectNestedClass(objectFieldInfo.classDeclaration, documentClass)
    }

  /** Gets nested classes that should be included in a specific class (for recursive nesting). */
  private fun getNestedClassesForClass(parentClass: KSClassDeclaration): List<ObjectFieldInfo> =
    globalObjectFields.values.filter { objectFieldInfo ->
      isDirectNestedClass(objectFieldInfo.classDeclaration, parentClass)
    }

  private fun isDirectNestedClass(
    nestedClass: KSClassDeclaration,
    parentClass: KSClassDeclaration,
  ): Boolean {
    // Check if this is a direct nested class (not nested within another nested class)
    return parentClass.declarations.filterIsInstance<KSClassDeclaration>().any {
      it.qualifiedName?.asString() == nestedClass.qualifiedName?.asString()
    }
  }

  /** Adds nested objects to a Q-class builder. */
  fun addNestedObjectsToBuilder(
    documentClass: KSClassDeclaration,
    parentObjectBuilder: TypeSpec.Builder,
    fieldGenerators: FieldGenerators,
    fieldTypeExtractor: FieldTypeExtractor,
    objectFieldRegistry: ObjectFieldRegistry,
    importContext: ImportContext,
  ) {
    val nestedClasses = getNestedClassesForDocument(documentClass)

    nestedClasses.forEach { objectFieldInfo ->
      val nestedObjectBuilder =
        createNestedObjectBuilder(
          objectFieldInfo,
          fieldGenerators,
          fieldTypeExtractor,
          objectFieldRegistry,
          importContext,
        )
      parentObjectBuilder.addType(nestedObjectBuilder.build())
    }
  }

  /** Adds nested objects to a specific class builder (for recursive nesting). */
  private fun addNestedObjectsToClassBuilder(
    parentClass: KSClassDeclaration,
    parentObjectBuilder: TypeSpec.Builder,
    fieldGenerators: FieldGenerators,
    fieldTypeExtractor: FieldTypeExtractor,
    objectFieldRegistry: ObjectFieldRegistry,
    importContext: ImportContext,
  ) {
    val nestedClasses = getNestedClassesForClass(parentClass)

    nestedClasses.forEach { objectFieldInfo ->
      val nestedObjectBuilder =
        createNestedObjectBuilder(
          objectFieldInfo,
          fieldGenerators,
          fieldTypeExtractor,
          objectFieldRegistry,
          importContext,
        )
      parentObjectBuilder.addType(nestedObjectBuilder.build())
    }
  }

  /** Creates a TypeSpec.Builder for a nested object class. */
  private fun createNestedObjectBuilder(
    objectFieldInfo: ObjectFieldInfo,
    fieldGenerators: FieldGenerators,
    fieldTypeExtractor: FieldTypeExtractor,
    objectFieldRegistry: ObjectFieldRegistry,
    importContext: ImportContext,
  ): TypeSpec.Builder {
    val nestedClassName = objectFieldInfo.classDeclaration.simpleName.asString()

    val objectBuilder =
      TypeSpec.classBuilder(nestedClassName)
        .addKdoc(
          """
                    Query DSL object fields for nested class [${objectFieldInfo.qualifiedName}].

                    This nested object was automatically generated by QElasticsearch annotation processor
                    from the source nested class [${objectFieldInfo.qualifiedName}].

                    @see ${objectFieldInfo.qualifiedName}
                    @generated by QElasticsearch annotation processor
                    """
            .trimIndent()
        )
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

    addPropertiesFromClassToObjectBuilder(
      objectFieldInfo.classDeclaration,
      objectBuilder,
      fieldGenerators,
      fieldTypeExtractor,
      objectFieldRegistry,
      importContext,
    )

    // Recursively add nested objects for this class
    addNestedObjectsToClassBuilder(
      objectFieldInfo.classDeclaration,
      objectBuilder,
      fieldGenerators,
      fieldTypeExtractor,
      objectFieldRegistry,
      importContext,
    )

    return objectBuilder
  }

  /** Adds properties from a class declaration to an object builder. */
  private fun addPropertiesFromClassToObjectBuilder(
    classDeclaration: KSClassDeclaration,
    objectBuilder: TypeSpec.Builder,
    fieldGenerators: FieldGenerators,
    fieldTypeExtractor: FieldTypeExtractor,
    objectFieldRegistry: ObjectFieldRegistry,
    importContext: ImportContext,
  ) {
    val typeParameterResolver = classDeclaration.typeParameters.toTypeParameterResolver()

    processUniqueProperties(classDeclaration) { property ->
      fieldGenerators.processProperty(
        property,
        objectBuilder,
        importContext,
        typeParameterResolver,
        fieldTypeExtractor,
        objectFieldRegistry,
      )
    }

    // Process annotated getter methods for interfaces
    logger.info("Checking class ${classDeclaration.simpleName.asString()} for getter methods...")
    processAnnotatedGetterMethods(classDeclaration) { method ->
      logger.info("Found annotated getter method: ${method.simpleName.asString()}")
      fieldGenerators.processAnnotatedMethod(
        method,
        objectBuilder,
        importContext,
        typeParameterResolver,
      )
    }

    // Don't add nested objects here to avoid duplicates
    // They will be added as top-level objects
  }

  private fun registerObjectField(
    classDeclaration: KSClassDeclaration,
    documentClass: KSClassDeclaration?,
  ) {
    val targetPackage = classDeclaration.packageName.asString()

    if (isStandardLibraryType(targetPackage)) {
      return
    }

    val className = generateUniqueQClassName(classDeclaration)
    val objectFieldKey = generateObjectFieldKey(classDeclaration)

    val parentDocumentClass =
      if (documentClass != null && isActuallyNestedClass(classDeclaration, documentClass)) {
        documentClass
      } else {
        isNestedWithinAnyQClass(classDeclaration)
      }

    globalObjectFields[objectFieldKey] =
      ObjectFieldInfo(
        className = className,
        packageName = targetPackage,
        classDeclaration = classDeclaration,
        qualifiedName =
          classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString(),
        parentDocumentClass = parentDocumentClass,
      )
  }

  private fun collectNestedClasses(
    parentClass: KSClassDeclaration,
    allClasses: MutableList<KSClassDeclaration>,
  ) {
    parentClass.declarations.filterIsInstance<KSClassDeclaration>().forEach { nestedClass ->
      if (
        nestedClass.classKind == ClassKind.CLASS || nestedClass.classKind == ClassKind.INTERFACE
      ) {
        allClasses.add(nestedClass)
        collectNestedClasses(nestedClass, allClasses)
      }
    }
  }

  private fun collectObjectFieldsFromClass(
    classDeclaration: KSClassDeclaration,
    fieldTypeExtractor: FieldTypeExtractor,
  ) {
    processClassProperties(classDeclaration, fieldTypeExtractor) { nestedClass ->
      val objectFieldKey = generateObjectFieldKey(nestedClass)
      if (objectFieldKey !in globalObjectFields) {
        registerObjectField(nestedClass, null)
        collectObjectFieldsFromClass(nestedClass, fieldTypeExtractor)
      }
    }
  }

  private fun generateUniqueQClassName(classDeclaration: KSClassDeclaration): String {
    val simpleName = classDeclaration.simpleName.asString()
    val parentClass = classDeclaration.parentDeclaration as? KSClassDeclaration

    return if (parentClass != null) {
      "Q${parentClass.simpleName.asString()}$simpleName"
    } else {
      "Q$simpleName"
    }
  }

  private fun generateObjectFieldKey(classDeclaration: KSClassDeclaration): String =
    classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()

  private fun isActuallyNestedClass(
    nestedClass: KSClassDeclaration,
    parentClass: KSClassDeclaration?,
  ): Boolean {
    if (parentClass == null) return false

    // Check if the nested class is contained within the parent class hierarchy
    return isNestedWithinClass(nestedClass, parentClass)
  }

  private fun isNestedWithinClass(
    nestedClass: KSClassDeclaration,
    parentClass: KSClassDeclaration,
  ): Boolean {
    // Check direct nested classes
    val directNestedClasses = parentClass.declarations.filterIsInstance<KSClassDeclaration>()

    // Check if it's a direct child
    if (
      directNestedClasses.any {
        it.qualifiedName?.asString() == nestedClass.qualifiedName?.asString()
      }
    ) {
      return true
    }

    // Recursively check nested classes
    return directNestedClasses.any { directChild -> isNestedWithinClass(nestedClass, directChild) }
  }

  private fun isNestedWithinAnyQClass(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
    var parentClass = classDeclaration.parentDeclaration
    while (parentClass != null) {
      if (parentClass is KSClassDeclaration) {
        val hasDocumentAnnotation =
          parentClass.annotations.any { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() ==
              Document::class.qualifiedName
          }
        val hasFieldAnnotations =
          parentClass.getAllProperties().any { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            fieldAnnotation != null
          }
        if (hasDocumentAnnotation || hasFieldAnnotations) {
          return parentClass
        }
      }
      parentClass = parentClass.parentDeclaration
    }
    return null
  }

  fun getGlobalObjectFields(): Map<String, ObjectFieldInfo> = globalObjectFields.toMap()

  /** Common helper to process properties of a class and extract object/nested fields. */
  private fun processClassProperties(
    classDeclaration: KSClassDeclaration,
    fieldTypeExtractor: FieldTypeExtractor,
    onNestedClassFound: (KSClassDeclaration) -> Unit,
  ) {
    classDeclaration.getAllProperties().forEach { property ->
      val fieldAnnotation = property.findAnnotation(Field::class) ?: return@forEach

      val fieldType = fieldTypeExtractor.determineFieldType(property, fieldAnnotation)

      if (fieldType.isObjectType) {
        val nestedClass = extractNestedClassFromProperty(property)
        if (nestedClass != null) {
          onNestedClassFound(nestedClass)
        }
      }
    }
  }

  /** Extracts nested class from property, handling both collections and direct object types. */
  private fun extractNestedClassFromProperty(property: KSPropertyDeclaration): KSClassDeclaration? =
    if (isCollectionType(getSimpleTypeName(property.type))) {
      getCollectionElementType(property)
    } else {
      property.type.resolve().declaration as? KSClassDeclaration
    }

  /** Processes unique properties of a class to avoid duplicates. */
  private fun processUniqueProperties(
    classDeclaration: KSClassDeclaration,
    onProperty: (KSPropertyDeclaration) -> Unit,
  ) {
    val processedPropertyNames = mutableSetOf<String>()
    classDeclaration.getAllProperties().forEach { property ->
      val propertyName = property.simpleName.asString()
      if (propertyName !in processedPropertyNames) {
        onProperty(property)
        processedPropertyNames.add(propertyName)
      }
    }
  }

  // Extension function to find annotations
  private fun KSPropertyDeclaration.findAnnotation(
    annotationClass: kotlin.reflect.KClass<*>
  ): KSAnnotation? =
    annotations.find {
      it.annotationType.resolve().declaration.qualifiedName?.asString() ==
        annotationClass.qualifiedName
    }

  /** Checks if a function is annotated with @Field. */
  private fun isAnnotatedMethod(method: KSFunctionDeclaration): Boolean {
    return method.parameters.isEmpty() &&
      method.returnType != null &&
      method.findFunctionAnnotation(Field::class) != null
  }

  /** Extension function to find annotations on functions. */
  private fun KSFunctionDeclaration.findFunctionAnnotation(
    annotationClass: kotlin.reflect.KClass<*>
  ): KSAnnotation? =
    annotations.find {
      it.annotationType.resolve().declaration.qualifiedName?.asString() ==
        annotationClass.qualifiedName
    }

  /** Processes annotated getter methods from a class declaration. */
  fun processAnnotatedGetterMethods(
    classDeclaration: KSClassDeclaration,
    processor: (KSFunctionDeclaration) -> Unit,
  ) {
    val processedMethodNames = mutableSetOf<String>()
    val allMethods = classDeclaration.getAllFunctions().toList()
    logger.info("Found ${allMethods.size} methods in ${classDeclaration.simpleName.asString()}")

    allMethods.forEach { method ->
      val methodName = method.simpleName.asString()
      logger.info("Checking method: $methodName, isAnnotated: ${isAnnotatedMethod(method)}")
      if (methodName !in processedMethodNames && isAnnotatedMethod(method)) {
        processor(method)
        processedMethodNames.add(methodName)
      }
    }
    logger.info(
      "Processed ${processedMethodNames.size} getter methods for ${classDeclaration.simpleName.asString()}"
    )
  }
}
