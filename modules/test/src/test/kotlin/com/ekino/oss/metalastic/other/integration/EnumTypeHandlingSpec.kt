package com.ekino.oss.metalastic.other.integration

import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.integration.MetaJavaTestDocument
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/** Test cases to verify that enum types are properly handled in the generated DSL. */
class EnumTypeHandlingSpec :
  ShouldSpec({
    should("have correct parameterized type for enum fields") {
      // Test TestStatus enum field
      val statusField = MetaJavaTestDocument.javaTestDocument.status
      statusField.shouldBeInstanceOf<KeywordField<*>>()

      // Test Priority enum field
      val priorityField = MetaJavaTestDocument.javaTestDocument.priorityLevel
      priorityField.shouldBeInstanceOf<KeywordField<*>>()

      // Test that field names are correct
      statusField.name() shouldBe "status"
      priorityField.name() shouldBe "priorityLevel"
    }

    should("have correct paths for enum fields") {
      val statusField = MetaJavaTestDocument.javaTestDocument.status
      val priorityField = MetaJavaTestDocument.javaTestDocument.priorityLevel

      // Verify field path properties
      statusField.path() shouldBe "status"
      priorityField.path() shouldBe "priorityLevel"
    }

    should("work correctly for enum fields from NestedTestDocument") {
      val nestedStatusField = MetaNestedTestDocument.nestedTestDocument.status

      nestedStatusField.shouldBeInstanceOf<KeywordField<*>>()
      nestedStatusField.name() shouldBe "status"
      nestedStatusField.path() shouldBe "status"
    }

    should("verify enum types in nested document") {
      // Test nested document enum fields
      val nestedStatus = MetaNestedTestDocument.nestedTestDocument.status
      nestedStatus.path() shouldBe "status"
    }
  })
