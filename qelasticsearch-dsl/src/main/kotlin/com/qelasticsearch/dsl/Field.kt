package com.qelasticsearch.dsl

/**
 * Sealed class for all field types in the DSL.
 * Provides path traversal functionality for nested field access.
 * Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field(
    val name: String,
    val parentPath: String = "",
    /**
     * Information about parent nested segments
     */
    val parentNestedSegments: List<String> = emptyList(),
) {
    /**
     * The enhanced path with nested information
     */
    open val fieldPath: FieldPath =
        if (parentPath.isEmpty()) {
            FieldPath.simple(name)
        } else {
            FieldPath("$parentPath.$name", parentNestedSegments)
        }

    /**
     * The full dotted path of this field (e.g., "address.city")
     * Provided for backward compatibility
     */
    open val path: String = fieldPath.path
}

// Text fields
class TextField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class KeywordField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Numeric fields
class LongField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class IntegerField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class ShortField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class ByteField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class DoubleField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class FloatField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class HalfFloatField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class ScaledFloatField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Date fields
class DateField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class DateNanosField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Boolean field
class BooleanField<T>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Binary field
class BinaryField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Geo fields
class IpField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class GeoPointField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class GeoShapeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Specialized fields
class CompletionField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class TokenCountField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class PercolatorField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class RankFeatureField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class RankFeaturesField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class FlattenedField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class ShapeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class PointField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class ConstantKeywordField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class WildcardField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Range fields
class IntegerRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class FloatRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class LongRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class DoubleRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class DateRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

class IpRangeField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
) : Field(name, parentPath, parentNestedSegments)

// Object and nested fields
class ObjectField<T : ObjectFields>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
    val objectFields: T,
) : Field(name, parentPath, parentNestedSegments) {
    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path, parentNestedSegments)
    }
}

class NestedField<T : ObjectFields>(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
    val objectFields: T,
) : Field(name, parentPath, parentNestedSegments) {
    // For nested fields, add the current path as a nested segment
    override val fieldPath: FieldPath =
        if (parentPath.isEmpty()) {
            FieldPath.nested(name)
        } else {
            FieldPath("$parentPath.$name", parentNestedSegments + "$parentPath.$name")
        }

    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path, fieldPath.nestedSegments)
    }
}

// Multi-field support
class MultiField(
    name: String,
    parentPath: String = "",
    parentNestedSegments: List<String> = emptyList(),
    val mainField: Field,
    private val innerFields: Map<String, Field> = emptyMap(),
) : Field(name, parentPath, parentNestedSegments) {
    // Access to the main field itself
    fun main(): Field = mainField

    // Dynamic property access for inner fields with correct parent path
    operator fun get(suffix: String): Field? =
        innerFields[suffix]?.let { field ->
            // Create a new field with the correct parent path
            when (field) {
                is TextField<*> -> TextField<Any>(field.name, this.path, this.parentNestedSegments)
                is KeywordField<*> -> KeywordField<Any>(field.name, this.path, this.parentNestedSegments)
                is LongField<*> -> LongField<Any>(field.name, this.path, this.parentNestedSegments)
                is IntegerField<*> -> IntegerField<Any>(field.name, this.path, this.parentNestedSegments)
                is ShortField<*> -> ShortField<Any>(field.name, this.path, this.parentNestedSegments)
                is ByteField<*> -> ByteField<Any>(field.name, this.path, this.parentNestedSegments)
                is DoubleField<*> -> DoubleField<Any>(field.name, this.path, this.parentNestedSegments)
                is FloatField<*> -> FloatField<Any>(field.name, this.path, this.parentNestedSegments)
                is HalfFloatField -> HalfFloatField(field.name, this.path, this.parentNestedSegments)
                is ScaledFloatField -> ScaledFloatField(field.name, this.path, this.parentNestedSegments)
                is DateField<*> -> DateField<Any>(field.name, this.path, this.parentNestedSegments)
                is DateNanosField -> DateNanosField(field.name, this.path, this.parentNestedSegments)
                is BooleanField<*> -> BooleanField<Any>(field.name, this.path, this.parentNestedSegments)
                is BinaryField -> BinaryField(field.name, this.path, this.parentNestedSegments)
                is IpField -> IpField(field.name, this.path, this.parentNestedSegments)
                is TokenCountField -> TokenCountField(field.name, this.path, this.parentNestedSegments)
                is PercolatorField -> PercolatorField(field.name, this.path, this.parentNestedSegments)
                is FlattenedField -> FlattenedField(field.name, this.path, this.parentNestedSegments)
                is RankFeatureField -> RankFeatureField(field.name, this.path, this.parentNestedSegments)
                is RankFeaturesField -> RankFeaturesField(field.name, this.path, this.parentNestedSegments)
                is WildcardField -> WildcardField(field.name, this.path, this.parentNestedSegments)
                is ConstantKeywordField -> ConstantKeywordField(field.name, this.path, this.parentNestedSegments)
                is IntegerRangeField -> IntegerRangeField(field.name, this.path, this.parentNestedSegments)
                is FloatRangeField -> FloatRangeField(field.name, this.path, this.parentNestedSegments)
                is LongRangeField -> LongRangeField(field.name, this.path, this.parentNestedSegments)
                is DoubleRangeField -> DoubleRangeField(field.name, this.path, this.parentNestedSegments)
                is DateRangeField -> DateRangeField(field.name, this.path, this.parentNestedSegments)
                is IpRangeField -> IpRangeField(field.name, this.path, this.parentNestedSegments)
                else -> field // fallback for any other field types
            }
        }
}

// MultiField proxy that allows both Field access and dynamic inner field access
class MultiFieldProxy(
    private val multiField: MultiField,
) : Field(multiField.name, multiField.parentPath, multiField.parentNestedSegments) {
    // Delegate to main field for path access
    override val path: String = multiField.path

    // Access to the main field
    fun main(): Field = multiField.main()

    // Dynamic property access - non-nullable, creates default fields if not defined
    val search: Field get() = multiField["search"] ?: TextField<String>("search", multiField.path, multiField.parentNestedSegments)
    val keyword: Field get() = multiField["keyword"] ?: KeywordField<String>("keyword", multiField.path, multiField.parentNestedSegments)
    val raw: Field get() = multiField["raw"] ?: KeywordField<String>("raw", multiField.path, multiField.parentNestedSegments)

    // Allow custom suffix access - nullable for dynamic access
    operator fun get(suffix: String): Field? = multiField[suffix]
}
