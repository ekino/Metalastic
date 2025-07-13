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
                class AddressFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
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
                class SomeMultiField(parent: ObjectField, name: String) : MultiField<TextField<String>>(parent, TextField(parent, name)) {
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
                index.multiField.mainField().shouldBeInstanceOf<TextField<String>>()
                index.multiField.search.path() shouldBe "multiField.search"
                index.multiField.description.path() shouldBe "multiField.description"
            }

            should("create nested fields via delegate") {
                class TagFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
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
                class PriceFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                    val amount by double<Double>()
                    val currency by keyword<String>()
                    val discount by double<Double>()
                }

                class CategoryFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                    val id by keyword<String>()
                    val name by text<String>()
                    val level by integer<Int>()
                }

                class ReviewFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
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
                class PermissionsFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                    val read by boolean<Boolean>()
                    val write by boolean<Boolean>()
                    val admin by boolean<Boolean>()
                }

                class ProfileFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                    val firstName by text<String>()
                    val lastName by text<String>()
                    val bio by text<String>()
                    val avatar by keyword<String>()
                    val permissions by nestedField<PermissionsFields>()
                }

                class ActivityFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                    val action by keyword<String>()
                    val timestamp by date<Date>()
                    val ipAddress by ip()
                    val userAgent by text<String>()
                }

                val user =
                    object : Index("users") {
                        val email by keyword<String>()
                        val username by keyword<String>()
                        val profile by nestedField<ProfileFields>()
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

                // Test nested path functionality - corrected semantics
                // Fields within nested objects should be considered nested paths
                user.activities.action.isNestedPath() shouldBe true
                user.profile.permissions.admin
                    .isNestedPath() shouldBe true

                // But nested objects themselves should not be considered nested paths
                user.activities.isNestedPath() shouldBe false
                user.profile.isNestedPath() shouldBe false

                // Regular fields should not be nested paths
                user.email.isNestedPath() shouldBe false

                // Test nested paths collection
                val activityNestedPaths =
                    user.activities.action
                        .nestedPaths()
                        .toList()
                activityNestedPaths shouldBe listOf("activities")
            }

            context("Nested Object Functionality") {
                should("thoroughly test nested object functionality with multiple nesting levels") {
                    // Create a complex structure with multiple levels of nesting
                    class DeepMetricsFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                        val clicks by long<Long>()
                        val impressions by long<Long>()
                        val conversionRate by double<Double>()
                    }

                    class CampaignAnalyticsFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                        val period by keyword<String>()
                        val metrics by objectField<DeepMetricsFields>() // Regular object field
                        val dailyMetrics by nestedField<DeepMetricsFields>() // Nested field
                    }

                    class AdvertiserFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                        val name by text<String>()
                        val campaigns by nestedField<CampaignAnalyticsFields>() // Nested field
                    }

                    class LocationFields(parent: ObjectField?, name: String, nested: Boolean = false) : ObjectField(parent, name, nested) {
                        val city by text<String>()
                        val country by keyword<String>()
                        val advertisers by nestedField<AdvertiserFields>() // Nested field
                    }

                    val complexDocument =
                        object : Index("complex_analytics") {
                            val documentId by keyword<String>()
                            val timestamp by date<Date>()
                            val location by objectField<LocationFields>() // Regular object field
                            val globalAdvertisers by nestedField<AdvertiserFields>() // Nested field at root
                        }

                    // Test nested path detection at different levels

                    // Root level fields - should not be nested paths
                    complexDocument.documentId.isNestedPath() shouldBe false
                    complexDocument.timestamp.isNestedPath() shouldBe false

                    // Regular object fields and their children - should not be nested paths
                    complexDocument.location.isNestedPath() shouldBe false
                    complexDocument.location.city.isNestedPath() shouldBe false
                    complexDocument.location.country.isNestedPath() shouldBe false

                    // Nested object containers themselves - should not be nested paths
                    complexDocument.location.advertisers.isNestedPath() shouldBe false
                    complexDocument.globalAdvertisers.isNestedPath() shouldBe false

                    // Fields within nested objects - should be nested paths
                    complexDocument.location.advertisers.name
                        .isNestedPath() shouldBe true
                    complexDocument.location.advertisers.campaigns
                        .isNestedPath() shouldBe true
                    complexDocument.globalAdvertisers.name.isNestedPath() shouldBe true
                    complexDocument.globalAdvertisers.campaigns.isNestedPath() shouldBe true

                    // Fields within nested objects at deeper levels - should be nested paths
                    complexDocument.location.advertisers.campaigns.period
                        .isNestedPath() shouldBe true
                    complexDocument.location.advertisers.campaigns.metrics.clicks
                        .isNestedPath() shouldBe true
                    complexDocument.location.advertisers.campaigns.dailyMetrics.clicks
                        .isNestedPath() shouldBe true

                    // Test nestedPaths() collection at different levels

                    // Fields in single-level nested structure
                    val singleLevelPaths =
                        complexDocument.globalAdvertisers.name
                            .nestedPaths()
                            .toList()
                    singleLevelPaths shouldBe listOf("globalAdvertisers")

                    // Fields in two-level nested structure (location.advertisers.*)
                    val twoLevelPaths =
                        complexDocument.location.advertisers.name
                            .nestedPaths()
                            .toList()
                    twoLevelPaths shouldBe listOf("location.advertisers")

                    // Fields in three-level nested structure (location.advertisers.campaigns.*)
                    val threeLevelPaths =
                        complexDocument.location.advertisers.campaigns.period
                            .nestedPaths()
                            .toList()
                    threeLevelPaths shouldBe listOf("location.advertisers.campaigns", "location.advertisers")

                    // Fields in mixed nested structure (regular object + nested + nested)
                    val mixedPaths =
                        complexDocument.location.advertisers.campaigns.metrics.clicks
                            .nestedPaths()
                            .toList()
                    mixedPaths shouldBe listOf("location.advertisers.campaigns", "location.advertisers")

                    // Fields in complex nested structure (nested + nested + nested)
                    val complexPaths =
                        complexDocument.location.advertisers.campaigns.dailyMetrics.impressions
                            .nestedPaths()
                            .toList()
                    complexPaths shouldBe
                        listOf(
                            "location.advertisers.campaigns.dailyMetrics",
                            "location.advertisers.campaigns",
                            "location.advertisers",
                        )

                    // Test parent() navigation
                    complexDocument.location.advertisers.campaigns.dailyMetrics.clicks
                        .parent() shouldBe
                        complexDocument.location.advertisers.campaigns.dailyMetrics
                    complexDocument.location.advertisers.campaigns.dailyMetrics
                        .parent() shouldBe
                        complexDocument.location.advertisers.campaigns
                    complexDocument.location.advertisers.campaigns
                        .parent() shouldBe complexDocument.location.advertisers
                    complexDocument.location.advertisers.parent() shouldBe complexDocument.location
                    complexDocument.location.parent() shouldBe complexDocument
                    complexDocument.parent() shouldBe null // Root index has no parent

                    // Test parents() sequence
                    val allParents =
                        complexDocument.location.advertisers.campaigns.dailyMetrics.clicks
                            .parents()
                            .toList()
                    allParents.size shouldBe 5
                    allParents[0] shouldBe
                        complexDocument.location.advertisers.campaigns.dailyMetrics
                    allParents[1] shouldBe complexDocument.location.advertisers.campaigns
                    allParents[2] shouldBe complexDocument.location.advertisers
                    allParents[3] shouldBe complexDocument.location
                    allParents[4] shouldBe complexDocument

                    // Test path construction
                    complexDocument.location.advertisers.campaigns.dailyMetrics.clicks
                        .path() shouldBe
                        "location.advertisers.campaigns.dailyMetrics.clicks"
                    complexDocument.globalAdvertisers.campaigns.period
                        .path() shouldBe
                        "globalAdvertisers.campaigns.period"
                }
            }
        },
    )
