package com.ekino.oss.metalastic.core

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.typeOf

/**
 * Basic test suite for core field functionality after delegation removal. Tests basic field
 * creation and path construction without complex delegation patterns.
 */
class BasicFieldsSpec :
  ShouldSpec({
    should("create Index with helper method field initialization") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val name = text<String>("name")
          val age = integer<Int>("age")
          val active = boolean<Boolean>("active")
          val score = double<Double>("score")
          val category = keyword<String>("category")
        }

      // Verify field types
      index.name.shouldBeInstanceOf<TextField<String>>()
      index.age.shouldBeInstanceOf<IntegerField<Int>>()
      index.active.shouldBeInstanceOf<BooleanField<Boolean>>()
      index.score.shouldBeInstanceOf<DoubleField<Double>>()
      index.category.shouldBeInstanceOf<KeywordField<String>>()
    }

    should("construct correct field paths") {
      val index =
        object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {
          val email = keyword<String>("email")
          val firstName = text<String>("firstName")
          val lastLogin = date<java.util.Date>("lastLogin")
        }

      index.email.path() shouldBe "email"
      index.firstName.path() shouldBe "firstName"
      index.lastLogin.path() shouldBe "lastLogin"
    }

    should("create simple ObjectFields with helper method initialization") {
      class SimpleAddress(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val city = text<String>("city")
        val country = keyword<String>("country")
      }

      val address = SimpleAddress(null, "address")

      address.city.shouldBeInstanceOf<TextField<String>>()
      address.country.shouldBeInstanceOf<KeywordField<String>>()
      address.city.path() shouldBe "address.city"
      address.country.path() shouldBe "address.country"
    }

    should("handle nested path construction") {
      class NestedObject(parent: ObjectField<*>?, path: String, nested: Boolean = false) :
        ObjectField<Any>(parent, path, nested, typeOf<Any>()) {
        val field1 = text<String>("field1")
        val field2 = integer<Int>("field2")
      }

      val parent = object : ObjectField<Any>(name = "", fieldType = typeOf<Any>()) {}
      val nestedObj = NestedObject(parent, "nested")

      nestedObj.field1.path() shouldBe "nested.field1"
      nestedObj.field2.path() shouldBe "nested.field2"
    }
  })
