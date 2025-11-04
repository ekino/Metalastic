package com.ekino.oss.metalastic.other.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "boolean_getter_test")
class BooleanGetterTestDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Text) var title: String = ""

  @Field(type = FieldType.Nested) var booleanItems: List<BooleanTestItem> = emptyList()
}
