package com.metalastic.other.integration

import java.time.LocalDate
import java.time.temporal.Temporal
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "outer")
data class OuterDocument<X : Number, Z : Temporal>(
  @Field(type = FieldType.Object) val inner: InnerDocument<X, LocalDate>,
  @Field(type = FieldType.Text) val inner2: X,
) {

  @Document(indexName = "inner")
  data class InnerDocument<Z : Number, out T : Temporal>(
    @Field(type = FieldType.Text) val name: String,
    @Field(type = FieldType.Nested) val nested: NestedObject,
  ) {
    class NestedObject {
      @Field(type = FieldType.Keyword) var id: Int? = null
    }
  }
}
