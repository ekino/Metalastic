/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com;


import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class SameName {
    @Field(type = FieldType.Auto)
    public String fromSamePackage;
}
