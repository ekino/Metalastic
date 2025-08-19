package com.qelasticsearch.dsl

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

/** Property delegate for field creation */
class FieldDelegate<T : Field>(private val fieldFactory: (parent: ObjectField, path: String) -> T) :
  ReadOnlyProperty<ObjectField, T> {
  private lateinit var field: T

  override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
    if (!this::field.isInitialized) {
      field = fieldFactory(thisRef, thisRef.appendPath(property))
    }
    return field
  }
}

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

class MultiFieldDelegate<T : MultiField<*>>(private val clazz: KClass<T>) :
  ReadOnlyProperty<ObjectField, T> {
  private lateinit var field: T

  override fun getValue(thisRef: ObjectField, property: KProperty<*>): T {
    if (!this::field.isInitialized) {
      field = clazz.primaryConstructor!!.call(thisRef, thisRef.appendPath(property))
    }
    return field
  }
}

internal fun ObjectField.appendPath(property: KProperty<*>): String =
  if (path().isEmpty()) {
    property.name
  } else {
    "${path()}.${property.name}"
  }

// Object field delegates
inline fun <reified T : ObjectField> objectField(): ObjectFieldDelegate<T> {
  return ObjectFieldDelegate(T::class, nested = false)
}

inline fun <reified T : ObjectField> nestedField(): ObjectFieldDelegate<T> {
  return ObjectFieldDelegate(T::class, nested = true)
}

inline fun <reified T : MultiField<*>> multiField(): MultiFieldDelegate<T> {
  return MultiFieldDelegate(T::class)
}
