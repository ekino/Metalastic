/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.example.dataset;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "record_with_interface")
public record RecordWithInterface(
        @Field(type = FieldType.Keyword) String id,
        @Field(type = FieldType.Text) String description
) implements Identifiable {

    @Override
    @Field(type = FieldType.Keyword)
    public String displayName() {
        return id + ": " + description;
    }
}
