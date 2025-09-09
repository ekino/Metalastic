package com.qelasticsearchtest.integration

import com.qelasticsearch.metamodels.main.Metamodels
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EnhancedPathSpec :
  ShouldSpec({
    should("verify complex nested field paths") {
      // Test that complex nested paths work correctly
      val field = Metamodels.javaTestDocument.someInnerClass.someOtherInnerClass.someField
      field.path() shouldBe "someInnerClass.someOtherInnerClass.someField"
    }

    should("verify simple field paths") {
      Metamodels.javaTestDocument.id.path() shouldBe "id"
      Metamodels.javaTestDocument.title.path() shouldBe "title"
    }

    should("verify object field paths") {
      Metamodels.javaTestDocument.address.path() shouldBe "address"
    }
  })
