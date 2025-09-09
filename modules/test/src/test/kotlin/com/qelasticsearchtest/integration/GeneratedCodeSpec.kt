package com.qelasticsearchtest.integration

import com.qelasticsearch.Metamodels
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test that demonstrates the generated Metamodels.testDocument class works correctly */
class GeneratedCodeSpec :
  ShouldSpec({
    should("have correct indexName for generated Metamodels.testDocument") {
      Metamodels.testDocument.indexName() shouldBe "test_document"
    }

    should("have correct index path for generated Metamodels.testDocument") {
      Metamodels.testDocument.path() shouldBe ""
    }

    should("have correct index name for generated Metamodels.testDocument") {
      Metamodels.testDocument.name() shouldBe ""
    }

    should("have all expected fields in generated Metamodels.testDocument") {
      Metamodels.testDocument.id shouldNotBe null
      Metamodels.testDocument.name shouldNotBe null
      Metamodels.testDocument.age shouldNotBe null
      Metamodels.testDocument.active shouldNotBe null
      Metamodels.testDocument.createdDate shouldNotBe null
      Metamodels.testDocument.address shouldNotBe null
      Metamodels.testDocument.tags shouldNotBe null
    }

    should("have correct field paths in generated Metamodels.testDocument") {
      Metamodels.testDocument.id.path() shouldBe "id"
      Metamodels.testDocument.name.path() shouldBe "name"
      Metamodels.testDocument.age.path() shouldBe "age"
      Metamodels.testDocument.active.path() shouldBe "active"
      Metamodels.testDocument.createdDate.path() shouldBe "createdDate"
      Metamodels.testDocument.address.city.path() shouldBe "address.city"
      Metamodels.testDocument.tags.name.path() shouldBe "tags.name"
    }

    should("have correct field paths in generated Metamodels.exampleDocument") {
      Metamodels.exampleDocument.testDocument.path() shouldBe "testDocument"
      Metamodels.exampleDocument.testDocument.age.path() shouldBe "testDocument.age"
      Metamodels.exampleDocument.testDocument.tags.name.path() shouldBe "testDocument.tags.name"
      Metamodels.exampleDocument.testDocument.address.city.path() shouldBe
        "testDocument.address.city"
    }
  })
