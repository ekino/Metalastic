package com.metalastic.other.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Test to verify that nested class collision handling works correctly. Ensures that external and
 * nested classes with the same simple name generate different Q-classes.
 */
class NestedClassCollisionSpec :
  ShouldSpec({
    should("generate separate Q-classes for external and nested classes with same name") {
      // External JavaTag class should generate MetaJavaTag
      val externalTagField = MetaJavaTestDocument.javaTestDocument.tags
      externalTagField shouldNotBe null

      // Should have fields from external JavaTag class
      externalTagField.name.path() shouldBe "tags.name"
      externalTagField.weight.path() shouldBe "tags.weight"
      externalTagField.description.path() shouldBe "tags.description"

      // Nested JavaTag class should generate nested object within
      // MetaJavaTestDocument.javaTestDocument
      val nestedTagField = MetaJavaTestDocument.javaTestDocument.tags2
      nestedTagField shouldNotBe null

      // Nested class has different field names: tagName and size (not name, weight, description)
      nestedTagField.tagName.path() shouldBe "tags2.tagName"
      nestedTagField.size.path() shouldBe "tags2.size"

      // Verify they are different Q-class types
      externalTagField::class.simpleName shouldBe "MetaJavaTag"
      nestedTagField::class.simpleName shouldBe "JavaTag" // Q prefix removed for nested classes
    }

    should("correctly resolve field paths for both external and nested classes") {
      // External class field paths
      MetaJavaTestDocument.javaTestDocument.tags.name.path() shouldBe "tags.name"
      MetaJavaTestDocument.javaTestDocument.tags.weight.path() shouldBe "tags.weight"
      MetaJavaTestDocument.javaTestDocument.tags.description.path() shouldBe "tags.description"

      // Nested class field paths (different field names)
      MetaJavaTestDocument.javaTestDocument.tags2.tagName.path() shouldBe "tags2.tagName"
      MetaJavaTestDocument.javaTestDocument.tags2.size.path() shouldBe "tags2.size"

      // Field paths verified above
    }
  })
