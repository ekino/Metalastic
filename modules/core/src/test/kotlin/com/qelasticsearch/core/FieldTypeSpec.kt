package com.qelasticsearch.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

/**
 * Comprehensive test suite for all DSL field types. Tests field creation, type safety, path
 * construction, and edge cases.
 */
class FieldTypeSpec :
  ShouldSpec({
    // Create a mock parent for tests
    val mockParent = object : Index("test") {}

    should("create TextField with correct properties") {
      val field = TextField<String>(mockParent, "title")

      field.name() shouldBe "title"
      field.path() shouldBe "title"
      field.shouldBeInstanceOf<TextField<String>>()
    }

    should("create KeywordField with correct properties") {
      val field = KeywordField<String>(mockParent, "status")

      field.name() shouldBe "status"
      field.path() shouldBe "status"
      field.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle empty parent path correctly") {
      val field = TextField<String>(mockParent, "content")
      field.path() shouldBe "content"
    }

    should("construct paths without dots in fieldName") {
      val field = KeywordField<String>(mockParent, "type")
      field.path() shouldBe "type"
    }
    should("create LongField correctly") {
      val field = LongField<Long>(mockParent, "timestamp")
      field.shouldBeInstanceOf<LongField<Long>>()
      field.path() shouldBe "timestamp"
    }

    should("create IntegerField correctly") {
      val field = IntegerField<Int>(mockParent, "count")
      field.shouldBeInstanceOf<IntegerField<Int>>()
      field.path() shouldBe "count"
    }

    should("create ShortField correctly") {
      val field = ShortField<Short>(mockParent, "port")
      field.shouldBeInstanceOf<ShortField<Short>>()
    }

    should("create ByteField correctly") {
      val field = ByteField<Byte>(mockParent, "flag")
      field.shouldBeInstanceOf<ByteField<Byte>>()
    }
    should("create DoubleField correctly") {
      val field = DoubleField<Double>(mockParent, "price")
      field.shouldBeInstanceOf<DoubleField<Double>>()
      field.path() shouldBe "price"
    }

    should("create FloatField correctly") {
      val field = FloatField<Float>(mockParent, "score")
      field.shouldBeInstanceOf<FloatField<Float>>()
    }

    should("create HalfFloatField correctly") {
      val field = HalfFloatField(mockParent, "ratio")
      field.shouldBeInstanceOf<HalfFloatField>()
    }

    should("create ScaledFloatField correctly") {
      val field = ScaledFloatField(mockParent, "percentage")
      field.shouldBeInstanceOf<ScaledFloatField>()
    }
    should("create DateField correctly") {
      val field = DateField<Date>(mockParent, "createdAt")
      field.shouldBeInstanceOf<DateField<Date>>()
      field.path() shouldBe "createdAt"
    }

    should("create DateNanosField correctly") {
      val field = DateNanosField(mockParent, "timestamp")
      field.shouldBeInstanceOf<DateNanosField>()
    }
    should("create BooleanField correctly") {
      val field = BooleanField<Boolean>(mockParent, "isActive")
      field.shouldBeInstanceOf<BooleanField<Boolean>>()
      field.path() shouldBe "isActive"
    }

    should("create BinaryField correctly") {
      val field = BinaryField(mockParent, "data")
      field.shouldBeInstanceOf<BinaryField>()
    }
    should("create IntegerRangeField correctly") {
      val field = IntegerRangeField(mockParent, "ageRange")
      field.shouldBeInstanceOf<IntegerRangeField>()
    }

    should("create LongRangeField correctly") {
      val field = LongRangeField(mockParent, "timestampRange")
      field.shouldBeInstanceOf<LongRangeField>()
      field.path() shouldBe "timestampRange"
    }

    should("create DoubleRangeField correctly") {
      val field = DoubleRangeField(mockParent, "priceRange")
      field.shouldBeInstanceOf<DoubleRangeField>()
    }

    should("create FloatRangeField correctly") {
      val field = FloatRangeField(mockParent, "scoreRange")
      field.shouldBeInstanceOf<FloatRangeField>()
    }

    should("create DateRangeField correctly") {
      val field = DateRangeField(mockParent, "validityRange")
      field.shouldBeInstanceOf<DateRangeField>()
    }

    should("create IpRangeField correctly") {
      val field = IpRangeField(mockParent, "ipRange")
      field.shouldBeInstanceOf<IpRangeField>()
      field.path() shouldBe "ipRange"
    }
    should("create IpField correctly") {
      val field = IpField(mockParent, "clientIp")
      field.shouldBeInstanceOf<IpField>()
      field.path() shouldBe "clientIp"
    }

    should("create GeoPointField correctly") {
      val field = GeoPointField(mockParent, "location")
      field.shouldBeInstanceOf<GeoPointField>()
    }

    should("create GeoShapeField correctly") {
      val field = GeoShapeField(mockParent, "boundary")
      field.shouldBeInstanceOf<GeoShapeField>()
      field.path() shouldBe "boundary"
    }

    should("create CompletionField correctly") {
      val field = CompletionField(mockParent, "suggest")
      field.shouldBeInstanceOf<CompletionField>()
    }

    should("create TokenCountField correctly") {
      val field = TokenCountField(mockParent, "wordCount")
      field.shouldBeInstanceOf<TokenCountField>()
      field.path() shouldBe "wordCount"
    }

    should("create PercolatorField correctly") {
      val field = PercolatorField(mockParent, "query")
      field.shouldBeInstanceOf<PercolatorField>()
    }
    should("create RankFeatureField correctly") {
      val field = RankFeatureField(mockParent, "relevance")
      field.shouldBeInstanceOf<RankFeatureField>()
      field.path() shouldBe "relevance"
    }

    should("create RankFeaturesField correctly") {
      val field = RankFeaturesField(mockParent, "features")
      field.shouldBeInstanceOf<RankFeaturesField>()
    }

    should("create FlattenedField correctly") {
      val field = FlattenedField(mockParent, "metadata")
      field.shouldBeInstanceOf<FlattenedField>()
      field.path() shouldBe "metadata"
    }

    should("create WildcardField correctly") {
      val field = WildcardField(mockParent, "pattern")
      field.shouldBeInstanceOf<WildcardField>()
    }

    should("create ConstantKeywordField correctly") {
      val field = ConstantKeywordField(mockParent, "category")
      field.shouldBeInstanceOf<ConstantKeywordField>()
      field.path() shouldBe "category"
    }

    should("create ShapeField correctly") {
      val field = ShapeField(mockParent, "geometry")
      field.shouldBeInstanceOf<ShapeField>()
    }

    should("create PointField correctly") {
      val field = PointField(mockParent, "coordinate")
      field.shouldBeInstanceOf<PointField>()
      field.path() shouldBe "coordinate"
    }
    should("handle null parent path as empty string") {
      val field = TextField<String>(mockParent, "content")
      field.path() shouldBe "content"
    }

    should("handle simple field paths") {
      val field = KeywordField<String>(mockParent, "code")
      field.path() shouldBe "code"
    }

    should("handle field names without dots") {
      val field = TextField<String>(mockParent, "name")
      field.path() shouldBe "name"
    }

    should("handle deep nested paths through parent hierarchy") {
      // Create nested parent structure: document -> address -> geo -> country
      val documentParent = object : Index("document") {}
      val addressParent = object : ObjectField(documentParent, "address", false) {}
      val geoParent = object : ObjectField(addressParent, "geo", false) {}
      val countryParent = object : ObjectField(geoParent, "country", false) {}

      // Field name should be simple, path built from parent hierarchy
      val field = KeywordField<String>(countryParent, "code")
      field.path() shouldBe "address.geo.country.code"
    }
    should("maintain generic type information for TextField") {
      val field = TextField<String>(mockParent, "title")
      // Type should be preserved (compile-time check)
      field.shouldBeInstanceOf<TextField<String>>()
    }

    should("maintain generic type information for complex types") {
      val field = KeywordField<List<String>>(mockParent, "tags")
      field.shouldBeInstanceOf<KeywordField<List<String>>>()
    }

    should("handle nullable types") {
      val field = TextField<String?>(mockParent, "optionalField")
      field.shouldBeInstanceOf<TextField<String?>>()
    }

    should("create multiField") {
      // Create a custom multifield using the new architecture
      class TestMultiField(parent: ObjectField, path: String) :
        MultiField<TextField<String>>(parent, TextField(parent, path))

      val field = TestMultiField(mockParent, "mainField")

      field.path() shouldBe "mainField"
      field.name() shouldBe "mainField"
    }
  })
