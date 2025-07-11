package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class ObjectFields(
    name: String = "",
    parent: ObjectFields? = null,
) : Field(name, parent) {
    // Text field delegates
    protected inline fun <reified T> text() = FieldDelegate { name -> TextField<T>(name, this) }

    // Keyword field delegates
    protected inline fun <reified T> keyword() = FieldDelegate { name -> KeywordField<T>(name, this) }

    // Numeric field delegates
    protected inline fun <reified T> long() = FieldDelegate { name -> LongField<T>(name, this) }

    protected inline fun <reified T> integer() = FieldDelegate { name -> IntegerField<T>(name, this) }

    protected inline fun <reified T> short() = FieldDelegate { name -> ShortField<T>(name, this) }

    protected inline fun <reified T> byte() = FieldDelegate { name -> ByteField<T>(name, this) }

    protected inline fun <reified T> double() = FieldDelegate { name -> DoubleField<T>(name, this) }

    protected inline fun <reified T> float() = FieldDelegate { name -> FloatField<T>(name, this) }

    protected fun halfFloat() = FieldDelegate { name -> HalfFloatField(name, this) }

    protected fun scaledFloat() = FieldDelegate { name -> ScaledFloatField(name, this) }

    // Date field delegates
    protected inline fun <reified T> date() = FieldDelegate { name -> DateField<T>(name, this) }

    protected fun dateNanos() = FieldDelegate { name -> DateNanosField(name, this) }

    // Boolean field delegate
    protected inline fun <reified T> boolean() = FieldDelegate { name -> BooleanField<T>(name, this) }

    // Binary field delegate
    protected fun binary() = FieldDelegate { name -> BinaryField(name, this) }

    // Geo field delegates
    protected fun ip() = FieldDelegate { name -> IpField(name, this) }

    protected fun geoPoint() = FieldDelegate { name -> GeoPointField(name, this) }

    protected fun geoShape() = FieldDelegate { name -> GeoShapeField(name, this) }

    // Specialized field delegates
    protected fun completion() = FieldDelegate { name -> CompletionField(name, this) }

    protected fun tokenCount() = FieldDelegate { name -> TokenCountField(name, this) }

    protected fun percolator() = FieldDelegate { name -> PercolatorField(name, this) }

    protected fun rankFeature() = FieldDelegate { name -> RankFeatureField(name, this) }

    protected fun rankFeatures() = FieldDelegate { name -> RankFeaturesField(name, this) }

    protected fun flattened() = FieldDelegate { name -> FlattenedField(name, this) }

    protected fun shape() = FieldDelegate { name -> ShapeField(name, this) }

    protected fun point() = FieldDelegate { name -> PointField(name, this) }

    protected fun constantKeyword() = FieldDelegate { name -> ConstantKeywordField(name, this) }

    protected fun wildcard() = FieldDelegate { name -> WildcardField(name, this) }

    // Range field delegates
    protected fun integerRange() = FieldDelegate { name -> IntegerRangeField(name, this) }

    protected fun floatRange() = FieldDelegate { name -> FloatRangeField(name, this) }

    protected fun longRange() = FieldDelegate { name -> LongRangeField(name, this) }

    protected fun doubleRange() = FieldDelegate { name -> DoubleRangeField(name, this) }

    protected fun dateRange() = FieldDelegate { name -> DateRangeField(name, this) }

    protected fun ipRange() = FieldDelegate { name -> IpRangeField(name, this) }

    // Object field delegates
    protected fun <T : ObjectFields> objectField(
        clazz: KClass<T>,
        nested: Boolean = false,
    ): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(clazz, nested)
        return delegate
    }

    protected fun <T : ObjectFields> nestedField(clazz: KClass<T>): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(clazz, nested = true)
        return delegate
    }

    // Multi-field delegate
    protected fun <T : Field> multiField(
        mainField: T,
        configure: MultiFieldBuilder.() -> Unit = {},
    ) = FieldDelegate { name ->
        val builder = MultiFieldBuilder()
        builder.configure()
        MultiField(parent, mainField, builder.build())
    }

    // Multi-field proxy delegate that allows .search, .keyword access
    protected fun <T : Field> multiFieldProxy(
        mainField: T,
        configure: MultiFieldBuilder.() -> Unit = {},
    ) = MultiFieldProxyDelegate { name ->
        val builder = MultiFieldBuilder()
        builder.configure()
        // Create a new field with the proper name and parent path, not using the passed mainField's name
        val properMainField =
            when (mainField) {
                is TextField<*> -> TextField<Any>(name, parent)
                is KeywordField<*> -> KeywordField<Any>(name, parent)
                else -> throw IllegalArgumentException("Unsupported main field type for MultiField: ${mainField::class}")
            }
        val multiField = MultiField(parent, properMainField, builder.build())
        MultiFieldProxy(multiField)
    }
}

/**
 * Property delegate for field creation
 */
class FieldDelegate<T : Field>(
    private val fieldFactory: (String) -> T,
) : ReadOnlyProperty<Any?, T> {
    private var field: T? = null

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        if (field == null) {
            field = fieldFactory(property.name)
        }
        return field!!
    }
}

/**
 * Property delegate for MultiFieldProxy creation
 */
class MultiFieldProxyDelegate(
    private val proxyFactory: (String) -> MultiFieldProxy,
) : ReadOnlyProperty<Any?, MultiFieldProxy> {
    private var proxy: MultiFieldProxy? = null

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): MultiFieldProxy {
        if (proxy == null) {
            proxy = proxyFactory(property.name)
        }
        return proxy!!
    }
}

/**
 * Special delegate for object fields that enables direct field traversal
 * This allows person.address.city instead of person.address.objectFields.city
 */
class ObjectFieldDelegate<T : ObjectFields>(
    private val clazz: KClass<T>,
    private val nested: Boolean = false,
) : ReadOnlyProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = clazz.primaryConstructor!!.call(property.name, thisRef as ObjectFields)
}

/**
 * Builder for multi-field inner fields
 */
class MultiFieldBuilder {
    private val innerFields = mutableMapOf<String, Field>()

    fun field(
        suffix: String,
        fieldFactory: () -> Field,
    ) {
        innerFields[suffix] = fieldFactory()
    }

    fun field(
        suffix: String,
        field: Field,
    ) {
        innerFields[suffix] = field
    }

    internal fun build(): Map<String, Field> = innerFields.toMap()
}
