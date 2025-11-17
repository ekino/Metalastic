/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com.ekino.oss.metalastic;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class AClass {

    @Field(type = FieldType.Text)
    public String name;
}
