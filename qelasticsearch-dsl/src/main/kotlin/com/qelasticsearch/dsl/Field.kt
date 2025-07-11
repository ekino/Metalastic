package com.qelasticsearch.dsl

/**
 * Sealed class for all field types in the DSL.
 * Provides path traversal functionality for nested field access.
 * Using sealed class ensures exhaustive pattern matching and type safety.
 */
sealed class Field(
    private val name: String,
    parentPath: FieldPath = FieldPath(""),
) {
    private val path = parentPath.child(name)

    open fun path() = path

    fun name() = name
}

// Text fields
class TextField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class KeywordField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Numeric fields
class LongField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class IntegerField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class ShortField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class ByteField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class DoubleField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class FloatField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class HalfFloatField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class ScaledFloatField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Date fields
class DateField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class DateNanosField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Boolean field
class BooleanField<T>(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Binary field
class BinaryField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Geo fields
class IpField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class GeoPointField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class GeoShapeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Specialized fields
class CompletionField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class TokenCountField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class PercolatorField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class RankFeatureField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class RankFeaturesField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class FlattenedField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class ShapeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class PointField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class ConstantKeywordField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class WildcardField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Range fields
class IntegerRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class FloatRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class LongRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class DoubleRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class DateRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

class IpRangeField(
    name: String,
    path: FieldPath = FieldPath(""),
) : Field(name, path)

// Object and nested fields
class ObjectField<T : ObjectFields>(
    name: String,
    path: FieldPath = FieldPath(""),
    val objectFields: T,
) : Field(name, path) {
    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path)
    }
}

class NestedField<T : ObjectFields>(
    name: String,
    path: FieldPath = FieldPath(""),
    val objectFields: T,
) : Field(
        name,
        if (path.isRoot) {
            FieldPath.nested(name)
        } else {
            FieldPath("$path.$name", path.nestedSegments + "$path.$name")
        },
    ) {
    init {
        // Update the parent path for all nested fields
        objectFields.updateParentPath(path)
    }
}

// Multi-field support
class MultiField(
    parentPath: FieldPath = FieldPath(""),
    val mainField: Field,
    private val innerFields: Map<String, Field> = emptyMap(),
) : Field(mainField.name(), parentPath) {
    // Access to the main field itself
    fun main(): Field = mainField

    // Dynamic property access for inner fields with correct parent path
    operator fun get(suffix: String): Field? =
        innerFields[suffix]?.let { field ->
            // Create a new field with the correct parent path
            when (field) {
                is TextField<*> -> TextField<Any>(field.name(), path())
                is KeywordField<*> -> KeywordField<Any>(field.name(), path())
                is LongField<*> -> LongField<Any>(field.name(), path())
                is IntegerField<*> -> IntegerField<Any>(field.name(), path())
                is ShortField<*> -> ShortField<Any>(field.name(), path())
                is ByteField<*> -> ByteField<Any>(field.name(), path())
                is DoubleField<*> -> DoubleField<Any>(field.name(), path())
                is FloatField<*> -> FloatField<Any>(field.name(), path())
                is HalfFloatField -> HalfFloatField(field.name(), path())
                is ScaledFloatField -> ScaledFloatField(field.name(), path())
                is DateField<*> -> DateField<Any>(field.name(), path())
                is DateNanosField -> DateNanosField(field.name(), path())
                is BooleanField<*> -> BooleanField<Any>(field.name(), path())
                is BinaryField -> BinaryField(field.name(), path())
                is IpField -> IpField(field.name(), path())
                is TokenCountField -> TokenCountField(field.name(), path())
                is PercolatorField -> PercolatorField(field.name(), path())
                is FlattenedField -> FlattenedField(field.name(), path())
                is RankFeatureField -> RankFeatureField(field.name(), path())
                is RankFeaturesField -> RankFeaturesField(field.name(), path())
                is WildcardField -> WildcardField(field.name(), path())
                is ConstantKeywordField -> ConstantKeywordField(field.name(), path())
                is IntegerRangeField -> IntegerRangeField(field.name(), path())
                is FloatRangeField -> FloatRangeField(field.name(), path())
                is LongRangeField -> LongRangeField(field.name(), path())
                is DoubleRangeField -> DoubleRangeField(field.name(), path())
                is DateRangeField -> DateRangeField(field.name(), path())
                is IpRangeField -> IpRangeField(field.name(), path())
                else -> field // fallback for any other field types
            }
        }
}

// MultiField proxy that allows both Field access and dynamic inner field access
class MultiFieldProxy(
    private val multiField: MultiField,
) {
    // Access to the main field and its path
    fun main(): Field = multiField.main()

    fun path(): FieldPath = multiField.path()

    // Dynamic property access - non-nullable, creates default fields if not defined
    val search: Field
        get() = multiField["search"] ?: TextField<String>("search", multiField.path())
    val keyword: Field
        get() =
            multiField["keyword"] ?: KeywordField<String>(
                "keyword",
                multiField.path(),
            )
    val raw: Field
        get() =
            multiField["raw"] ?: KeywordField<String>(
                "raw",
                multiField.path(),
            )

    // Allow custom suffix access - nullable for dynamic access
    operator fun get(suffix: String): Field? = multiField[suffix]
}
