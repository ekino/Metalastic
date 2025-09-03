package com.qelasticsearch.core.delegation

import com.qelasticsearch.core.DynamicField
import com.qelasticsearch.core.MultiField
import com.qelasticsearch.core.ObjectField
import kotlin.reflect.KProperty

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

// Dynamic field delegate
inline fun <reified T> dynamicField(): FieldDelegate<DynamicField<T>> {
  return FieldDelegate { parent, path -> DynamicField<T>(parent, path) }
}
