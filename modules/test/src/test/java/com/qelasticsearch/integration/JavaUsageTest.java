package com.qelasticsearch.integration;

import com.qelasticsearch.Metamodels;

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
        assertEquals("java_test_document", Metamodels.javaTestDocument.indexName());
        // Test direct field access (new approach)
        assertNotNull(Metamodels.javaTestDocument.id);
        assertNotNull(Metamodels.javaTestDocument.title);
        assertNotNull(Metamodels.javaTestDocument.description);
        assertNotNull(Metamodels.javaTestDocument.priority);
        assertNotNull(Metamodels.javaTestDocument.isActive);
        assertNotNull(Metamodels.javaTestDocument.createdAt);
        assertNotNull(Metamodels.javaTestDocument.score);
        assertNotNull(Metamodels.javaTestDocument.category);
        assertNotNull(Metamodels.javaTestDocument.address);
        assertNotNull(Metamodels.javaTestDocument.tags);
        assertNotNull(Metamodels.javaTestDocument.multiFieldName);
    }

    @Test
    public void shouldProvideCorrectPathsFromJava() {
        // Verify paths are accessible from Java using direct field access
        assertEquals("id", Metamodels.javaTestDocument.id.path());
        assertEquals("title", Metamodels.javaTestDocument.title.path());
        assertEquals("description", Metamodels.javaTestDocument.description.path());
        assertEquals("priority", Metamodels.javaTestDocument.priority.path());
        assertEquals("isActive", Metamodels.javaTestDocument.isActive.path());
        assertEquals("createdAt", Metamodels.javaTestDocument.createdAt.path());
        assertEquals("score", Metamodels.javaTestDocument.score.path());
        assertEquals("category", Metamodels.javaTestDocument.category.path());
        assertEquals("address.city", Metamodels.javaTestDocument.address.city.path());
        assertEquals("tags.name", Metamodels.javaTestDocument.tags.name.path());
        assertEquals("multiFieldName", Metamodels.javaTestDocument.multiFieldName.path());
    }

    @Test
    public void shouldFollowQueryDslPatterns() {
        // Demonstrate QueryDSL-like usage patterns from Java using direct field access
        
        // This is how it would be used in QueryDSL-style queries - much cleaner now!
        var idField = Metamodels.javaTestDocument.id;
        var titleField = Metamodels.javaTestDocument.title;
        var isActiveField = Metamodels.javaTestDocument.isActive;

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
        assertNotNull(Metamodels.javaTestDocument);
    }

    @Test
    public void shouldEnableDirectFieldAccessFromJava() {
        // Test the new direct @JvmField access pattern - this is what we wanted!
        // Now Java code can access Metamodels.javaTestDocument.id directly instead of Metamodels.javaTestDocument.INSTANCE.getId()
        
        // Verify direct field access works
        assertNotNull(Metamodels.javaTestDocument.id);
        assertNotNull(Metamodels.javaTestDocument.title);
        assertNotNull(Metamodels.javaTestDocument.description);
        assertNotNull(Metamodels.javaTestDocument.priority);
        assertNotNull(Metamodels.javaTestDocument.isActive);
        assertNotNull(Metamodels.javaTestDocument.createdAt);
        assertNotNull(Metamodels.javaTestDocument.score);
        assertNotNull(Metamodels.javaTestDocument.category);
        assertNotNull(Metamodels.javaTestDocument.address);
        assertNotNull(Metamodels.javaTestDocument.tags);
        
        // Verify field paths work correctly
        assertEquals("id", Metamodels.javaTestDocument.id.path());
        assertEquals("title", Metamodels.javaTestDocument.title.path());
        assertEquals("address", Metamodels.javaTestDocument.address.path());
        assertEquals("tags", Metamodels.javaTestDocument.tags.path());
        
        // Verify field names work correctly  
        assertEquals("id", Metamodels.javaTestDocument.id.name());
        assertEquals("title", Metamodels.javaTestDocument.title.name());
        assertEquals("address", Metamodels.javaTestDocument.address.name());
        assertEquals("tags", Metamodels.javaTestDocument.tags.name());

        // Test nested field access
        assertEquals("address.city", Metamodels.javaTestDocument.address.city.path());
        assertEquals("tags.name", Metamodels.javaTestDocument.tags.name.path());
        
        // This is exactly what the user wanted: clean, direct access!
        var idField = Metamodels.javaTestDocument.id;        // Instead of Metamodels.javaTestDocument.INSTANCE.getId()
        var titleField = Metamodels.javaTestDocument.title;  // Instead of Metamodels.javaTestDocument.INSTANCE.getTitle()
        var addressField = Metamodels.javaTestDocument.address; // Instead of Metamodels.javaTestDocument.INSTANCE.getAddress()
        
        assertNotNull(idField);
        assertNotNull(titleField);
        assertNotNull(addressField);
    }
}