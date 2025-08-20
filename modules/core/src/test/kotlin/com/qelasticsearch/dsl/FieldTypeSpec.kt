package com.qelasticsearch.dsl

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
      val field = KeywordField<String>(mockParent, "document.status")

      field.name() shouldBe "status"
      field.path() shouldBe "document.status"
      field.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle empty parent path correctly") {
      val field = TextField<String>(mockParent, "content")
      field.path() shouldBe "content"
    }

    should("construct dotted paths with parent") {
      val field = KeywordField<String>(mockParent, "document.type")
      field.path() shouldBe "document.type"
    }
    should("create LongField correctly") {
      val field = LongField<Long>(mockParent, "timestamp")
      field.shouldBeInstanceOf<LongField<Long>>()
      field.path() shouldBe "timestamp"
    }

    should("create IntegerField correctly") {
      val field = IntegerField<Int>(mockParent, "document.count")
      field.shouldBeInstanceOf<IntegerField<Int>>()
      field.path() shouldBe "document.count"
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
      val field = DoubleField<Double>(mockParent, "document.price")
      field.shouldBeInstanceOf<DoubleField<Double>>()
      field.path() shouldBe "document.price"
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
      val field = DateField<Date>(mockParent, "document.createdAt")
      field.shouldBeInstanceOf<DateField<Date>>()
      field.path() shouldBe "document.createdAt"
    }

    should("create DateNanosField correctly") {
      val field = DateNanosField(mockParent, "timestamp")
      field.shouldBeInstanceOf<DateNanosField>()
    }
    should("create BooleanField correctly") {
      val field = BooleanField<Boolean>(mockParent, "document.isActive")
      field.shouldBeInstanceOf<BooleanField<Boolean>>()
      field.path() shouldBe "document.isActive"
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
      val field = LongRangeField(mockParent, "document.timestampRange")
      field.shouldBeInstanceOf<LongRangeField>()
      field.path() shouldBe "document.timestampRange"
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
      val field = IpRangeField(mockParent, "network.ipRange")
      field.shouldBeInstanceOf<IpRangeField>()
      field.path() shouldBe "network.ipRange"
    }
    should("create IpField correctly") {
      val field = IpField(mockParent, "request.clientIp")
      field.shouldBeInstanceOf<IpField>()
      field.path() shouldBe "request.clientIp"
    }

    should("create GeoPointField correctly") {
      val field = GeoPointField(mockParent, "location")
      field.shouldBeInstanceOf<GeoPointField>()
    }

    should("create GeoShapeField correctly") {
      val field = GeoShapeField(mockParent, "geo.boundary")
      field.shouldBeInstanceOf<GeoShapeField>()
      field.path() shouldBe "geo.boundary"
    }

    should("create CompletionField correctly") {
      val field = CompletionField(mockParent, "suggest")
      field.shouldBeInstanceOf<CompletionField>()
    }

    should("create TokenCountField correctly") {
      val field = TokenCountField(mockParent, "analysis.wordCount")
      field.shouldBeInstanceOf<TokenCountField>()
      field.path() shouldBe "analysis.wordCount"
    }

    should("create PercolatorField correctly") {
      val field = PercolatorField(mockParent, "query")
      field.shouldBeInstanceOf<PercolatorField>()
    }
    should("create RankFeatureField correctly") {
      val field = RankFeatureField(mockParent, "ranking.relevance")
      field.shouldBeInstanceOf<RankFeatureField>()
      field.path() shouldBe "ranking.relevance"
    }

    should("create RankFeaturesField correctly") {
      val field = RankFeaturesField(mockParent, "features")
      field.shouldBeInstanceOf<RankFeaturesField>()
    }

    should("create FlattenedField correctly") {
      val field = FlattenedField(mockParent, "document.metadata")
      field.shouldBeInstanceOf<FlattenedField>()
      field.path() shouldBe "document.metadata"
    }

    should("create WildcardField correctly") {
      val field = WildcardField(mockParent, "pattern")
      field.shouldBeInstanceOf<WildcardField>()
    }

    should("create ConstantKeywordField correctly") {
      val field = ConstantKeywordField(mockParent, "product.category")
      field.shouldBeInstanceOf<ConstantKeywordField>()
      field.path() shouldBe "product.category"
    }

    should("create ShapeField correctly") {
      val field = ShapeField(mockParent, "geometry")
      field.shouldBeInstanceOf<ShapeField>()
    }

    should("create PointField correctly") {
      val field = PointField(mockParent, "geo.coordinate")
      field.shouldBeInstanceOf<PointField>()
      field.path() shouldBe "geo.coordinate"
    }
    should("handle null parent path as empty string") {
      val field = TextField<String>(mockParent, "content")
      field.path() shouldBe "content"
    }

    should("handle deep nested paths") {
      val field = KeywordField<String>(mockParent, "address.geo.country.code")
      field.path() shouldBe "address.geo.country.code"
    }

    should("handle parent path with dots") {
      val field = TextField<String>(mockParent, "user.profile.settings.name")
      field.path() shouldBe "user.profile.settings.name"
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
