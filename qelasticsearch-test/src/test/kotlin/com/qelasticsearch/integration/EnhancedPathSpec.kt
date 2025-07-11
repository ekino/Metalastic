package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EnhancedPathSpec :
    ShouldSpec({
        should("verify complex nested field paths") {
            // Test that complex nested paths work correctly
            val field = QJavaTestDocument.someInnerClass.someOtherInnerClass.someField
            field.path() shouldBe "someInnerClass.someOtherInnerClass.someField"
        }

        should("verify simple field paths") {
            QJavaTestDocument.id.path() shouldBe "id"
            QJavaTestDocument.title.path() shouldBe "title"
        }

        should("verify object field paths") {
            QJavaTestDocument.address.path() shouldBe "address"
        }
    })
