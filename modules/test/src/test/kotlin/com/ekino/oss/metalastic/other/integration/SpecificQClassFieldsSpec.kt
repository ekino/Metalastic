package com.metalastic.other.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class SpecificQClassFieldsSpec :
  ShouldSpec({
    should("generate MetaTestActivity for interface nested fields") {
      // Verify that activities field uses specific MetaTestActivity Q-class
      val activitiesField = MetaNestedTestDocument.nestedTestDocument.activities

      activitiesField shouldNotBe null
      activitiesField.shouldBeInstanceOf<MetaTestActivity<List<TestActivity>>>()
    }

    should("generate MetaTestMetadata for interface object fields") {
      // Verify that metadata field uses specific MetaTestMetadata Q-class
      val metadataField = MetaNestedTestDocument.nestedTestDocument.metadata

      metadataField shouldNotBe null
      metadataField.shouldBeInstanceOf<MetaTestMetadata<TestMetadata?>>()
    }

    should("still generate proper Q-classes for classes with Field annotations") {
      // Verify that operation field still generates proper Q-class
      val operationField = MetaNestedTestDocument.nestedTestDocument.operation

      operationField shouldNotBe null
      operationField.shouldBeInstanceOf<
        MetaNestedTestDocument.Operation<NestedTestDocument.Operation>
      >()
    }

    should("provide correct path structure for document") {
      MetaNestedTestDocument.nestedTestDocument.indexName() shouldBe "nested_test_document"

      // Test nested path construction
      MetaNestedTestDocument.nestedTestDocument.operation.active.path() shouldBe "operation.active"
      MetaNestedTestDocument.nestedTestDocument.operation.states.id.path() shouldBe
        "operation.states.id"
    }

    should("generate compilable and type-safe code with specific Q-classes") {
      // This test verifies that the generated code compiles with specific Q-classes
      // instead of generic Unknown fields - providing better type safety

      val document = MetaNestedTestDocument.nestedTestDocument

      // These should all be accessible without compilation errors
      val id = document.id
      val name = document.name
      val operation = document.operation
      val activities = document.activities // MetaTestActivity
      val metadata = document.metadata // MetaTestMetadata

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

      // Verify the specific Q-classes provide proper type information
      activities.shouldBeInstanceOf<MetaTestActivity<List<TestActivity>>>()
      metadata.shouldBeInstanceOf<MetaTestMetadata<TestMetadata?>>()
    }
  })
