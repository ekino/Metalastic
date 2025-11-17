/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.other.integration

import com.ekino.oss.metalastic.integration.MetaExampleDocument
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test that demonstrates the generated MetaTestDocument.testDocument class works correctly */
class GeneratedCodeSpec :
  ShouldSpec({
    should("have correct indexName for generated MetaTestDocument.testDocument") {
      MetaTestDocument.testDocument.indexName() shouldBe "test_document"
    }

    should("have correct index path for generated MetaTestDocument.testDocument") {
      MetaTestDocument.testDocument.path() shouldBe ""
    }

    should("have correct index name for generated MetaTestDocument.testDocument") {
      MetaTestDocument.testDocument.name() shouldBe ""
    }

    should("have all expected fields in generated MetaTestDocument.testDocument") {
      MetaTestDocument.testDocument.id shouldNotBe null
      MetaTestDocument.testDocument.name shouldNotBe null
      MetaTestDocument.testDocument.age shouldNotBe null
      MetaTestDocument.testDocument.active shouldNotBe null
      MetaTestDocument.testDocument.createdDate shouldNotBe null
      MetaTestDocument.testDocument.address shouldNotBe null
      MetaTestDocument.testDocument.tags shouldNotBe null
    }

    should("have correct field paths in generated MetaTestDocument.testDocument") {
      MetaTestDocument.testDocument.id.path() shouldBe "id"
      MetaTestDocument.testDocument.name.path() shouldBe "name"
      MetaTestDocument.testDocument.age.path() shouldBe "age"
      MetaTestDocument.testDocument.active.path() shouldBe "active"
      MetaTestDocument.testDocument.createdDate.path() shouldBe "createdDate"
      MetaTestDocument.testDocument.address.city.path() shouldBe "address.city"
      MetaTestDocument.testDocument.tags.name.path() shouldBe "tags.name"
    }

    should("have correct field paths in generated MetaExampleDocument.exampleDocument") {
      MetaExampleDocument.exampleDocument.testDocument.path() shouldBe "testDocument"
      MetaExampleDocument.exampleDocument.testDocument.age.path() shouldBe "testDocument.age"
      MetaExampleDocument.exampleDocument.testDocument.tags.name.path() shouldBe
        "testDocument.tags.name"
      MetaExampleDocument.exampleDocument.testDocument.address.city.path() shouldBe
        "testDocument.address.city"
    }
  })
