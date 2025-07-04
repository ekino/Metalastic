package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class ObjectFields {
    var parentPath: String = ""
    var parentNestedSegments: List<String> = emptyList()

    /**
     * Updates the parent path for all fields in this object.
     * This is used when this object is nested within another object.
     */
    internal fun updateParentPath(
        newParentPath: String,
        nestedSegments: List<String> = emptyList(),
    ) {
        parentPath = newParentPath
        parentNestedSegments = nestedSegments
        // Update parent path for any object field delegates
        updateObjectFieldDelegates()
    }

    private fun updateObjectFieldDelegates() {
        // Use reflection to find and update ObjectFieldDelegate instances
        this::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            val delegate = field.get(this)
            if (delegate is ObjectFieldDelegate<*>) {
                delegate.setParentPath(parentPath, parentNestedSegments)
            }
        }
    }

    // Text field delegates
    protected inline fun <reified T> text() = FieldDelegate { name -> TextField<T>(name, parentPath, parentNestedSegments) }

    // Keyword field delegates
    protected inline fun <reified T> keyword() = FieldDelegate { name -> KeywordField<T>(name, parentPath, parentNestedSegments) }

    // Numeric field delegates
    protected inline fun <reified T> long() = FieldDelegate { name -> LongField<T>(name, parentPath, parentNestedSegments) }

    protected inline fun <reified T> integer() = FieldDelegate { name -> IntegerField<T>(name, parentPath, parentNestedSegments) }

    protected inline fun <reified T> short() = FieldDelegate { name -> ShortField<T>(name, parentPath, parentNestedSegments) }

    protected inline fun <reified T> byte() = FieldDelegate { name -> ByteField<T>(name, parentPath, parentNestedSegments) }

    protected inline fun <reified T> double() = FieldDelegate { name -> DoubleField<T>(name, parentPath, parentNestedSegments) }

    protected inline fun <reified T> float() = FieldDelegate { name -> FloatField<T>(name, parentPath, parentNestedSegments) }

    protected fun halfFloat() = FieldDelegate { name -> HalfFloatField(name, parentPath, parentNestedSegments) }

    protected fun scaledFloat() = FieldDelegate { name -> ScaledFloatField(name, parentPath, parentNestedSegments) }

    // Date field delegates
    protected inline fun <reified T> date() = FieldDelegate { name -> DateField<T>(name, parentPath, parentNestedSegments) }

    protected fun dateNanos() = FieldDelegate { name -> DateNanosField(name, parentPath, parentNestedSegments) }

    // Boolean field delegate
    protected inline fun <reified T> boolean() = FieldDelegate { name -> BooleanField<T>(name, parentPath, parentNestedSegments) }

    // Binary field delegate
    protected fun binary() = FieldDelegate { name -> BinaryField(name, parentPath, parentNestedSegments) }

    // Geo field delegates
    protected fun ip() = FieldDelegate { name -> IpField(name, parentPath, parentNestedSegments) }

    protected fun geoPoint() = FieldDelegate { name -> GeoPointField(name, parentPath, parentNestedSegments) }

    protected fun geoShape() = FieldDelegate { name -> GeoShapeField(name, parentPath, parentNestedSegments) }

    // Specialized field delegates
    protected fun completion() = FieldDelegate { name -> CompletionField(name, parentPath, parentNestedSegments) }

    protected fun tokenCount() = FieldDelegate { name -> TokenCountField(name, parentPath, parentNestedSegments) }

    protected fun percolator() = FieldDelegate { name -> PercolatorField(name, parentPath, parentNestedSegments) }

    protected fun rankFeature() = FieldDelegate { name -> RankFeatureField(name, parentPath, parentNestedSegments) }

    protected fun rankFeatures() = FieldDelegate { name -> RankFeaturesField(name, parentPath, parentNestedSegments) }

    protected fun flattened() = FieldDelegate { name -> FlattenedField(name, parentPath, parentNestedSegments) }

    protected fun shape() = FieldDelegate { name -> ShapeField(name, parentPath, parentNestedSegments) }

    protected fun point() = FieldDelegate { name -> PointField(name, parentPath, parentNestedSegments) }

    protected fun constantKeyword() = FieldDelegate { name -> ConstantKeywordField(name, parentPath, parentNestedSegments) }

    protected fun wildcard() = FieldDelegate { name -> WildcardField(name, parentPath, parentNestedSegments) }

    // Range field delegates
    protected fun integerRange() = FieldDelegate { name -> IntegerRangeField(name, parentPath, parentNestedSegments) }

    protected fun floatRange() = FieldDelegate { name -> FloatRangeField(name, parentPath, parentNestedSegments) }

    protected fun longRange() = FieldDelegate { name -> LongRangeField(name, parentPath, parentNestedSegments) }

    protected fun doubleRange() = FieldDelegate { name -> DoubleRangeField(name, parentPath, parentNestedSegments) }

    protected fun dateRange() = FieldDelegate { name -> DateRangeField(name, parentPath, parentNestedSegments) }

    protected fun ipRange() = FieldDelegate { name -> IpRangeField(name, parentPath, parentNestedSegments) }

    // Object field delegates
    protected fun <T : ObjectFields> objectField(
        objectFields: T,
        nested: Boolean = false,
    ) = ObjectFieldDelegate(objectFields, nested)

    protected fun <T : ObjectFields> nestedField(objectFields: T) = ObjectFieldDelegate(objectFields, nested = true)

    // Multi-field delegate
    protected fun <T : Field> multiField(
        mainField: T,
        configure: MultiFieldBuilder.() -> Unit = {},
    ) = FieldDelegate { name ->
        val builder = MultiFieldBuilder()
        builder.configure()
        MultiField(name, parentPath, parentNestedSegments, mainField, builder.build())
    }

    // Multi-field proxy delegate that allows .search, .keyword access
    protected fun <T : Field> multiFieldProxy(
        mainField: T,
        configure: MultiFieldBuilder.() -> Unit = {},
    ) = MultiFieldProxyDelegate { name ->
        val builder = MultiFieldBuilder()
        builder.configure()
        val multiField = MultiField(name, parentPath, parentNestedSegments, mainField, builder.build())
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
            // If this is an ObjectField or NestedField, we need to initialize its nested fields with the correct parent path
            val currentField = field!!
            when (currentField) {
                is ObjectField<*> -> {
                    currentField.objectFields.updateParentPath(currentField.path)
                }
                is NestedField<*> -> {
                    currentField.objectFields.updateParentPath(currentField.path, currentField.fieldPath.nestedSegments)
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
    var parentPath: String = ""
    var parentNestedSegments: List<String> = emptyList()

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        val currentPath = if (parentPath.isEmpty()) property.name else "$parentPath.${property.name}"
        val currentNestedSegments =
            if (nested) {
                parentNestedSegments + currentPath
            } else {
                parentNestedSegments
            }
        objectFields.updateParentPath(currentPath, currentNestedSegments)
        return objectFields
    }

    fun setParentPath(
        path: String,
        nestedSegments: List<String> = emptyList(),
    ) {
        parentPath = path
        parentNestedSegments = nestedSegments
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

/**
 * Generic placeholder for object fields where the referenced class
 * doesn't have @Field annotations and cannot generate a specific Q-class.
 * This maintains the semantic meaning that this is an object field.
 */
object UnknownObjectFields : ObjectFields()

/**
 * Generic placeholder for nested fields where the referenced class
 * doesn't have @Field annotations and cannot generate a specific Q-class.
 * This maintains the semantic meaning that this is a nested field.
 */
object UnknownNestedFields : ObjectFields()
