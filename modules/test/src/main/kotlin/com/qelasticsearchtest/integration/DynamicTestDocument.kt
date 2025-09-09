package com.qelasticsearchtest.integration

import com.qelasticsearch.core.QDynamicField
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "dynamic_test")
class DynamicTestDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Text) var name: String = ""

  @QDynamicField var runtimeScore: Double = 0.0

  @QDynamicField var tags: List<String> = emptyList()

  @QDynamicField var categories: Set<String> = emptySet()

  @QDynamicField var metadata: Map<String, String> = emptyMap()

  @QDynamicField var isActive: Boolean = false

  @QDynamicField var count: Int = 0
}
