/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.other.integration

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

/** Exercises the range and rank field type mappings that have a dedicated core field class. */
@Document(indexName = "range_and_rank_fields")
class RangeAndRankFieldsDocument {
  @Field(type = FieldType.Keyword) var id: String = ""

  @Field(type = FieldType.Ip_Range) var allowedIps: String = ""

  @Field(type = FieldType.Rank_Feature) var pagerank: Double = 0.0

  @Field(type = FieldType.Rank_Features) var topics: Map<String, Double> = emptyMap()

  @Field(type = FieldType.Integer_Range) var ageRange: String = ""
}
