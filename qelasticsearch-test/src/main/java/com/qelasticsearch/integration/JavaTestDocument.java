package com.qelasticsearch.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

/**
 * Java document class to test interoperability with QElasticsearch processor.
 * This simulates a real-world scenario where Java + Spring Data Elasticsearch are used together.
 */
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

    @Field(type = FieldType.Keyword)
    private List<Priority> priorities;


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
                    @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard"),
                    @InnerField(suffix = "zobi", type = FieldType.Text, analyzer = "standard")
            }
    )
    private String multiFieldName;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "SEARCH", type = FieldType.Text)
    )
    private String description2;

    @Field(type = FieldType.Object)
    private JavaAddress.WithoutAnnotatedField withoutAnnotatedField;

    @Field(type = FieldType.Object)
    private JavaAddress.SomeInnerClass someInnerClass;

    // Constructors
    public JavaTestDocument() {
    }

    public JavaTestDocument(String id, List<String> aList, List<ParametrizedType<String>> parametrizedTypeList,
                            ParametrizedType<String> someParametrizedType, Map<String, Integer> mapField,
                            MultiArgType<String, Integer, Boolean> multiArgType, String noAnnot, String title,
                            String description, Integer priority, Boolean isActive, Date createdAt, Date date,
                            LocalDate localDate, LocalDateTime localDateTime, Instant instant, Double score,
                            String category, TestStatus status, Priority priorityLevel, JavaAddress address,
                            List<com.qelasticsearch.integration.JavaTag> tags, List<JavaTag> tags2,
                            String multiFieldName, String description2) {
        this.id = id;
        this.aList = aList;
        this.parametrizedTypeList = parametrizedTypeList;
        this.someParametrizedType = someParametrizedType;
        this.mapField = mapField;
        this.multiArgType = multiArgType;
        this.noAnnot = noAnnot;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.date = date;
        this.localDate = localDate;
        this.localDateTime = localDateTime;
        this.instant = instant;
        this.score = score;
        this.category = category;
        this.status = status;
        this.priorityLevel = priorityLevel;
        this.address = address;
        this.tags = tags;
        this.tags2 = tags2;
        this.multiFieldName = multiFieldName;
        this.description2 = description2;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAList() {
        return aList;
    }

    public void setAList(List<String> aList) {
        this.aList = aList;
    }

    public List<ParametrizedType<String>> getParametrizedTypeList() {
        return parametrizedTypeList;
    }

    public void setParametrizedTypeList(List<ParametrizedType<String>> parametrizedTypeList) {
        this.parametrizedTypeList = parametrizedTypeList;
    }

    public ParametrizedType<String> getSomeParametrizedType() {
        return someParametrizedType;
    }

    public void setSomeParametrizedType(ParametrizedType<String> someParametrizedType) {
        this.someParametrizedType = someParametrizedType;
    }

    public Map<String, Integer> getMapField() {
        return mapField;
    }

    public void setMapField(Map<String, Integer> mapField) {
        this.mapField = mapField;
    }

    public MultiArgType<String, Integer, Boolean> getMultiArgType() {
        return multiArgType;
    }

    public void setMultiArgType(MultiArgType<String, Integer, Boolean> multiArgType) {
        this.multiArgType = multiArgType;
    }

    public String getNoAnnot() {
        return noAnnot;
    }

    public void setNoAnnot(String noAnnot) {
        this.noAnnot = noAnnot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public Priority getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Priority priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public JavaAddress getAddress() {
        return address;
    }

    public void setAddress(JavaAddress address) {
        this.address = address;
    }

    public List<com.qelasticsearch.integration.JavaTag> getTags() {
        return tags;
    }

    public void setTags(List<com.qelasticsearch.integration.JavaTag> tags) {
        this.tags = tags;
    }

    public List<JavaTag> getTags2() {
        return tags2;
    }

    public void setTags2(List<JavaTag> tags2) {
        this.tags2 = tags2;
    }

    public String getMultiFieldName() {
        return multiFieldName;
    }

    public void setMultiFieldName(String multiFieldName) {
        this.multiFieldName = multiFieldName;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
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

    @Field(type = FieldType.Nested, index = false)
    private List<Priority> nestedPriorities;

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
