package com.qelasticsearch.core.delegation

import com.qelasticsearch.core.Field
import com.qelasticsearch.core.ObjectField
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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
