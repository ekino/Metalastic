package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class FieldContainer {
    // Text field delegates
    protected inline fun <reified T> text() = FieldDelegate { path -> TextField<T>(path) }

    // Keyword field delegates
    protected inline fun <reified T> keyword() = FieldDelegate { path -> KeywordField<T>(path) }

    // Numeric field delegates
    protected inline fun <reified T> long() = FieldDelegate { path -> LongField<T>(path) }

    protected inline fun <reified T> integer() = FieldDelegate { path -> IntegerField<T>(path) }

    protected inline fun <reified T> short() = FieldDelegate { path -> ShortField<T>(path) }

    protected inline fun <reified T> byte() = FieldDelegate { path -> ByteField<T>(path) }

    protected inline fun <reified T> double() = FieldDelegate { path -> DoubleField<T>(path) }

    protected inline fun <reified T> float() = FieldDelegate { path -> FloatField<T>(path) }

    protected fun halfFloat() = FieldDelegate { path -> HalfFloatField(path) }

    protected fun scaledFloat() = FieldDelegate { path -> ScaledFloatField(path) }

    // Date field delegates
    protected inline fun <reified T> date() = FieldDelegate { path -> DateField<T>(path) }

    protected fun dateNanos() = FieldDelegate { path -> DateNanosField(path) }

    // Boolean field delegate
    protected inline fun <reified T> boolean() = FieldDelegate { path -> BooleanField<T>(path) }

    // Binary field delegate
    protected fun binary() = FieldDelegate { path -> BinaryField(path) }

    // Geo field delegates
    protected fun ip() = FieldDelegate { path -> IpField(path) }

    protected fun geoPoint() = FieldDelegate { path -> GeoPointField(path) }

    protected fun geoShape() = FieldDelegate { path -> GeoShapeField(path) }

    // Specialized field delegates
    protected fun completion() = FieldDelegate { path -> CompletionField(path) }

    protected fun tokenCount() = FieldDelegate { path -> TokenCountField(path) }

    protected fun percolator() = FieldDelegate { path -> PercolatorField(path) }

    protected fun rankFeature() = FieldDelegate { path -> RankFeatureField(path) }

    protected fun rankFeatures() = FieldDelegate { path -> RankFeaturesField(path) }

    protected fun flattened() = FieldDelegate { path -> FlattenedField(path) }

    protected fun shape() = FieldDelegate { path -> ShapeField(path) }

    protected fun point() = FieldDelegate { path -> PointField(path) }

    protected fun constantKeyword() = FieldDelegate { path -> ConstantKeywordField(path) }

    protected fun wildcard() = FieldDelegate { path -> WildcardField(path) }

    // Range field delegates
    protected fun integerRange() = FieldDelegate { path -> IntegerRangeField(path) }

    protected fun floatRange() = FieldDelegate { path -> FloatRangeField(path) }

    protected fun longRange() = FieldDelegate { path -> LongRangeField(path) }

    protected fun doubleRange() = FieldDelegate { path -> DoubleRangeField(path) }

    protected fun dateRange() = FieldDelegate { path -> DateRangeField(path) }

    protected fun ipRange() = FieldDelegate { path -> IpRangeField(path) }

    // Object field delegates
    protected inline fun <reified T : ObjectField> objectField(nested: Boolean = false): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(T::class, nested)
        return delegate
    }

    protected inline fun <reified T : ObjectField> nestedField(): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(T::class, nested = true)
        return delegate
    }

    protected inline fun <reified T : MultiField<*>> multiField(): MultiFieldDelegate<T> {
        val delegate = MultiFieldDelegate(T::class)
        return delegate
    }
}

/**
 * Property delegate for field creation
 */
class FieldDelegate<T : Field>(
    private val fieldFactory: (path: String) -> T,
) : ReadOnlyProperty<Any?, T> {
    private lateinit var field: T

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        if (!this::field.isInitialized) {
            field = fieldFactory((thisRef as Field).appendPath(property))
        }
        return field
    }
}

/**
 * Special delegate for object fields that enables direct field traversal
 */
class ObjectFieldDelegate<T : ObjectField>(
    private val clazz: KClass<T>,
    private val nested: Boolean = false,
) : ReadOnlyProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = clazz.primaryConstructor!!.call((thisRef as Field).appendPath(property))
}

class MultiFieldDelegate<T : MultiField<*>>(
    private val clazz: KClass<T>,
) : ReadOnlyProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = clazz.primaryConstructor!!.call((thisRef as Field).appendPath(property))
}

private fun Field.appendPath(property: KProperty<*>): String =
    if (path().isEmpty()) {
        property.name
    } else {
        "${path()}.${property.name}"
    }
