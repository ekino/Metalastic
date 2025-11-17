/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

@file:Suppress("TooManyFunctions")

package com.ekino.oss.metalastic.processor.collecting

import com.ekino.oss.metalastic.processor.CoreConstants.MethodPrefixes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/** Utility functions for collection type detection and type exploration. */

/** Checks if a package is a standard library type that should be skipped. */
fun KSDeclaration.isStandardLibraryType(): Boolean =
  packageName.asString().let { packageName ->
    packageName.startsWith("kotlin.") ||
      packageName.startsWith("java.") ||
      packageName.startsWith("javax.") ||
      packageName == "kotlin" ||
      packageName == "java"
  }

/** Checks if a type declaration represents a supported collection type. */
fun KSDeclaration.isSupportedCollectionType(): Boolean {
  val simpleName = simpleName.asString()
  return simpleName in
    setOf(
      // Kotlin collections
      "List",
      "MutableList",
      "ArrayList",
      "LinkedList",
      "Set",
      "MutableSet",
      "HashSet",
      "LinkedHashSet",
      "Collection",
      "MutableCollection",
      // Java collections
      "Array",
    )
}

fun KSPropertyDeclaration.hasFieldAnnotation() =
  isAnnotationPresent(Field::class) || isAnnotationPresent(MultiField::class)

fun KSPropertyDeclaration.hasFieldTypeObjectOrNested() =
  getAnnotationsByType(Field::class).firstOrNull()?.type.let {
    it == FieldType.Object || it == FieldType.Nested
  }

fun KSClassDeclaration.fullyQualifiedName(): String = qualifiedName!!.asString()

fun KSFunctionDeclaration.toPropertyName(): String =
  simpleName.asString().let { methodName ->
    when {
      methodName.startsWith(MethodPrefixes.GET) ->
        methodName.removePrefix(MethodPrefixes.GET).replaceFirstChar { it.lowercase() }

      methodName.startsWith(MethodPrefixes.IS) ->
        methodName.removePrefix(MethodPrefixes.IS).replaceFirstChar { it.lowercase() }

      else -> methodName
    }
  }

fun KSDeclaration.resolveType() =
  when (this) {
    is KSPropertyDeclaration -> type.resolve()
    is KSFunctionDeclaration -> returnType?.resolve()
    else -> null
  } ?: error("Unsupported  KSDeclaration")

/**
 * Extracts the target type for exploration from a property. Handles both direct types and
 * collection types with single generic parameters. Returns null for unsupported types or standard
 * library types.
 */
fun KSDeclaration.extractPotentialQClass(): KSClassDeclaration? {
  val resolvedType = resolveType()
  val declaration = resolvedType.declaration

  // DON'T short-circuit standard library types here - we need to check collections first!
  val isSupportedCollection = declaration.isSupportedCollectionType()

  return when {
    // Single-parameter collection types
    isSupportedCollection && resolvedType.arguments.size == 1 -> {
      val firstArg = resolvedType.arguments.first()
      val elementType = firstArg.type?.resolve()
      if (elementType != null) {
        val isStandardLib = elementType.declaration.isStandardLibraryType()
        if (!isStandardLib) {
          elementType
        } else {
          null
        }
      } else {
        null
      }
    }

    // Multi-parameter collections (Map<K,V>, etc.) - SKIP
    isSupportedCollection && resolvedType.arguments.size > 1 -> {
      null
    }

    // Direct object types (but skip standard library types)
    !isSupportedCollection -> {
      if (declaration.isStandardLibraryType()) {
        null
      } else {
        resolvedType
      }
    }

    else -> {
      null
    }
  }?.declaration as? KSClassDeclaration
}

fun KSDeclaration.toFieldName() =
  when (this) {
    is KSPropertyDeclaration -> simpleName.asString()
    is KSFunctionDeclaration -> toPropertyName()

    else -> error("Unexpected declaration type") // should never happen
  }

/**
 * Checks if a string is a valid Kotlin identifier that follows naming conventions.
 *
 * Valid Kotlin property names should:
 * - Be valid Java/Kotlin identifiers
 * - Follow camelCase naming convention (no underscores unless truly necessary)
 * - Start with lowercase letter
 */
fun String.isValidKotlinIdentifier(): Boolean =
  isNotEmpty() &&
    first().isJavaIdentifierStart() &&
    drop(1).all { it.isJavaIdentifierPart() } &&
    followsKotlinNamingConvention()

/**
 * Checks if a string follows Kotlin property naming conventions (camelCase). Allows underscores
 * only if the name starts with underscore (private convention).
 */
private fun String.followsKotlinNamingConvention(): Boolean {
  return when {
    isEmpty() -> false
    first() == '_' -> true // Allow _privateProperty convention
    first().isUpperCase() -> false // Property names should start with lowercase
    contains('_') -> false // Avoid snake_case in favor of camelCase
    else -> true
  }
}
