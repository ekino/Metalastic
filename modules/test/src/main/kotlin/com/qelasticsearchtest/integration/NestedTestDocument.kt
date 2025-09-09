package com.qelasticsearchtest.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

enum class TestStatus {
  ACTIVE,
  INACTIVE,
  PENDING,
}

interface TestActivity {
  fun getActivityType(): String
}

interface TestMetadata {
  fun getVersion(): String
}

@Document(indexName = "nested_test_document")
class NestedTestDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Text) var name: String = ""

  @Field(type = FieldType.Keyword) var status: TestStatus = TestStatus.PENDING

  @Field(type = FieldType.Keyword) var statusList: List<TestStatus> = emptyList()

  @Field(type = FieldType.Object) var operation: Operation = Operation()

  @Field(type = FieldType.Nested) var activities: List<TestActivity> = emptyList()

  @Field(type = FieldType.Object) var metadata: TestMetadata? = null

  // Nested static class
  class Operation {
    @Field(type = FieldType.Boolean) var active: Boolean = false

    @Field(type = FieldType.Object) var states: List<OperationState> = emptyList()
  }

  // Nested static class
  class OperationState {
    @Field(type = FieldType.Keyword) var id: String = ""

    @Field(type = FieldType.Text) var description: String = ""
  }
}
