package com.ekino.oss.metalastic.other.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "getter_method_test")
class GetterMethodTestDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Text) var name: String = ""

  @Field(type = FieldType.Nested, ignoreFields = ["description"])
  var items: List<TestItem> = emptyList()
}

interface TestItem {
  @Field(type = FieldType.Keyword) fun getCategory(): String

  @Field(type = FieldType.Text) fun getDisplayName(): String

  @Field(type = FieldType.Integer) fun getPriority(): Int

  @Field(type = FieldType.Boolean) fun getActive(): Boolean

  // This method should be ignored (no @Field annotation)
  fun getDescription(): String
}

interface BooleanTestItem {
  @Field(type = FieldType.Boolean) fun isEnabled(): Boolean

  @Field(type = FieldType.Boolean) fun isVerified(): Boolean

  @Field(type = FieldType.Boolean) fun isPublished(): Boolean

  @Field(type = FieldType.Boolean) fun someOtherMethod(): Boolean

  // This method should be ignored (no @Field annotation)
  fun isHidden(): Boolean
}
