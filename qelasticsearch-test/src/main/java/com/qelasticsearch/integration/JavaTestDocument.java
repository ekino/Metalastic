package com.qelasticsearch.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

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

    @Field(type = FieldType.Keyword)
    private List<String> aList;

    @Field(type = FieldType.Keyword)
    private List<ParametrizedType<String>> parametrizedTypeList;

    @Field(type = FieldType.Keyword)
    private ParametrizedType<String> someParametrizedType;

    @Field(type = FieldType.Keyword)
    private Map<String, Integer> mapField;

    @Field(type = FieldType.Keyword)
    private MultiArgType<String, Integer, Boolean> multiArgType;

    private String noAnnot;

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

    @Field(type = FieldType.Date)
    private Date date;

    @Field(type = FieldType.Date)
    private LocalDate localDate;

    @Field(type = FieldType.Date)
    private LocalDateTime localDateTime;

    @Field(type = FieldType.Date)
    private Instant instant;

    @Field(type = FieldType.Double)
    private Double score;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private TestStatus status;

    @Field(type = FieldType.Keyword)
    private Priority priorityLevel;

    @Field(type = FieldType.Object)
    private JavaAddress address;

    @Field(type = FieldType.Nested)
    private List<com.qelasticsearch.integration.JavaTag> tags;

    @Field(type = FieldType.Nested)
    private List<JavaTag> tags2;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard")
            }
    )
    private String multiFieldName;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "SEARCH", type = FieldType.Text)
    )
    private String description2;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class JavaTag {
        @Field(type = FieldType.Keyword)
        private String tagName;

        @Field(type = FieldType.Integer)
        private Integer size;
    }
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

