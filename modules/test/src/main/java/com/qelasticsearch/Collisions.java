package com.qelasticsearch;

import com.qelasticsearch.integration.JavaTag;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "collisions")
public class Collisions {

    @Field(type = FieldType.Text)
    public String name;

    @Field(type = FieldType.Object)
    public AClass fromNested;

    @Field(type = FieldType.Object)
    public com.qelasticsearch.AClass samePackage;

    @Field(type = FieldType.Object)
    public com.qelasticsearch.integration.AClass fromIntegration;

    @Field(type = FieldType.Object)
    public JavaTag javaTag;

    public static class AClass {
        @Field(type = FieldType.Text)
        public String name;
    }
}
