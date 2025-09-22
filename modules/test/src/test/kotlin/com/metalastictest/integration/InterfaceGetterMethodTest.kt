package com.metalastictest.integration

import com.metalastic.core.BooleanField
import com.metalastic.core.IntegerField
import com.metalastic.core.KeywordField
import com.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for interface getter method support in QElasticsearch annotation processor.
 *
 * This test verifies that interfaces with @Field annotated getter methods are correctly processed
 * and included in generated Q-classes.
 */
class InterfaceGetterMethodTest :
  ShouldSpec({
    should("generate Q-class fields from interface getter methods") {
      // Get all declared fields - @JvmField makes properties accessible as Java fields
      val publicFields =
        QTestItem::class.java.declaredFields.filter { field ->
          java.lang.reflect.Modifier.isPublic(field.modifiers) &&
            !java.lang.reflect.Modifier.isStatic(field.modifiers) &&
            !field.name.contains("\$") // Exclude synthetic fields
        }

      // Verify that TestItem interface getter methods are processed into Q-class properties
      publicFields.size shouldBe 4

      // Extract property names from field names
      val fieldNames = publicFields.map { it.name }.toSet()
      fieldNames shouldBe setOf("category", "displayName", "priority", "active")
    }

    should("generate correct field types from getter method return types") {
      val testItem = QTestItem<Any>(null, "test", false)

      // Verify field types match getter method return types
      testItem.category.shouldBeInstanceOf<KeywordField<String>>()
      testItem.displayName.shouldBeInstanceOf<TextField<String>>()
      testItem.priority.shouldBeInstanceOf<IntegerField<Int>>()
      testItem.active.shouldBeInstanceOf<BooleanField<Boolean>>()
    }

    should("integrate interface fields in document Q-classes") {
      // Verify that the document Q-class correctly references the interface Q-class
      QGetterMethodTestDocument.getterMethodTestDocument.items::class.java.simpleName shouldBe
        "QTestItem"
    }

    should("support path traversal for interface fields") {
      // Verify path construction works for interface fields
      QGetterMethodTestDocument.getterMethodTestDocument.items.category.path() shouldBe
        "items.category"
      QGetterMethodTestDocument.getterMethodTestDocument.items.displayName.path() shouldBe
        "items.displayName"
      QGetterMethodTestDocument.getterMethodTestDocument.items.priority.path() shouldBe
        "items.priority"
      QGetterMethodTestDocument.getterMethodTestDocument.items.active.path() shouldBe "items.active"
    }

    should("support nested path information") {
      // Verify nested field path information
      QGetterMethodTestDocument.getterMethodTestDocument.items.category.isNestedPath() shouldBe true
      QGetterMethodTestDocument.getterMethodTestDocument.items.category
        .nestedPaths()
        .count() shouldBe 1
      QGetterMethodTestDocument.getterMethodTestDocument.items.category
        .nestedPaths()
        .first() shouldBe "items"
    }

    should("ignore non-annotated getter methods") {
      // Verify that getDescription() method without @Field annotation is ignored
      val fieldNames = QTestItem::class.java.declaredFields.map { it.name }.toSet()
      fieldNames.contains("description") shouldBe false
    }
  })
