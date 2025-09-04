package com.qelasticsearch.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Demonstrates usage of QElasticsearch from Java code,
 * showcasing interoperability with Java + QueryDSL patterns
 */
public class JavaUsageTest {

    @Test
    public void shouldAccessGeneratedFieldsFromJava() {
        // Test accessing the generated Q-class from Java
        assertEquals("java_test_document", QJavaTestDocument.INSTANCE.indexName());
        // Test direct field access (new approach)
        assertNotNull(QJavaTestDocument.id);
        assertNotNull(QJavaTestDocument.title);
        assertNotNull(QJavaTestDocument.description);
        assertNotNull(QJavaTestDocument.priority);
        assertNotNull(QJavaTestDocument.isActive);
        assertNotNull(QJavaTestDocument.createdAt);
        assertNotNull(QJavaTestDocument.score);
        assertNotNull(QJavaTestDocument.category);
        assertNotNull(QJavaTestDocument.address);
        assertNotNull(QJavaTestDocument.tags);
        assertNotNull(QJavaTestDocument.multiFieldName);
    }

    @Test
    public void shouldProvideCorrectPathsFromJava() {
        // Verify paths are accessible from Java using direct field access
        assertEquals("id", QJavaTestDocument.id.path());
        assertEquals("title", QJavaTestDocument.title.path());
        assertEquals("description", QJavaTestDocument.description.path());
        assertEquals("priority", QJavaTestDocument.priority.path());
        assertEquals("isActive", QJavaTestDocument.isActive.path());
        assertEquals("createdAt", QJavaTestDocument.createdAt.path());
        assertEquals("score", QJavaTestDocument.score.path());
        assertEquals("category", QJavaTestDocument.category.path());
        assertEquals("address.city", QJavaTestDocument.address.city.path());
        assertEquals("tags.name", QJavaTestDocument.tags.name.path());
        assertEquals("multiFieldName", QJavaTestDocument.multiFieldName.path());
    }

    @Test
    public void shouldFollowQueryDslPatterns() {
        // Demonstrate QueryDSL-like usage patterns from Java using direct field access
        
        // This is how it would be used in QueryDSL-style queries - much cleaner now!
        var idField = QJavaTestDocument.id;
        var titleField = QJavaTestDocument.title;
        var isActiveField = QJavaTestDocument.isActive;

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

        // And our Q-class was generated from this Java class
        assertNotNull(QJavaTestDocument.INSTANCE);
    }

    @Test
    public void shouldEnableDirectFieldAccessFromJava() {
        // Test the new direct @JvmField access pattern - this is what we wanted!
        // Now Java code can access QJavaTestDocument.id directly instead of QJavaTestDocument.INSTANCE.getId()
        
        // Verify direct field access works
        assertNotNull(QJavaTestDocument.id);
        assertNotNull(QJavaTestDocument.title);
        assertNotNull(QJavaTestDocument.description);
        assertNotNull(QJavaTestDocument.priority);
        assertNotNull(QJavaTestDocument.isActive);
        assertNotNull(QJavaTestDocument.createdAt);
        assertNotNull(QJavaTestDocument.score);
        assertNotNull(QJavaTestDocument.category);
        assertNotNull(QJavaTestDocument.address);
        assertNotNull(QJavaTestDocument.tags);
        
        // Verify field paths work correctly
        assertEquals("id", QJavaTestDocument.id.path());
        assertEquals("title", QJavaTestDocument.title.path());
        assertEquals("address", QJavaTestDocument.address.path());
        assertEquals("tags", QJavaTestDocument.tags.path());
        
        // Verify field names work correctly  
        assertEquals("id", QJavaTestDocument.id.name());
        assertEquals("title", QJavaTestDocument.title.name());
        assertEquals("address", QJavaTestDocument.address.name());
        assertEquals("tags", QJavaTestDocument.tags.name());

        // Test nested field access
        assertEquals("address.city", QJavaTestDocument.address.city.path());
        assertEquals("tags.name", QJavaTestDocument.tags.name.path());
        
        // This is exactly what the user wanted: clean, direct access!
        var idField = QJavaTestDocument.id;        // Instead of QJavaTestDocument.INSTANCE.getId()
        var titleField = QJavaTestDocument.title;  // Instead of QJavaTestDocument.INSTANCE.getTitle()
        var addressField = QJavaTestDocument.address; // Instead of QJavaTestDocument.INSTANCE.getAddress()
        
        assertNotNull(idField);
        assertNotNull(titleField);
        assertNotNull(addressField);
    }
}