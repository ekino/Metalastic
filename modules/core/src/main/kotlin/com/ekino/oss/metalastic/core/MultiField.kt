/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.core

import kotlin.reflect.KType

/**
 * Base class for the container generated for a Spring Data `@MultiField`, grouping a main field
 * with its `@InnerField` variants under the same Elasticsearch field name.
 *
 * @param T the Kotlin type carried by the main field
 * @param M the [Field] type of the main field
 */
abstract class MultiField<T : Any?, M : Field<*>>(
  private val parent: ObjectField<*>,
  private val mainField: M,
  fieldType: KType,
) : Container<T>(name = mainField.name(), fieldType = fieldType) {

  init {
    parent.register(this)
  }

  /** Returns the main field of this multi-field group. */
  fun mainField(): M = mainField

  override fun parent(): Container<*> = parent
}
