package com.qelasticsearch.integration

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "test_document")
class TestDocument {
    @Id
    @Field(type = FieldType.Keyword)
    var id: String = ""

    @Field(type = FieldType.Text)
    var name: String = ""

    @Field(type = FieldType.Integer)
    var age: Int = 0

    @Field(type = FieldType.Boolean)
    var active: Boolean = false

    @Field(type = FieldType.Date)
    var createdDate: java.util.Date = java.util.Date()

    @Field(type = FieldType.Object)
    var address: Address = Address()

    @Field(type = FieldType.Nested)
    var tags: List<Tag> = emptyList()
}

class Address {
    @Field(type = FieldType.Text)
    var city: String = ""

    @Field(type = FieldType.Keyword)
    var country: String = ""

    @Field(type = FieldType.Keyword)
    var zipCode: String = ""
}

class Tag {
    @Field(type = FieldType.Keyword)
    var name: String = ""

    @Field(type = FieldType.Integer)
    var weight: Int = 0
}
