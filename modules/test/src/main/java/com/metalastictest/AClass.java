package com.metalastictest;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class AClass {

    @Field(type = FieldType.Text)
    public String name;
}
