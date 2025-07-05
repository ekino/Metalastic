package com.qelasticsearch.integration

import com.qelasticsearch.dsl.KeywordField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Test cases to verify that enum types are properly handled in the generated DSL.
 */
class EnumTypeHandlingSpec :
    ShouldSpec({

        should("have correct parameterized type for enum fields") {
            // Test TestStatus enum field
            val statusField = QJavaTestDocument.status
            statusField.shouldBeInstanceOf<KeywordField<*>>()

            // Test Priority enum field
            val priorityField = QJavaTestDocument.priorityLevel
            priorityField.shouldBeInstanceOf<KeywordField<*>>()

            // Test that field names are correct
            statusField.name shouldBe "status"
            priorityField.name shouldBe "priorityLevel"
        }

        should("have correct paths for enum fields") {
            val statusField = QJavaTestDocument.status
            val priorityField = QJavaTestDocument.priorityLevel

            // Verify field paths
            statusField.path shouldBe "status"
            priorityField.path shouldBe "priorityLevel"

            // Verify field path properties
            statusField.fieldPath.path shouldBe "status"
            priorityField.fieldPath.path shouldBe "priorityLevel"
            statusField.fieldPath.isNested shouldBe false
            priorityField.fieldPath.isNested shouldBe false
        }

        should("work correctly for enum fields from NestedTestDocument") {
            val nestedStatusField = QNestedTestDocument.status

            nestedStatusField.shouldBeInstanceOf<KeywordField<*>>()
            nestedStatusField.name shouldBe "status"
            nestedStatusField.path shouldBe "status"
        }

        should("verify enum types in nested document") {
            // Test document structure
            QJavaTestDocument.path shouldBe ""
            QNestedTestDocument.path shouldBe ""

            // Test nested document enum fields
            val nestedStatus = QNestedTestDocument.status
            nestedStatus.fieldPath.isNested shouldBe false
        }
    })
