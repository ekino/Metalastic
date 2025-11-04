package com.metalastic.integration;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class NameCollision {
    @Field(type = FieldType.Text)
    public String separateClassField;
}
