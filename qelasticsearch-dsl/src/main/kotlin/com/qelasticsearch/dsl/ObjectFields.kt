package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Base class for object fields that can contain nested fields.
 * Used for both nested objects and the root index.
 */
abstract class ObjectFields {
    private var parentPath: String = ""
    
    /**
     * Updates the parent path for all fields in this object.
     * This is used when this object is nested within another object.
     */
    internal fun updateParentPath(newParentPath: String) {
        parentPath = newParentPath
        // Update parent path for any object field delegates
        updateObjectFieldDelegates()
    }
    
    private fun updateObjectFieldDelegates() {
        // Use reflection to find and update ObjectFieldDelegate instances
        this::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            val delegate = field.get(this)
            if (delegate is ObjectFieldDelegate<*>) {
                delegate.setParentPath(parentPath)
            }
        }
    }
    
    // Text field delegates
    protected fun text() = FieldDelegate { name -> TextField(name, parentPath) }
    
    // Keyword field delegates
    protected fun keyword() = FieldDelegate { name -> KeywordField(name, parentPath) }
    
    // Numeric field delegates
    protected fun long() = FieldDelegate { name -> LongField(name, parentPath) }
    protected fun integer() = FieldDelegate { name -> IntegerField(name, parentPath) }
    protected fun short() = FieldDelegate { name -> ShortField(name, parentPath) }
    protected fun byte() = FieldDelegate { name -> ByteField(name, parentPath) }
    protected fun double() = FieldDelegate { name -> DoubleField(name, parentPath) }
    protected fun float() = FieldDelegate { name -> FloatField(name, parentPath) }
    protected fun halfFloat() = FieldDelegate { name -> HalfFloatField(name, parentPath) }
    protected fun scaledFloat() = FieldDelegate { name -> ScaledFloatField(name, parentPath) }
    
    // Date field delegates
    protected fun date() = FieldDelegate { name -> DateField(name, parentPath) }
    protected fun dateNanos() = FieldDelegate { name -> DateNanosField(name, parentPath) }
    
    // Boolean field delegate
    protected fun boolean() = FieldDelegate { name -> BooleanField(name, parentPath) }
    
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
    protected fun <T : ObjectFields> objectField(objectFields: T, nested: Boolean = false) = 
        ObjectFieldDelegate(objectFields, nested)
    
    protected fun <T : ObjectFields> nestedField(objectFields: T) = 
        ObjectFieldDelegate(objectFields, nested = true)
    
    // Multi-field delegate
    protected fun <T : Field> multiField(mainField: T, configure: MultiFieldBuilder.() -> Unit = {}) = 
        FieldDelegate { name ->
            val builder = MultiFieldBuilder()
            builder.configure()
            MultiField(name, parentPath, mainField, builder.build())
        }
    
    // Multi-field proxy delegate that allows .search, .keyword access
    protected fun <T : Field> multiFieldProxy(mainField: T, configure: MultiFieldBuilder.() -> Unit = {}) = 
        MultiFieldProxyDelegate { name ->
            val builder = MultiFieldBuilder()
            builder.configure()
            val multiField = MultiField(name, parentPath, mainField, builder.build())
            MultiFieldProxy(multiField)
        }
}

/**
 * Property delegate for field creation
 */
class FieldDelegate<T : Field>(private val fieldFactory: (String) -> T) : ReadOnlyProperty<Any?, T> {
    private var field: T? = null
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (field == null) {
            field = fieldFactory(property.name)
            // If this is an ObjectField, we need to initialize its nested fields with the correct parent path
            if (field is ObjectField<*>) {
                (field as ObjectField<*>).objectFields.updateParentPath(field!!.path)
            }
        }
        return field!!
    }
}

/**
 * Property delegate for MultiFieldProxy creation
 */
class MultiFieldProxyDelegate(private val proxyFactory: (String) -> MultiFieldProxy) : ReadOnlyProperty<Any?, MultiFieldProxy> {
    private var proxy: MultiFieldProxy? = null
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): MultiFieldProxy {
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
    private val nested: Boolean = false
) : ReadOnlyProperty<Any?, T> {
    private var parentPath: String = ""
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val currentPath = if (parentPath.isEmpty()) property.name else "$parentPath.${property.name}"
        objectFields.updateParentPath(currentPath)
        return objectFields
    }
    
    fun setParentPath(path: String) {
        parentPath = path
    }
}

/**
 * Builder for multi-field inner fields
 */
class MultiFieldBuilder {
    private val innerFields = mutableMapOf<String, Field>()
    
    fun field(suffix: String, fieldFactory: () -> Field) {
        innerFields[suffix] = fieldFactory()
    }
    
    fun field(suffix: String, field: Field) {
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