package com.qelasticsearch.integration

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*

enum class TestStatus {
    ACTIVE, INACTIVE, PENDING
}

@Document(indexName = "nested_test_document")
class NestedTestDocument {
    @Id
    @Field(type = FieldType.Keyword)
    var id: String = ""
    
    @Field(type = FieldType.Text)
    var name: String = ""
    
    @Field(type = FieldType.Keyword)
    var status: TestStatus = TestStatus.PENDING
    
    @Field(type = FieldType.Keyword)
    var statusList: List<TestStatus> = emptyList()
    
    @Field(type = FieldType.Object)
    var operation: Operation = Operation()
    
    // Nested static class - similar to IndexMandateOperation
    class Operation {
        @Field(type = FieldType.Boolean)
        var active: Boolean = false
        
        @Field(type = FieldType.Object)
        var states: List<OperationState> = emptyList()
    }
    
    // Nested static class - similar to IndexMandateOperationState
    class OperationState {
        @Field(type = FieldType.Keyword)
        var id: String = ""
        
        @Field(type = FieldType.Text)
        var description: String = ""
    }
}