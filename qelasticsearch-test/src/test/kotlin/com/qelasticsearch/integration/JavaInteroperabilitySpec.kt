package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Test Java interoperability with QueryDSL style usage patterns
 */
class JavaInteroperabilitySpec :
    ShouldSpec({

        should("generate QJavaTestDocument from Java class") {
            // Verify that KSP can process plain Java classes
            QJavaTestDocument.indexName shouldBe "java_test_document"
        }

        should("have accessible Java generated fields with correct types") {
            // Test basic field access
            QJavaTestDocument.id shouldNotBe null
            QJavaTestDocument.title shouldNotBe null
            QJavaTestDocument.description shouldNotBe null
            QJavaTestDocument.priority shouldNotBe null
            QJavaTestDocument.isActive shouldNotBe null
            QJavaTestDocument.createdAt shouldNotBe null
            QJavaTestDocument.score shouldNotBe null
            QJavaTestDocument.category shouldNotBe null
            QJavaTestDocument.address shouldNotBe null
            QJavaTestDocument.tags shouldNotBe null
            QJavaTestDocument.multiFieldName shouldNotBe null
        }

        should("have correct paths for QueryDSL compatibility") {
            // Verify path generation follows QueryDSL conventions
            QJavaTestDocument.id.path() shouldBe "id"
            QJavaTestDocument.title.path() shouldBe "title"
            QJavaTestDocument.description.path() shouldBe "description"
            QJavaTestDocument.priority.path() shouldBe "priority"
            QJavaTestDocument.isActive.path() shouldBe "isActive"
            QJavaTestDocument.createdAt.path() shouldBe "createdAt"
            QJavaTestDocument.score.path() shouldBe "score"
            QJavaTestDocument.category.path() shouldBe "category"
            QJavaTestDocument.address.city
                .path() shouldBe "address.city"
            QJavaTestDocument.tags.name
                .path() shouldBe "tags.name"
            QJavaTestDocument.multiFieldName.path() shouldBe "multiFieldName"
        }

        should("follow QueryDSL naming conventions") {
            // Verify Q-class naming follows QueryDSL pattern
            val className = QJavaTestDocument::class.simpleName
            className shouldBe "QJavaTestDocument"

            // Verify package structure
            val packageName = QJavaTestDocument::class.java.packageName
            packageName shouldBe "com.qelasticsearch.integration"
        }

        should("generate Java-friendly code with proper annotations") {
            // The generated file should have @file:JvmName annotation for Java interop
            // This is tested by the fact that we can access it from Java code

            // Verify object is accessible from Java perspective
            val javaClassName = QJavaTestDocument::class.java.simpleName
            javaClassName shouldBe "QJavaTestDocument"
        }

        should("handle boolean properties with is prefix correctly") {
            // Java convention: boolean isActive -> getIsActive()
            // Verify our processor handles this correctly
            QJavaTestDocument.isActive.path() shouldBe "isActive"
            QJavaTestDocument.isActive.name() shouldBe "isActive"
        }
    })
