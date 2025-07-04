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
    open val path: String = if (parentPath.isEmpty()) name else "$parentPath.$name"
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
class MultiField(
    name: String,
    parentPath: String = "",
    val mainField: Field,
    val innerFields: Map<String, Field> = emptyMap()
) : Field(name, parentPath) {
    
    // Dynamic property access for inner fields with correct parent path
    operator fun get(suffix: String): Field? = innerFields[suffix]?.let { field ->
        // Create a new field with the correct parent path
        when (field) {
            is TextField -> TextField(field.name, this.path)
            is KeywordField -> KeywordField(field.name, this.path)
            is LongField -> LongField(field.name, this.path)
            is IntegerField -> IntegerField(field.name, this.path)
            is ShortField -> ShortField(field.name, this.path)
            is ByteField -> ByteField(field.name, this.path)
            is DoubleField -> DoubleField(field.name, this.path)
            is FloatField -> FloatField(field.name, this.path)
            is HalfFloatField -> HalfFloatField(field.name, this.path)
            is ScaledFloatField -> ScaledFloatField(field.name, this.path)
            is DateField -> DateField(field.name, this.path)
            is DateNanosField -> DateNanosField(field.name, this.path)
            is BooleanField -> BooleanField(field.name, this.path)
            is BinaryField -> BinaryField(field.name, this.path)
            is IpField -> IpField(field.name, this.path)
            is TokenCountField -> TokenCountField(field.name, this.path)
            is PercolatorField -> PercolatorField(field.name, this.path)
            is FlattenedField -> FlattenedField(field.name, this.path)
            is RankFeatureField -> RankFeatureField(field.name, this.path)
            is RankFeaturesField -> RankFeaturesField(field.name, this.path)
            is WildcardField -> WildcardField(field.name, this.path)
            is ConstantKeywordField -> ConstantKeywordField(field.name, this.path)
            is IntegerRangeField -> IntegerRangeField(field.name, this.path)
            is FloatRangeField -> FloatRangeField(field.name, this.path)
            is LongRangeField -> LongRangeField(field.name, this.path)
            is DoubleRangeField -> DoubleRangeField(field.name, this.path)
            is DateRangeField -> DateRangeField(field.name, this.path)
            is IpRangeField -> IpRangeField(field.name, this.path)
            else -> field // fallback for any other field types
        }
    }
}

// MultiField proxy that allows both Field access and dynamic inner field access
class MultiFieldProxy(
    private val multiField: MultiField
) : Field(multiField.name, multiField.parentPath) {
    
    // Delegate to main field for path access
    override val path: String = multiField.path
    
    // Dynamic property access - non-nullable, creates default fields if not defined
    val search: Field get() = multiField["search"] ?: TextField("search", multiField.path)
    val keyword: Field get() = multiField["keyword"] ?: KeywordField("keyword", multiField.path)
    val raw: Field get() = multiField["raw"] ?: KeywordField("raw", multiField.path)
    val suggest: Field get() = multiField["suggest"] ?: TextField("suggest", multiField.path)
    
    // Allow custom suffix access - nullable for dynamic access
    operator fun get(suffix: String): Field? = multiField[suffix]
}