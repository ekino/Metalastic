package com.qelasticsearch.integration;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "person")
public class IndexPerson {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String firstName;

    @Field(type = FieldType.Keyword)
    private String lastName;

    @Field(type = FieldType.Integer)
    private Integer age;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private Date birthDate;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Object)
    private Address address;

    @Field(type = FieldType.Nested)
    private List<Activity> activities;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }

    public static class Address {
        @Field(type = FieldType.Text)
        private String street;

        @Field(type = FieldType.Keyword)
        private String city;

        @Field(type = FieldType.Keyword)
        private String zipCode;

        @Field(type = FieldType.Keyword)
        private String country;

        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class Activity {
        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Text)
        private String description;

        @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
        private Date startDate;

        @Field(type = FieldType.Boolean)
        private Boolean completed;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }

        public Boolean getCompleted() { return completed; }
        public void setCompleted(Boolean completed) { this.completed = completed; }
    }
}