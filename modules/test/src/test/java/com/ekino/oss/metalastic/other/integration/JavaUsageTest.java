/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/) 
 */

package com.ekino.oss.metalastic.other.integration;





import com.ekino.oss.metalastic.integration.JavaTestDocument;
import com.ekino.oss.metalastic.integration.MetaJavaTestDocument;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Demonstrates usage of Metalastic from Java code,
 * showcasing interoperability with Java + QueryDSL patterns
 */
public class JavaUsageTest {

    @Test
    public void shouldAccessGeneratedFieldsFromJava() {
        // Test accessing the generated Meta-class from Java
        assertEquals("java_test_document", MetaJavaTestDocument.javaTestDocument.indexName());
        // Test direct field access (new approach)
        assertNotNull(MetaJavaTestDocument.javaTestDocument.id);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.title);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.description);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.priority);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.isActive);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.createdAt);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.score);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.category);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.address);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.tags);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.multiFieldName);
    }

    @Test
    public void shouldProvideCorrectPathsFromJava() {
        // Verify paths are accessible from Java using direct field access
        assertEquals("id", MetaJavaTestDocument.javaTestDocument.id.path());
        assertEquals("title", MetaJavaTestDocument.javaTestDocument.title.path());
        assertEquals("description", MetaJavaTestDocument.javaTestDocument.description.path());
        assertEquals("priority", MetaJavaTestDocument.javaTestDocument.priority.path());
        assertEquals("isActive", MetaJavaTestDocument.javaTestDocument.isActive.path());
        assertEquals("createdAt", MetaJavaTestDocument.javaTestDocument.createdAt.path());
        assertEquals("score", MetaJavaTestDocument.javaTestDocument.score.path());
        assertEquals("category", MetaJavaTestDocument.javaTestDocument.category.path());
        assertEquals("address.city", MetaJavaTestDocument.javaTestDocument.address.city.path());
        assertEquals("tags.name", MetaJavaTestDocument.javaTestDocument.tags.name.path());
        assertEquals("multiFieldName", MetaJavaTestDocument.javaTestDocument.multiFieldName.path());
    }

    @Test
    public void shouldFollowQueryDslPatterns() {
        // Demonstrate QueryDSL-like usage patterns from Java using direct field access

        // This is how it would be used in QueryDSL-style queries - much cleaner now!
        var idField = MetaJavaTestDocument.javaTestDocument.id;
        var titleField = MetaJavaTestDocument.javaTestDocument.title;
        var isActiveField = MetaJavaTestDocument.javaTestDocument.isActive;

        // Field names should match the original property names
        assertEquals("id", idField.name());
        assertEquals("title", titleField.name());
        assertEquals("isActive", isActiveField.name());

        // Paths should be suitable for Elasticsearch queries
        assertEquals("id", idField.path());
        assertEquals("title", titleField.path());
        assertEquals("isActive", isActiveField.path());
    }

    @Test
    public void shouldWorkWithJavaClasses() {
        // The fact that this test compiles and runs proves that KSP
        // can process plain Java getters/setters correctly

        // Create a test document (this uses standard Java constructors)
        var testDoc = new JavaTestDocument();
        testDoc.setId("test-123");
        testDoc.setTitle("Test Document");
        testDoc.setIsActive(true);

        // Verify standard Java methods work
        assertEquals("test-123", testDoc.getId());
        assertEquals("Test Document", testDoc.getTitle());
        assertTrue(testDoc.getIsActive());

        // And our Meta-class was generated from this Java class
        assertNotNull(MetaJavaTestDocument.javaTestDocument);
    }

    @Test
    public void shouldEnableDirectFieldAccessFromJava() {
        // Test the new direct @JvmField access pattern - this is what we wanted!
        // Now Java code can access MetaJavaTestDocument.javaTestDocument.id directly instead of MetaJavaTestDocument.javaTestDocument.INSTANCE.getId()

        // Verify direct field access works
        assertNotNull(MetaJavaTestDocument.javaTestDocument.id);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.title);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.description);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.priority);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.isActive);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.createdAt);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.score);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.category);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.address);
        assertNotNull(MetaJavaTestDocument.javaTestDocument.tags);

        // Verify field paths work correctly
        assertEquals("id", MetaJavaTestDocument.javaTestDocument.id.path());
        assertEquals("title", MetaJavaTestDocument.javaTestDocument.title.path());
        assertEquals("address", MetaJavaTestDocument.javaTestDocument.address.path());
        assertEquals("tags", MetaJavaTestDocument.javaTestDocument.tags.path());

        // Verify field names work correctly
        assertEquals("id", MetaJavaTestDocument.javaTestDocument.id.name());
        assertEquals("title", MetaJavaTestDocument.javaTestDocument.title.name());
        assertEquals("address", MetaJavaTestDocument.javaTestDocument.address.name());
        assertEquals("tags", MetaJavaTestDocument.javaTestDocument.tags.name());

        // Test nested field access
        assertEquals("address.city", MetaJavaTestDocument.javaTestDocument.address.city.path());
        assertEquals("tags.name", MetaJavaTestDocument.javaTestDocument.tags.name.path());

        // This is exactly what the user wanted: clean, direct access!
        var idField = MetaJavaTestDocument.javaTestDocument.id;        // Instead of MetaJavaTestDocument.javaTestDocument.INSTANCE.getId()
        var titleField = MetaJavaTestDocument.javaTestDocument.title;  // Instead of MetaJavaTestDocument.javaTestDocument.INSTANCE.getTitle()
        var addressField = MetaJavaTestDocument.javaTestDocument.address; // Instead of MetaJavaTestDocument.javaTestDocument.INSTANCE.getAddress()
        
        assertNotNull(idField);
        assertNotNull(titleField);
        assertNotNull(addressField);
    }
}