/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "outer")
data class OuterDocument(@Field(type = FieldType.Object) val inner: InnerDocument) {

  @Document(indexName = "inner")
  data class InnerDocument(
    @Field(type = FieldType.Text) val name: String,
    @Field(type = FieldType.Nested) val nested: NestedObject,
  ) {
    class NestedObject {
      @Field(type = FieldType.Keyword) var id: Int? = null
    }
  }
}
