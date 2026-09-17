/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.core

import kotlin.reflect.KType

/**
 * Base class for containers that model an Elasticsearch `object` or `nested` field, i.e. a
 * container with a parent field in the metamodel hierarchy (as opposed to the root [Document]).
 */
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
