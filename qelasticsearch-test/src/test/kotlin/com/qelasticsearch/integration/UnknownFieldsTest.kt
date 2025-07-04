package com.qelasticsearch.integration

import com.qelasticsearch.dsl.UnknownNestedFields
import com.qelasticsearch.dsl.UnknownObjectFields
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UnknownFieldsTest {
    @Test
    fun `should generate UnknownNestedFields for interface nested fields`() {
        // Verify that activities field uses UnknownNestedFields
        val activitiesField = QNestedTestDocument.activities

        assertNotNull(activitiesField)
        assertTrue(activitiesField is UnknownNestedFields)
    }

    @Test
    fun `should generate UnknownObjectFields for interface object fields`() {
        // Verify that metadata field uses UnknownObjectFields
        val metadataField = QNestedTestDocument.metadata

        assertNotNull(metadataField)
        assertTrue(metadataField is UnknownObjectFields)
    }

    @Test
    fun `should still generate proper Q-classes for classes with Field annotations`() {
        // Verify that operation field still generates proper Q-class
        val operationField = QNestedTestDocument.operation

        assertNotNull(operationField)
        assertTrue(operationField is QNestedTestDocumentOperation)
    }

    @Test
    fun `should provide correct path structure for document`() {
        assertEquals("nested_test_document", QNestedTestDocument.indexName)
        assertEquals("", QNestedTestDocument.path)

        // Test nested path construction
        assertEquals("operation.active", QNestedTestDocument.operation.active.path)
        assertEquals("operation.states.id", QNestedTestDocument.operation.states.id.path)
    }

    @Test
    fun `generated code should compile and be type-safe`() {
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

        assertNotNull(id)
        assertNotNull(name)
        assertNotNull(operation)
        assertNotNull(activities)
        assertNotNull(metadata)
        assertNotNull(operationActive)
        assertNotNull(stateId)
    }
}
