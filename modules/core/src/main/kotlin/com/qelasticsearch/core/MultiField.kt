package com.qelasticsearch.core

abstract class MultiField<T : Field>(parent: ObjectField, internal val mainField: T) :
  ObjectField(parent, mainField.name()) {
  fun mainField(): T = mainField
}
