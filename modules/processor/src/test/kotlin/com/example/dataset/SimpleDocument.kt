package com.example.dataset

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "simple")
data class SimpleDocument(@Field(type = FieldType.Nested) val externalReference: ObjectClass)
