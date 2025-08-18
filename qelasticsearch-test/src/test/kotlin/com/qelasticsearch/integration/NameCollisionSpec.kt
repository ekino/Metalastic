package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for name collision resolution in nested classes.
 *
 * This test verifies that when multiple classes have the same name but exist at different nesting
 * levels, the annotation processor correctly generates separate Q-classes and references them
 * appropriately.
 */
class NameCollisionSpec :
  ShouldSpec({
    context("Name collision resolution") {
      should("generate separate Q-classes for each NameCollision class") {
        // Verify that both NameCollision classes exist
        QExampleDocument.NameCollision::class shouldNotBe null
        QExampleDocument.NestedObject.NameCollision::class shouldNotBe null

        // Verify they are different classes
        QExampleDocument.NameCollision::class shouldNotBe
          QExampleDocument.NestedObject.NameCollision::class
      }

      should("correctly reference outer NameCollision class") {
        val outerNameCollision = QExampleDocument.nameCollision

        // Verify it's not null and is the correct object
        outerNameCollision shouldNotBe null
        outerNameCollision.shouldBeInstanceOf<QExampleDocument.NameCollision>()

        // Verify it has the correct field
        val firstLevelField = outerNameCollision.firstLevel
        firstLevelField.path() shouldBe "nameCollision.firstLevel"
      }

      should("correctly reference inner NameCollision class") {
        val innerNameCollision = QExampleDocument.nestedObject.nameCollision

        // Verify it's not null and is the correct object
        innerNameCollision shouldNotBe null
        innerNameCollision.shouldBeInstanceOf<QExampleDocument.NestedObject.NameCollision>()

        // Verify it has the correct field
        val secondLevelField = innerNameCollision.secondLevel
        secondLevelField.path() shouldBe "nestedObject.nameCollision.secondLevel"
      }

      should("have different field paths for different NameCollision classes") {
        val outerFirstLevel = QExampleDocument.nameCollision.firstLevel
        val innerSecondLevel = QExampleDocument.nestedObject.nameCollision.secondLevel

        // The paths should be different
        outerFirstLevel.path() shouldNotBe innerSecondLevel.path()

        // Verify the actual paths
        outerFirstLevel.path() shouldBe "nameCollision.firstLevel"
        innerSecondLevel.path() shouldBe "nestedObject.nameCollision.secondLevel"
      }

      should("handle separate class references correctly") {
        val separateClass = QExampleDocument.fromSeparateClass

        // This should reference the standalone QNameCollision class
        separateClass.shouldBeInstanceOf<QNameCollision>()

        // Verify it has the correct field
        val separateField = separateClass.separateClassField
        separateField.path() shouldBe "fromSeparateClass.separateClassField"
      }

      should("maintain correct nesting hierarchy") {
        val nestedObject = QExampleDocument.nestedObject

        // Verify nestedObject has the correct structure
        nestedObject.shouldBeInstanceOf<QExampleDocument.NestedObject>()

        // Verify it has both someField and nameCollision
        val someField = nestedObject.someField
        someField.path() shouldBe "nestedObject.someField"

        val nameCollision = nestedObject.nameCollision
        nameCollision.shouldBeInstanceOf<QExampleDocument.NestedObject.NameCollision>()
      }

      should("generate unique class names for nested classes") {
        // Verify the classes have different simple names or are properly nested
        val outerClass = QExampleDocument.NameCollision::class
        val innerClass = QExampleDocument.NestedObject.NameCollision::class

        // They should be different classes
        outerClass shouldNotBe innerClass

        // The inner class should be nested within NestedObject
        innerClass.java.enclosingClass shouldBe QExampleDocument.NestedObject::class.java

        // The outer class should be nested within QExampleDocument
        outerClass.java.enclosingClass shouldBe QExampleDocument::class.java
      }

      should("support field access through different paths") {
        // Test that we can access fields through different paths without ambiguity

        // Access outer NameCollision field
        val path1 = QExampleDocument.nameCollision.firstLevel.path()

        // Access inner NameCollision field
        val path2 = QExampleDocument.nestedObject.nameCollision.secondLevel.path()

        // Access separate class field
        val path3 = QExampleDocument.fromSeparateClass.separateClassField.path()

        // All paths should be unique
        path1 shouldNotBe path2
        path1 shouldNotBe path3
        path2 shouldNotBe path3

        // Verify the exact paths
        path1 shouldBe "nameCollision.firstLevel"
        path2 shouldBe "nestedObject.nameCollision.secondLevel"
        path3 shouldBe "fromSeparateClass.separateClassField"
      }

      should("handle nested detection correctly") {
        // Test the path building for object fields
        val outerField = QExampleDocument.nameCollision.firstLevel
        val innerField = QExampleDocument.nestedObject.nameCollision.secondLevel

        // Verify paths are correct for object fields
        outerField.path() shouldBe "nameCollision.firstLevel"
        innerField.path() shouldBe "nestedObject.nameCollision.secondLevel"
      }

      should("maintain type safety across name collisions") {
        // Verify that the compiler can distinguish between the different classes
        val outer: QExampleDocument.NameCollision = QExampleDocument.nameCollision
        val inner: QExampleDocument.NestedObject.NameCollision =
          QExampleDocument.nestedObject.nameCollision

        // These should be different types
        outer::class shouldNotBe inner::class

        // But both should have their respective fields accessible
        outer.firstLevel.path() shouldBe "nameCollision.firstLevel"
        inner.secondLevel.path() shouldBe "nestedObject.nameCollision.secondLevel"
      }
    }

    context("Edge cases and complex scenarios") {
      should("handle multiple levels of nesting") {
        // Test that the fix works for the actual structure we have
        val document = QExampleDocument

        // Test multi-level access
        val nestedAccess = document.nestedObject.nameCollision.secondLevel
        nestedAccess.path() shouldBe "nestedObject.nameCollision.secondLevel"

        // Verify the path contains the full hierarchy
        nestedAccess.path().split(".").size shouldBe 3
        nestedAccess.path().split(".")[0] shouldBe "nestedObject"
        nestedAccess.path().split(".")[1] shouldBe "nameCollision"
        nestedAccess.path().split(".")[2] shouldBe "secondLevel"
      }

      should("support different field types in same-named classes") {
        // The outer NameCollision has firstLevel (Text)
        val outerField = QExampleDocument.nameCollision.firstLevel
        // The inner NameCollision has secondLevel (Text)
        val innerField = QExampleDocument.nestedObject.nameCollision.secondLevel

        // Both are Text fields but have different paths
        outerField.path() shouldBe "nameCollision.firstLevel"
        innerField.path() shouldBe "nestedObject.nameCollision.secondLevel"
      }

      should("maintain functional consistency") {
        // Multiple references to the same object should have same behavior
        val ref1 = QExampleDocument.nameCollision
        val ref2 = QExampleDocument.nameCollision
        ref1.firstLevel.path() shouldBe ref2.firstLevel.path()

        val ref3 = QExampleDocument.nestedObject.nameCollision
        val ref4 = QExampleDocument.nestedObject.nameCollision
        ref3.secondLevel.path() shouldBe ref4.secondLevel.path()

        // Different objects should have different paths
        ref1.firstLevel.path() shouldNotBe ref3.secondLevel.path()
      }
    }
  })
