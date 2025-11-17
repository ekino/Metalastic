/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "test_document")
class TestDocument : IndexActivity {
  private var id: String = ""

  @Field(type = FieldType.Object) var address: Address = Address()

  @Field(type = FieldType.Keyword) override fun getId() = id
}

interface IndexActivity {
  @Field(type = FieldType.Keyword) fun getId(): String
}

class Address
