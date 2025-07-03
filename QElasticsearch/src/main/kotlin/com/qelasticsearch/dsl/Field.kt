package com.qelasticsearch.dsl

/**
 * Sealed class for all field types in the DSL.
 * Provides path traversal functionality for nested field access.
 * Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field(
    val name: String,
    val parentPath: String = ""
) {
    /**
     * The full dotted path of this field (e.g., "address.city")
     */
    val path: String = if (parentPath.isEmpty()) name else "$parentPath.$name"
}

// Text fields
class TextField(name: String, parentPath: String = "") : Field(name, parentPath)
class KeywordField(name: String, parentPath: String = "") : Field(name, parentPath)

// Numeric fields
class LongField(name: String, parentPath: String = "") : Field(name, parentPath)
class IntegerField(name: String, parentPath: String = "") : Field(name, parentPath)
class ShortField(name: String, parentPath: String = "") : Field(name, parentPath)
class ByteField(name: String, parentPath: String = "") : Field(name, parentPath)
class DoubleField(name: String, parentPath: String = "") : Field(name, parentPath)
class FloatField(name: String, parentPath: String = "") : Field(name, parentPath)
class HalfFloatField(name: String, parentPath: String = "") : Field(name, parentPath)
class ScaledFloatField(name: String, parentPath: String = "") : Field(name, parentPath)

// Date fields
class DateField(name: String, parentPath: String = "") : Field(name, parentPath)
class DateNanosField(name: String, parentPath: String = "") : Field(name, parentPath)

// Boolean field
class BooleanField(name: String, parentPath: String = "") : Field(name, parentPath)

// Binary field
class BinaryField(name: String, parentPath: String = "") : Field(name, parentPath)

// Geo fields
class IpField(name: String, parentPath: String = "") : Field(name, parentPath)
class GeoPointField(name: String, parentPath: String = "") : Field(name, parentPath)
class GeoShapeField(name: String, parentPath: String = "") : Field(name, parentPath)

// Specialized fields
class CompletionField(name: String, parentPath: String = "") : Field(name, parentPath)
class TokenCountField(name: String, parentPath: String = "") : Field(name, parentPath)
class PercolatorField(name: String, parentPath: String = "") : Field(name, parentPath)
class RankFeatureField(name: String, parentPath: String = "") : Field(name, parentPath)
class RankFeaturesField(name: String, parentPath: String = "") : Field(name, parentPath)
class FlattenedField(name: String, parentPath: String = "") : Field(name, parentPath)
class ShapeField(name: String, parentPath: String = "") : Field(name, parentPath)
class PointField(name: String, parentPath: String = "") : Field(name, parentPath)
class ConstantKeywordField(name: String, parentPath: String = "") : Field(name, parentPath)
class WildcardField(name: String, parentPath: String = "") : Field(name, parentPath)

// Range fields
class IntegerRangeField(name: String, parentPath: String = "") : Field(name, parentPath)
class FloatRangeField(name: String, parentPath: String = "") : Field(name, parentPath)
class LongRangeField(name: String, parentPath: String = "") : Field(name, parentPath)
class DoubleRangeField(name: String, parentPath: String = "") : Field(name, parentPath)
class DateRangeField(name: String, parentPath: String = "") : Field(name, parentPath)
class IpRangeField(name: String, parentPath: String = "") : Field(name, parentPath)

// Object and nested fields
class ObjectField<T : ObjectFields>(
    name: String,
    parentPath: String = "",
    val objectFields: T
) : Field(name, parentPath) {
    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path)
    }
}

class NestedField<T : ObjectFields>(
    name: String,
    parentPath: String = "",
    val objectFields: T
) : Field(name, parentPath) {
    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path)
    }
}

// Multi-field support
class MultiField<T : Field>(
    name: String,
    parentPath: String = "",
    val mainField: T,
    val innerFields: Map<String, Field> = emptyMap()
) : Field(name, parentPath)