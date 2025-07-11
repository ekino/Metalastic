package com.qelasticsearch.dsl

/**
 * Sealed class for all field types in the DSL.
 * Provides path traversal functionality for nested field access.
 * Using sealed class ensures exhaustive pattern matching and type safety.
 */
open class Field(
    private val name: String,
    protected val parent: ObjectFields?,
) {
    open fun path(): String =
        parent
            ?.path()
            ?.takeIf { it.isNotEmpty() }
            ?.plus(".")
            .orEmpty() + name

    fun name() = name
}

// Text fields
open class TextField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

open class KeywordField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Numeric fields
class LongField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class IntegerField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class ShortField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class ByteField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class DoubleField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class FloatField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class HalfFloatField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class ScaledFloatField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Date fields
class DateField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class DateNanosField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Boolean field
class BooleanField<T>(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Binary field
class BinaryField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Geo fields
class IpField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class GeoPointField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class GeoShapeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Specialized fields
class CompletionField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class TokenCountField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class PercolatorField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class RankFeatureField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class RankFeaturesField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class FlattenedField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class ShapeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class PointField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class ConstantKeywordField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class WildcardField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Range fields
class IntegerRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class FloatRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class LongRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class DoubleRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class DateRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

class IpRangeField(
    name: String,
    parent: ObjectFields?,
) : Field(name, parent)

// Object and nested fields
class ObjectField<T : ObjectFields>(
    name: String,
    parent: ObjectFields?,
    val objectFields: T,
) : Field(name, parent)

class NestedField<T : ObjectFields>(
    name: String,
    parent: ObjectFields?,
    val objectFields: T,
) : Field(
        name,
        parent,
    )

// Multi-field support
class MultiField(
    parent: ObjectFields?,
    val mainField: Field,
    private val innerFields: Map<String, Field> = emptyMap(),
) : Field(mainField.name(), parent) {
    // Access to the main field itself
    fun main(): Field = mainField

    // Dynamic property access for inner fields with correct parent path
    operator fun get(suffix: String): Field? =
        innerFields[suffix]?.let { field ->
            // Create a new field with the correct parent path
            when (field) {
                is TextField<*> -> TextField<Any>(field.name(), parent)
                is KeywordField<*> -> KeywordField<Any>(field.name(), parent)
                is LongField<*> -> LongField<Any>(field.name(), parent)
                is IntegerField<*> -> IntegerField<Any>(field.name(), parent)
                is ShortField<*> -> ShortField<Any>(field.name(), parent)
                is ByteField<*> -> ByteField<Any>(field.name(), parent)
                is DoubleField<*> -> DoubleField<Any>(field.name(), parent)
                is FloatField<*> -> FloatField<Any>(field.name(), parent)
                is HalfFloatField -> HalfFloatField(field.name(), parent)
                is ScaledFloatField -> ScaledFloatField(field.name(), parent)
                is DateField<*> -> DateField<Any>(field.name(), parent)
                is DateNanosField -> DateNanosField(field.name(), parent)
                is BooleanField<*> -> BooleanField<Any>(field.name(), parent)
                is BinaryField -> BinaryField(field.name(), parent)
                is IpField -> IpField(field.name(), parent)
                is TokenCountField -> TokenCountField(field.name(), parent)
                is PercolatorField -> PercolatorField(field.name(), parent)
                is FlattenedField -> FlattenedField(field.name(), parent)
                is RankFeatureField -> RankFeatureField(field.name(), parent)
                is RankFeaturesField -> RankFeaturesField(field.name(), parent)
                is WildcardField -> WildcardField(field.name(), parent)
                is ConstantKeywordField -> ConstantKeywordField(field.name(), parent)
                is IntegerRangeField -> IntegerRangeField(field.name(), parent)
                is FloatRangeField -> FloatRangeField(field.name(), parent)
                is LongRangeField -> LongRangeField(field.name(), parent)
                is DoubleRangeField -> DoubleRangeField(field.name(), parent)
                is DateRangeField -> DateRangeField(field.name(), parent)
                is IpRangeField -> IpRangeField(field.name(), parent)
                // TODO(add other range fields)
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

    fun path(): String = multiField.path()

    // Dynamic property access - non-nullable, creates default fields if not defined
    val search: Field
        get() =
            multiField["search"] ?: object : TextField<String>("search", null) {
                override fun path(): String = "${multiField.path()}.search"
            }
    val keyword: Field
        get() =
            multiField["keyword"] ?: object : KeywordField<String>("keyword", null) {
                override fun path(): String = "${multiField.path()}.keyword"
            }
    val raw: Field
        get() =
            multiField["raw"] ?: object : KeywordField<String>("raw", null) {
                override fun path(): String = "${multiField.path()}.raw"
            }

    // Allow custom suffix access - nullable for dynamic access
    operator fun get(suffix: String): Field? = multiField[suffix]
}
