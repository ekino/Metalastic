package com.ekino.oss.metalastic.other.integration

import com.ekino.oss.metalastic.integration.MetaJavaTestDocument
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EnhancedPathSpec :
  ShouldSpec({
    should("verify complex nested field paths") {
      // Test that complex nested paths work correctly
      val field = MetaJavaTestDocument.javaTestDocument.someInnerClass.someOtherInnerClass.someField
      field.path() shouldBe "someInnerClass.someOtherInnerClass.someField"
    }

    should("verify simple field paths") {
      MetaJavaTestDocument.javaTestDocument.id.path() shouldBe "id"
      MetaJavaTestDocument.javaTestDocument.title.path() shouldBe "title"
    }

    should("verify object field paths") {
      MetaJavaTestDocument.javaTestDocument.address.path() shouldBe "address"
    }
  })
