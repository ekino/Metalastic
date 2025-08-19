package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DynamicFieldSpec :
  ShouldSpec({
    should("support dynamic fields with various types") {
      // Test that we can access dynamic fields with different types
      QDynamicTestDocument.runtimeScore.path() shouldBe "runtimeScore"
      QDynamicTestDocument.tags.path() shouldBe "tags"
      QDynamicTestDocument.categories.path() shouldBe "categories"
      QDynamicTestDocument.metadata.path() shouldBe "metadata"
      QDynamicTestDocument.isActive.path() shouldBe "isActive"
      QDynamicTestDocument.count.path() shouldBe "count"
    }
  })
