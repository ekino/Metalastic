package com.metalastictest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test that demonstrates the generated QTestDocument.testDocument class works correctly */
class GeneratedCodeSpec :
  ShouldSpec({
    should("have correct indexName for generated QTestDocument.testDocument") {
      QTestDocument.testDocument.indexName() shouldBe "test_document"
    }

    should("have correct index path for generated QTestDocument.testDocument") {
      QTestDocument.testDocument.path() shouldBe ""
    }

    should("have correct index name for generated QTestDocument.testDocument") {
      QTestDocument.testDocument.name() shouldBe ""
    }

    should("have all expected fields in generated QTestDocument.testDocument") {
      QTestDocument.testDocument.id shouldNotBe null
      QTestDocument.testDocument.name shouldNotBe null
      QTestDocument.testDocument.age shouldNotBe null
      QTestDocument.testDocument.active shouldNotBe null
      QTestDocument.testDocument.createdDate shouldNotBe null
      QTestDocument.testDocument.address shouldNotBe null
      QTestDocument.testDocument.tags shouldNotBe null
    }

    should("have correct field paths in generated QTestDocument.testDocument") {
      QTestDocument.testDocument.id.path() shouldBe "id"
      QTestDocument.testDocument.name.path() shouldBe "name"
      QTestDocument.testDocument.age.path() shouldBe "age"
      QTestDocument.testDocument.active.path() shouldBe "active"
      QTestDocument.testDocument.createdDate.path() shouldBe "createdDate"
      QTestDocument.testDocument.address.city.path() shouldBe "address.city"
      QTestDocument.testDocument.tags.name.path() shouldBe "tags.name"
    }

    should("have correct field paths in generated QExampleDocument.exampleDocument") {
      QExampleDocument.exampleDocument.testDocument.path() shouldBe "testDocument"
      QExampleDocument.exampleDocument.testDocument.age.path() shouldBe "testDocument.age"
      QExampleDocument.exampleDocument.testDocument.tags.name.path() shouldBe
        "testDocument.tags.name"
      QExampleDocument.exampleDocument.testDocument.address.city.path() shouldBe
        "testDocument.address.city"
    }
  })
