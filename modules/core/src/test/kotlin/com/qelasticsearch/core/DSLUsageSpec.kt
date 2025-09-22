package com.qelasticsearch.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

/**
 * Comprehensive test suite for DSL usage patterns and delegate functions. Tests the actual
 * user-facing DSL syntax and delegate property creation.
 */
class DSLUsageSpec :
  ShouldSpec({
    should("create text field via helper method") {
      val index =
        object : ObjectField<Any>(name = "") {
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
        object : ObjectField<Any>(name = "") {
          val status = keyword<String>("status")
          val category = keyword<String>("category")
        }

      index.status.shouldBeInstanceOf<KeywordField<String>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle generic types with delegates") {
      val index =
        object : ObjectField<Any>(name = "") {
          val tags = KeywordField<List<String>>(this, "tags")
          val metadata = KeywordField<Map<String, String>>(this, "metadata")
        }

      index.tags.shouldBeInstanceOf<KeywordField<List<String>>>()
      index.metadata.shouldBeInstanceOf<KeywordField<Map<String, String>>>()
    }
    should("create all numeric field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "") {
          val count = LongField<Long>(this, "count")
          val views = IntegerField<Int>(this, "views")
          val ratio = DoubleField<Double>(this, "ratio")
          val score = FloatField<Float>(this, "score")
          val precision = HalfFloatField<Boolean>(this, "precision")
          val percentage = ScaledFloatField<Long>(this, "percentage")
          val port = ShortField<Short>(this, "port")
          val flag = ByteField<Byte>(this, "flag")
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
        object : ObjectField<Any>(name = "") {
          val createdAt = DateField<Date>(this, "createdAt")
          val timestamp = DateNanosField<Date>(this, "timestamp")
        }

      index.createdAt.shouldBeInstanceOf<DateField<Date>>()
      index.timestamp.shouldBeInstanceOf<DateNanosField<Date>>()
    }
    should("create all range field types via delegates") {
      val index =
        object : ObjectField<Any>(name = "") {
          val ageRange = IntegerRangeField<String>(this, "ageRange")
          val timeRange = LongRangeField<String>(this, "timeRange")
          val priceRange = DoubleRangeField<String>(this, "priceRange")
          val scoreRange = FloatRangeField<String>(this, "scoreRange")
          val dateRange = DateRangeField<String>(this, "dateRange")
          val ipRange = IpRangeField<String>(this, "ipRange")
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
        object : ObjectField<Any>(name = "") {
          val isActive = BooleanField<Boolean>(this, "isActive")
          val data = BinaryField<String>(this, "data")
          val clientIp = IpField<String>(this, "clientIp")
          val suggest = CompletionField<String>(this, "suggest")
          val wordCount = TokenCountField<String>(this, "wordCount")
          val query = PercolatorField<String>(this, "query")
          val relevance = RankFeatureField<String>(this, "relevance")
          val features = RankFeatureField<String>(this, "features")
          val metadata = FlattenedField<String>(this, "metadata")
          val pattern = WildcardField<String>(this, "pattern")
          val category = ConstantKeywordField<String>(this, "category")
          val geometry = ShapeField<String>(this, "geometry")
          val coordinate = PointField<String>(this, "coordinate")
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
        ObjectField<Any>(parent, name, nested) {
        val street = TextField<String>(this, "street")
        val city = TextField<String>(this, "city")
        val zipCode = KeywordField<String>(this, "zipCode")
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
          val address = AddressFields(this, "address")
        }

      index.address.shouldBeInstanceOf<AddressFields>()
      index.address.street.shouldBeInstanceOf<TextField<String>>()
      index.address.city.shouldBeInstanceOf<TextField<String>>()
      index.address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("create object multifield via delegate") {
      class SomeMultiField(parent: ObjectField<*>, name: String) :
        MultiField<String, TextField<String>>(parent, TextField(parent, name)) {
        val search = TextField<String>(this, "search")
        val description = TextField<String>(this, "description")
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
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
        ObjectField<Any>(parent, name, nested) {
        val name = KeywordField<String>(this, "name")
        val weight = IntegerField<Int>(this, "weight")
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val title = TextField<String>(this, "title")
          val tags = TagFields(this, "tags", true)
        }

      index.tags.shouldBeInstanceOf<TagFields>()
      index.tags.name.shouldBeInstanceOf<KeywordField<String>>()
      index.tags.weight.shouldBeInstanceOf<IntegerField<Int>>()
    }
    should("support complex e-commerce document structure") {
      class PriceFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested) {
        val amount = DoubleField<Double>(this, "amount")
        val currency = KeywordField<String>(this, "currency")
        val discount = DoubleField<Double>(this, "discount")
      }

      class CategoryFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested) {
        val id = KeywordField<String>(this, "id")
        val name = TextField<String>(this, "name")
        val level = IntegerField<Int>(this, "level")
      }

      class ReviewFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested) {
        val rating = FloatField<Float>(this, "rating")
        val comment = TextField<String>(this, "comment")
        val reviewer = KeywordField<String>(this, "reviewer")
        val reviewDate = DateField<Date>(this, "reviewDate")
      }

      val product =
        object : ObjectField<Any>(name = "") {
          val title = TextField<String>(this, "title")
          val description = TextField<String>(this, "description")
          val sku = KeywordField<String>(this, "sku")
          val price = PriceFields(this, "price")
          val category = CategoryFields(this, "category")
          val tags = KeywordField<List<String>>(this, "tags")
          val inStock = BooleanField<Boolean>(this, "inStock")
          val stockCount = IntegerField<Int>(this, "stockCount")
          val reviews = ReviewFields(this, "reviews", true)
          val createdAt = DateField<Date>(this, "createdAt")
          val updatedAt = DateField<Date>(this, "updatedAt")
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
      class PermissionsFields(
        parent: ObjectField<*>?,
        name: String,
        nested: kotlin.Boolean = false,
      ) : ObjectField<Any>(parent, name, nested) {
        val read = BooleanField<Boolean>(this, "read")
        val write = BooleanField<Boolean>(this, "write")
        val admin = BooleanField<Boolean>(this, "admin")
      }

      class ProfileFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested) {
        val firstName = TextField<String>(this, "firstName")
        val lastName = TextField<String>(this, "lastName")
        val bio = TextField<String>(this, "bio")
        val avatar = KeywordField<String>(this, "avatar")
        val permissions = PermissionsFields(this, "permissions", true)
      }

      class ActivityFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
        ObjectField<Any>(parent, name, nested) {
        val action = KeywordField<String>(this, "action")
        val timestamp = DateField<Date>(this, "timestamp")
        val ipAddress = IpField<Any>(this, "ipAddress")
        val userAgent = TextField<String>(this, "userAgent")
      }

      val user =
        object : ObjectField<Any>(name = "") {
          val email = KeywordField<String>(this, "email")
          val username = KeywordField<String>(this, "username")
          val profile = ProfileFields(this, "profile", true)
          val isActive = BooleanField<kotlin.Boolean>(this, "isActive")
          val roles = KeywordField<List<String>>(this, "roles")
          val lastLogin = DateField<Date>(this, "lastLogin")
          val activities = ActivityFields(this, "activities", true)
          val createdAt = DateField<Date>(this, "createdAt")
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
          ObjectField<Any>(parent, name, nested) {
          val clicks = LongField<Long>(this, "clicks")
          val impressions = LongField<Long>(this, "impressions")
          val conversionRate = DoubleField<Double>(this, "conversionRate")
        }

        class CampaignAnalyticsFields(
          parent: ObjectField<*>?,
          name: String,
          nested: Boolean = false,
        ) : ObjectField<Any>(parent, name, nested) {
          val period = KeywordField<String>(this, "period")
          val metrics = DeepMetricsFields(this, "metrics") // Regular object field
          val dailyMetrics = DeepMetricsFields(this, "dailyMetrics", true) // Nested field
        }

        class AdvertiserFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
          ObjectField<Any>(parent, name, nested) {
          val name = TextField<String>(this, "name")
          val campaigns = CampaignAnalyticsFields(this, "campaigns", true) // Nested field
        }

        class LocationFields(parent: ObjectField<*>?, name: String, nested: Boolean = false) :
          ObjectField<Any>(parent, name, nested) {
          val city = TextField<String>(this, "city")
          val country = KeywordField<String>(this, "country")
          val advertisers = AdvertiserFields(this, "advertisers", true) // Nested field
        }

        val complexDocument =
          object : ObjectField<Any>(name = "") {
            val documentId = KeywordField<String>(this, "documentId")
            val timestamp = DateField<Date>(this, "timestamp")
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
