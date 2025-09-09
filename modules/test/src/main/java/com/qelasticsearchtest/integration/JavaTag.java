package com.qelasticsearchtest.integration;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * External JavaTag class to test nested class collision handling
 */
public class JavaTag {
    @Field(type = FieldType.Keyword)
    private String name;
    
    @Field(type = FieldType.Integer)
    private Integer weight;
    
    @Field(type = FieldType.Text)
    private String description;

    public JavaTag() {
    }

    public JavaTag(String name, Integer weight, String description) {
        this.name = name;
        this.weight = weight;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}