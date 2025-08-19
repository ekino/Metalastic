package com.qelasticsearch.dsl

import com.qelasticsearch.dsl.delegate.nestedField
import com.qelasticsearch.dsl.delegate.objectField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Comprehensive test suite for Index and ObjectFields functionality. Tests index creation, object
 * field traversal, nested structures, and path construction.
 */
class IndexAndObjectFieldsSpec :
  ShouldSpec({
    should("create Index with correct name and empty path") {
      val index =
        object : Index("test-index") {
          val name by text<String>()
          val age by integer<Int>()
        }

      index.indexName shouldBe "test-index"
    }

    should("create fields with correct paths in Index") {
      val index =
        object : Index("user") {
          val email by keyword<String>()
          val firstName by text<String>()
          val lastLogin by date<java.util.Date>()
        }

      index.email.path() shouldBe "email"
      index.firstName.path() shouldBe "firstName"
      index.lastLogin.path() shouldBe "lastLogin"
    }

    should("maintain field types in Index") {
      val index =
        object : Index("product") {
          val title by text<String>()
          val price by double<Double>()
          val inStock by boolean<Boolean>()
          val category by keyword<String>()
        }

      index.title.shouldBeInstanceOf<TextField<String>>()
      index.price.shouldBeInstanceOf<DoubleField<Double>>()
      index.inStock.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }
    should("create ObjectFields with correct field types") {
      class AddressFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val street by text<String>()
        val city by text<String>()
        val country by keyword<String>()
        val zipCode by keyword<String>()
      }

      val address = AddressFields(null, "address")

      address.street.shouldBeInstanceOf<TextField<String>>()
      address.city.shouldBeInstanceOf<TextField<String>>()
      address.country.shouldBeInstanceOf<KeywordField<String>>()
      address.zipCode.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("handle empty ObjectFields") {
      class EmptyFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested)

      val empty = EmptyFields(null, "empty")
      // Should not throw any exceptions
      empty.shouldBeInstanceOf<ObjectField>()
    }
    should("construct correct paths for simple object fields") {
      class AddressFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val city by text<String>()
        val country by keyword<String>()
      }

      val person =
        object : Index("person") {
          val name by text<String>()
          val address by objectField<AddressFields>()
        }
      person.address.city.path() shouldBe "address.city"
      person.address.country.path() shouldBe "address.country"
    }

    should("handle nested object fields correctly") {
      class LocationFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val latitude by double<Double>()
        val longitude by double<Double>()
      }

      class AddressFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val street by text<String>()
        val city by text<String>()
        val location by objectField<LocationFields>()
      }

      val venue =
        object : Index("venue") {
          val name by text<String>()
          val address by objectField<AddressFields>()
        }

      venue.address.street.path() shouldBe "address.street"
      venue.address.city.path() shouldBe "address.city"
      venue.address.location.latitude.path() shouldBe "address.location.latitude"
      venue.address.location.longitude.path() shouldBe "address.location.longitude"
    }

    should("handle multiple object fields in same index") {
      class ContactFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val email by keyword<String>()
        val phone by keyword<String>()
      }

      class AddressFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val street by text<String>()
        val city by text<String>()
      }

      val person =
        object : Index("person") {
          val name by text<String>()
          val contact by objectField<ContactFields>()
          val address by objectField<AddressFields>()
        }

      person.contact.email.path() shouldBe "contact.email"
      person.contact.phone.path() shouldBe "contact.phone"
      person.address.street.path() shouldBe "address.street"
      person.address.city.path() shouldBe "address.city"
    }
    should("create nested fields correctly") {
      class TagFields(parent: ObjectField?, path: String, nested: Boolean = true) :
        ObjectField(parent, path, nested) {
        val name by keyword<String>()
        val weight by integer<Int>()
      }

      val article =
        object : Index("article") {
          val title by text<String>()
          val tags by nestedField<TagFields>()
        }

      article.tags.name.path() shouldBe "tags.name"
      article.tags.weight.path() shouldBe "tags.weight"
    }

    should("handle nested fields with object fields inside") {
      class LocationFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val city by text<String>()
        val country by keyword<String>()
      }

      class EventFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val name by text<String>()
        val location by objectField<LocationFields>()
      }

      val user =
        object : Index("user") {
          val name by text<String>()
          val events by nestedField<EventFields>()
        }

      user.events.name.path() shouldBe "events.name"
      user.events.location.city.path() shouldBe "events.location.city"
      user.events.location.country.path() shouldBe "events.location.country"
    }
    should("handle deep nesting correctly") {
      class MetricsFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val views by long<Long>()
        val clicks by long<Long>()
        val conversions by double<Double>()
      }

      class AnalyticsFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val period by keyword<String>()
        val metrics by objectField<MetricsFields>()
      }

      class CampaignFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val name by text<String>()
        val budget by double<Double>()
        val analytics by nestedField<AnalyticsFields>()
      }

      val advertiser =
        object : Index("advertiser") {
          val company by text<String>()
          val campaigns by nestedField<CampaignFields>()
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
      class StatisticsFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val count by long<Long>()
        val average by double<Double>()
        val isValid by boolean<Boolean>()
        val lastUpdated by date<java.util.Date>()
        val tags by keyword<List<String>>()
      }

      class DocumentFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val content by text<String>()
        val metadata by flattened()
        val statistics by objectField<StatisticsFields>()
      }

      val search =
        object : Index("search") {
          val query by text<String>()
          val documents by nestedField<DocumentFields>()
          val timestamp by date<java.util.Date>()
        }

      // Verify all field types work in nested context
      search.query.shouldBeInstanceOf<TextField<String>>()
      search.documents.content.shouldBeInstanceOf<TextField<String>>()
      search.documents.metadata.shouldBeInstanceOf<FlattenedField>()
      search.documents.statistics.count.shouldBeInstanceOf<LongField<Long>>()
      search.documents.statistics.average.shouldBeInstanceOf<DoubleField<Double>>()
      search.documents.statistics.isValid.shouldBeInstanceOf<BooleanField<Boolean>>()
      search.documents.statistics.lastUpdated.shouldBeInstanceOf<DateField<java.util.Date>>()
      search.documents.statistics.tags.shouldBeInstanceOf<KeywordField<List<String>>>()
      search.timestamp.shouldBeInstanceOf<DateField<java.util.Date>>()

      // Verify paths are constructed correctly
      search.query.path() shouldBe "query"
      search.documents.content.path() shouldBe "documents.content"
      search.documents.statistics.count.path() shouldBe "documents.statistics.count"
      search.documents.statistics.lastUpdated.path() shouldBe "documents.statistics.lastUpdated"
      search.timestamp.path() shouldBe "timestamp"
    }
    should("handle ObjectFields with same field names in different contexts") {
      class CommonFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val name by text<String>()
        val id by keyword<String>()
      }

      val index =
        object : Index("entity") {
          val name by text<String>() // Same name as in CommonFields
          val id by keyword<String>() // Same name as in CommonFields
          val user by objectField<CommonFields>()
          val group by objectField<CommonFields>()
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
      class TestFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        // Note: This might not be practical in real use, but testing edge case
      }

      val index =
        object : Index("test") {
          val content by text<String>()
        }

      index.content.path() shouldBe "content"
    }
    should("maintain type safety through object traversal") {
      class TypedFields(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val stringField by text<String>()
        val intField by integer<Int>()
        val doubleField by double<Double>()
        val boolField by boolean<Boolean>()
        val listField by keyword<List<String>>()
        val mapField by flattened()
      }

      val index =
        object : Index("typed") {
          val typed by objectField<TypedFields>()
        }

      // Compile-time type safety checks (these should compile without issues)
      index.typed.stringField.shouldBeInstanceOf<TextField<String>>()
      index.typed.intField.shouldBeInstanceOf<IntegerField<Int>>()
      index.typed.doubleField.shouldBeInstanceOf<DoubleField<Double>>()
      index.typed.boolField.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.typed.listField.shouldBeInstanceOf<KeywordField<List<String>>>()
      index.typed.mapField.shouldBeInstanceOf<FlattenedField>()
    }
  })
