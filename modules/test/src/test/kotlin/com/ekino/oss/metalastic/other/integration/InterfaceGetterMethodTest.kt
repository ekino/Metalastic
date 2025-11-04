package com.ekino.oss.metalastic.other.integration

import com.ekino.oss.metalastic.core.BooleanField
import com.ekino.oss.metalastic.core.IntegerField
import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.core.TextField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.typeOf

/**
 * Tests for interface getter method support in Metalastic annotation processor.
 *
 * This test verifies that interfaces with @Field annotated getter methods are correctly processed
 * and included in generated Q-classes.
 */
class InterfaceGetterMethodTest :
  ShouldSpec({
    should("generate Q-class fields from interface getter methods") {
      // Get all declared fields - @JvmField makes properties accessible as Java fields
      val publicFields =
        MetaTestItem::class.java.declaredFields.filter { field ->
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
      val testItem = MetaTestItem<Any>(null, "test", false, typeOf<Any>())

      // Verify field types match getter method return types
      testItem.category.shouldBeInstanceOf<KeywordField<String>>()
      testItem.displayName.shouldBeInstanceOf<TextField<String>>()
      testItem.priority.shouldBeInstanceOf<IntegerField<Int>>()
      testItem.active.shouldBeInstanceOf<BooleanField<Boolean>>()
    }

    should("integrate interface fields in document Q-classes") {
      // Verify that the document Q-class correctly references the interface Q-class
      MetaGetterMethodTestDocument.getterMethodTestDocument.items::class.java.simpleName shouldBe
        "MetaTestItem"
    }

    should("support path traversal for interface fields") {
      // Verify path construction works for interface fields
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.category.path() shouldBe
        "items.category"
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.displayName.path() shouldBe
        "items.displayName"
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.priority.path() shouldBe
        "items.priority"
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.active.path() shouldBe
        "items.active"
    }

    should("support nested path information") {
      // Verify nested field path information
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.category.isNestedPath() shouldBe
        true
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.category
        .nestedPaths()
        .count() shouldBe 1
      MetaGetterMethodTestDocument.getterMethodTestDocument.items.category
        .nestedPaths()
        .first() shouldBe "items"
    }

    should("ignore non-annotated getter methods") {
      // Verify that getDescription() method without @Field annotation is ignored
      val fieldNames = MetaTestItem::class.java.declaredFields.map { it.name }.toSet()
      fieldNames.contains("description") shouldBe false
    }
  })
