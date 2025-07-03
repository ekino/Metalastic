package com.qelasticsearch.integration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * Java document class using Lombok annotations to test interoperability
 * with QElasticsearch processor. This simulates a real-world scenario
 * where Java + Lombok + Spring Data Elasticsearch are used together.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "java_test_document")
public class JavaTestDocument {
    
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    
    @Field(type = FieldType.Text)
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Integer)
    private Integer priority;
    
    @Field(type = FieldType.Boolean)
    private Boolean isActive;
    
    @Field(type = FieldType.Date)
    private Date createdAt;
    
    @Field(type = FieldType.Double)
    private Double score;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Object)
    private JavaAddress address;
    
    @Field(type = FieldType.Nested)
    private List<JavaTag> tags;
    
    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard")
        }
    )
    private String multiFieldName;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class JavaAddress {
    @Field(type = FieldType.Text)
    private String street;
    
    @Field(type = FieldType.Text)
    private String city;
    
    @Field(type = FieldType.Keyword)
    private String zipCode;
    
    @Field(type = FieldType.Keyword)
    private String country;
    
    @Field(type = FieldType.Keyword) // Simplified for test - would normally be geo_point
    private String location;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class JavaTag {
    @Field(type = FieldType.Keyword)
    private String name;
    
    @Field(type = FieldType.Integer)
    private Integer weight;
    
    @Field(type = FieldType.Text)
    private String description;
}