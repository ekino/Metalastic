package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Test that demonstrates the generated QTestDocument class works correctly
 */
class GeneratedCodeSpec :
    ShouldSpec({

        should("have correct index name for generated QTestDocument") {
            QTestDocument.indexName shouldBe "test_document"
        }

        should("have all expected fields in generated QTestDocument") {
            QTestDocument.id shouldNotBe null
            QTestDocument.name shouldNotBe null
            QTestDocument.age shouldNotBe null
            QTestDocument.active shouldNotBe null
            QTestDocument.createdDate shouldNotBe null
            QTestDocument.address shouldNotBe null
            QTestDocument.tags shouldNotBe null
        }

        should("have correct field paths in generated QTestDocument") {
            QTestDocument.id.path().path shouldBe "id"
            QTestDocument.name.path().path shouldBe "name"
            QTestDocument.age.path().path shouldBe "age"
            QTestDocument.active.path().path shouldBe "active"
            QTestDocument.createdDate.path().path shouldBe "createdDate"
            QTestDocument.address.city
                .path()
                .path shouldBe "address.city"
            QTestDocument.tags.name
                .path()
                .path shouldBe "tags.name"
        }

        should("extend Index correctly") {
            QTestDocument.parentPath.path shouldBe ""
        }
    })
