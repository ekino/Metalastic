package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class BooleanGetterMethodTest :
  ShouldSpec({
    context("Boolean getter method processing") {
      should("generate Q-class fields from boolean isXxx() getter methods") {
        // Get delegate fields to verify properties exist
        val delegateFields =
          QBooleanTestItem::class.java.declaredFields.filter { field ->
            field.name.endsWith("\$delegate")
          }

        // Verify that BooleanTestItem interface annotated methods are processed into Q-class
        // properties
        delegateFields.size shouldBe 4

        // Extract property names from delegate field names
        val fieldNames = delegateFields.map { it.name.removeSuffix("\$delegate") }.toSet()
        fieldNames shouldBe setOf("enabled", "verified", "published", "someOtherMethod")
      }

      should("generate correct property names from annotated methods") {
        // isEnabled() -> enabled (is prefix removed)
        // isVerified() -> verified (is prefix removed)
        // isPublished() -> published (is prefix removed)
        // someOtherMethod() -> someOtherMethod (no prefix, used as-is)
        val item = QBooleanTestItem(null, "", false)

        // Verify property names match expected pattern
        item.enabled.path() shouldBe "enabled"
        item.verified.path() shouldBe "verified"
        item.published.path() shouldBe "published"
        item.someOtherMethod.path() shouldBe "someOtherMethod"
      }

      should("generate correct field types from annotated methods") {
        val booleanItem = QBooleanTestItem(null, "test", false)

        // Verify field types match annotated method return types
        booleanItem.enabled::class.java.simpleName shouldBe "BooleanField"
        booleanItem.verified::class.java.simpleName shouldBe "BooleanField"
        booleanItem.published::class.java.simpleName shouldBe "BooleanField"
        booleanItem.someOtherMethod::class.java.simpleName shouldBe "BooleanField"
      }

      should("ignore non-annotated boolean getter methods") {
        // Verify that isHidden() method without @Field annotation is ignored
        val fieldNames = QBooleanTestItem::class.java.declaredFields.map { it.name }.toSet()
        fieldNames.contains("hidden") shouldBe false
        fieldNames.contains("hidden\$delegate") shouldBe false
      }

      should("integrate boolean fields in document Q-classes") {
        // Verify the document Q-class properly references the boolean item Q-class
        QBooleanGetterTestDocument.booleanItems::class.java.simpleName shouldBe "QBooleanTestItem"
      }

      should("support nested path information for boolean fields") {
        // Verify nested boolean field paths
        QBooleanGetterTestDocument.booleanItems.enabled.path() shouldBe "booleanItems.enabled"
        QBooleanGetterTestDocument.booleanItems.verified.path() shouldBe "booleanItems.verified"
        QBooleanGetterTestDocument.booleanItems.published.path() shouldBe "booleanItems.published"
        QBooleanGetterTestDocument.booleanItems.someOtherMethod.path() shouldBe
          "booleanItems.someOtherMethod"
      }

      should("support path traversal for boolean interface fields") {
        // Verify path construction works for boolean interface fields
        QBooleanGetterTestDocument.booleanItems.enabled.path() shouldBe "booleanItems.enabled"
        QBooleanGetterTestDocument.booleanItems.verified.path() shouldBe "booleanItems.verified"
        QBooleanGetterTestDocument.booleanItems.published.path() shouldBe "booleanItems.published"
        QBooleanGetterTestDocument.booleanItems.someOtherMethod.path() shouldBe
          "booleanItems.someOtherMethod"
      }

      should("support nested field path information for boolean fields") {
        // Verify nested field path information
        QBooleanGetterTestDocument.booleanItems.enabled.isNestedPath() shouldBe true
        QBooleanGetterTestDocument.booleanItems.enabled.nestedPaths().count() shouldBe 1
        QBooleanGetterTestDocument.booleanItems.enabled.nestedPaths().first() shouldBe
          "booleanItems"
      }

      should("support both getXxx() and isXxx() patterns") {
        // Verify TestItem still works (getActive -> active)
        val testItem = QTestItem(null, "", false)
        testItem.active.path() shouldBe "active"
        testItem.active::class.java.simpleName shouldBe "BooleanField"

        // Verify BooleanTestItem works (isEnabled -> enabled)
        val booleanItem = QBooleanTestItem(null, "", false)
        booleanItem.enabled.path() shouldBe "enabled"
        booleanItem.enabled::class.java.simpleName shouldBe "BooleanField"
      }
    }
  })
