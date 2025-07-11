// Simple test file to check compilation
package test

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "test_nested")
class TestNested {
    @Id
    @Field(type = FieldType.Keyword)
    var id: String = ""
    
    @Field(type = FieldType.Object)
    var nested: NestedClass = NestedClass()
    
    class NestedClass {
        @Field(type = FieldType.Text)
        var name: String = ""
    }
}