package com.metalastic.core

/**
 * Sealed class for all field types in the DSL. Provides path traversal functionality for nested
 * field access. Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field<T>(private val parent: ObjectField<*>? = null, private val name: String) {

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

  fun isNestedPath(): kotlin.Boolean = parents().any { it.nested() }

  fun nestedPaths(): Sequence<String> =
    parents().mapNotNull {
      if (it.nested()) {
        it.path()
      } else {
        null
      }
    }
}
