package com.metalastictest;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "samenamespace")
public class SameNameUsages {
    @Field(type = FieldType.Auto)
    public SameName fromSamePackage;

    @Field(type = FieldType.Auto)
    public com.metalastic.SameName fromQelasticsearch;

    @Field(type = FieldType.Auto)
    public com.metalastic.integration.SameName fromQelasticsearchIntegration;


    @Field(type = FieldType.Object)
    public SameName fromSamePackageObject;

    @Field(type = FieldType.Object)
    public com.metalastic.SameName fromQelasticsearchObject;

    @Field(type = FieldType.Object)
    public com.metalastic.integration.SameName fromQelasticsearchIntegrationObject;

}
