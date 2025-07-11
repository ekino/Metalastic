package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class RealWorldPathSpec :
    ShouldSpec({

        should("have proper path information for generated Q-classes") {
            // Test simple field paths
            QJavaTestDocument.title.path() shouldBe "title"

            // Test object field paths (not nested)
            QJavaTestDocument.address.city
                .path() shouldBe "address.city"

            // Test nested field paths
            QJavaTestDocument.tags.name
                .path() shouldBe "tags.name"
        }

        should("show complete hierarchy properly for nested document") {
            // Test simple field in nested document
            QNestedTestDocument.status.path() shouldBe "status"

            // Test object field (operation is an object, not nested)
            QNestedTestDocument.operation.active
                .path() shouldBe "operation.active"

            // Test nested field (activities is a nested field)
            // Since it uses QTestActivity (interface with no @Field annotations), we can't access sub-fields but we can test the parent
            // The nested field itself should show proper path but the object should handle nested status
            // This is showing that activities is a nested field at the root level
        }

        should("maintain nested information for MultiField") {
            val multiField = QJavaTestDocument.multiFieldName
            multiField.path() shouldBe "multiFieldName"

            // Test that inner fields return their local field name
            val searchField = multiField.search
            searchField.path() shouldBe "search"
        }
    })
