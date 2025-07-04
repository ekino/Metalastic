package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.qelasticsearch.dsl.KeywordField
import org.junit.jupiter.api.Test

/**
 * Test cases to verify that enum types are properly handled in the generated DSL.
 */
class EnumTypeHandlingTest {
    @Test
    fun `enum fields should have correct parameterized type`() {
        // Test TestStatus enum field
        val statusField = QJavaTestDocument.status
        assertThat(statusField).isInstanceOf(KeywordField::class)

        // Test Priority enum field
        val priorityField = QJavaTestDocument.priorityLevel
        assertThat(priorityField).isInstanceOf(KeywordField::class)

        // Test that field names are correct
        assertThat(statusField.name).isEqualTo("status")
        assertThat(priorityField.name).isEqualTo("priorityLevel")
    }

    @Test
    fun `enum fields should have correct paths`() {
        val statusField = QJavaTestDocument.status
        val priorityField = QJavaTestDocument.priorityLevel

        // Verify field paths
        assertThat(statusField.path).isEqualTo("status")
        assertThat(priorityField.path).isEqualTo("priorityLevel")

        // Verify field path properties
        assertThat(statusField.fieldPath.path).isEqualTo("status")
        assertThat(priorityField.fieldPath.path).isEqualTo("priorityLevel")
        assertThat(statusField.fieldPath.isNested).isEqualTo(false)
        assertThat(priorityField.fieldPath.isNested).isEqualTo(false)
    }

    @Test
    fun `enum fields from NestedTestDocument should also work correctly`() {
        val nestedStatusField = QNestedTestDocument.status

        assertThat(nestedStatusField).isInstanceOf(KeywordField::class)
        assertThat(nestedStatusField.name).isEqualTo("status")
        assertThat(nestedStatusField.path).isEqualTo("status")
    }

    @Test
    fun `verify enum types in nested document`() {
        // Test document structure
        assertThat(QJavaTestDocument.path).isEqualTo("")
        assertThat(QNestedTestDocument.path).isEqualTo("")

        // Test nested document enum fields
        val nestedStatus = QNestedTestDocument.status
        assertThat(nestedStatus.fieldPath.isNested).isEqualTo(false)
    }
}
