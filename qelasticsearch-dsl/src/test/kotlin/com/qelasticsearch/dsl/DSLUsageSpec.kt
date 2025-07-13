package com.qelasticsearch.dsl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

/**
 * Comprehensive test suite for DSL usage patterns and delegate functions.
 * Tests the actual user-facing DSL syntax and delegate property creation.
 */
class DSLUsageSpec :
    ShouldSpec(
        {

            should("create text field via delegate") {
                val index =
                    object : Index("test") {
                        val title by text<String>()
                        val content by text<String>()
                    }

                index.title.shouldBeInstanceOf<TextField<String>>()
                index.content.shouldBeInstanceOf<TextField<String>>()
                index.title.path() shouldBe "title"
                index.content.path() shouldBe "content"
            }

            should("create keyword field via delegate") {
                val index =
                    object : Index("test") {
                        val status by keyword<String>()
                        val category by keyword<String>()
                    }

                index.status.shouldBeInstanceOf<KeywordField<String>>()
                index.category.shouldBeInstanceOf<KeywordField<String>>()
            }

            should("handle generic types with delegates") {
                val index =
                    object : Index("test") {
                        val tags by keyword<List<String>>()
                        val metadata by keyword<Map<String, String>>()
                    }

                index.tags.shouldBeInstanceOf<KeywordField<List<String>>>()
                index.metadata.shouldBeInstanceOf<KeywordField<Map<String, String>>>()
            }
            should("create all numeric field types via delegates") {
                val index =
                    object : Index("metrics") {
                        val count by long<Long>()
                        val views by integer<Int>()
                        val ratio by double<Double>()
                        val score by float<Float>()
                        val precision by halfFloat()
                        val percentage by scaledFloat()
                        val port by short<Short>()
                        val flag by byte<Byte>()
                    }

                index.count.shouldBeInstanceOf<LongField<Long>>()
                index.views.shouldBeInstanceOf<IntegerField<Int>>()
                index.ratio.shouldBeInstanceOf<DoubleField<Double>>()
                index.score.shouldBeInstanceOf<FloatField<Float>>()
                index.precision.shouldBeInstanceOf<HalfFloatField>()
                index.percentage.shouldBeInstanceOf<ScaledFloatField>()
                index.port.shouldBeInstanceOf<ShortField<Short>>()
                index.flag.shouldBeInstanceOf<ByteField<Byte>>()
            }
            should("create date field types via delegates") {
                val index =
                    object : Index("events") {
                        val createdAt by date<Date>()
                        val timestamp by dateNanos()
                    }

                index.createdAt.shouldBeInstanceOf<DateField<Date>>()
                index.timestamp.shouldBeInstanceOf<DateNanosField>()
            }
            should("create all range field types via delegates") {
                val index =
                    object : Index("ranges") {
                        val ageRange by integerRange()
                        val timeRange by longRange()
                        val priceRange by doubleRange()
                        val scoreRange by floatRange()
                        val dateRange by dateRange()
                        val ipRange by ipRange()
                    }

                index.ageRange.shouldBeInstanceOf<IntegerRangeField>()
                index.timeRange.shouldBeInstanceOf<LongRangeField>()
                index.priceRange.shouldBeInstanceOf<DoubleRangeField>()
                index.scoreRange.shouldBeInstanceOf<FloatRangeField>()
                index.dateRange.shouldBeInstanceOf<DateRangeField>()
                index.ipRange.shouldBeInstanceOf<IpRangeField>()
            }
            should("create specialized field types via delegates") {
                val index =
                    object : Index("specialized") {
                        val isActive by boolean<Boolean>()
                        val data by binary()
                        val clientIp by ip()
                        val location by geoPoint()
                        val boundary by geoShape()
                        val suggest by completion()
                        val wordCount by tokenCount()
                        val query by percolator()
                        val relevance by rankFeature()
                        val features by rankFeatures()
                        val metadata by flattened()
                        val pattern by wildcard()
                        val category by constantKeyword()
                        val geometry by shape()
                        val coordinate by point()
                    }

                index.isActive.shouldBeInstanceOf<BooleanField<Boolean>>()
                index.data.shouldBeInstanceOf<BinaryField>()
                index.clientIp.shouldBeInstanceOf<IpField>()
                index.location.shouldBeInstanceOf<GeoPointField>()
                index.boundary.shouldBeInstanceOf<GeoShapeField>()
                index.suggest.shouldBeInstanceOf<CompletionField>()
                index.wordCount.shouldBeInstanceOf<TokenCountField>()
                index.query.shouldBeInstanceOf<PercolatorField>()
                index.relevance.shouldBeInstanceOf<RankFeatureField>()
                index.features.shouldBeInstanceOf<RankFeaturesField>()
                index.metadata.shouldBeInstanceOf<FlattenedField>()
                index.pattern.shouldBeInstanceOf<WildcardField>()
                index.category.shouldBeInstanceOf<ConstantKeywordField>()
                index.geometry.shouldBeInstanceOf<ShapeField>()
                index.coordinate.shouldBeInstanceOf<PointField>()
            }
            should("create object fields via delegate") {
                class AddressFields(
                    name: String,
                ) : ObjectField(name) {
                    val street by text<String>()
                    val city by text<String>()
                    val zipCode by keyword<String>()
                }

                val index =
                    object : Index("person") {
                        val name by text<String>()
                        val address by objectField<AddressFields>()
                    }

                index.address.shouldBeInstanceOf<AddressFields>()
                index.address.street.shouldBeInstanceOf<TextField<String>>()
                index.address.city.shouldBeInstanceOf<TextField<String>>()
                index.address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
            }

            should("create object multifield via delegate") {
                class SomeMultiField(
                    name: String,
                ) : MultiField<TextField<String>>(TextField(name)) {
                    val search by text<String>()
                    val description by text<String>()
                }

                val index =
                    object : Index("person") {
                        val name by text<String>()
                        val multiField by multiField<SomeMultiField>()
                    }

                index.multiField.shouldBeInstanceOf<MultiField<TextField<*>>>()
                index.multiField.path() shouldBe "multiField"
                index.multiField.search.path() shouldBe "multiField.search"
                index.multiField.description.path() shouldBe "multiField.description"
            }

            should("create nested fields via delegate") {
                class TagFields(
                    name: String,
                ) : ObjectField(name) {
                    val name by keyword<String>()
                    val weight by integer<Int>()
                }

                val index =
                    object : Index("article") {
                        val title by text<String>()
                        val tags by nestedField<TagFields>()
                    }

                index.tags.shouldBeInstanceOf<TagFields>()
                index.tags.name.shouldBeInstanceOf<KeywordField<String>>()
                index.tags.weight.shouldBeInstanceOf<IntegerField<Int>>()
            }
            should("support complex e-commerce document structure") {
                class PriceFields(
                    name: String,
                ) : ObjectField(name) {
                    val amount by double<Double>()
                    val currency by keyword<String>()
                    val discount by double<Double>()
                }

                class CategoryFields(
                    name: String,
                ) : ObjectField(name) {
                    val id by keyword<String>()
                    val name by text<String>()
                    val level by integer<Int>()
                }

                class ReviewFields(
                    name: String,
                ) : ObjectField(name) {
                    val rating by float<Float>()
                    val comment by text<String>()
                    val reviewer by keyword<String>()
                    val reviewDate by date<Date>()
                }

                val product =
                    object : Index("products") {
                        val title by text<String>()
                        val description by text<String>()
                        val sku by keyword<String>()
                        val price by objectField<PriceFields>()
                        val category by objectField<CategoryFields>()
                        val tags by keyword<List<String>>()
                        val inStock by boolean<Boolean>()
                        val stockCount by integer<Int>()
                        val reviews by nestedField<ReviewFields>()
                        val createdAt by date<Date>()
                        val updatedAt by date<Date>()
                    }

                // Verify structure
                product.title.shouldBeInstanceOf<TextField<String>>()
                product.price.amount.shouldBeInstanceOf<DoubleField<Double>>()
                product.category.name.shouldBeInstanceOf<TextField<String>>()
                product.reviews.rating.shouldBeInstanceOf<FloatField<Float>>()

                // Verify paths
                product.title.path() shouldBe "title"
                product.price.amount
                    .path() shouldBe "price.amount"
                product.category.name
                    .path() shouldBe "category.name"
                product.reviews.comment
                    .path() shouldBe "reviews.comment"
            }

            should("support user management document structure") {
                class PermissionsFields(
                    name: String,
                ) : ObjectField(name) {
                    val read by boolean<Boolean>()
                    val write by boolean<Boolean>()
                    val admin by boolean<Boolean>()
                }

                class ProfileFields(
                    name: String,
                ) : ObjectField(name) {
                    val firstName by text<String>()
                    val lastName by text<String>()
                    val bio by text<String>()
                    val avatar by keyword<String>()
                    val permissions by objectField<PermissionsFields>()
                }

                class ActivityFields(
                    name: String,
                ) : ObjectField(name) {
                    val action by keyword<String>()
                    val timestamp by date<Date>()
                    val ipAddress by ip()
                    val userAgent by text<String>()
                }

                val user =
                    object : Index("users") {
                        val email by keyword<String>()
                        val username by keyword<String>()
                        val profile by objectField<ProfileFields>()
                        val isActive by boolean<Boolean>()
                        val roles by keyword<List<String>>()
                        val lastLogin by date<Date>()
                        val activities by nestedField<ActivityFields>()
                        val createdAt by date<Date>()
                    }

                // Test deep nesting
                user.profile.permissions.admin
                    .path() shouldBe "profile.permissions.admin"
                user.activities.ipAddress
                    .path() shouldBe "activities.ipAddress"

                // Test field types
                user.profile.permissions.admin
                    .shouldBeInstanceOf<BooleanField<Boolean>>()
                user.activities.ipAddress.shouldBeInstanceOf<IpField>()
            }

            // Data-driven test for all field types
            data class FieldTestCase(
                val name: String,
                val factory: () -> Field,
                val expectedType: String,
            )

//        should("create all field types correctly with factory methods") {
//            val testCases =
//                listOf(
//                    FieldTestCase("text", { TextField<String>("test") }, "TextField"),
//                    FieldTestCase("keyword", { KeywordField<String>("test") }, "KeywordField"),
//                    FieldTestCase("long", { LongField<Long>("test") }, "LongField"),
//                    FieldTestCase("integer", { IntegerField<Int>("test") }, "IntegerField"),
//                    FieldTestCase("short", { ShortField<Short>("test") }, "ShortField"),
//                    FieldTestCase("byte", { ByteField<Byte>("test") }, "ByteField"),
//                    FieldTestCase("double", { DoubleField<Double>("test") }, "DoubleField"),
//                    FieldTestCase("float", { FloatField<Float>("test") }, "FloatField"),
//                    FieldTestCase("halfFloat", { HalfFloatField("test") }, "HalfFloatField"),
//                    FieldTestCase("scaledFloat", { ScaledFloatField("test") }, "ScaledFloatField"),
//                    FieldTestCase("date", { DateField<java.util.Date>("test") }, "DateField"),
//                    FieldTestCase("dateNanos", { DateNanosField("test") }, "DateNanosField"),
//                    FieldTestCase("boolean", { BooleanField<Boolean>("test") }, "BooleanField"),
//                    FieldTestCase("binary", { BinaryField("test") }, "BinaryField"),
//                    FieldTestCase("ip", { IpField("test") }, "IpField"),
//                    FieldTestCase("geoPoint", { GeoPointField("test") }, "GeoPointField"),
//                    FieldTestCase("geoShape", { GeoShapeField("test") }, "GeoShapeField"),
//                    FieldTestCase("completion", { CompletionField("test") }, "CompletionField"),
//                    FieldTestCase("tokenCount", { TokenCountField("test") }, "TokenCountField"),
//                    FieldTestCase("percolator", { PercolatorField("test") }, "PercolatorField"),
//                    FieldTestCase("rankFeature", { RankFeatureField("test") }, "RankFeatureField"),
//                    FieldTestCase("rankFeatures", { RankFeaturesField("test") }, "RankFeaturesField"),
//                    FieldTestCase("flattened", { FlattenedField("test") }, "FlattenedField"),
//                    FieldTestCase("wildcard", { WildcardField("test") }, "WildcardField"),
//                    FieldTestCase("constantKeyword", { ConstantKeywordField("test") }, "ConstantKeywordField"),
//                    FieldTestCase("shape", { ShapeField("test") }, "ShapeField"),
//                    FieldTestCase("point", { PointField("test") }, "PointField"),
//                    FieldTestCase("integerRange", { IntegerRangeField("test") }, "IntegerRangeField"),
//                    FieldTestCase("floatRange", { FloatRangeField("test") }, "FloatRangeField"),
//                    FieldTestCase("longRange", { LongRangeField("test") }, "LongRangeField"),
//                    FieldTestCase("doubleRange", { DoubleRangeField("test") }, "DoubleRangeField"),
//                    FieldTestCase("dateRange", { DateRangeField("test") }, "DateRangeField"),
//                    FieldTestCase("ipRange", { IpRangeField("test") }, "IpRangeField"),
//                )
//
//            testCases.forEach { testCase ->
//                val field = testCase.factory()
//                field.name() shouldBe "test"
//                field.path().path shouldBe "test"
//                field::class.simpleName shouldBe testCase.expectedType
//            }
//        }

//        should("use property name as field name") {
//            val index =
//                object : Index("test") {
//                    val customFieldName by text<String>()
//                    val anotherField by keyword<String>()
//                }
//
//            index.customFieldName.name() shouldBe "customFieldName"
//            index.anotherField.name() shouldBe "anotherField"
//        }

//        should("handle camelCase property names") {
//            val index =
//                object : Index("test") {
//                    val firstName by text<String>()
//                    val lastName by text<String>()
//                    val dateOfBirth by date<Date>()
//                }
//
//            index.firstName.name() shouldBe "firstName"
//            index.lastName.name() shouldBe "lastName"
//            index.dateOfBirth.name() shouldBe "dateOfBirth"
//        }

//        should("handle snake_case property names") {
//            val index =
//                object : Index("test") {
//                    val first_name by text<String>()
//                    val last_name by text<String>()
//                    val created_at by date<Date>()
//                }
//
//            index.first_name.name() shouldBe "first_name"
//            index.last_name.name() shouldBe "last_name"
//            index.created_at.name() shouldBe "created_at"
//        }
        },
    )
