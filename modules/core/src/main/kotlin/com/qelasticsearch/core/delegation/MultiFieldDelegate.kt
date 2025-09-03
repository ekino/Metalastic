package com.qelasticsearch.core.delegation

import com.qelasticsearch.core.MultiField
import com.qelasticsearch.core.ObjectField
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

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
