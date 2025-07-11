package com.qelasticsearch.integration

import com.qelasticsearch.dsl.FieldPath
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class EnhancedPathSpec :
    ShouldSpec({

        should("correctly identify nested vs non-nested paths") {
            // Simple path (not nested)
            val simplePath = FieldPath.simple("name")
            simplePath.path shouldBe "name"
            simplePath.isNested shouldBe false
            simplePath.nestedSegments.shouldBeEmpty()
            // Nested path
            val nestedPath = FieldPath.nested("activities")
            nestedPath.path shouldBe "activities"
            nestedPath.isNested shouldBe true
            nestedPath.nestedSegments shouldContainExactly listOf("activities")
        }

        should("correctly handle complex nested structures") {
            // Create a path with multiple nested segments
            val complexPath = FieldPath("user.addresses.city", listOf("user.addresses"))
            complexPath.path shouldBe "user.addresses.city"
            complexPath.isNested shouldBe true
            complexPath.nestedSegments shouldContainExactly listOf("user.addresses")
            complexPath.isCompletelyNested shouldBe true
            complexPath.rootNestedPath shouldBe "user.addresses"
        }

        should("handle FieldPath child creation correctly") {
            val parent = FieldPath.simple("user")
            val objectChild = parent.child("name")
            val nestedChild = parent.child("addresses", isChildNested = true)

            objectChild.path shouldBe "user.name"
            objectChild.isNested shouldBe false

            nestedChild.path shouldBe "user.addresses"
            nestedChild.isNested shouldBe true
            nestedChild.nestedSegments shouldContainExactly listOf("user.addresses")
        }
    })
