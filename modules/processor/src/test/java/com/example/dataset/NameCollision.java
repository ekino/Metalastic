/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com.example.dataset;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class NameCollision {
    @Field(type = FieldType.Text)
    public String separateClassField;
}
