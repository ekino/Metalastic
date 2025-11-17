/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com.example.dataset;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "example")
public class ExampleDocument {

    @Field(type = FieldType.Keyword)
    public String id;

    @Field(type = FieldType.Object)
    public TestDocument testDocument;
    
    @Field(type = FieldType.Object)
    public NameCollision nameCollision;

    @Field(type = FieldType.Object)
    public NestedObject nestedObject;

    @Field(type = FieldType.Object)
    public NestedObject nestedObject2;

    @Field(type = FieldType.Object)
    public com.example.dataset.NameCollision fromSeparateClass;

    public static class NameCollision {
        @Field(type = FieldType.Text)
        public String firstLevel;
    }

    public static class NestedObject {
        @Field(type = FieldType.Text)
        public String someField;

        @Field(type = FieldType.Object)
        public NameCollision nameCollision;

        public static class NameCollision {
            @Field(type = FieldType.Text)
            public String secondLevel;
        }
    }
}
