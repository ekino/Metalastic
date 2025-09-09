package com.qelasticsearchtest.integration

import com.qelasticsearch.Metamodels
import com.qelasticsearch.core.KeywordField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/** Test cases to verify that enum types are properly handled in the generated DSL. */
class EnumTypeHandlingSpec :
  ShouldSpec({
    should("have correct parameterized type for enum fields") {
      // Test TestStatus enum field
      val statusField = Metamodels.javaTestDocument.status
      statusField.shouldBeInstanceOf<KeywordField<*>>()

      // Test Priority enum field
      val priorityField = Metamodels.javaTestDocument.priorityLevel
      priorityField.shouldBeInstanceOf<KeywordField<*>>()

      // Test that field names are correct
      statusField.name() shouldBe "status"
      priorityField.name() shouldBe "priorityLevel"
    }

    should("have correct paths for enum fields") {
      val statusField = Metamodels.javaTestDocument.status
      val priorityField = Metamodels.javaTestDocument.priorityLevel

      // Verify field path properties
      statusField.path() shouldBe "status"
      priorityField.path() shouldBe "priorityLevel"
    }

    should("work correctly for enum fields from NestedTestDocument") {
      val nestedStatusField = Metamodels.nestedTestDocument.status

      nestedStatusField.shouldBeInstanceOf<KeywordField<*>>()
      nestedStatusField.name() shouldBe "status"
      nestedStatusField.path() shouldBe "status"
    }

    should("verify enum types in nested document") {
      // Test nested document enum fields
      val nestedStatus = Metamodels.nestedTestDocument.status
      nestedStatus.path() shouldBe "status"
    }
  })
