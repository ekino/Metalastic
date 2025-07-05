package com.qelasticsearch.integration

import com.qelasticsearch.dsl.UnknownNestedFields
import com.qelasticsearch.dsl.UnknownObjectFields
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class UnknownFieldsSpec :
    ShouldSpec({

        should("generate UnknownNestedFields for interface nested fields") {
            // Verify that activities field uses UnknownNestedFields
            val activitiesField = QNestedTestDocument.activities

            activitiesField shouldNotBe null
            activitiesField.shouldBeInstanceOf<UnknownNestedFields>()
        }

        should("generate UnknownObjectFields for interface object fields") {
            // Verify that metadata field uses UnknownObjectFields
            val metadataField = QNestedTestDocument.metadata

            metadataField shouldNotBe null
            metadataField.shouldBeInstanceOf<UnknownObjectFields>()
        }

        should("still generate proper Q-classes for classes with Field annotations") {
            // Verify that operation field still generates proper Q-class
            val operationField = QNestedTestDocument.operation

            operationField shouldNotBe null
            operationField.shouldBeInstanceOf<QNestedTestDocumentOperation>()
        }

        should("provide correct path structure for document") {
            QNestedTestDocument.indexName shouldBe "nested_test_document"
            QNestedTestDocument.path shouldBe ""

            // Test nested path construction
            QNestedTestDocument.operation.active.path shouldBe "operation.active"
            QNestedTestDocument.operation.states.id.path shouldBe "operation.states.id"
        }

        should("generate compilable and type-safe code") {
            // This test just verifies that the generated code compiles
            // and the types are correct - the fact that this test compiles
            // means the UnknownFields approach is working

            val document = QNestedTestDocument

            // These should all be accessible without compilation errors
            val id = document.id
            val name = document.name
            val operation = document.operation
            val activities = document.activities // UnknownNestedFields
            val metadata = document.metadata // UnknownObjectFields

            // Verify we can access nested fields through the proper Q-class
            val operationActive = document.operation.active
            val stateId = document.operation.states.id

            id shouldNotBe null
            name shouldNotBe null
            operation shouldNotBe null
            activities shouldNotBe null
            metadata shouldNotBe null
            operationActive shouldNotBe null
            stateId shouldNotBe null
        }
    })
