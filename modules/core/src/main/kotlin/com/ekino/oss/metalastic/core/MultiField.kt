/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.core

import kotlin.reflect.KType

abstract class MultiField<T : Any?, M : Field<*>>(
  private val parent: ObjectField<*>,
  private val mainField: M,
  fieldType: KType,
) : Container<T>(name = mainField.name(), fieldType = fieldType) {

  init {
    parent.register(this)
  }

  fun mainField(): M = mainField

  override fun parent(): Container<*> = parent
}
