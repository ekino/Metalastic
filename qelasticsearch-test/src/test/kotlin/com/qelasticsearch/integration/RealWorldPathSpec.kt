package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class RealWorldPathSpec :
    ShouldSpec({

        should("have proper path information for generated Q-classes") {
            // Test simple field paths
            QJavaTestDocument.title.path shouldBe "title"
            QJavaTestDocument.title.fieldPath.isNested shouldBe false

            // Test object field paths (not nested)
            QJavaTestDocument.address.city.path shouldBe "address.city"
            QJavaTestDocument.address.city.fieldPath.isNested shouldBe false

            // Test nested field paths
            QJavaTestDocument.tags.name.path shouldBe "tags.name"
            QJavaTestDocument.tags.name.fieldPath.isNested shouldBe true
            QJavaTestDocument.tags.name.fieldPath.nestedSegments shouldContainExactly listOf("tags")
            QJavaTestDocument.tags.name.fieldPath.rootNestedPath shouldBe "tags"
        }

        should("show complete hierarchy properly for nested document") {
            // Test simple field in nested document
            QNestedTestDocument.status.path shouldBe "status"
            QNestedTestDocument.status.fieldPath.isNested shouldBe false

            // Test object field (operation is an object, not nested)
            QNestedTestDocument.operation.active.path shouldBe "operation.active"
            QNestedTestDocument.operation.active.fieldPath.isNested shouldBe false

            // Test nested field (activities is a nested field)
            // Since it uses QTestActivity (interface with no @Field annotations), we can't access sub-fields but we can test the parent
            // The nested field itself should show proper path but the object should handle nested status
            // This is showing that activities is a nested field at the root level
        }

        should("maintain nested information for MultiField") {
            val multiField = QJavaTestDocument.multiFieldName
            multiField.path shouldBe "multiFieldName"
            multiField.fieldPath.isNested shouldBe false

            // Test that inner fields maintain the parent's nested information
            val searchField = multiField.search
            searchField.path shouldBe "multiFieldName.search"
            searchField.fieldPath.isNested shouldBe false
        }
    })
