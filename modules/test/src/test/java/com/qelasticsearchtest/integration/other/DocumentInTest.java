package com.qelasticsearchtest.integration.other;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "document_in_test2")
public class DocumentInTest {
    @Field(type = FieldType.Keyword)
    public String id;
}
