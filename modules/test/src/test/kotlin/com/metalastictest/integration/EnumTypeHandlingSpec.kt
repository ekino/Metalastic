package com.metalastictest.integration

import com.metalastic.core.KeywordField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/** Test cases to verify that enum types are properly handled in the generated DSL. */
class EnumTypeHandlingSpec :
  ShouldSpec({
    should("have correct parameterized type for enum fields") {
      // Test TestStatus enum field
      val statusField = QJavaTestDocument.javaTestDocument.status
      statusField.shouldBeInstanceOf<KeywordField<*>>()

      // Test Priority enum field
      val priorityField = QJavaTestDocument.javaTestDocument.priorityLevel
      priorityField.shouldBeInstanceOf<KeywordField<*>>()

      // Test that field names are correct
      statusField.name() shouldBe "status"
      priorityField.name() shouldBe "priorityLevel"
    }

    should("have correct paths for enum fields") {
      val statusField = QJavaTestDocument.javaTestDocument.status
      val priorityField = QJavaTestDocument.javaTestDocument.priorityLevel

      // Verify field path properties
      statusField.path() shouldBe "status"
      priorityField.path() shouldBe "priorityLevel"
    }

    should("work correctly for enum fields from NestedTestDocument") {
      val nestedStatusField = QNestedTestDocument.nestedTestDocument.status

      nestedStatusField.shouldBeInstanceOf<KeywordField<*>>()
      nestedStatusField.name() shouldBe "status"
      nestedStatusField.path() shouldBe "status"
    }

    should("verify enum types in nested document") {
      // Test nested document enum fields
      val nestedStatus = QNestedTestDocument.nestedTestDocument.status
      nestedStatus.path() shouldBe "status"
    }
  })
