package com.qelasticsearch.dsl

abstract class MultiField<T : Field>(parent: ObjectField, internal val mainField: T) :
  ObjectField(parent, mainField.path()) {
  fun mainField(): T = mainField
}
