/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

data class ObjectClass(
  @Field(type = FieldType.Keyword) val id: String,
  @Field(type = FieldType.Object) val field: OtherObjectClass,
)
