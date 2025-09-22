package com.example.dataset

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

data class OtherObjectClass(@Field(type = FieldType.Keyword) val id: String)
