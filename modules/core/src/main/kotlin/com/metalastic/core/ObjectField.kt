package com.metalastic.core

import kotlin.reflect.KType

abstract class ObjectField<T : Any?>(
  private val parent: ObjectField<*>? = null,
  name: String,
  nested: Boolean = false,
  fieldType: KType,
) : Container<T>(name, nested, fieldType) {

  init {
    parent?.register(this)
  }

  override fun parent(): ObjectField<*>? = parent
}
