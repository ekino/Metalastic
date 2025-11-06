package com.example.dataset;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

/**
 * Java document class to test interoperability with Metalastic processor.
 * This simulates a real-world scenario where Java + Spring Data Elasticsearch are used together.
 */
@Document(indexName = "java_test_document")
public class JavaTestDocument {
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private List<String> aList;

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


    @Field(type = FieldType.Object)
    private JavaAddress address;

    @Field(type = FieldType.Nested)
    private List<JavaTag> tags2;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard"),
                    @InnerField(suffix = "somethingElse", type = FieldType.Text, analyzer = "standard")
            }
    )
    private String multiFieldName;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword)
    )
    private String description2;

    @Field(type = FieldType.Object)
    private JavaAddress.WithoutAnnotatedField withoutAnnotatedField;

    @Field(type = FieldType.Object)
    private JavaAddress.SomeInnerClass someInnerClass;

    // Constructors
    public JavaTestDocument() {
    }


    // Nested inner class
    public static class JavaTag {
        @Field(type = FieldType.Keyword)
        private String tagName;

        @Field(type = FieldType.Integer)
        private Integer size;

        public JavaTag() {
        }

        public JavaTag(String tagName, Integer size) {
            this.tagName = tagName;
            this.size = size;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}

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

    public JavaAddress() {
    }

    public JavaAddress(String street, String city, String zipCode, String country, String location) {
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
        this.location = location;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static class WithoutAnnotatedField {
        private Integer someField;
    }

    public static class SomeInnerClass {
        @Field(type = FieldType.Integer)
        private Integer someField;

        @Field(type = FieldType.Object)
        private SomeOtherInnerClass someOtherInnerClass;
    }

    public static class SomeOtherInnerClass {
        @Field(type = FieldType.Integer)
        private String someField;
    }
}
