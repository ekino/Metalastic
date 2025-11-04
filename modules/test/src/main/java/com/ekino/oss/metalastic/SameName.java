package com.ekino.oss.metalastic;


import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class SameName {
    @Field(type = FieldType.Auto)
    public String fromQelasticsearchPackage;
}
