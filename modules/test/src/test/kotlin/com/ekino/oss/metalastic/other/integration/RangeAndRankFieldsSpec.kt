/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.other.integration

import com.ekino.oss.metalastic.core.IntegerRangeField
import com.ekino.oss.metalastic.core.IpRangeField
import com.ekino.oss.metalastic.core.RankFeatureField
import com.ekino.oss.metalastic.core.RankFeaturesField
import com.ekino.oss.metalastic.other.integration.MetaRangeAndRankFieldsDocument.Companion.rangeAndRankFieldsDocument
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/** Verifies that every range and rank FieldType maps to its dedicated core field class. */
class RangeAndRankFieldsSpec :
  ShouldSpec({
    should("map FieldType.Ip_Range to IpRangeField") {
      val field = rangeAndRankFieldsDocument.allowedIps
      field.shouldBeInstanceOf<IpRangeField<*>>()
      field.path() shouldBe "allowedIps"
    }

    should("map FieldType.Rank_Features to RankFeaturesField") {
      val field = rangeAndRankFieldsDocument.topics
      field.shouldBeInstanceOf<RankFeaturesField<*>>()
      field.path() shouldBe "topics"
    }

    should("keep FieldType.Rank_Feature mapped to RankFeatureField") {
      rangeAndRankFieldsDocument.pagerank.shouldBeInstanceOf<RankFeatureField<*>>()
    }

    should("keep FieldType.Integer_Range mapped to IntegerRangeField") {
      rangeAndRankFieldsDocument.ageRange.shouldBeInstanceOf<IntegerRangeField<*>>()
    }
  })
