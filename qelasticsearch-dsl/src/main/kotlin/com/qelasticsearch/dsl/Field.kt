package com.qelasticsearch.dsl

/**
 * Sealed interface for all field types in the DSL.
 * Provides path traversal functionality for nested field access.
 * Using sealed class ensures exhaustive pattern matching and type safety.
 *
 * All Elasticsearch field types inherit from this base class, providing
 * a unified interface for path calculation and field naming.
 */
sealed interface Field {
    fun path(): String

    fun name(): String = path().substringAfterLast('.')
}

// Text fields
class TextField<T>(
    path: String,
) : PathElement(path),
    Field

class KeywordField<T>(
    path: String,
) : PathElement(path),
    Field

// Numeric fields
class LongField<T>(
    path: String,
) : PathElement(path),
    Field

class IntegerField<T>(
    path: String,
) : PathElement(path),
    Field

class ShortField<T>(
    path: String,
) : PathElement(path),
    Field

class ByteField<T>(
    path: String,
) : PathElement(path),
    Field

class DoubleField<T>(
    path: String,
) : PathElement(path),
    Field

class FloatField<T>(
    path: String,
) : PathElement(path),
    Field

class HalfFloatField(
    path: String,
) : PathElement(path),
    Field

class ScaledFloatField(
    path: String,
) : PathElement(path),
    Field

// Date fields
class DateField<T>(
    path: String,
) : PathElement(path),
    Field

class DateNanosField(
    path: String,
) : PathElement(path),
    Field

// Boolean field
class BooleanField<T>(
    path: String,
) : PathElement(path),
    Field

// Binary field
class BinaryField(
    path: String,
) : PathElement(path),
    Field

// Geo fields
class IpField(
    path: String,
) : PathElement(path),
    Field

class GeoPointField(
    path: String,
) : PathElement(path),
    Field

class GeoShapeField(
    path: String,
) : PathElement(path),
    Field

// Specialized fields
class CompletionField(
    path: String,
) : PathElement(path),
    Field

class TokenCountField(
    path: String,
) : PathElement(path),
    Field

class PercolatorField(
    path: String,
) : PathElement(path),
    Field

class RankFeatureField(
    path: String,
) : PathElement(path),
    Field

class RankFeaturesField(
    path: String,
) : PathElement(path),
    Field

class FlattenedField(
    path: String,
) : PathElement(path),
    Field

class ShapeField(
    path: String,
) : PathElement(path),
    Field

class PointField(
    path: String,
) : PathElement(path),
    Field

class ConstantKeywordField(
    path: String,
) : PathElement(path),
    Field

class WildcardField(
    path: String,
) : PathElement(path),
    Field

// Range fields
class IntegerRangeField(
    path: String,
) : PathElement(path),
    Field

class FloatRangeField(
    path: String,
) : PathElement(path),
    Field

class LongRangeField(
    path: String,
) : PathElement(path),
    Field

class DoubleRangeField(
    path: String,
) : PathElement(path),
    Field

class DateRangeField(
    path: String,
) : PathElement(path),
    Field

class IpRangeField(
    path: String,
) : PathElement(path),
    Field

// Object and nested fields
abstract class ObjectField(
    internal val path: String = "",
) : FieldContainer(),
    Field {
    override fun path(): String = path
}

// class NestedField<T : ObjectFields>(
//    name: String,
// ) : Field
