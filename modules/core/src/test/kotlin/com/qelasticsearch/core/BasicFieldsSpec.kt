package com.qelasticsearch.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Basic test suite for core field functionality after delegation removal. Tests basic field
 * creation and path construction without complex delegation patterns.
 */
class BasicFieldsSpec :
  ShouldSpec({
    should("create Index with helper method field initialization") {
      val index =
        object : Index("test-index") {
          val name = textField<String>("name")
          val age = integerField<Int>("age")
          val active = booleanField<Boolean>("active")
          val score = doubleField<Double>("score")
          val category = keywordField<String>("category")
        }

      index.indexName() shouldBe "test-index"

      // Verify field types
      index.name.shouldBeInstanceOf<TextField<String>>()
      index.age.shouldBeInstanceOf<IntegerField<Int>>()
      index.active.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.score.shouldBeInstanceOf<DoubleField<Double>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("construct correct field paths") {
      val index =
        object : Index("user") {
          val email = keywordField<String>("email")
          val firstName = textField<String>("firstName")
          val lastLogin = dateField<java.util.Date>("lastLogin")
        }

      index.email.path() shouldBe "email"
      index.firstName.path() shouldBe "firstName"
      index.lastLogin.path() shouldBe "lastLogin"
    }

    should("create simple ObjectFields with helper method initialization") {
      class SimpleAddress(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val city = textField<String>("city")
        val country = keywordField<String>("country")
      }

      val address = SimpleAddress(null, "address")

      address.city.shouldBeInstanceOf<TextField<String>>()
      address.country.shouldBeInstanceOf<KeywordField<String>>()
      address.city.path() shouldBe "address.city"
      address.country.path() shouldBe "address.country"
    }

    should("handle nested path construction") {
      class NestedObject(parent: ObjectField?, path: String, nested: Boolean = false) :
        ObjectField(parent, path, nested) {
        val field1 = textField<String>("field1")
        val field2 = integerField<Int>("field2")
      }

      val parent = object : Index("root") {}
      val nestedObj = NestedObject(parent, "nested")

      nestedObj.field1.path() shouldBe "nested.field1"
      nestedObj.field2.path() shouldBe "nested.field2"
    }
  })
