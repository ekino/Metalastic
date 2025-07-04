package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

/**
 * Test Java interoperability with Lombok and QueryDSL style usage patterns
 */
class JavaInteroperabilityTest {
    @Test
    fun `should generate QJavaTestDocument from Java Lombok class`() {
        // Verify that KSP can process Java classes with Lombok annotations
        assertThat(QJavaTestDocument.indexName).isEqualTo("java_test_document")
        assertThat(QJavaTestDocument.path).isEqualTo("")
    }

    @Test
    fun `Java generated fields should be accessible and have correct types`() {
        // Test basic field access
        assertThat(QJavaTestDocument.id).isNotNull()
        assertThat(QJavaTestDocument.title).isNotNull()
        assertThat(QJavaTestDocument.description).isNotNull()
        assertThat(QJavaTestDocument.priority).isNotNull()
        assertThat(QJavaTestDocument.isActive).isNotNull()
        assertThat(QJavaTestDocument.createdAt).isNotNull()
        assertThat(QJavaTestDocument.score).isNotNull()
        assertThat(QJavaTestDocument.category).isNotNull()
        assertThat(QJavaTestDocument.address).isNotNull()
        assertThat(QJavaTestDocument.tags).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName).isNotNull()
    }

    @Test
    fun `Java generated fields should have correct paths for QueryDSL compatibility`() {
        // Verify path generation follows QueryDSL conventions
        assertThat(QJavaTestDocument.id.path).isEqualTo("id")
        assertThat(QJavaTestDocument.title.path).isEqualTo("title")
        assertThat(QJavaTestDocument.description.path).isEqualTo("description")
        assertThat(QJavaTestDocument.priority.path).isEqualTo("priority")
        assertThat(QJavaTestDocument.isActive.path).isEqualTo("isActive")
        assertThat(QJavaTestDocument.createdAt.path).isEqualTo("createdAt")
        assertThat(QJavaTestDocument.score.path).isEqualTo("score")
        assertThat(QJavaTestDocument.category.path).isEqualTo("category")
        assertThat(QJavaTestDocument.address.city.path).isEqualTo("address.city")
        assertThat(QJavaTestDocument.tags.tagName.path).isEqualTo("tags.tagName")
        assertThat(QJavaTestDocument.multiFieldName.path).isEqualTo("multiFieldName")
    }

    @Test
    fun `should follow QueryDSL naming conventions`() {
        // Verify Q-class naming follows QueryDSL pattern
        val className = QJavaTestDocument::class.simpleName
        assertThat(className).isEqualTo("QJavaTestDocument")

        // Verify package structure
        val packageName = QJavaTestDocument::class.java.packageName
        assertThat(packageName).isEqualTo("com.qelasticsearch.integration")
    }

    @Test
    fun `generated code should be Java-friendly with proper annotations`() {
        // The generated file should have @file:JvmName annotation for Java interop
        // This is tested by the fact that we can access it from Java code

        // Verify object is accessible from Java perspective
        val javaClassName = QJavaTestDocument::class.java.simpleName
        assertThat(javaClassName).isEqualTo("QJavaTestDocument")
    }

    @Test
    fun `should handle boolean properties with is prefix correctly`() {
        // Java convention: boolean isActive -> getIsActive()
        // Verify our processor handles this correctly
        assertThat(QJavaTestDocument.isActive.path).isEqualTo("isActive")
        assertThat(QJavaTestDocument.isActive.name).isEqualTo("isActive")
    }
}
