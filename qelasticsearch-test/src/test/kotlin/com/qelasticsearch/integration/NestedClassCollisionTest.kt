package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

/**
 * Test to verify that nested class collision handling works correctly.
 * Ensures that external and nested classes with the same simple name generate different Q-classes.
 */
class NestedClassCollisionTest {
    
    @Test
    fun `should generate separate Q-classes for external and nested classes with same name`() {
        // External JavaTag class should generate QJavaTag
        val externalTagField = QJavaTestDocument.tags
        assertThat(externalTagField).isNotNull()
        
        // Should have fields from external JavaTag class
        assertThat(externalTagField.name.path).isEqualTo("tags.name")
        assertThat(externalTagField.weight.path).isEqualTo("tags.weight")
        assertThat(externalTagField.description.path).isEqualTo("tags.description")
        
        // Nested JavaTag class should generate QJavaTestDocumentJavaTag
        val nestedTagField = QJavaTestDocument.tags2
        assertThat(nestedTagField).isNotNull()
        
        // Should have fields from nested JavaTestDocument.JavaTag class
        assertThat(nestedTagField.tagName.path).isEqualTo("tags2.tagName")
        assertThat(nestedTagField.size.path).isEqualTo("tags2.size")
        
        // Verify they are different Q-class types
        assertThat(externalTagField::class.simpleName).isEqualTo("QJavaTag")
        assertThat(nestedTagField::class.simpleName).isEqualTo("QJavaTestDocumentJavaTag")
    }
    
    @Test
    fun `should correctly resolve field paths for both external and nested classes`() {
        // External class field paths
        assertThat(QJavaTestDocument.tags.name.fieldPath.path).isEqualTo("tags.name")
        assertThat(QJavaTestDocument.tags.weight.fieldPath.path).isEqualTo("tags.weight")
        assertThat(QJavaTestDocument.tags.description.fieldPath.path).isEqualTo("tags.description")
        
        // Nested class field paths
        assertThat(QJavaTestDocument.tags2.tagName.fieldPath.path).isEqualTo("tags2.tagName")
        assertThat(QJavaTestDocument.tags2.size.fieldPath.path).isEqualTo("tags2.size")
        
        // Both should be nested fields
        assertThat(QJavaTestDocument.tags.name.fieldPath.isNested).isEqualTo(true)
        assertThat(QJavaTestDocument.tags2.tagName.fieldPath.isNested).isEqualTo(true)
    }
}