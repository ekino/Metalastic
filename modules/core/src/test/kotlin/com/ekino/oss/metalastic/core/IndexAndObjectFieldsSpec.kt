package com.ekino.oss.metalastic.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date
import kotlin.reflect.typeOf

/**
 * Comprehensive test suite for Index and ObjectFields functionality. Tests index creation, object
 * field traversal, nested structures, and path construction.
 */
class IndexAndObjectFieldsSpec :
  ShouldSpec({
    should("create fields with correct paths in Index") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val email = KeywordField<String>(this, "email", typeOf<String>())
          val firstName = TextField<String>(this, "firstName", typeOf<String>())
          val lastLogin = DateField<Date>(this, "lastLogin", typeOf<Date>())
        }

      index.email.path() shouldBe "email"
      index.firstName.path() shouldBe "firstName"
      index.lastLogin.path() shouldBe "lastLogin"
    }

    should("maintain field types in Index") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val title = TextField<String>(this, "title", typeOf<String>())
          val price = DoubleField<Double>(this, "price", typeOf<Double>())
          val inStock = BooleanField<Boolean>(this, "inStock", typeOf<Boolean>())
          val category = KeywordField<String>(this, "category", typeOf<String>())
        }

      index.title.shouldBeInstanceOf<TextField<String>>()
      index.price.shouldBeInstanceOf<DoubleField<Double>>()
      index.inStock.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }
    should("create ObjectFields with correct field types") {
      class AddressFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val street = TextField<String>(this, "street", typeOf<String>())
        val city = TextField<String>(this, "city", typeOf<String>())
        val country = KeywordField<String>(this, "country", typeOf<String>())
        val zipCode = KeywordField<String>(this, "zipCode", typeOf<String>())
      }

      val address = AddressFields(null, "address")

      address.street.shouldBeInstanceOf<TextField<String>>()
      address.city.shouldBeInstanceOf<TextField<String>>()
      address.country.shouldBeInstanceOf<KeywordField<String>>()
      address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle empty ObjectFields") {
      class EmptyFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>())

      val empty = EmptyFields(null, "empty")
      // Should not throw any exceptions
      empty.shouldBeInstanceOf<Container<Any>>()
    }
    should("construct correct paths for simple object fields") {
      class AddressFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val city = TextField<String>(this, "city", typeOf<String>())
        val country = KeywordField<String>(this, "country", typeOf<String>())
      }

      val person =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val address = AddressFields(this, "address")
        }
      person.address.city.path() shouldBe "address.city"
      person.address.country.path() shouldBe "address.country"
    }

    should("handle nested object fields correctly") {
      class LocationFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val latitude = DoubleField<Double>(this, "latitude", typeOf<Double>())
        val longitude = DoubleField<Double>(this, "longitude", typeOf<Double>())
      }

      class AddressFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val street = TextField<String>(this, "street", typeOf<String>())
        val city = TextField<String>(this, "city", typeOf<String>())
        val location = LocationFields(this, "location")
      }

      val venue =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val address = AddressFields(this, "address")
        }

      venue.address.street.path() shouldBe "address.street"
      venue.address.city.path() shouldBe "address.city"
      venue.address.location.latitude.path() shouldBe "address.location.latitude"
      venue.address.location.longitude.path() shouldBe "address.location.longitude"
    }

    should("handle multiple object fields in same index") {
      class ContactFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val email = KeywordField<String>(this, "email", typeOf<String>())
        val phone = KeywordField<String>(this, "phone", typeOf<String>())
      }

      class AddressFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val street = TextField<String>(this, "street", typeOf<String>())
        val city = TextField<String>(this, "city", typeOf<String>())
      }

      val person =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val contact = ContactFields(this, "contact")
          val address = AddressFields(this, "address")
        }

      person.contact.email.path() shouldBe "contact.email"
      person.contact.phone.path() shouldBe "contact.phone"
      person.address.street.path() shouldBe "address.street"
      person.address.city.path() shouldBe "address.city"
    }
    should("create nested fields correctly") {
      class TagFields(parent: ObjectField<*>?, path: String, nested: Boolean = true) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val name = KeywordField<String>(this, "name", typeOf<String>())
        val weight = IntegerField<Int>(this, "weight", typeOf<Int>())
      }

      val article =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val title = TextField<String>(this, "title", typeOf<String>())
          val tags = TagFields(this, "tags", true)
        }

      article.tags.name.path() shouldBe "tags.name"
      article.tags.weight.path() shouldBe "tags.weight"
    }

    should("handle nested fields with object fields inside") {
      class LocationFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val city = TextField<String>(this, "city", typeOf<String>())
        val country = KeywordField<String>(this, "country", typeOf<String>())
      }

      class EventFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val name = TextField<String>(this, "name", typeOf<String>())
        val location = LocationFields(this, "location")
      }

      val user =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = TextField<String>(this, "name", typeOf<String>())
          val events = EventFields(this, "events", true)
        }

      user.events.name.path() shouldBe "events.name"
      user.events.location.city.path() shouldBe "events.location.city"
      user.events.location.country.path() shouldBe "events.location.country"
    }
    should("handle deep nesting correctly") {
      class MetricsFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val views = LongField<Long>(this, "views", typeOf<Long>())
        val clicks = LongField<Long>(this, "clicks", typeOf<Long>())
        val conversions = DoubleField<Double>(this, "conversions", typeOf<Double>())
      }

      class AnalyticsFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val period = KeywordField<String>(this, "period", typeOf<String>())
        val metrics = MetricsFields(this, "metrics")
      }

      class CampaignFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val name = TextField<String>(this, "name", typeOf<String>())
        val budget = DoubleField<Double>(this, "budget", typeOf<Double>())
        val analytics = AnalyticsFields(this, "analytics", true)
      }

      val advertiser =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val company = TextField<String>(this, "company", typeOf<String>())
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
      class StatisticsFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val count = LongField<Long>(this, "count", typeOf<Long>())
        val average = DoubleField<Double>(this, "average", typeOf<Double>())
        val isValid = BooleanField<Boolean>(this, "isValid", typeOf<Boolean>())
        val lastUpdated = DateField<Date>(this, "lastUpdated", typeOf<Date>())
        val tags = KeywordField<List<String>>(this, "tags", typeOf<List<String>>())
      }

      class DocumentFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val content = TextField<String>(this, "content", typeOf<String>())
        val metadata = FlattenedField<Math>(this, "metadata", typeOf<Math>())
        val statistics = StatisticsFields(this, "statistics")
      }

      val search =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val query = TextField<String>(this, "query", typeOf<String>())
          val documents = DocumentFields(this, "documents", true)
          val timestamp = DateField<Date>(this, "timestamp", typeOf<Date>())
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
      class CommonFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val name = TextField<String>(this, "name", typeOf<String>())
        val id = KeywordField<String>(this, "id", typeOf<String>())
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name =
            TextField<String>(this, "name", typeOf<String>()) // Same name as in CommonFields
          val id =
            KeywordField<String>(this, "id", typeOf<String>()) // Same name as in CommonFields
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
      class TestFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        // Note: This might not be practical in real use, but testing edge case
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val content = TextField<String>(this, "content", typeOf<String>())
        }

      index.content.path() shouldBe "content"
    }
    should("maintain type safety through object traversal") {
      class TypedFields(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val stringField = TextField<String>(this, "stringField", typeOf<String>())
        val intField = IntegerField<Int>(this, "intField", typeOf<Int>())
        val doubleField = DoubleField<Double>(this, "doubleField", typeOf<Double>())
        val boolField = BooleanField<Boolean>(this, "boolField", typeOf<Boolean>())
        val listField = KeywordField<List<String>>(this, "listField", typeOf<List<String>>())
        val mapField = FlattenedField<Short>(this, "mapField", typeOf<Short>())
      }

      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
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
