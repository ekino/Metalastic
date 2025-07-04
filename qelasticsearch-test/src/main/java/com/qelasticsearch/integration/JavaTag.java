package com.qelasticsearch.integration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * External JavaTag class to test nested class collision handling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JavaTag {
    @Field(type = FieldType.Keyword)
    private String name;
    
    @Field(type = FieldType.Integer)
    private Integer weight;
    
    @Field(type = FieldType.Text)
    private String description;
}