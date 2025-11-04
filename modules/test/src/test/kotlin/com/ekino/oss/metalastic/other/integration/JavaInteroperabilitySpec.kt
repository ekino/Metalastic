package com.metalastic.other.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test Java interoperability with QueryDSL style usage patterns */
class JavaInteroperabilitySpec :
  ShouldSpec({
    should("generate MetaJavaTestDocument.javaTestDocument from Java class") {
      // Verify that KSP can process plain Java classes
      MetaJavaTestDocument.javaTestDocument.indexName() shouldBe "java_test_document"
    }

    should("have accessible Java generated fields with correct types") {
      // Test basic field access
      MetaJavaTestDocument.javaTestDocument.id shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.title shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.description shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.priority shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.isActive shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.createdAt shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.score shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.category shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.address shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.tags shouldNotBe null
      MetaJavaTestDocument.javaTestDocument.multiFieldName shouldNotBe null
    }

    should("have correct paths for QueryDSL compatibility") {
      // Verify path generation follows QueryDSL conventions
      MetaJavaTestDocument.javaTestDocument.id.path() shouldBe "id"
      MetaJavaTestDocument.javaTestDocument.title.path() shouldBe "title"
      MetaJavaTestDocument.javaTestDocument.description.path() shouldBe "description"
      MetaJavaTestDocument.javaTestDocument.priority.path() shouldBe "priority"
      MetaJavaTestDocument.javaTestDocument.isActive.path() shouldBe "isActive"
      MetaJavaTestDocument.javaTestDocument.createdAt.path() shouldBe "createdAt"
      MetaJavaTestDocument.javaTestDocument.score.path() shouldBe "score"
      MetaJavaTestDocument.javaTestDocument.category.path() shouldBe "category"
      MetaJavaTestDocument.javaTestDocument.address.city.path() shouldBe "address.city"
      MetaJavaTestDocument.javaTestDocument.tags.name.path() shouldBe "tags.name"
      MetaJavaTestDocument.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"
    }

    should("follow QueryDSL naming conventions") {
      // Verify Q-class naming follows QueryDSL pattern
      val className = MetaJavaTestDocument.javaTestDocument::class.simpleName
      className shouldBe "MetaJavaTestDocument"

      // Verify package structure
      val packageName = MetaJavaTestDocument.javaTestDocument::class.java.packageName
      packageName shouldBe "com.metalastictest.integration"
    }

    should("generate Java-friendly code with proper annotations") {
      // The generated file should have @file:JvmName annotation for Java interop
      // This is tested by the fact that we can access it from Java code

      // Verify object is accessible from Java perspective
      val javaClassName = MetaJavaTestDocument.javaTestDocument::class.java.simpleName
      javaClassName shouldBe "MetaJavaTestDocument"
    }

    should("handle boolean properties with is prefix correctly") {
      // Java convention: boolean isActive -> getIsActive()
      // Verify our processor handles this correctly
      MetaJavaTestDocument.javaTestDocument.isActive.path() shouldBe "isActive"
      MetaJavaTestDocument.javaTestDocument.isActive.name() shouldBe "isActive"
    }
  })
