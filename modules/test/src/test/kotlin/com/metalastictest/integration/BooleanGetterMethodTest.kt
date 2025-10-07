package com.metalastictest.integration

import com.metalastic.core.BooleanField
import com.metalastic.integration.MetaIndexPerson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.typeOf

class BooleanGetterMethodTest :
  ShouldSpec({
    context("Boolean getter method processing") {
      should("break") { MetaIndexPerson.indexPerson }
      should("generate Q-class fields from boolean isXxx() getter methods") {
        // Get all declared fields - @JvmField makes properties accessible as Java fields
        val publicFields =
          MetaBooleanTestItem::class.java.declaredFields.filter { field ->
            java.lang.reflect.Modifier.isPublic(field.modifiers) &&
              !java.lang.reflect.Modifier.isStatic(field.modifiers) &&
              !field.name.contains("\$") // Exclude synthetic fields
          }

        // Verify that BooleanTestItem interface annotated methods are processed into Q-class
        // properties
        publicFields.size shouldBe 4

        // Extract property names from field names
        val fieldNames = publicFields.map { it.name }.toSet()
        fieldNames shouldBe setOf("enabled", "verified", "published", "someOtherMethod")
      }

      should("generate correct property names from annotated methods") {
        // isEnabled() -> enabled (is prefix removed)
        // isVerified() -> verified (is prefix removed)
        // isPublished() -> published (is prefix removed)
        // someOtherMethod() -> someOtherMethod (no prefix, used as-is)
        val item = MetaBooleanTestItem<Any>(null, "", false, typeOf<Any>())

        // Verify property names match expected pattern
        item.enabled.path() shouldBe "enabled"
        item.verified.path() shouldBe "verified"
        item.published.path() shouldBe "published"
        item.someOtherMethod.path() shouldBe "someOtherMethod"
      }

      should("generate correct field types from annotated methods") {
        val booleanItem = MetaBooleanTestItem<Any>(null, "test", false, typeOf<Any>())

        // Verify field types match annotated method return types
        booleanItem.enabled.shouldBeInstanceOf<BooleanField<Boolean>>()
        booleanItem.verified.shouldBeInstanceOf<BooleanField<Boolean>>()
        booleanItem.published.shouldBeInstanceOf<BooleanField<Boolean>>()
        booleanItem.someOtherMethod.shouldBeInstanceOf<BooleanField<Boolean>>()
      }

      should("ignore non-annotated boolean getter methods") {
        // Verify that isHidden() method without @Field annotation is ignored
        val fieldNames = MetaBooleanTestItem::class.java.declaredFields.map { it.name }.toSet()
        fieldNames.contains("hidden") shouldBe false
        fieldNames.contains("hidden\$delegate") shouldBe false
      }

      should("integrate boolean fields in document Q-classes") {
        // Verify the document Q-class properly references the boolean item Q-class
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems::class
          .java
          .simpleName shouldBe "MetaBooleanTestItem"
      }

      should("support nested path information for boolean fields") {
        // Verify nested boolean field paths
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.enabled.path() shouldBe
          "booleanItems.enabled"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.verified
          .path() shouldBe "booleanItems.verified"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.published
          .path() shouldBe "booleanItems.published"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.someOtherMethod
          .path() shouldBe "booleanItems.someOtherMethod"
      }

      should("support path traversal for boolean interface fields") {
        // Verify path construction works for boolean interface fields
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.enabled.path() shouldBe
          "booleanItems.enabled"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.verified
          .path() shouldBe "booleanItems.verified"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.published
          .path() shouldBe "booleanItems.published"
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.someOtherMethod
          .path() shouldBe "booleanItems.someOtherMethod"
      }

      should("support nested field path information for boolean fields") {
        // Verify nested field path information
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.enabled
          .isNestedPath() shouldBe true
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.enabled
          .nestedPaths()
          .count() shouldBe 1
        MetaBooleanGetterTestDocument.booleanGetterTestDocument.booleanItems.enabled
          .nestedPaths()
          .first() shouldBe "booleanItems"
      }

      should("support both getXxx() and isXxx() patterns") {
        // Verify TestItem still works (getActive -> active)
        val testItem = MetaTestItem<Any>(null, "", false, typeOf<Any>())
        testItem.active.path() shouldBe "active"
        testItem.active.shouldBeInstanceOf<BooleanField<Boolean>>()

        // Verify BooleanTestItem works (isEnabled -> enabled)
        val booleanItem = MetaBooleanTestItem<Any>(null, "", false, typeOf<Any>())
        booleanItem.enabled.path() shouldBe "enabled"
        booleanItem.enabled.shouldBeInstanceOf<BooleanField<Boolean>>()
      }
    }
  })
