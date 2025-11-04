package com.ekino.oss.metalastic;

import com.ekino.oss.metalastic.integration.SameName;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "samenamespace")
public class SameNameUsages {
    @Field(type = FieldType.Auto)
    public com.SameName fromSamePackage;

    @Field(type = FieldType.Auto)
    public com.ekino.oss.metalastic.SameName fromQelasticsearch;

    @Field(type = FieldType.Auto)
    public SameName fromQelasticsearchIntegration;


    @Field(type = FieldType.Object)
    public com.SameName fromSamePackageObject;

    @Field(type = FieldType.Object)
    public com.ekino.oss.metalastic.SameName fromQelasticsearchObject;

    @Field(type = FieldType.Object)
    public SameName fromQelasticsearchIntegrationObject;

}
