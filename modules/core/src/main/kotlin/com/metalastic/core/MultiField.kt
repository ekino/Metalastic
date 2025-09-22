package com.metalastic.core

abstract class MultiField<T, M : Field<*>>(parent: ObjectField<*>, internal val mainField: M) :
  ObjectField<T>(parent, mainField.name()) {
  fun mainField(): M = mainField
}
