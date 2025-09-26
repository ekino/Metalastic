package com.metalastic.core

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Sealed class for all field types in the DSL. Provides path traversal functionality for nested
 * field access. Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field<T : Any?>(
  private val parent: ObjectField<*>? = null,
  private val name: String,
  private val fieldType: KType,
) {

  private val path: String by lazy {
    parents()
      .filterNot { it.name().isEmpty() }
      .toList()
      .reversed()
      .joinToString(separator = ".") { it.name() }
      .takeIf { it.isNotEmpty() }
      ?.let { "$it.$name" } ?: name
  }

  fun path(): String = path

  fun name(): String = name

  fun parent(): ObjectField<*>? = parent

  fun parents() = generateSequence(parent()) { it.parent() }

  fun isNestedPath(): Boolean = parents().any { it.isNested() }

  fun nestedPaths(): Sequence<String> =
    parents().mapNotNull {
      if (it.isNested()) {
        it.path()
      } else {
        null
      }
    }

  fun fieldType(): KType = fieldType

  /**
   * Returns the KClass for this field's type, or null if the type is not a concrete class (e.g., if
   * it's a type parameter or complex type intersection).
   *
   * For generated metamodels, this will always return a non-null KClass.
   *
   * @return The KClass representing this field's type, or null if not a concrete class
   */
  @Suppress("UNCHECKED_CAST")
  fun fieldClass(): KClass<out T & Any>? = fieldType.classifier as? KClass<out T & Any>
}
