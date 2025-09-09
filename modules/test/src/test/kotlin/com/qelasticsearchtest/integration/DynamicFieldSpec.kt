package com.qelasticsearchtest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DynamicFieldSpec :
  ShouldSpec({
    should("support dynamic fields with various types") {
      // Test that we can access dynamic fields with different types
      QDynamicTestDocument.dynamicTestDocument.runtimeScore.path() shouldBe "runtimeScore"
      QDynamicTestDocument.dynamicTestDocument.tags.path() shouldBe "tags"
      QDynamicTestDocument.dynamicTestDocument.categories.path() shouldBe "categories"
      QDynamicTestDocument.dynamicTestDocument.metadata.path() shouldBe "metadata"
      QDynamicTestDocument.dynamicTestDocument.isActive.path() shouldBe "isActive"
      QDynamicTestDocument.dynamicTestDocument.count.path() shouldBe "count"
    }
  })
