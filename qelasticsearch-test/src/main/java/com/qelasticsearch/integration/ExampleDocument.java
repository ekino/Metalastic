package com.qelasticsearch.integration;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "example")
public class ExampleDocument {

    @Id
    public String id;

    @Field(type = FieldType.Object)
    public NameCollision nameCollision;

    @Field(type = FieldType.Object)
    public NestedObject nestedObject;

    @Field(type = FieldType.Object)
    public NestedObject nestedObject2;

    @Field(type = FieldType.Object)
    public com.qelasticsearch.integration.NameCollision fromSeparateClass;

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
