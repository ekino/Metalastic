package com.metalastictest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test Java interoperability with QueryDSL style usage patterns */
class JavaInteroperabilitySpec :
  ShouldSpec({
    should("generate QJavaTestDocument.javaTestDocument from Java class") {
      // Verify that KSP can process plain Java classes
      QJavaTestDocument.javaTestDocument.indexName() shouldBe "java_test_document"
    }

    should("have accessible Java generated fields with correct types") {
      // Test basic field access
      QJavaTestDocument.javaTestDocument.id shouldNotBe null
      QJavaTestDocument.javaTestDocument.title shouldNotBe null
      QJavaTestDocument.javaTestDocument.description shouldNotBe null
      QJavaTestDocument.javaTestDocument.priority shouldNotBe null
      QJavaTestDocument.javaTestDocument.isActive shouldNotBe null
      QJavaTestDocument.javaTestDocument.createdAt shouldNotBe null
      QJavaTestDocument.javaTestDocument.score shouldNotBe null
      QJavaTestDocument.javaTestDocument.category shouldNotBe null
      QJavaTestDocument.javaTestDocument.address shouldNotBe null
      QJavaTestDocument.javaTestDocument.tags shouldNotBe null
      QJavaTestDocument.javaTestDocument.multiFieldName shouldNotBe null
    }

    should("have correct paths for QueryDSL compatibility") {
      // Verify path generation follows QueryDSL conventions
      QJavaTestDocument.javaTestDocument.id.path() shouldBe "id"
      QJavaTestDocument.javaTestDocument.title.path() shouldBe "title"
      QJavaTestDocument.javaTestDocument.description.path() shouldBe "description"
      QJavaTestDocument.javaTestDocument.priority.path() shouldBe "priority"
      QJavaTestDocument.javaTestDocument.isActive.path() shouldBe "isActive"
      QJavaTestDocument.javaTestDocument.createdAt.path() shouldBe "createdAt"
      QJavaTestDocument.javaTestDocument.score.path() shouldBe "score"
      QJavaTestDocument.javaTestDocument.category.path() shouldBe "category"
      QJavaTestDocument.javaTestDocument.address.city.path() shouldBe "address.city"
      QJavaTestDocument.javaTestDocument.tags.name.path() shouldBe "tags.name"
      QJavaTestDocument.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"
    }

    should("follow QueryDSL naming conventions") {
      // Verify Q-class naming follows QueryDSL pattern
      val className = QJavaTestDocument.javaTestDocument::class.simpleName
      className shouldBe "QJavaTestDocument"

      // Verify package structure
      val packageName = QJavaTestDocument.javaTestDocument::class.java.packageName
      packageName shouldBe "com.metalastictest.integration"
    }

    should("generate Java-friendly code with proper annotations") {
      // The generated file should have @file:JvmName annotation for Java interop
      // This is tested by the fact that we can access it from Java code

      // Verify object is accessible from Java perspective
      val javaClassName = QJavaTestDocument.javaTestDocument::class.java.simpleName
      javaClassName shouldBe "QJavaTestDocument"
    }

    should("handle boolean properties with is prefix correctly") {
      // Java convention: boolean isActive -> getIsActive()
      // Verify our processor handles this correctly
      QJavaTestDocument.javaTestDocument.isActive.path() shouldBe "isActive"
      QJavaTestDocument.javaTestDocument.isActive.name() shouldBe "isActive"
    }
  })
