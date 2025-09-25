package com.metalastic.core

import kotlin.reflect.KType

abstract class MultiField<T : Any?, M : Field<*>>(
  parent: ObjectField<*>,
  private val mainField: M,
  fieldType: KType,
) : ObjectField<T>(parent = parent, name = mainField.name(), fieldType = fieldType) {
  fun mainField(): M = mainField
}
