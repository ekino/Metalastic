package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class ObjectFields {
    var parentPath: FieldPath = FieldPath("")
    private val childDelegates = mutableListOf<ObjectFieldDelegate<*>>()

    /**
     * Updates the parent path for all fields in this object.
     * This is used when this object is nested within another object.
     */
    internal fun updateParentPath(newParentPath: FieldPath) {
        parentPath = newParentPath
        // Update parent path for any registered object field delegates
        childDelegates.forEach { delegate ->
            delegate.path = parentPath
        }
    }

    private fun registerObjectDelegate(delegate: ObjectFieldDelegate<*>) {
        childDelegates.add(delegate)
    }

    // Text field delegates
    protected inline fun <reified T> text() = FieldDelegate { name -> TextField<T>(name, parentPath) }

    // Keyword field delegates
    protected inline fun <reified T> keyword() = FieldDelegate { name -> KeywordField<T>(name, parentPath) }

    // Numeric field delegates
    protected inline fun <reified T> long() = FieldDelegate { name -> LongField<T>(name, parentPath) }

    protected inline fun <reified T> integer() = FieldDelegate { name -> IntegerField<T>(name, parentPath) }

    protected inline fun <reified T> short() = FieldDelegate { name -> ShortField<T>(name, parentPath) }

    protected inline fun <reified T> byte() = FieldDelegate { name -> ByteField<T>(name, parentPath) }

    protected inline fun <reified T> double() = FieldDelegate { name -> DoubleField<T>(name, parentPath) }

    protected inline fun <reified T> float() = FieldDelegate { name -> FloatField<T>(name, parentPath) }

    protected fun halfFloat() = FieldDelegate { name -> HalfFloatField(name, parentPath) }

    protected fun scaledFloat() = FieldDelegate { name -> ScaledFloatField(name, parentPath) }

    // Date field delegates
    protected inline fun <reified T> date() = FieldDelegate { name -> DateField<T>(name, parentPath) }

    protected fun dateNanos() = FieldDelegate { name -> DateNanosField(name, parentPath) }

    // Boolean field delegate
    protected inline fun <reified T> boolean() = FieldDelegate { name -> BooleanField<T>(name, parentPath) }

    // Binary field delegate
    protected fun binary() = FieldDelegate { name -> BinaryField(name, parentPath) }

    // Geo field delegates
    protected fun ip() = FieldDelegate { name -> IpField(name, parentPath) }

    protected fun geoPoint() = FieldDelegate { name -> GeoPointField(name, parentPath) }

    protected fun geoShape() = FieldDelegate { name -> GeoShapeField(name, parentPath) }

    // Specialized field delegates
    protected fun completion() = FieldDelegate { name -> CompletionField(name, parentPath) }

    protected fun tokenCount() = FieldDelegate { name -> TokenCountField(name, parentPath) }

    protected fun percolator() = FieldDelegate { name -> PercolatorField(name, parentPath) }

    protected fun rankFeature() = FieldDelegate { name -> RankFeatureField(name, parentPath) }

    protected fun rankFeatures() = FieldDelegate { name -> RankFeaturesField(name, parentPath) }

    protected fun flattened() = FieldDelegate { name -> FlattenedField(name, parentPath) }

    protected fun shape() = FieldDelegate { name -> ShapeField(name, parentPath) }

    protected fun point() = FieldDelegate { name -> PointField(name, parentPath) }

    protected fun constantKeyword() = FieldDelegate { name -> ConstantKeywordField(name, parentPath) }

    protected fun wildcard() = FieldDelegate { name -> WildcardField(name, parentPath) }

    // Range field delegates
    protected fun integerRange() = FieldDelegate { name -> IntegerRangeField(name, parentPath) }

    protected fun floatRange() = FieldDelegate { name -> FloatRangeField(name, parentPath) }

    protected fun longRange() = FieldDelegate { name -> LongRangeField(name, parentPath) }

    protected fun doubleRange() = FieldDelegate { name -> DoubleRangeField(name, parentPath) }

    protected fun dateRange() = FieldDelegate { name -> DateRangeField(name, parentPath) }

    protected fun ipRange() = FieldDelegate { name -> IpRangeField(name, parentPath) }

    // Object field delegates
    protected fun <T : ObjectFields> objectField(
        objectFields: T,
        nested: Boolean = false,
    ): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(objectFields, nested)
        registerObjectDelegate(delegate)
        return delegate
    }

    protected fun <T : ObjectFields> nestedField(objectFields: T): ObjectFieldDelegate<T> {
        val delegate = ObjectFieldDelegate(objectFields, nested = true)
        registerObjectDelegate(delegate)
        return delegate
    }

    // Multi-field delegate
    protected fun <T : Field> multiField(
        mainField: T,
        configure: MultiFieldBuilder.() -> Unit = {},
    ) = FieldDelegate { name ->
        val builder = MultiFieldBuilder()
        builder.configure()
        MultiField(parentPath, mainField, builder.build())
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
                is TextField<*> -> TextField<Any>(name, parentPath)
                is KeywordField<*> -> KeywordField<Any>(name, parentPath)
                else -> throw IllegalArgumentException("Unsupported main field type for MultiField: ${mainField::class}")
            }
        val multiField = MultiField(parentPath, properMainField, builder.build())
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
            // If this is an ObjectField or NestedField,
            // we need to initialize its nested fields with the correct parent path
            val currentField = field!!
            when (currentField) {
                is ObjectField<*> -> {
                    currentField.objectFields.updateParentPath(currentField.path())
                }

                is NestedField<*> -> {
                    currentField.objectFields.updateParentPath(currentField.path())
                }

                else -> {
                    // No special handling needed for other field types
                }
            }
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
    private val objectFields: T,
    private val nested: Boolean = false,
) : ReadOnlyProperty<Any?, T> {
    var path: FieldPath = FieldPath("")

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        val currentPath =
            if (path.isRoot) property.name else "${path.path}.${property.name}"
        val newPath =
            FieldPath(
                currentPath,
                if (nested) {
                    path.nestedSegments + currentPath
                } else {
                    path.nestedSegments
                },
            )
        objectFields.updateParentPath(newPath)
        return objectFields
    }
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
