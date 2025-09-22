package com.qelasticsearch.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

/**
 * Comprehensive test suite for Index and ObjectFields functionality. Tests index creation, object
 * field traversal, nested structures, and path construction.
 */
class IndexAndObjectFieldsSpec :
  ShouldSpec({
    should("create fields with correct paths in Index") {
      val index =
        object : ObjectField<Any>(name = "") {
          val email = KeywordField<String>(this, "email")
          val firstName = TextField<String>(this, "firstName")
          val lastLogin = DateField<Date>(this, "lastLogin")
        }

      index.email.path() shouldBe "email"
      index.firstName.path() shouldBe "firstName"
      index.lastLogin.path() shouldBe "lastLogin"
    }

    should("maintain field types in Index") {
      val index =
        object : ObjectField<Any>(name = "") {
          val title = TextField<String>(this, "title")
          val price = DoubleField<Double>(this, "price")
          val inStock = BooleanField<kotlin.Boolean>(this, "inStock")
          val category = KeywordField<String>(this, "category")
        }

      index.title.shouldBeInstanceOf<TextField<String>>()
      index.price.shouldBeInstanceOf<DoubleField<Double>>()
      index.inStock.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }
    should("create ObjectFields with correct field types") {
      class AddressFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val street = TextField<String>(this, "street")
        val city = TextField<String>(this, "city")
        val country = KeywordField<String>(this, "country")
        val zipCode = KeywordField<String>(this, "zipCode")
      }

      val address = AddressFields(null, "address")

      address.street.shouldBeInstanceOf<TextField<String>>()
      address.city.shouldBeInstanceOf<TextField<String>>()
      address.country.shouldBeInstanceOf<KeywordField<String>>()
      address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle empty ObjectFields") {
      class EmptyFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested)

      val empty = EmptyFields(null, "empty")
      // Should not throw any exceptions
      empty.shouldBeInstanceOf<ObjectField<Any>>()
    }
    should("construct correct paths for simple object fields") {
      class AddressFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val city = TextField<String>(this, "city")
        val country = KeywordField<String>(this, "country")
      }

      val person =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
          val address = AddressFields(this, "address")
        }
      person.address.city.path() shouldBe "address.city"
      person.address.country.path() shouldBe "address.country"
    }

    should("handle nested object fields correctly") {
      class LocationFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val latitude = DoubleField<Double>(this, "latitude")
        val longitude = DoubleField<Double>(this, "longitude")
      }

      class AddressFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val street = TextField<String>(this, "street")
        val city = TextField<String>(this, "city")
        val location = LocationFields(this, "location")
      }

      val venue =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
          val address = AddressFields(this, "address")
        }

      venue.address.street.path() shouldBe "address.street"
      venue.address.city.path() shouldBe "address.city"
      venue.address.location.latitude.path() shouldBe "address.location.latitude"
      venue.address.location.longitude.path() shouldBe "address.location.longitude"
    }

    should("handle multiple object fields in same index") {
      class ContactFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val email = KeywordField<String>(this, "email")
        val phone = KeywordField<String>(this, "phone")
      }

      class AddressFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val street = TextField<String>(this, "street")
        val city = TextField<String>(this, "city")
      }

      val person =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
          val contact = ContactFields(this, "contact")
          val address = AddressFields(this, "address")
        }

      person.contact.email.path() shouldBe "contact.email"
      person.contact.phone.path() shouldBe "contact.phone"
      person.address.street.path() shouldBe "address.street"
      person.address.city.path() shouldBe "address.city"
    }
    should("create nested fields correctly") {
      class TagFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = true) :
        ObjectField<Any>(parent, path, nested) {
        val name = KeywordField<String>(this, "name")
        val weight = IntegerField<Int>(this, "weight")
      }

      val article =
        object : ObjectField<Any>(name = "") {
          val title = TextField<String>(this, "title")
          val tags = TagFields(this, "tags", true)
        }

      article.tags.name.path() shouldBe "tags.name"
      article.tags.weight.path() shouldBe "tags.weight"
    }

    should("handle nested fields with object fields inside") {
      class LocationFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val city = TextField<String>(this, "city")
        val country = KeywordField<String>(this, "country")
      }

      class EventFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val name = TextField<String>(this, "name")
        val location = LocationFields(this, "location")
      }

      val user =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name")
          val events = EventFields(this, "events", true)
        }

      user.events.name.path() shouldBe "events.name"
      user.events.location.city.path() shouldBe "events.location.city"
      user.events.location.country.path() shouldBe "events.location.country"
    }
    should("handle deep nesting correctly") {
      class MetricsFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val views = LongField<Long>(this, "views")
        val clicks = LongField<Long>(this, "clicks")
        val conversions = DoubleField<Double>(this, "conversions")
      }

      class AnalyticsFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val period = KeywordField<String>(this, "period")
        val metrics = MetricsFields(this, "metrics")
      }

      class CampaignFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val name = TextField<String>(this, "name")
        val budget = DoubleField<Double>(this, "budget")
        val analytics = AnalyticsFields(this, "analytics", true)
      }

      val advertiser =
        object : ObjectField<Any>(name = "") {
          val company = TextField<String>(this, "company")
          val campaigns = CampaignFields(this, "campaigns", true)
        }

      // Test deep path construction
      advertiser.campaigns.name.path() shouldBe "campaigns.name"
      advertiser.campaigns.budget.path() shouldBe "campaigns.budget"
      advertiser.campaigns.analytics.period.path() shouldBe "campaigns.analytics.period"
      advertiser.campaigns.analytics.metrics.views.path() shouldBe
        "campaigns.analytics.metrics.views"
      advertiser.campaigns.analytics.metrics.clicks.path() shouldBe
        "campaigns.analytics.metrics.clicks"
      advertiser.campaigns.analytics.metrics.conversions.path() shouldBe
        "campaigns.analytics.metrics.conversions"
    }
    should("handle all field types in nested structures") {
      class StatisticsFields(
        parent: ObjectField<*>?,
        path: String,
        nested: kotlin.Boolean = false,
      ) : ObjectField<Any>(parent, path, nested) {
        val count = LongField<Long>(this, "count")
        val average = DoubleField<Double>(this, "average")
        val isValid = BooleanField<kotlin.Boolean>(this, "isValid")
        val lastUpdated = DateField<java.util.Date>(this, "lastUpdated")
        val tags = KeywordField<List<String>>(this, "tags")
      }

      class DocumentFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val content = TextField<String>(this, "content")
        val metadata = FlattenedField<Math>(this, "metadata")
        val statistics = StatisticsFields(this, "statistics")
      }

      val search =
        object : ObjectField<Any>(name = "") {
          val query = TextField<String>(this, "query")
          val documents = DocumentFields(this, "documents", true)
          val timestamp = DateField<java.util.Date>(this, "timestamp")
        }

      // Verify all field types work in nested context
      search.query.shouldBeInstanceOf<TextField<String>>()
      search.documents.content.shouldBeInstanceOf<TextField<String>>()
      search.documents.metadata.shouldBeInstanceOf<FlattenedField<Math>>()
      search.documents.statistics.count.shouldBeInstanceOf<LongField<Long>>()
      search.documents.statistics.average.shouldBeInstanceOf<DoubleField<Double>>()
      search.documents.statistics.isValid.shouldBeInstanceOf<BooleanField<Boolean>>()
      search.documents.statistics.lastUpdated.shouldBeInstanceOf<DateField<Date>>()
      search.documents.statistics.tags.shouldBeInstanceOf<KeywordField<List<String>>>()
      search.timestamp.shouldBeInstanceOf<DateField<Date>>()

      // Verify paths are constructed correctly
      search.query.path() shouldBe "query"
      search.documents.content.path() shouldBe "documents.content"
      search.documents.statistics.count.path() shouldBe "documents.statistics.count"
      search.documents.statistics.lastUpdated.path() shouldBe "documents.statistics.lastUpdated"
      search.timestamp.path() shouldBe "timestamp"
    }
    should("handle ObjectFields with same field names in different contexts") {
      class CommonFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val name = TextField<String>(this, "name")
        val id = KeywordField<String>(this, "id")
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val name = TextField<String>(this, "name") // Same name as in CommonFields
          val id = KeywordField<String>(this, "id") // Same name as in CommonFields
          val user = CommonFields(this, "user")
          val group = CommonFields(this, "group")
        }

      // Root level fields
      index.name.path() shouldBe "name"
      index.id.path() shouldBe "id"

      // Object fields with same names should have different paths
      index.user.name.path() shouldBe "user.name"
      index.user.id.path() shouldBe "user.id"
      index.group.name.path() shouldBe "group.name"
      index.group.id.path() shouldBe "group.id"
    }

    should("handle empty field names gracefully") {
      class TestFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        // Note: This might not be practical in real use, but testing edge case
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val content = TextField<String>(this, "content")
        }

      index.content.path() shouldBe "content"
    }
    should("maintain type safety through object traversal") {
      class TypedFields(parent: ObjectField<*>?, path: String, nested: kotlin.Boolean = false) :
        ObjectField<Any>(parent, path, nested) {
        val stringField = TextField<String>(this, "stringField")
        val intField = IntegerField<Int>(this, "intField")
        val doubleField = DoubleField<Double>(this, "doubleField")
        val boolField = BooleanField<kotlin.Boolean>(this, "boolField")
        val listField = KeywordField<List<String>>(this, "listField")
        val mapField = FlattenedField<Short>(this, "mapField")
      }

      val index =
        object : ObjectField<Any>(name = "") {
          val typed = TypedFields(this, "typed")
        }

      // Compile-time type safety checks (these should compile without issues)
      index.typed.stringField.shouldBeInstanceOf<TextField<String>>()
      index.typed.intField.shouldBeInstanceOf<IntegerField<Int>>()
      index.typed.doubleField.shouldBeInstanceOf<DoubleField<Double>>()
      index.typed.boolField.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.typed.listField.shouldBeInstanceOf<KeywordField<List<String>>>()
      index.typed.mapField.shouldBeInstanceOf<FlattenedField<Short>>()
    }
  })
