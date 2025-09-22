package com.metalastictest.integration;





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
        // Test accessing the generated Q-class from Java
        assertEquals("java_test_document", QJavaTestDocument.javaTestDocument.indexName());
        // Test direct field access (new approach)
        assertNotNull(QJavaTestDocument.javaTestDocument.id);
        assertNotNull(QJavaTestDocument.javaTestDocument.title);
        assertNotNull(QJavaTestDocument.javaTestDocument.description);
        assertNotNull(QJavaTestDocument.javaTestDocument.priority);
        assertNotNull(QJavaTestDocument.javaTestDocument.isActive);
        assertNotNull(QJavaTestDocument.javaTestDocument.createdAt);
        assertNotNull(QJavaTestDocument.javaTestDocument.score);
        assertNotNull(QJavaTestDocument.javaTestDocument.category);
        assertNotNull(QJavaTestDocument.javaTestDocument.address);
        assertNotNull(QJavaTestDocument.javaTestDocument.tags);
        assertNotNull(QJavaTestDocument.javaTestDocument.multiFieldName);
    }

    @Test
    public void shouldProvideCorrectPathsFromJava() {
        // Verify paths are accessible from Java using direct field access
        assertEquals("id", QJavaTestDocument.javaTestDocument.id.path());
        assertEquals("title", QJavaTestDocument.javaTestDocument.title.path());
        assertEquals("description", QJavaTestDocument.javaTestDocument.description.path());
        assertEquals("priority", QJavaTestDocument.javaTestDocument.priority.path());
        assertEquals("isActive", QJavaTestDocument.javaTestDocument.isActive.path());
        assertEquals("createdAt", QJavaTestDocument.javaTestDocument.createdAt.path());
        assertEquals("score", QJavaTestDocument.javaTestDocument.score.path());
        assertEquals("category", QJavaTestDocument.javaTestDocument.category.path());
        assertEquals("address.city", QJavaTestDocument.javaTestDocument.address.city.path());
        assertEquals("tags.name", QJavaTestDocument.javaTestDocument.tags.name.path());
        assertEquals("multiFieldName", QJavaTestDocument.javaTestDocument.multiFieldName.path());
    }

    @Test
    public void shouldFollowQueryDslPatterns() {
        // Demonstrate QueryDSL-like usage patterns from Java using direct field access
        
        // This is how it would be used in QueryDSL-style queries - much cleaner now!
        var idField = QJavaTestDocument.javaTestDocument.id;
        var titleField = QJavaTestDocument.javaTestDocument.title;
        var isActiveField = QJavaTestDocument.javaTestDocument.isActive;

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
        assertNotNull(QJavaTestDocument.javaTestDocument);
    }

    @Test
    public void shouldEnableDirectFieldAccessFromJava() {
        // Test the new direct @JvmField access pattern - this is what we wanted!
        // Now Java code can access QJavaTestDocument.javaTestDocument.id directly instead of QJavaTestDocument.javaTestDocument.INSTANCE.getId()
        
        // Verify direct field access works
        assertNotNull(QJavaTestDocument.javaTestDocument.id);
        assertNotNull(QJavaTestDocument.javaTestDocument.title);
        assertNotNull(QJavaTestDocument.javaTestDocument.description);
        assertNotNull(QJavaTestDocument.javaTestDocument.priority);
        assertNotNull(QJavaTestDocument.javaTestDocument.isActive);
        assertNotNull(QJavaTestDocument.javaTestDocument.createdAt);
        assertNotNull(QJavaTestDocument.javaTestDocument.score);
        assertNotNull(QJavaTestDocument.javaTestDocument.category);
        assertNotNull(QJavaTestDocument.javaTestDocument.address);
        assertNotNull(QJavaTestDocument.javaTestDocument.tags);
        
        // Verify field paths work correctly
        assertEquals("id", QJavaTestDocument.javaTestDocument.id.path());
        assertEquals("title", QJavaTestDocument.javaTestDocument.title.path());
        assertEquals("address", QJavaTestDocument.javaTestDocument.address.path());
        assertEquals("tags", QJavaTestDocument.javaTestDocument.tags.path());
        
        // Verify field names work correctly  
        assertEquals("id", QJavaTestDocument.javaTestDocument.id.name());
        assertEquals("title", QJavaTestDocument.javaTestDocument.title.name());
        assertEquals("address", QJavaTestDocument.javaTestDocument.address.name());
        assertEquals("tags", QJavaTestDocument.javaTestDocument.tags.name());

        // Test nested field access
        assertEquals("address.city", QJavaTestDocument.javaTestDocument.address.city.path());
        assertEquals("tags.name", QJavaTestDocument.javaTestDocument.tags.name.path());
        
        // This is exactly what the user wanted: clean, direct access!
        var idField = QJavaTestDocument.javaTestDocument.id;        // Instead of QJavaTestDocument.javaTestDocument.INSTANCE.getId()
        var titleField = QJavaTestDocument.javaTestDocument.title;  // Instead of QJavaTestDocument.javaTestDocument.INSTANCE.getTitle()
        var addressField = QJavaTestDocument.javaTestDocument.address; // Instead of QJavaTestDocument.javaTestDocument.INSTANCE.getAddress()
        
        assertNotNull(idField);
        assertNotNull(titleField);
        assertNotNull(addressField);
    }
}