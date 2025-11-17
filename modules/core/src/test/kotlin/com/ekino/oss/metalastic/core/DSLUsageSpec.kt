/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Comprehensive test suite for DSL usage patterns and delegate functions. Tests the actual
 * user-facing DSL syntax and delegate property creation.
 */
class DSLUsageSpec :
  ShouldSpec({
    should("self referencing") {
      class SelfReferenced(
        parent: ObjectField<*>?,
        name: String,
        nested: Boolean = false,
        fieldType: KType,
      ) : ObjectField<Any>(parent = parent, name = name, nested = nested, fieldType = fieldType) {
        val title = text<String>("title")

        val self: SelfReferenced by lazy { SelfReferenced(this, "self", false, typeOf<Any>()) }
      }

      val index = SelfReferenced(null, "", false, typeOf<Any>())
    }

    should("create text field via helper method") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val title = text<String>("title")
          val content = text<String>("content")
        }

      index.title.shouldBeInstanceOf<TextField<String>>()
      index.content.shouldBeInstanceOf<TextField<String>>()
      index.title.path() shouldBe "title"
      index.content.path() shouldBe "content"
    }

    should("create keyword field via helper method") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val status = keyword<String>("status")
          val category = keyword<String>("category")
        }

      index.status.shouldBeInstanceOf<KeywordField<String>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle generic types with delegates") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val tags = KeywordField<List<String>>(this, "tags", typeOf<List<String>>())
          val metadata =
            KeywordField<Map<String, String>>(this, "metadata", typeOf<Map<String, String>>())
        }

      index.tags.shouldBeInstanceOf<KeywordField<List<String>>>()
      index.metadata.shouldBeInstanceOf<KeywordField<Map<String, String>>>()
    }
    should("create all numeric field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val count = LongField<Long>(this, "count", typeOf<Long>())
          val views = IntegerField<Int>(this, "views", typeOf<Int>())
          val ratio = DoubleField<Double>(this, "ratio", typeOf<Double>())
          val score = FloatField<Float>(this, "score", typeOf<Float>())
          val precision = HalfFloatField<Boolean>(this, "precision", typeOf<Boolean>())
          val percentage = ScaledFloatField<Long>(this, "percentage", typeOf<Long>())
          val port = ShortField<Short>(this, "port", typeOf<Short>())
          val flag = ByteField<Byte>(this, "flag", typeOf<Byte>())
        }

      index.count.shouldBeInstanceOf<LongField<Long>>()
      index.views.shouldBeInstanceOf<IntegerField<Int>>()
      index.ratio.shouldBeInstanceOf<DoubleField<Double>>()
      index.score.shouldBeInstanceOf<FloatField<Float>>()
      index.precision.shouldBeInstanceOf<HalfFloatField<Boolean>>()
      index.percentage.shouldBeInstanceOf<ScaledFloatField<Long>>()
      index.port.shouldBeInstanceOf<ShortField<Short>>()
      index.flag.shouldBeInstanceOf<ByteField<Byte>>()
    }
    should("create date field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val createdAt = DateField<Date>(this, "createdAt", typeOf<Date>())
          val timestamp = DateNanosField<Date>(this, "timestamp", typeOf<Date>())
        }

      index.createdAt.shouldBeInstanceOf<DateField<Date>>()
      index.timestamp.shouldBeInstanceOf<DateNanosField<Date>>()
    }
    should("create all range field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val ageRange = IntegerRangeField<String>(this, "ageRange", typeOf<String>())
          val timeRange = LongRangeField<String>(this, "timeRange", typeOf<String>())
          val priceRange = DoubleRangeField<String>(this, "priceRange", typeOf<String>())
          val scoreRange = FloatRangeField<String>(this, "scoreRange", typeOf<String>())
          val dateRange = DateRangeField<String>(this, "dateRange", typeOf<String>())
          val ipRange = IpRangeField<String>(this, "ipRange", typeOf<String>())
        }

      index.ageRange.shouldBeInstanceOf<IntegerRangeField<String>>()
      index.timeRange.shouldBeInstanceOf<LongRangeField<String>>()
      index.priceRange.shouldBeInstanceOf<DoubleRangeField<String>>()
      index.scoreRange.shouldBeInstanceOf<FloatRangeField<String>>()
      index.dateRange.shouldBeInstanceOf<DateRangeField<String>>()
      index.ipRange.shouldBeInstanceOf<IpRangeField<String>>()
    }
    should("create specialized field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val isActive = BooleanField<Boolean>(this, "isActive", typeOf<Boolean>())
          val data = BinaryField<String>(this, "data", typeOf<String>())
          val clientIp = IpField<String>(this, "clientIp", typeOf<String>())
          val suggest = CompletionField<String>(this, "suggest", typeOf<String>())
          val wordCount = TokenCountField<String>(this, "wordCount", typeOf<String>())
          val query = PercolatorField<String>(this, "query", typeOf<String>())
          val relevance = RankFeatureField<String>(this, "relevance", typeOf<String>())
          val features = RankFeatureField<String>(this, "features", typeOf<String>())
          val metadata = FlattenedField<String>(this, "metadata", typeOf<String>())
          val pattern = WildcardField<String>(this, "pattern", typeOf<String>())
          val category = ConstantKeywordField<String>(this, "category", typeOf<String>())
          val geometry = ShapeField<String>(this, "geometry", typeOf<String>())
          val coordinate = PointField<String>(this, "coordinate", typeOf<String>())
        }

      index.isActive.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.data.shouldBeInstanceOf<BinaryField<String>>()
      index.clientIp.shouldBeInstanceOf<IpField<String>>()
      index.suggest.shouldBeInstanceOf<CompletionField<String>>()
      index.wordCount.shouldBeInstanceOf<TokenCountField<String>>()
      index.query.shouldBeInstanceOf<PercolatorField<String>>()
      index.relevance.shouldBeInstanceOf<RankFeatureField<String>>()
      index.features.shouldBeInstanceOf<RankFeatureField<String>>()
      index.metadata.shouldBeInstanceOf<FlattenedField<String>>()
      index.pattern.shouldBeInstanceOf<WildcardField<String>>()
      index.category.shouldBeInstanceOf<ConstantKeywordField<String>>()
      index.geometry.shouldBeInstanceOf<ShapeField<String>>()
      index.coordinate.shouldBeInstanceOf<PointField<String>>()
    }
    should("create object fields via delegate") {
      class AddressFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val street = TextField<String>(this, "street", typeOf<String>())
        val city = TextField<String>(this, "city", typeOf<String>())
        val zipCode = KeywordField<String>(this, "zipCode", typeOf<String>())
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val address = AddressFields(this, "address")
        }

      index.address.shouldBeInstanceOf<AddressFields>()
      index.address.street.shouldBeInstanceOf<TextField<String>>()
      index.address.city.shouldBeInstanceOf<TextField<String>>()
      index.address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("create object multifield via delegate") {
      class SomeMultiField(parent: ObjectField<*>, name: String) :
        MultiField<String, TextField<String>>(
          parent,
          TextField(parent, name, typeOf<String>()),
          typeOf<String>(),
        ) {
        val search = TextField<String>(this, "search", typeOf<String>())
        val description = TextField<String>(this, "description", typeOf<String>())
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val multiField = SomeMultiField(this, "multiField")
        }

      index.multiField.shouldBeInstanceOf<MultiField<String, TextField<*>>>()
      index.multiField.path() shouldBe "multiField"
      index.multiField.mainField().shouldBeInstanceOf<TextField<String>>()
      index.multiField.search.path() shouldBe "multiField.search"
      index.multiField.description.path() shouldBe "multiField.description"
    }

    should("create nested fields via delegate") {
      class TagFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val name = KeywordField<String>(this, "name", typeOf<String>())
        val weight = IntegerField<Int>(this, "weight", typeOf<Int>())
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val title = TextField<String>(this, "title", typeOf<String>())
          val tags = TagFields(this, "tags", true)
        }

      index.tags.shouldBeInstanceOf<TagFields>()
      index.tags.name.shouldBeInstanceOf<KeywordField<String>>()
      index.tags.weight.shouldBeInstanceOf<IntegerField<Int>>()
    }
    should("support complex e-commerce document structure") {
      class PriceFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val amount = DoubleField<Double>(this, "amount", typeOf<Double>())
        val currency = KeywordField<String>(this, "currency", typeOf<String>())
        val discount = DoubleField<Double>(this, "discount", typeOf<Double>())
      }

      class CategoryFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val id = KeywordField<String>(this, "id", typeOf<String>())
        val name = TextField<String>(this, "name", typeOf<String>())
        val level = IntegerField<Int>(this, "level", typeOf<Int>())
      }

      class ReviewFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val rating = FloatField<Float>(this, "rating", typeOf<Float>())
        val comment = TextField<String>(this, "comment", typeOf<String>())
        val reviewer = KeywordField<String>(this, "reviewer", typeOf<String>())
        val reviewDate = DateField<Date>(this, "reviewDate", typeOf<Date>())
      }

      val product =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val title = TextField<String>(this, "title", typeOf<String>())
          val description = TextField<String>(this, "description", typeOf<String>())
          val sku = KeywordField<String>(this, "sku", typeOf<String>())
          val price = PriceFields(this, "price")
          val category = CategoryFields(this, "category")
          val tags = KeywordField<List<String>>(this, "tags", typeOf<List<String>>())
          val inStock = BooleanField<Boolean>(this, "inStock", typeOf<Boolean>())
          val stockCount = IntegerField<Int>(this, "stockCount", typeOf<Int>())
          val reviews = ReviewFields(this, "reviews", true)
          val createdAt = DateField<Date>(this, "createdAt", typeOf<Date>())
          val updatedAt = DateField<Date>(this, "updatedAt", typeOf<Date>())
        }

      // Verify structure
      product.title.shouldBeInstanceOf<TextField<String>>()
      product.price.amount.shouldBeInstanceOf<DoubleField<Double>>()
      product.category.name.shouldBeInstanceOf<TextField<String>>()
      product.reviews.rating.shouldBeInstanceOf<FloatField<Float>>()

      // Verify paths
      product.title.path() shouldBe "title"
      product.price.amount.path() shouldBe "price.amount"
      product.category.name.path() shouldBe "category.name"
      product.reviews.comment.path() shouldBe "reviews.comment"
    }

    should("support user management document structure") {
      class PermissionsFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val read = BooleanField<Boolean>(this, "read", typeOf<Boolean>())
        val write = BooleanField<Boolean>(this, "write", typeOf<Boolean>())
        val admin = BooleanField<Boolean>(this, "admin", typeOf<Boolean>())
      }

      class ProfileFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val firstName = TextField<String>(this, "firstName", typeOf<String>())
        val lastName = TextField<String>(this, "lastName", typeOf<String>())
        val bio = TextField<String>(this, "bio", typeOf<String>())
        val avatar = KeywordField<String>(this, "avatar", typeOf<String>())
        val permissions = PermissionsFields(this, "permissions", true)
      }

      class ActivityFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
        val action = KeywordField<String>(this, "action", typeOf<String>())
        val timestamp = DateField<Date>(this, "timestamp", typeOf<Date>())
        val ipAddress = IpField<Any>(this, "ipAddress", typeOf<Any>())
        val userAgent = TextField<String>(this, "userAgent", typeOf<String>())
      }

      val user =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val email = KeywordField<String>(this, "email", typeOf<String>())
          val username = KeywordField<String>(this, "username", typeOf<String>())
          val profile = ProfileFields(this, "profile", true)
          val isActive = BooleanField<Boolean>(this, "isActive", typeOf<Boolean>())
          val roles = KeywordField<List<String>>(this, "roles", typeOf<List<String>>())
          val lastLogin = DateField<Date>(this, "lastLogin", typeOf<Date>())
          val activities = ActivityFields(this, "activities", true)
          val createdAt = DateField<Date>(this, "createdAt", typeOf<Date>())
        }

      // Test deep nesting
      user.profile.permissions.admin.path() shouldBe "profile.permissions.admin"
      user.activities.ipAddress.path() shouldBe "activities.ipAddress"

      // Test field types
      user.profile.permissions.admin.shouldBeInstanceOf<BooleanField<Boolean>>()
      user.activities.ipAddress.shouldBeInstanceOf<IpField<Any>>()

      // Test nested path functionality - corrected semantics
      // Fields within nested objects should be considered nested paths
      user.activities.action.isNestedPath() shouldBe true
      user.profile.permissions.admin.isNestedPath() shouldBe true

      // But nested objects themselves should not be considered nested paths
      user.activities.isNestedPath() shouldBe false
      user.profile.isNestedPath() shouldBe false

      // Regular fields should not be nested paths
      user.email.isNestedPath() shouldBe false

      // Test nested paths collection
      val activityNestedPaths = user.activities.action.nestedPaths().toList()
      activityNestedPaths shouldBe listOf("activities")
    }

    context("Nested Object Functionality") {
      should("thoroughly test nested object functionality with multiple nesting levels") {
        // Create a complex structure with multiple levels of nesting
        class DeepMetricsFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
          ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
          val clicks = LongField<Long>(this, "clicks", typeOf<Long>())
          val impressions = LongField<Long>(this, "impressions", typeOf<Long>())
          val conversionRate = DoubleField<Double>(this, "conversionRate", typeOf<Double>())
        }

        class CampaignAnalyticsFields(
          parent: ObjectField<*>?,
          name: String,
          nested: Boolean = false,
        ) : ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
          val period = KeywordField<String>(this, "period", typeOf<String>())
          val metrics = DeepMetricsFields(this, "metrics") // Regular object field
          val dailyMetrics = DeepMetricsFields(this, "dailyMetrics", true) // Nested field
        }

        class AdvertiserFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
          ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val campaigns = CampaignAnalyticsFields(this, "campaigns", true) // Nested field
        }

        class LocationFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
          ObjectField<Any>(parent, name, nested, typeOf<Any>()) {
          val city = TextField<String>(this, "city", typeOf<String>())
          val country = KeywordField<String>(this, "country", typeOf<String>())
          val advertisers = AdvertiserFields(this, "advertisers", true) // Nested field
        }

        val complexDocument =
          object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
            val documentId = KeywordField<String>(this, "documentId", typeOf<String>())
            val timestamp = DateField<Date>(this, "timestamp", typeOf<Date>())
            val location = LocationFields(this, "location") // Regular object field
            val globalAdvertisers =
              AdvertiserFields(this, "globalAdvertisers", true) // Nested field at root
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
        complexDocument.location.advertisers.name.isNestedPath() shouldBe true
        complexDocument.location.advertisers.campaigns.isNestedPath() shouldBe true
        complexDocument.globalAdvertisers.name.isNestedPath() shouldBe true
        complexDocument.globalAdvertisers.campaigns.isNestedPath() shouldBe true

        // Fields within nested objects at deeper levels - should be nested paths
        complexDocument.location.advertisers.campaigns.period.isNestedPath() shouldBe true
        complexDocument.location.advertisers.campaigns.metrics.clicks.isNestedPath() shouldBe true
        complexDocument.location.advertisers.campaigns.dailyMetrics.clicks.isNestedPath() shouldBe
          true

        // Test nestedPaths() collection at different levels

        // Fields in single-level nested structure
        val singleLevelPaths = complexDocument.globalAdvertisers.name.nestedPaths().toList()
        singleLevelPaths shouldBe listOf("globalAdvertisers")

        // Fields in two-level nested structure (location.advertisers.*)
        val twoLevelPaths = complexDocument.location.advertisers.name.nestedPaths().toList()
        twoLevelPaths shouldBe listOf("location.advertisers")

        // Fields in three-level nested structure (location.advertisers.campaigns.*)
        val threeLevelPaths =
          complexDocument.location.advertisers.campaigns.period.nestedPaths().toList()
        threeLevelPaths shouldBe listOf("location.advertisers.campaigns", "location.advertisers")

        // Fields in mixed nested structure (regular object + nested + nested)
        val mixedPaths =
          complexDocument.location.advertisers.campaigns.metrics.clicks.nestedPaths().toList()
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
        complexDocument.location.advertisers.campaigns.dailyMetrics.clicks.parent() shouldBe
          complexDocument.location.advertisers.campaigns.dailyMetrics
        complexDocument.location.advertisers.campaigns.dailyMetrics.parent() shouldBe
          complexDocument.location.advertisers.campaigns
        complexDocument.location.advertisers.campaigns.parent() shouldBe
          complexDocument.location.advertisers
        complexDocument.location.advertisers.parent() shouldBe complexDocument.location
        complexDocument.location.parent() shouldBe complexDocument
        complexDocument.parent() shouldBe null // Root index has no parent

        // Test parents() sequence
        val allParents =
          complexDocument.location.advertisers.campaigns.dailyMetrics.clicks.parents().toList()
        allParents.size shouldBe 5
        allParents[0] shouldBe complexDocument.location.advertisers.campaigns.dailyMetrics
        allParents[1] shouldBe complexDocument.location.advertisers.campaigns
        allParents[2] shouldBe complexDocument.location.advertisers
        allParents[3] shouldBe complexDocument.location
        allParents[4] shouldBe complexDocument

        // Test path construction
        complexDocument.location.advertisers.campaigns.dailyMetrics.clicks.path() shouldBe
          "location.advertisers.campaigns.dailyMetrics.clicks"
        complexDocument.globalAdvertisers.campaigns.period.path() shouldBe
          "globalAdvertisers.campaigns.period"
      }
    }
  })
