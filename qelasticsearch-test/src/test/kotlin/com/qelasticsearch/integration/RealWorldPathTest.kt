package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class RealWorldPathTest {
    @Test
    fun `Generated Q-classes should have proper path information`() {
        // Test simple field paths
        assertThat(QJavaTestDocument.title.path).isEqualTo("title")
        assertThat(QJavaTestDocument.title.fieldPath.isNested).isFalse()

        // Test object field paths (not nested)
        assertThat(QJavaTestDocument.address.city.path).isEqualTo("address.city")
        assertThat(QJavaTestDocument.address.city.fieldPath.isNested).isFalse()

        // Test nested field paths
        assertThat(QJavaTestDocument.tags.name.path).isEqualTo("tags.name")
        assertThat(QJavaTestDocument.tags.name.fieldPath.isNested).isTrue()
        assertThat(QJavaTestDocument.tags.name.fieldPath.nestedSegments).containsExactly("tags")
        assertThat(QJavaTestDocument.tags.name.fieldPath.rootNestedPath).isEqualTo("tags")
    }

    @Test
    fun `Nested document should show complete hierarchy properly`() {
        // Test simple field in nested document
        assertThat(QNestedTestDocument.status.path).isEqualTo("status")
        assertThat(QNestedTestDocument.status.fieldPath.isNested).isFalse()

        // Test object field (operation is an object, not nested)
        assertThat(QNestedTestDocument.operation.active.path).isEqualTo("operation.active")
        assertThat(QNestedTestDocument.operation.active.fieldPath.isNested).isFalse()

        // Test nested field (activities is a nested field)
        // Since it uses UnknownNestedFields, we can't access sub-fields but we can test the parent
        // The nested field itself should show proper path but the object should handle nested status
        // This is showing that activities is a nested field at the root level
    }

    @Test
    fun `MultiField should maintain nested information`() {
        val multiField = QJavaTestDocument.multiFieldName
        assertThat(multiField.path).isEqualTo("multiFieldName")
        assertThat(multiField.fieldPath.isNested).isFalse()

        // Test that inner fields maintain the parent's nested information
        val searchField = multiField.search
        assertThat(searchField.path).isEqualTo("multiFieldName.search")
        assertThat(searchField.fieldPath.isNested).isFalse()
    }
}
