/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.core

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * # Metalastic Metamodel types Hierarchy
 *
 * Sealed class for all metamodel types in the DSL. Provides path traversal functionality for nested
 * field access. Using sealed class ensures exhaustive pattern matching and type safety.
 *
 * ```
 * sealed class Metamodel<T>
 * ├── sealed class Field<T> : Metamodel<T>
 * │   ├── class AliasField<T> : Field<T>
 * │   ├── class AnnotatedTextField<T> : Field<T>
 * │   ├── class AutoField<T> : Field<T>
 * │   ├── class BinaryField<T> : Field<T>
 * │   ├── class BooleanField<T> : Field<T>
 * │   ├── class ByteField<T> : Field<T>
 * │   ├── class CompletionField<T> : Field<T>
 * │   ├── class ConstantKeywordField<T> : Field<T>
 * │   ├── class DateField<T> : Field<T>
 * │   ├── class DateNanosField<T> : Field<T>
 * │   ├── class DateRangeField<T> : Field<T>
 * │   ├── class DenseVectorField<T> : Field<T>
 * │   ├── class DoubleField<T> : Field<T>
 * │   ├── class DoubleRangeField<T> : Field<T>
 * │   ├── class FlattenedField<T> : Field<T>
 * │   ├── class FloatField<T> : Field<T>
 * │   ├── class FloatRangeField<T> : Field<T>
 * │   ├── class HalfFloatField<T> : Field<T>
 * │   ├── class IntegerField<T> : Field<T>
 * │   ├── class IntegerRangeField<T> : Field<T>
 * │   ├── class IpField<T> : Field<T>
 * │   ├── class IpRangeField<T> : Field<T>
 * │   ├── class KeywordField<T> : Field<T>
 * │   ├── class LongField<T> : Field<T>
 * │   ├── class LongRangeField<T> : Field<T>
 * │   ├── class MatchOnlyTextField<T> : Field<T>
 * │   ├── class Murmur3Field<T> : Field<T>
 * │   ├── class PercolatorField<T> : Field<T>
 * │   ├── class PointField<T> : Field<T>
 * │   ├── class RankFeatureField<T> : Field<T>
 * │   ├── class RankFeaturesField<T> : Field<T>
 * │   ├── class ScaledFloatField<T> : Field<T>
 * │   ├── class SearchAsYouTypeField<T> : Field<T>
 * │   ├── class ShapeField<T> : Field<T>
 * │   ├── class ShortField<T> : Field<T>
 * │   ├── class TextField<T> : Field<T>
 * │   ├── class TokenCountField<T> : Field<T>
 * │   ├── class VersionField<T> : Field<T>
 * │   └── class WildcardField<T> : Field<T>
 * │
 * └── abstract class Container<T> : Metamodel<T>
 *     ├── abstract class MultiField<T, M> : Container<T>
 *     └── abstract class ObjectField<T> : Container<T>
 *         └── abstract class Document<T> : ObjectField<T>
 * ```
 *
 * @param T type of property this metamodel represents
 */
sealed class Metamodel<T : Any?>(private val name: String, private val fieldType: KType) {

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

  abstract fun parent(): Container<*>?

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
