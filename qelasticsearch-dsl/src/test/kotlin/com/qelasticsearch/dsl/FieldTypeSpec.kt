package com.qelasticsearch.dsl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

/**
 * Comprehensive test suite for all DSL field types.
 * Tests field creation, type safety, path construction, and edge cases.
 */
class FieldTypeSpec :
    ShouldSpec({

        should("create TextField with correct properties") {
            val field = TextField<String>("title")

            field.name() shouldBe "title"
            field.path().path shouldBe "title"
            field.shouldBeInstanceOf<TextField<String>>()
        }

        should("create KeywordField with correct properties") {
            val field = KeywordField<String>("status", FieldPath("document"))

            field.name() shouldBe "status"
            field.path().path shouldBe "document.status"
            field.shouldBeInstanceOf<KeywordField<String>>()
        }

        should("handle empty parent path correctly") {
            val field = TextField<String>("content")
            field.path().path shouldBe "content"
        }

        should("construct dotted paths with parent") {
            val field = KeywordField<String>("type", FieldPath("metadata"))
            field.path().path shouldBe "metadata.type"
        }
        should("create LongField correctly") {
            val field = LongField<Long>("timestamp")
            field.shouldBeInstanceOf<LongField<Long>>()
            field.path().path shouldBe "timestamp"
        }

        should("create IntegerField correctly") {
            val field = IntegerField<Int>("count", FieldPath("stats"))
            field.shouldBeInstanceOf<IntegerField<Int>>()
            field.path().path shouldBe "stats.count"
        }

        should("create ShortField correctly") {
            val field = ShortField<Short>("port")
            field.shouldBeInstanceOf<ShortField<Short>>()
        }

        should("create ByteField correctly") {
            val field = ByteField<Byte>("flag")
            field.shouldBeInstanceOf<ByteField<Byte>>()
        }
        should("create DoubleField correctly") {
            val field = DoubleField<Double>("price", FieldPath("product"))
            field.shouldBeInstanceOf<DoubleField<Double>>()
            field.path().path shouldBe "product.price"
        }

        should("create FloatField correctly") {
            val field = FloatField<Float>("score")
            field.shouldBeInstanceOf<FloatField<Float>>()
        }

        should("create HalfFloatField correctly") {
            val field = HalfFloatField("ratio")
            field.shouldBeInstanceOf<HalfFloatField>()
        }

        should("create ScaledFloatField correctly") {
            val field = ScaledFloatField("percentage")
            field.shouldBeInstanceOf<ScaledFloatField>()
        }
        should("create DateField correctly") {
            val field = DateField<Date>("createdAt", FieldPath("audit"))
            field.shouldBeInstanceOf<DateField<Date>>()
            field.path().path shouldBe "audit.createdAt"
        }

        should("create DateNanosField correctly") {
            val field = DateNanosField("timestamp")
            field.shouldBeInstanceOf<DateNanosField>()
        }
        should("create BooleanField correctly") {
            val field = BooleanField<Boolean>("isActive", FieldPath("status"))
            field.shouldBeInstanceOf<BooleanField<Boolean>>()
            field.path().path shouldBe "status.isActive"
        }

        should("create BinaryField correctly") {
            val field = BinaryField("data")
            field.shouldBeInstanceOf<BinaryField>()
        }
        should("create IntegerRangeField correctly") {
            val field = IntegerRangeField("ageRange")
            field.shouldBeInstanceOf<IntegerRangeField>()
        }

        should("create LongRangeField correctly") {
            val field = LongRangeField("timestampRange", FieldPath("filter"))
            field.shouldBeInstanceOf<LongRangeField>()
            field.path().path shouldBe "filter.timestampRange"
        }

        should("create DoubleRangeField correctly") {
            val field = DoubleRangeField("priceRange")
            field.shouldBeInstanceOf<DoubleRangeField>()
        }

        should("create FloatRangeField correctly") {
            val field = FloatRangeField("scoreRange")
            field.shouldBeInstanceOf<FloatRangeField>()
        }

        should("create DateRangeField correctly") {
            val field = DateRangeField("validityRange")
            field.shouldBeInstanceOf<DateRangeField>()
        }

        should("create IpRangeField correctly") {
            val field = IpRangeField("ipRange", FieldPath("network"))
            field.shouldBeInstanceOf<IpRangeField>()
            field.path().path shouldBe "network.ipRange"
        }
        should("create IpField correctly") {
            val field = IpField("clientIp", FieldPath("request"))
            field.shouldBeInstanceOf<IpField>()
            field.path().path shouldBe "request.clientIp"
        }

        should("create GeoPointField correctly") {
            val field = GeoPointField("location")
            field.shouldBeInstanceOf<GeoPointField>()
        }

        should("create GeoShapeField correctly") {
            val field = GeoShapeField("boundary", FieldPath("geo"))
            field.shouldBeInstanceOf<GeoShapeField>()
            field.path().path shouldBe "geo.boundary"
        }

        should("create CompletionField correctly") {
            val field = CompletionField("suggest")
            field.shouldBeInstanceOf<CompletionField>()
        }

        should("create TokenCountField correctly") {
            val field = TokenCountField("wordCount", FieldPath("analysis"))
            field.shouldBeInstanceOf<TokenCountField>()
            field.path().path shouldBe "analysis.wordCount"
        }

        should("create PercolatorField correctly") {
            val field = PercolatorField("query")
            field.shouldBeInstanceOf<PercolatorField>()
        }
        should("create RankFeatureField correctly") {
            val field = RankFeatureField("relevance", FieldPath("ranking"))
            field.shouldBeInstanceOf<RankFeatureField>()
            field.path().path shouldBe "ranking.relevance"
        }

        should("create RankFeaturesField correctly") {
            val field = RankFeaturesField("features")
            field.shouldBeInstanceOf<RankFeaturesField>()
        }

        should("create FlattenedField correctly") {
            val field = FlattenedField("metadata", FieldPath("document"))
            field.shouldBeInstanceOf<FlattenedField>()
            field.path().path shouldBe "document.metadata"
        }

        should("create WildcardField correctly") {
            val field = WildcardField("pattern")
            field.shouldBeInstanceOf<WildcardField>()
        }

        should("create ConstantKeywordField correctly") {
            val field = ConstantKeywordField("category", FieldPath("product"))
            field.shouldBeInstanceOf<ConstantKeywordField>()
            field.path().path shouldBe "product.category"
        }

        should("create ShapeField correctly") {
            val field = ShapeField("geometry")
            field.shouldBeInstanceOf<ShapeField>()
        }

        should("create PointField correctly") {
            val field = PointField("coordinate", FieldPath("geo"))
            field.shouldBeInstanceOf<PointField>()
            field.path().path shouldBe "geo.coordinate"
        }
        should("handle null parent path as empty string") {
            val field = TextField<String>("content")
            field.path().path shouldBe "content"
        }

        should("handle deep nested paths") {
            val field = KeywordField<String>("code", FieldPath("address.geo.country"))
            field.path().path shouldBe "address.geo.country.code"
        }

        should("handle parent path with dots") {
            val field = TextField<String>("name", FieldPath("user.profile.settings"))
            field.path().path shouldBe "user.profile.settings.name"
        }
        should("maintain generic type information for TextField") {
            val field = TextField<String>("title")
            // Type should be preserved (compile-time check)
            field.shouldBeInstanceOf<TextField<String>>()
        }

        should("maintain generic type information for complex types") {
            val field = KeywordField<List<String>>("tags")
            field.shouldBeInstanceOf<KeywordField<List<String>>>()
        }

        should("handle nullable types") {
            val field = TextField<String?>("optionalField")
            field.shouldBeInstanceOf<TextField<String?>>()
        }

        should("create multiField") {
            val field =
                MultiField(
                    parentPath = FieldPath(""),
                    mainField = TextField<String>("mainField"),
                    innerFields = emptyMap(),
                )

            field.path().path shouldBe "mainField"
            field.name() shouldBe "mainField"
        }
    })
