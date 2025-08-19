package com.qelasticsearch.dsl.delegation

import com.qelasticsearch.dsl.ObjectField
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

/** Special delegate for object fields that enables direct field traversal */
class ObjectFieldDelegate<T : ObjectField>(
  private val clazz: KClass<T>,
  private val nested: Boolean = false,
) : ReadOnlyProperty<ObjectField, T> {
  private lateinit var field: T

  override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
    if (!this::field.isInitialized) {
      field = clazz.primaryConstructor!!.call(thisRef, thisRef.appendPath(property), nested)
    }
    return field
  }
}
