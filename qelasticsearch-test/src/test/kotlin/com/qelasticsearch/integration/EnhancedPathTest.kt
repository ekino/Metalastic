package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import com.qelasticsearch.dsl.FieldPath

class EnhancedPathTest {

    @Test
    fun `FieldPath should correctly identify nested vs non-nested paths`() {
        // Simple path (not nested)
        val simplePath = FieldPath.simple("name")
        assertThat(simplePath.path).isEqualTo("name")
        assertThat(simplePath.isNested).isFalse()
        assertThat(simplePath.nestedSegments).isEmpty()
        
        // Nested path
        val nestedPath = FieldPath.nested("activities")
        assertThat(nestedPath.path).isEqualTo("activities")
        assertThat(nestedPath.isNested).isTrue()
        assertThat(nestedPath.nestedSegments).containsExactly("activities")
    }
    
    @Test
    fun `FieldPath should correctly handle complex nested structures`() {
        // Create a path with multiple nested segments
        val complexPath = FieldPath("user.addresses.city", listOf("user.addresses"))
        assertThat(complexPath.path).isEqualTo("user.addresses.city")
        assertThat(complexPath.isNested).isTrue()
        assertThat(complexPath.nestedSegments).containsExactly("user.addresses")
        assertThat(complexPath.isCompletelyNested).isTrue()
        assertThat(complexPath.rootNestedPath).isEqualTo("user.addresses")
    }
    
    @Test
    fun `FieldPath child creation should work correctly`() {
        val parent = FieldPath.simple("user")
        val objectChild = parent.child("name")
        val nestedChild = parent.child("addresses", isChildNested = true)
        
        assertThat(objectChild.path).isEqualTo("user.name")
        assertThat(objectChild.isNested).isFalse()
        
        assertThat(nestedChild.path).isEqualTo("user.addresses")
        assertThat(nestedChild.isNested).isTrue()
        assertThat(nestedChild.nestedSegments).containsExactly("user.addresses")
    }
}