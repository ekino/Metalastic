package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Test to verify that nested class collision handling works correctly.
 * Ensures that external and nested classes with the same simple name generate different Q-classes.
 */
class NestedClassCollisionSpec :
    ShouldSpec({

        should("generate separate Q-classes for external and nested classes with same name") {
            // External JavaTag class should generate QJavaTag
            val externalTagField = QJavaTestDocument.tags
            externalTagField shouldNotBe null

            // Should have fields from external JavaTag class
            externalTagField.name.path shouldBe "tags.name"
            externalTagField.weight.path shouldBe "tags.weight"
            externalTagField.description.path shouldBe "tags.description"

            // Nested JavaTag class should generate QJavaTestDocumentJavaTag
            val nestedTagField = QJavaTestDocument.tags2
            nestedTagField shouldNotBe null

            // Should have fields from nested JavaTestDocument.JavaTag class
            nestedTagField.tagName.path shouldBe "tags2.tagName"
            nestedTagField.size.path shouldBe "tags2.size"

            // Verify they are different Q-class types
            externalTagField::class.simpleName shouldBe "QJavaTag"
            nestedTagField::class.simpleName shouldBe "QJavaTestDocumentJavaTag"
        }

        should("correctly resolve field paths for both external and nested classes") {
            // External class field paths
            QJavaTestDocument.tags.name.fieldPath.path shouldBe "tags.name"
            QJavaTestDocument.tags.weight.fieldPath.path shouldBe "tags.weight"
            QJavaTestDocument.tags.description.fieldPath.path shouldBe "tags.description"

            // Nested class field paths
            QJavaTestDocument.tags2.tagName.fieldPath.path shouldBe "tags2.tagName"
            QJavaTestDocument.tags2.size.fieldPath.path shouldBe "tags2.size"

            // Both should be nested fields
            QJavaTestDocument.tags.name.fieldPath.isNested shouldBe true
            QJavaTestDocument.tags2.tagName.fieldPath.isNested shouldBe true
        }
    })
