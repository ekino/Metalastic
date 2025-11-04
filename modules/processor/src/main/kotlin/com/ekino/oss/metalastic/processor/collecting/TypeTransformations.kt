package com.ekino.oss.metalastic.processor.collecting

import com.ekino.oss.metalastic.core.UnExposablePrivateClass
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

/** Type transformation utilities for KSP symbol processing. */

/**
 * Checks if this KSType represents a type parameter (like T, E, K, V).
 *
 * Type parameters are generic placeholders defined on classes, functions, or interfaces that need
 * special handling during code generation to avoid unresolved type references.
 *
 * @return true if this type is a type parameter, false if it's a concrete type
 */
fun KSType.isTypeParameter(): Boolean = declaration is KSTypeParameter

/**
 * Converts a KSType to TypeName, handling type parameters and private classes safely.
 *
 * Type parameters are converted to `Any` and private classes are converted to
 * `UnExposablePrivateClass` to avoid compilation issues when they are not available or accessible
 * in the generated code context. Accessible concrete types are processed with safe generic
 * arguments using star projections where needed.
 *
 * **Examples:**
 * - Type parameter `T` → `Any`
 * - Private class `PrivateClass` → `UnExposablePrivateClass`
 * - Concrete type `String` → `String`
 * - Complex type `List<T>` → `List<*>`
 * - Complex type `Map<String, PrivateClass>` → `Map<String, *>`
 *
 * @param typeParameterResolver The resolver for converting KSP types to KotlinPoet TypeNames
 * @return A safe TypeName that can be used in generated code
 */
fun KSType.toSafeTypeName(typeParameterResolver: TypeParameterResolver): TypeName =
  when {
    this.declaration.getVisibility() == Visibility.PRIVATE ->
      UnExposablePrivateClass::class.asTypeName()
    isTypeParameter() -> Any::class.asTypeName()
    arguments.isEmpty() -> toTypeName(typeParameterResolver)
    else -> {
      // Handle generic types with private class arguments directly
      val rawType = replace(emptyList())
      val baseTypeName = rawType.toTypeName(typeParameterResolver)
      val safeArguments =
        arguments.map { arg ->
          val argType = arg.type?.resolve()
          when {
            argType?.declaration?.getVisibility() == Visibility.PRIVATE ->
              UnExposablePrivateClass::class.asTypeName()
            argType?.declaration is KSTypeParameter -> com.squareup.kotlinpoet.STAR
            else -> argType?.toSafeTypeName(typeParameterResolver) ?: com.squareup.kotlinpoet.STAR
          }
        }

      when (baseTypeName) {
        is ClassName -> baseTypeName.parameterizedBy(safeArguments)
        else -> baseTypeName
      }
    }
  }
