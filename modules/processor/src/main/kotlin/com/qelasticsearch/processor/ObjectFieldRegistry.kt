package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.FieldType

/** Handles object field registration and generation. */
class ObjectFieldRegistry(
  private val logger: KSPLogger,
  private val globalObjectFields: Map<String, ObjectFieldInfo>,
) {
  /** Generates an object field property. */
  fun generateObjectFieldProperty(
    objectBuilder: TypeSpec.Builder,
    property: KSPropertyDeclaration,
    propertyName: String,
    fieldType: ProcessedFieldType,
    importContext: ImportContext,
  ) {
    val actualClassDeclaration = findActualClassDeclaration(fieldType)
    if (actualClassDeclaration == null) {
      logger.warn("Could not find class declaration for field type: ${fieldType.kotlinTypeName}")
      return
    }

    val objectFieldInfo = getObjectFieldInfo(actualClassDeclaration, fieldType) ?: return
    val currentDocumentClass = findContainingDocumentClass(property)
    val propertyTypeName =
      determinePropertyTypeName(
        objectFieldInfo,
        isNestedInCurrentDocument(objectFieldInfo, currentDocumentClass),
      )

    val initializationDetails =
      determineInitializationDetails(
        objectFieldInfo,
        currentDocumentClass,
        property,
        propertyTypeName,
        importContext,
      )

    addImportsIfNeeded(importContext, initializationDetails, propertyTypeName)

    val isNested = fieldType.elasticsearchType == FieldType.Nested
    val kdoc =
      generateFieldKDoc(property, fieldType, listOf("@${fieldType.elasticsearchType.name}"))

    objectBuilder.addProperty(
      PropertySpec.builder(propertyName, propertyTypeName)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .addKdoc(kdoc)
        .initializer(initializationDetails.initializer, propertyName, isNested)
        .build()
    )
  }

  private fun getObjectFieldInfo(
    actualClassDeclaration: KSClassDeclaration,
    fieldType: ProcessedFieldType,
  ): ObjectFieldInfo? {
    val objectFieldKey = generateObjectFieldKey(actualClassDeclaration)
    val objectFieldInfo = globalObjectFields[objectFieldKey]
    if (objectFieldInfo == null) {
      logger.warn(
        "No ObjectField registered for key: $objectFieldKey (type: ${fieldType.kotlinTypeName})"
      )
    }
    return objectFieldInfo
  }

  private data class InitializationDetails(
    val initializer: String,
    val functionName: String,
    val needsImport: Boolean,
    val importPath: String? = null,
  )

  private fun determineInitializationDetails(
    objectFieldInfo: ObjectFieldInfo,
    currentDocumentClass: KSClassDeclaration?,
    property: KSPropertyDeclaration,
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
    importContext: ImportContext,
  ): InitializationDetails {
    val currentPackage = currentDocumentClass?.packageName?.asString() ?: ""
    val isNestedInThisDocument = isNestedInCurrentDocument(objectFieldInfo, currentDocumentClass)

    return when {
      isNestedInThisDocument -> handleNestedInCurrentDocument(propertyTypeName, currentPackage)
      isNestedClassFallback(objectFieldInfo, property) ->
        handleNestedClassFallback(propertyTypeName, currentPackage)
      objectFieldInfo.parentDocumentClass != null ->
        handleCrossDocumentNested(objectFieldInfo, propertyTypeName)
      else -> handleStandaloneClass(propertyTypeName, currentPackage)
    }
  }

  private fun isNestedClassFallback(
    objectFieldInfo: ObjectFieldInfo,
    property: KSPropertyDeclaration,
  ): Boolean {
    return objectFieldInfo.parentDocumentClass != null &&
      objectFieldInfo.parentDocumentClass.simpleName.asString() ==
        findContainingDocumentClass(property)?.simpleName?.asString()
  }

  private fun handleNestedInCurrentDocument(
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
    currentPackage: String,
  ): InitializationDetails {
    val className = extractClassName(propertyTypeName)
    val functionName = className.replaceFirstChar { it.lowercase() }
    val typeName = simplifyTypeNameIfSamePackage(propertyTypeName, currentPackage)

    return InitializationDetails(
      initializer = "$typeName(this, %S, %L)",
      functionName = functionName,
      needsImport = false,
    )
  }

  private fun handleNestedClassFallback(
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
    currentPackage: String,
  ): InitializationDetails {
    val className = extractClassName(propertyTypeName)
    val functionName = className.replaceFirstChar { it.lowercase() }
    val typeName = simplifyTypeNameIfSamePackage(propertyTypeName, currentPackage)

    return if (functionName == "nameCollision") {
      InitializationDetails(
        initializer = "$typeName(this, %S, %L)",
        functionName = functionName,
        needsImport = false,
      )
    } else {
      InitializationDetails(
        initializer = "$typeName(this, %S, %L)",
        functionName = functionName,
        needsImport = false,
      )
    }
  }

  private fun handleCrossDocumentNested(
    objectFieldInfo: ObjectFieldInfo,
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
  ): InitializationDetails {
    val fullTypeName = propertyTypeName.toString()
    val functionName =
      objectFieldInfo.classDeclaration.simpleName.asString().replaceFirstChar { it.lowercase() }

    return InitializationDetails(
      initializer = "$fullTypeName(this, %S, %L)",
      functionName = functionName,
      needsImport = false,
    )
  }

  private fun handleStandaloneClass(
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
    currentPackage: String,
  ): InitializationDetails {
    val className = extractClassName(propertyTypeName)
    val functionName = className.replaceFirstChar { it.lowercase() }
    val typeName = simplifyTypeNameIfSamePackage(propertyTypeName, currentPackage)

    return InitializationDetails(
      initializer = "$typeName(this, %S, %L)",
      functionName = functionName,
      needsImport = false,
    )
  }

  private fun extractClassName(typeName: com.squareup.kotlinpoet.TypeName): String {
    return when (typeName) {
      is ClassName -> typeName.simpleName
      else -> typeName.toString()
    }
  }

  private fun simplifyTypeNameIfSamePackage(
    typeName: com.squareup.kotlinpoet.TypeName,
    currentPackage: String,
  ): String {
    return when (typeName) {
      is ClassName -> {
        // Only simplify if it's in the same package
        if (typeName.packageName == currentPackage) {
          simplifyTypeNameWithoutImports(typeName)
        } else {
          typeName.toString()
        }
      }
      else -> simplifyTypeNameWithoutImports(typeName)
    }
  }

  private fun extractPackageName(typeName: com.squareup.kotlinpoet.TypeName): String {
    return when (typeName) {
      is ClassName -> typeName.packageName
      else -> ""
    }
  }

  private fun addImportsIfNeeded(
    importContext: ImportContext,
    details: InitializationDetails,
    propertyTypeName: com.squareup.kotlinpoet.TypeName,
  ) {
    if (details.needsImport && details.importPath != null) {
      val packageName = extractPackageName(propertyTypeName)
      if (packageName.isNotEmpty()) {
        importContext.usedImports.add(details.importPath)
      }
    }
  }

  private fun findActualClassDeclaration(fieldType: ProcessedFieldType): KSClassDeclaration? {
    val kotlinType = fieldType.kotlinType.resolve()

    return if (isCollectionType(getSimpleTypeName(fieldType.kotlinType))) {
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
  ): Boolean {
    if (currentDocumentClass == null) return false

    val sameParentDocument = objectFieldInfo.parentDocumentClass == currentDocumentClass
    val actuallyNested =
      isActuallyNestedClass(objectFieldInfo.classDeclaration, currentDocumentClass)
    val samePackage = objectFieldInfo.packageName == currentDocumentClass.packageName.asString()

    // Check if the qualified name contains either the source class name or the Q-class name
    val sourceClassName = currentDocumentClass.simpleName.asString()
    val qClassName = "Q$sourceClassName"
    val containsSourceName = objectFieldInfo.qualifiedName.contains("$sourceClassName.")
    val containsQName = objectFieldInfo.qualifiedName.contains("$qClassName.")

    return sameParentDocument &&
      actuallyNested &&
      samePackage &&
      (containsSourceName || containsQName)
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

    return if (
      rootDocumentQualifiedName != null && qualifiedName.startsWith(rootDocumentQualifiedName)
    ) {
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
          annotation.annotationType.resolve().declaration.qualifiedName?.asString() ==
            Document::class.qualifiedName
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
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() ==
              Document::class.qualifiedName
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
