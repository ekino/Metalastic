/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Document(indexName = "java_record_document")
public record JavaRecordDocument(
        @Field(type = FieldType.Keyword) String id,
        @Field(type = FieldType.Text) String title,
        String noField,
        @MultiField(
                mainField = @Field(type = FieldType.Keyword),
                otherFields = {
                        @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard")
                }
        )
        String code,
        @Field(type = FieldType.Object) JavaRecordAddress address,
        @Field(type = FieldType.Nested) List<JavaRecordTag> tags
) {
}
