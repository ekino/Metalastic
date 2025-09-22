package com.metalastic.processor.collecting

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Variance
import com.metalastic.processor.kspimplementation.StarProjectedKSTypeReference
import com.metalastic.processor.kspimplementation.StarProjectedTypeArgument
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
 * Converts a KSType to TypeName, handling type parameters safely.
 *
 * Type parameters are converted to star projections to avoid compilation issues when the type
 * parameter is not available in the generated code context. Concrete types are processed with star
 * projection for complex generics.
 *
 * **Examples:**
 * - Type parameter `T` → `Any`
 * - Concrete type `String` → `String`
 * - Complex type `List<T>` → `List<*>`
 *
 * @param typeParameterResolver The resolver for converting KSP types to KotlinPoet TypeNames
 * @return A safe TypeName that can be used in generated code
 */
fun KSType.toSafeTypeName(
  typeParameterResolver: TypeParameterResolver
): com.squareup.kotlinpoet.TypeName {
  return if (isTypeParameter()) {
    Any::class.asTypeName()
  } else {
    withStarProjectedGenericArguments().toTypeName(typeParameterResolver)
  }
}

/**
 * Converts complex generic types to use star projections where appropriate for cleaner code
 * generation.
 *
 * This function is essential for handling complex nested generics in Metalastic metamodel
 * generation. It converts type parameters that don't exist in the generated context to star
 * projections (`*`), while preserving concrete types.
 *
 * **Examples:**
 * - `Map<String, List<T>>` → `Map<String, List<*>>`
 * - `Set<? extends Something>` → `Set<*>`
 * - `MyClass<String>` → `MyClass<String>` (preserved)
 *
 * **Use Case in Metalastic:** When generating metamodel classes, complex field types like
 * `Map<String, List<SomeClass>>` need to be handled carefully. This function ensures the generated
 * code is compilable while maintaining as much type information as possible.
 *
 * @return A new [KSType] with type parameters replaced by star projections where necessary
 */
fun KSType.withStarProjectedGenericArguments(): KSType {
  val projectedArgs =
    arguments.map {
      val type = it.type?.resolve()
      if (type?.declaration is KSTypeParameter) {
        it.asStarProjectedTypeArgument()
      } else {
        StarProjectedKSTypeReference(it)
      }
    }
  return replace(projectedArgs)
}

/**
 * Converts a type argument to use star projection variance.
 *
 * This is used when we need to replace type parameters with star projections (`*`) to make the
 * generated code compilable in contexts where the original type parameter is not available.
 *
 * @return A [StarProjectedTypeArgument] with [Variance.STAR]
 */
fun KSTypeArgument.asStarProjectedTypeArgument() = StarProjectedTypeArgument(this)
