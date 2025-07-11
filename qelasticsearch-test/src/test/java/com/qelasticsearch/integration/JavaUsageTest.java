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
        assertEquals("java_test_document", QJavaTestDocument.INSTANCE.getIndexName());

        // Test field access
        assertNotNull(QJavaTestDocument.INSTANCE.getId());
        assertNotNull(QJavaTestDocument.INSTANCE.getTitle());
        assertNotNull(QJavaTestDocument.INSTANCE.getDescription());
        assertNotNull(QJavaTestDocument.INSTANCE.getPriority());
        assertNotNull(QJavaTestDocument.INSTANCE.isActive());
        assertNotNull(QJavaTestDocument.INSTANCE.getCreatedAt());
        assertNotNull(QJavaTestDocument.INSTANCE.getScore());
        assertNotNull(QJavaTestDocument.INSTANCE.getCategory());
        assertNotNull(QJavaTestDocument.INSTANCE.getAddress());
        assertNotNull(QJavaTestDocument.INSTANCE.getTags());
        assertNotNull(QJavaTestDocument.INSTANCE.getMultiFieldName());
    }

    @Test
    public void shouldProvideCorrectPathsFromJava() {
        // Verify paths are accessible from Java
        assertEquals("id", QJavaTestDocument.INSTANCE.getId().path());
        assertEquals("title", QJavaTestDocument.INSTANCE.getTitle().path());
        assertEquals("description", QJavaTestDocument.INSTANCE.getDescription().path());
        assertEquals("priority", QJavaTestDocument.INSTANCE.getPriority().path());
        assertEquals("isActive", QJavaTestDocument.INSTANCE.isActive().path());
        assertEquals("createdAt", QJavaTestDocument.INSTANCE.getCreatedAt().path());
        assertEquals("score", QJavaTestDocument.INSTANCE.getScore().path());
        assertEquals("category", QJavaTestDocument.INSTANCE.getCategory().path());
        assertEquals("address.city", QJavaTestDocument.INSTANCE.getAddress().getCity().path());
        assertEquals("tags.name", QJavaTestDocument.INSTANCE.getTags().getName().path());
        assertEquals("multiFieldName", QJavaTestDocument.INSTANCE.getMultiFieldName().path());
    }

    @Test
    public void shouldFollowQueryDslPatterns() {
        // Demonstrate QueryDSL-like usage patterns from Java
        var document = QJavaTestDocument.INSTANCE;

        // This is how it would be used in QueryDSL-style queries
        var idField = document.getId();
        var titleField = document.getTitle();
        var isActiveField = document.isActive();

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
}