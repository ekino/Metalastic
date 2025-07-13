package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class ObjectField(parent: ObjectField? = null, path: String = "", protected val nested: Boolean = false) : Field(parent, path) {
    @YellowColor
    fun nested(): Boolean = nested

    // Text field delegates
    inline fun <reified T> text() = FieldDelegate { parent, path -> TextField<T>(parent, path) }

    // Keyword field delegates
    inline fun <reified T> keyword() = FieldDelegate { parent, path -> KeywordField<T>(parent, path) }

    // Numeric field delegates
    inline fun <reified T> long() = FieldDelegate { parent, path -> LongField<T>(parent, path) }

    inline fun <reified T> integer() = FieldDelegate { parent, path -> IntegerField<T>(parent, path) }

    inline fun <reified T> short() = FieldDelegate { parent, path -> ShortField<T>(parent, path) }

    inline fun <reified T> byte() = FieldDelegate { parent, path -> ByteField<T>(parent, path) }

    inline fun <reified T> double() = FieldDelegate { parent, path -> DoubleField<T>(parent, path) }

    inline fun <reified T> float() = FieldDelegate { parent, path -> FloatField<T>(parent, path) }

    fun halfFloat() = FieldDelegate { parent, path -> HalfFloatField(parent, path) }

    fun scaledFloat() = FieldDelegate { parent, path -> ScaledFloatField(parent, path) }

    // Date field delegates
    inline fun <reified T> date() = FieldDelegate { parent, path -> DateField<T>(parent, path) }

    fun dateNanos() = FieldDelegate { parent, path -> DateNanosField(parent, path) }

    // Boolean field delegate
    inline fun <reified T> boolean() = FieldDelegate { parent, path -> BooleanField<T>(parent, path) }

    // Binary field delegate
    fun binary() = FieldDelegate { parent, path -> BinaryField(parent, path) }

    // Geo field delegates
    fun ip() = FieldDelegate { parent, path -> IpField(parent, path) }

    fun geoPoint() = FieldDelegate { parent, path -> GeoPointField(parent, path) }

    fun geoShape() = FieldDelegate { parent, path -> GeoShapeField(parent, path) }

    // Specialized field delegates
    fun completion() = FieldDelegate { parent, path -> CompletionField(parent, path) }

    fun tokenCount() = FieldDelegate { parent, path -> TokenCountField(parent, path) }

    fun percolator() = FieldDelegate { parent, path -> PercolatorField(parent, path) }

    fun rankFeature() = FieldDelegate { parent, path -> RankFeatureField(parent, path) }

    fun rankFeatures() = FieldDelegate { parent, path -> RankFeaturesField(parent, path) }

    fun flattened() = FieldDelegate { parent, path -> FlattenedField(parent, path) }

    fun shape() = FieldDelegate { parent, path -> ShapeField(parent, path) }

    fun point() = FieldDelegate { parent, path -> PointField(parent, path) }

    fun constantKeyword() = FieldDelegate { parent, path -> ConstantKeywordField(parent, path) }

    fun wildcard() = FieldDelegate { parent, path -> WildcardField(parent, path) }

    // Range field delegates
    fun integerRange() = FieldDelegate { parent, path -> IntegerRangeField(parent, path) }

    fun floatRange() = FieldDelegate { parent, path -> FloatRangeField(parent, path) }

    fun longRange() = FieldDelegate { parent, path -> LongRangeField(parent, path) }

    fun doubleRange() = FieldDelegate { parent, path -> DoubleRangeField(parent, path) }

    fun dateRange() = FieldDelegate { parent, path -> DateRangeField(parent, path) }

    fun ipRange() = FieldDelegate { parent, path -> IpRangeField(parent, path) }

    // Object field delegates
    @Suppress("MemberNameEqualsClassName")
    inline fun <reified T : ObjectField> objectField(nested: Boolean = false): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(T::class, nested)
        return delegate
    }

    inline fun <reified T : ObjectField> nestedField(): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(T::class, nested = true)
        return delegate
    }

    inline fun <reified T : MultiField<*>> multiField(): MultiFieldDelegate<T> {
        val delegate = MultiFieldDelegate(T::class)
        return delegate
    }
}

/**
 * Property delegate for field creation
 */
class FieldDelegate<T : Field>(private val fieldFactory: (parent: ObjectField, path: String) -> T) : ReadOnlyProperty<ObjectField, T> {
    private lateinit var field: T

    override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
        if (!this::field.isInitialized) {
            field = fieldFactory(thisRef, thisRef.appendPath(property))
        }
        return field
    }
}

/**
 * Special delegate for object fields that enables direct field traversal
 */
class ObjectFieldDelegate<T : ObjectField>(private val clazz: KClass<T>, private val nested: Boolean = false) : ReadOnlyProperty<ObjectField, T> {
    private lateinit var field: T

    override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
        if (!this::field.isInitialized) {
            field = clazz.primaryConstructor!!.call(thisRef, thisRef.appendPath(property), nested)
        }
        return field
    }
}

class MultiFieldDelegate<T : MultiField<*>>(private val clazz: KClass<T>) : ReadOnlyProperty<ObjectField, T> {
    private lateinit var field: T

    override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
        if (!this::field.isInitialized) {
            field = clazz.primaryConstructor!!.call(thisRef, thisRef.appendPath(property))
        }
        return field
    }
}

private fun ObjectField.appendPath(property: KProperty<*>): String = if (path().isEmpty()) {
    property.name
} else {
    "${path()}.${property.name}"
}
