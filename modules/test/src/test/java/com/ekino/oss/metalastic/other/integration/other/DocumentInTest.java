/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com.ekino.oss.metalastic.other.integration.other;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "document_in_test2")
public class DocumentInTest {
    @Field(type = FieldType.Keyword)
    public String id;
}
