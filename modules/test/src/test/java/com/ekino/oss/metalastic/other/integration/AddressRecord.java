/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.other.integration;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public record AddressRecord(
        @Field(type = FieldType.Text) String street,
        @Field(type = FieldType.Keyword) String city
) {
}
