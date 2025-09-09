package com.qelasticsearchtest.integration

import com.qelasticsearchtest.metamodels.main.Metamodels
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DynamicFieldSpec :
  ShouldSpec({
    should("support dynamic fields with various types") {
      // Test that we can access dynamic fields with different types
      Metamodels.dynamicTestDocument.runtimeScore.path() shouldBe "runtimeScore"
      Metamodels.dynamicTestDocument.tags.path() shouldBe "tags"
      Metamodels.dynamicTestDocument.categories.path() shouldBe "categories"
      Metamodels.dynamicTestDocument.metadata.path() shouldBe "metadata"
      Metamodels.dynamicTestDocument.isActive.path() shouldBe "isActive"
      Metamodels.dynamicTestDocument.count.path() shouldBe "count"
    }
  })
