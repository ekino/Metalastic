package com.qelasticsearch.dsl

/**
 * Sealed class for all field types in the DSL. Provides path traversal functionality for nested
 * field access. Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field(private val parent: ObjectField? = null, private val path: String) {

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
class TextField<T>(parent: ObjectField, path: String) : Field(parent, path)

class KeywordField<T>(parent: ObjectField, path: String) : Field(parent, path)

// Numeric fields
class LongField<T>(parent: ObjectField, path: String) : Field(parent, path)

class IntegerField<T>(parent: ObjectField, path: String) : Field(parent, path)

class ShortField<T>(parent: ObjectField, path: String) : Field(parent, path)

class ByteField<T>(parent: ObjectField, path: String) : Field(parent, path)

class DoubleField<T>(parent: ObjectField, path: String) : Field(parent, path)

class FloatField<T>(parent: ObjectField, path: String) : Field(parent, path)

class HalfFloatField(parent: ObjectField, path: String) : Field(parent, path)

class ScaledFloatField(parent: ObjectField, path: String) : Field(parent, path)

// Date fields
class DateField<T>(parent: ObjectField, path: String) : Field(parent, path)

class DateNanosField(parent: ObjectField, path: String) : Field(parent, path)

// Boolean field
class BooleanField<T>(parent: ObjectField, path: String) : Field(parent, path)

// Binary field
class BinaryField(parent: ObjectField, path: String) : Field(parent, path)

// Geo fields
class IpField(parent: ObjectField, path: String) : Field(parent, path)

class GeoPointField(parent: ObjectField, path: String) : Field(parent, path)

class GeoShapeField(parent: ObjectField, path: String) : Field(parent, path)

// Specialized fields
class CompletionField(parent: ObjectField, path: String) : Field(parent, path)

class TokenCountField(parent: ObjectField, path: String) : Field(parent, path)

class PercolatorField(parent: ObjectField, path: String) : Field(parent, path)

class RankFeatureField(parent: ObjectField, path: String) : Field(parent, path)

class RankFeaturesField(parent: ObjectField, path: String) : Field(parent, path)

class FlattenedField(parent: ObjectField, path: String) : Field(parent, path)

class ShapeField(parent: ObjectField, path: String) : Field(parent, path)

class PointField(parent: ObjectField, path: String) : Field(parent, path)

class ConstantKeywordField(parent: ObjectField, path: String) : Field(parent, path)

class WildcardField(parent: ObjectField, path: String) : Field(parent, path)

// Range fields
class IntegerRangeField(parent: ObjectField, path: String) : Field(parent, path)

class FloatRangeField(parent: ObjectField, path: String) : Field(parent, path)

class LongRangeField(parent: ObjectField, path: String) : Field(parent, path)

class DoubleRangeField(parent: ObjectField, path: String) : Field(parent, path)

class DateRangeField(parent: ObjectField, path: String) : Field(parent, path)

class IpRangeField(parent: ObjectField, path: String) : Field(parent, path)

// class NestedField<T : ObjectFields>(
//    name: String,
// ) : Field
