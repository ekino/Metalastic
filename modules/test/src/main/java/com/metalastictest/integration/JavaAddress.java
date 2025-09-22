package com.metalastictest.integration;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class JavaAddress {
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
