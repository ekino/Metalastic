package com.qelasticsearchtest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EnhancedPathSpec :
  ShouldSpec({
    should("verify complex nested field paths") {
      // Test that complex nested paths work correctly
      val field = QJavaTestDocument.javaTestDocument.someInnerClass.someOtherInnerClass.someField
      field.path() shouldBe "someInnerClass.someOtherInnerClass.someField"
    }

    should("verify simple field paths") {
      QJavaTestDocument.javaTestDocument.id.path() shouldBe "id"
      QJavaTestDocument.javaTestDocument.title.path() shouldBe "title"
    }

    should("verify object field paths") {
      QJavaTestDocument.javaTestDocument.address.path() shouldBe "address"
    }
  })
