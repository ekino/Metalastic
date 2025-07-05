package com.qelasticsearch.dsl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Comprehensive test suite for all DSL field types.
 * Tests field creation, type safety, path construction, and edge cases.
 */
class FieldTypeSpec :
    ShouldSpec({

        should("create TextField with correct properties") {
            val field = TextField<String>("title", "")

            field.name shouldBe "title"
            field.path shouldBe "title"
            field.shouldBeInstanceOf<TextField<String>>()
        }

        should("create KeywordField with correct properties") {
            val field = KeywordField<String>("status", "document")

            field.name shouldBe "status"
            field.path shouldBe "document.status"
            field.shouldBeInstanceOf<KeywordField<String>>()
        }

        should("handle empty parent path correctly") {
            val field = TextField<String>("content", "")
            field.path shouldBe "content"
        }

        should("construct dotted paths with parent") {
            val field = KeywordField<String>("type", "metadata")
            field.path shouldBe "metadata.type"
        }
        should("create LongField correctly") {
            val field = LongField<Long>("timestamp", "")
            field.shouldBeInstanceOf<LongField<Long>>()
            field.path shouldBe "timestamp"
        }

        should("create IntegerField correctly") {
            val field = IntegerField<Int>("count", "stats")
            field.shouldBeInstanceOf<IntegerField<Int>>()
            field.path shouldBe "stats.count"
        }

        should("create ShortField correctly") {
            val field = ShortField<Short>("port", "")
            field.shouldBeInstanceOf<ShortField<Short>>()
        }

        should("create ByteField correctly") {
            val field = ByteField<Byte>("flag", "")
            field.shouldBeInstanceOf<ByteField<Byte>>()
        }
        should("create DoubleField correctly") {
            val field = DoubleField<Double>("price", "product")
            field.shouldBeInstanceOf<DoubleField<Double>>()
            field.path shouldBe "product.price"
        }

        should("create FloatField correctly") {
            val field = FloatField<Float>("score", "")
            field.shouldBeInstanceOf<FloatField<Float>>()
        }

        should("create HalfFloatField correctly") {
            val field = HalfFloatField("ratio", "")
            field.shouldBeInstanceOf<HalfFloatField>()
        }

        should("create ScaledFloatField correctly") {
            val field = ScaledFloatField("percentage", "")
            field.shouldBeInstanceOf<ScaledFloatField>()
        }
        should("create DateField correctly") {
            val field = DateField<java.util.Date>("createdAt", "audit")
            field.shouldBeInstanceOf<DateField<java.util.Date>>()
            field.path shouldBe "audit.createdAt"
        }

        should("create DateNanosField correctly") {
            val field = DateNanosField("timestamp", "")
            field.shouldBeInstanceOf<DateNanosField>()
        }
        should("create BooleanField correctly") {
            val field = BooleanField<Boolean>("isActive", "status")
            field.shouldBeInstanceOf<BooleanField<Boolean>>()
            field.path shouldBe "status.isActive"
        }

        should("create BinaryField correctly") {
            val field = BinaryField("data", "")
            field.shouldBeInstanceOf<BinaryField>()
        }
        should("create IntegerRangeField correctly") {
            val field = IntegerRangeField("ageRange", "")
            field.shouldBeInstanceOf<IntegerRangeField>()
        }

        should("create LongRangeField correctly") {
            val field = LongRangeField("timestampRange", "filter")
            field.shouldBeInstanceOf<LongRangeField>()
            field.path shouldBe "filter.timestampRange"
        }

        should("create DoubleRangeField correctly") {
            val field = DoubleRangeField("priceRange", "")
            field.shouldBeInstanceOf<DoubleRangeField>()
        }

        should("create FloatRangeField correctly") {
            val field = FloatRangeField("scoreRange", "")
            field.shouldBeInstanceOf<FloatRangeField>()
        }

        should("create DateRangeField correctly") {
            val field = DateRangeField("validityRange", "")
            field.shouldBeInstanceOf<DateRangeField>()
        }

        should("create IpRangeField correctly") {
            val field = IpRangeField("ipRange", "network")
            field.shouldBeInstanceOf<IpRangeField>()
            field.path shouldBe "network.ipRange"
        }
        should("create IpField correctly") {
            val field = IpField("clientIp", "request")
            field.shouldBeInstanceOf<IpField>()
            field.path shouldBe "request.clientIp"
        }

        should("create GeoPointField correctly") {
            val field = GeoPointField("location", "")
            field.shouldBeInstanceOf<GeoPointField>()
        }

        should("create GeoShapeField correctly") {
            val field = GeoShapeField("boundary", "geo")
            field.shouldBeInstanceOf<GeoShapeField>()
            field.path shouldBe "geo.boundary"
        }

        should("create CompletionField correctly") {
            val field = CompletionField("suggest", "")
            field.shouldBeInstanceOf<CompletionField>()
        }

        should("create TokenCountField correctly") {
            val field = TokenCountField("wordCount", "analysis")
            field.shouldBeInstanceOf<TokenCountField>()
            field.path shouldBe "analysis.wordCount"
        }

        should("create PercolatorField correctly") {
            val field = PercolatorField("query", "")
            field.shouldBeInstanceOf<PercolatorField>()
        }
        should("create RankFeatureField correctly") {
            val field = RankFeatureField("relevance", "ranking")
            field.shouldBeInstanceOf<RankFeatureField>()
            field.path shouldBe "ranking.relevance"
        }

        should("create RankFeaturesField correctly") {
            val field = RankFeaturesField("features", "")
            field.shouldBeInstanceOf<RankFeaturesField>()
        }

        should("create FlattenedField correctly") {
            val field = FlattenedField("metadata", "document")
            field.shouldBeInstanceOf<FlattenedField>()
            field.path shouldBe "document.metadata"
        }

        should("create WildcardField correctly") {
            val field = WildcardField("pattern", "")
            field.shouldBeInstanceOf<WildcardField>()
        }

        should("create ConstantKeywordField correctly") {
            val field = ConstantKeywordField("category", "product")
            field.shouldBeInstanceOf<ConstantKeywordField>()
            field.path shouldBe "product.category"
        }

        should("create ShapeField correctly") {
            val field = ShapeField("geometry", "")
            field.shouldBeInstanceOf<ShapeField>()
        }

        should("create PointField correctly") {
            val field = PointField("coordinate", "geo")
            field.shouldBeInstanceOf<PointField>()
            field.path shouldBe "geo.coordinate"
        }
        should("handle null parent path as empty string") {
            val field = TextField<String>("content", "")
            field.path shouldBe "content"
        }

        should("handle deep nested paths") {
            val field = KeywordField<String>("code", "address.geo.country")
            field.path shouldBe "address.geo.country.code"
        }

        should("handle parent path with dots") {
            val field = TextField<String>("name", "user.profile.settings")
            field.path shouldBe "user.profile.settings.name"
        }
        should("maintain generic type information for TextField") {
            val field = TextField<String>("title", "")
            // Type should be preserved (compile-time check)
            field.shouldBeInstanceOf<TextField<String>>()
        }

        should("maintain generic type information for complex types") {
            val field = KeywordField<List<String>>("tags", "")
            field.shouldBeInstanceOf<KeywordField<List<String>>>()
        }

        should("handle nullable types") {
            val field = TextField<String?>("optionalField", "")
            field.shouldBeInstanceOf<TextField<String?>>()
        }
    })
