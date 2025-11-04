package com.metalastic.other.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "test_document")
class TestDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Text) var name: String = ""

  @Field(type = FieldType.Integer) var age: Int = 0

  @Field(type = FieldType.Boolean) var active: Boolean = false

  @Field(type = FieldType.Date) var createdDate: java.util.Date = java.util.Date()

  @Field(type = FieldType.Object) var address: Address = Address()

  @Field(type = FieldType.Nested) var tags: List<Tag> = emptyList()

  @Document(indexName = "test_document_inner")
  class TestDocument {
    @Field(type = FieldType.Keyword) var id: String = ""
  }
}

class Address {
  @Field(type = FieldType.Text) var city: String = ""

  @Field(type = FieldType.Keyword) var country: String = ""

  @Field(type = FieldType.Keyword) var zipCode: String = ""
}

class Tag {
  @Field(type = FieldType.Keyword) var name: String = ""

  @Field(type = FieldType.Integer) var weight: Int = 0
}

@Document(indexName = "test_document_with_typed_id")
@Suppress("UnusedPrivateClass")
private class SomePrivateClass(
  @Field(type = FieldType.Text)
  val privateUsage2: Map<SomeOtherPrivateClass, Map<String, SomeOtherPrivateClass>>,
  @Field(type = FieldType.Object) val privateUsage: List<SomeOtherPrivateClass>,
)

private class SomeOtherPrivateClass(@Field(type = FieldType.Keyword) val id: String)
