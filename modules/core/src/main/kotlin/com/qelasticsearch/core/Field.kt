package com.qelasticsearch.core

/**
 * Sealed class for all field types in the DSL. Provides path traversal functionality for nested
 * field access. Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field(private val parent: ObjectField? = null, fieldName: String) {
  private val path: String =
    parent?.let { if (it.path().isEmpty()) fieldName else "${it.path()}.$fieldName" } ?: fieldName

  @YellowColor fun path(): String = path

  @YellowColor fun name(): String = path.substringAfterLast('.')

  @YellowColor fun parent(): ObjectField? = parent

  @YellowColor fun parents() = generateSequence(parent()) { it.parent() }

  @YellowColor fun isNestedPath(): Boolean = parents().any { it.nested() }

  @YellowColor
  fun nestedPaths(): Sequence<String> =
    parents().mapNotNull {
      if (it.nested()) {
        it.path()
      } else {
        null
      }
    }
}

// Text fields
class TextField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class KeywordField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Numeric fields
class LongField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class IntegerField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class ShortField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class ByteField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class DoubleField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class FloatField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class HalfFloatField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class ScaledFloatField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Date fields
class DateField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class DateNanosField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Boolean field
class BooleanField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Binary field
class BinaryField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Geo fields
class IpField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class GeoPointField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class GeoShapeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Specialized fields
class CompletionField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class TokenCountField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class PercolatorField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class RankFeatureField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class RankFeaturesField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class FlattenedField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class ShapeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class PointField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class ConstantKeywordField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class WildcardField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Range fields
class IntegerRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class FloatRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class LongRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class DoubleRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class DateRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

class IpRangeField(parent: ObjectField, fieldName: String) : Field(parent, fieldName)

// Dynamic field for runtime/generic field references
class DynamicField<T>(parent: ObjectField, fieldName: String) : Field(parent, fieldName)
