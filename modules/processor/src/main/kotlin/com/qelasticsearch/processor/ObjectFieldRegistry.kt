package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.qelasticsearch.processor.CoreConstants.Q_PREFIX
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
  /** Collects object field types for import optimization. */
  fun collectObjectFieldType(
    property: KSPropertyDeclaration,
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

    // Instead of using complex KotlinPoet TypeName manipulation,
    // register the actual qualified class name directly
    val qualifiedName =
      determineQualifiedClassName(
        objectFieldInfo,
        isNestedInCurrentDocument(objectFieldInfo, currentDocumentClass),
      )

    importContext.registerTypeUsage(qualifiedName)
  }

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

    val isNested = fieldType.elasticsearchType == FieldType.Nested
    val kdoc =
      generateFieldKDoc(property, fieldType, listOf("@${fieldType.elasticsearchType.name}"))

    // Determine the optimal type name for constructor call
    val qualifiedName =
      determineQualifiedClassName(
        objectFieldInfo,
        isNestedInCurrentDocument(objectFieldInfo, currentDocumentClass),
      )

    // If the qualified name would reference a nested class within the current Q-class,
    // use simple name instead
    val currentQClassName =
      if (currentDocumentClass != null) {
        "$Q_PREFIX${currentDocumentClass.simpleName.asString()}"
      } else null

    val optimalTypeName =
      if (currentQClassName != null && qualifiedName.contains(".$currentQClassName.")) {
        // This is a nested class within the current Q-class, use simple name
        objectFieldInfo.classDeclaration.simpleName.asString()
      } else {
        // Use the import context to resolve the optimal name
        importContext.getOptimalTypeName(qualifiedName)
      }

    objectBuilder.addProperty(
      PropertySpec.builder(propertyName, propertyTypeName)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .addKdoc(kdoc)
        .initializer("$optimalTypeName(this, %S, %L)", propertyName, isNested)
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

    // Simple check: if the parent document class matches the current document class,
    // and they are actually nested (one contains the other), then it's nested in current document
    val sameParentDocument = objectFieldInfo.parentDocumentClass == currentDocumentClass
    val actuallyNested =
      isActuallyNestedClass(objectFieldInfo.classDeclaration, currentDocumentClass)

    return sameParentDocument && actuallyNested
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
      // For standalone classes, use the prefixed class name
      val qClassName =
        if (objectFieldInfo.className.startsWith(Q_PREFIX)) {
          objectFieldInfo.className
        } else {
          "$Q_PREFIX${objectFieldInfo.classDeclaration.simpleName.asString()}"
        }
      ClassName(objectFieldInfo.packageName, qClassName)
    }

  private fun generateUniqueQClassName(classDeclaration: KSClassDeclaration): String {
    val simpleName = classDeclaration.simpleName.asString()

    // Always use the simple prefix class name to avoid naming conflicts
    return "$Q_PREFIX$simpleName"
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

  private fun determineQualifiedClassName(
    objectFieldInfo: ObjectFieldInfo,
    isNestedInCurrentDocument: Boolean,
  ): String =
    if (isNestedInCurrentDocument) {
      // For nested classes within the same document, use the parent Q-class + nested path
      val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass!!)
      val parentQClassName = generateUniqueQClassName(rootDocumentClass)
      val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)
      "${objectFieldInfo.packageName}.${parentQClassName}.${nestedPath}"
    } else if (objectFieldInfo.parentDocumentClass != null) {
      // For nested classes in other documents, use the parent Q-class + nested path
      val rootDocumentClass = findRootDocumentClass(objectFieldInfo.parentDocumentClass)
      val parentQClassName = generateUniqueQClassName(rootDocumentClass)
      val nestedPath = extractNestedPath(objectFieldInfo, rootDocumentClass)
      "${objectFieldInfo.packageName}.${parentQClassName}.${nestedPath}"
    } else {
      // For standalone classes, use the prefixed class name
      val qClassName =
        if (objectFieldInfo.className.startsWith(Q_PREFIX)) {
          objectFieldInfo.className
        } else {
          "$Q_PREFIX${objectFieldInfo.classDeclaration.simpleName.asString()}"
        }
      "${objectFieldInfo.packageName}.$qClassName"
    }
}
