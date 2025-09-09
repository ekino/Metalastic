package com.qelasticsearchtest.integration

import com.qelasticsearchtest.metamodels.main.Metamodels
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Test Java interoperability with QueryDSL style usage patterns */
class JavaInteroperabilitySpec :
  ShouldSpec({
    should("generate Metamodels.javaTestDocument from Java class") {
      // Verify that KSP can process plain Java classes
      Metamodels.javaTestDocument.indexName() shouldBe "java_test_document"
    }

    should("have accessible Java generated fields with correct types") {
      // Test basic field access
      Metamodels.javaTestDocument.id shouldNotBe null
      Metamodels.javaTestDocument.title shouldNotBe null
      Metamodels.javaTestDocument.description shouldNotBe null
      Metamodels.javaTestDocument.priority shouldNotBe null
      Metamodels.javaTestDocument.isActive shouldNotBe null
      Metamodels.javaTestDocument.createdAt shouldNotBe null
      Metamodels.javaTestDocument.score shouldNotBe null
      Metamodels.javaTestDocument.category shouldNotBe null
      Metamodels.javaTestDocument.address shouldNotBe null
      Metamodels.javaTestDocument.tags shouldNotBe null
      Metamodels.javaTestDocument.multiFieldName shouldNotBe null
    }

    should("have correct paths for QueryDSL compatibility") {
      // Verify path generation follows QueryDSL conventions
      Metamodels.javaTestDocument.id.path() shouldBe "id"
      Metamodels.javaTestDocument.title.path() shouldBe "title"
      Metamodels.javaTestDocument.description.path() shouldBe "description"
      Metamodels.javaTestDocument.priority.path() shouldBe "priority"
      Metamodels.javaTestDocument.isActive.path() shouldBe "isActive"
      Metamodels.javaTestDocument.createdAt.path() shouldBe "createdAt"
      Metamodels.javaTestDocument.score.path() shouldBe "score"
      Metamodels.javaTestDocument.category.path() shouldBe "category"
      Metamodels.javaTestDocument.address.city.path() shouldBe "address.city"
      Metamodels.javaTestDocument.tags.name.path() shouldBe "tags.name"
      Metamodels.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"
    }

    should("follow QueryDSL naming conventions") {
      // Verify Q-class naming follows QueryDSL pattern
      val className = Metamodels.javaTestDocument::class.simpleName
      className shouldBe "QJavaTestDocument"

      // Verify package structure
      val packageName = Metamodels.javaTestDocument::class.java.packageName
      packageName shouldBe "com.qelasticsearchtest.integration"
    }

    should("generate Java-friendly code with proper annotations") {
      // The generated file should have @file:JvmName annotation for Java interop
      // This is tested by the fact that we can access it from Java code

      // Verify object is accessible from Java perspective
      val javaClassName = Metamodels.javaTestDocument::class.java.simpleName
      javaClassName shouldBe "QJavaTestDocument"
    }

    should("handle boolean properties with is prefix correctly") {
      // Java convention: boolean isActive -> getIsActive()
      // Verify our processor handles this correctly
      Metamodels.javaTestDocument.isActive.path() shouldBe "isActive"
      Metamodels.javaTestDocument.isActive.name() shouldBe "isActive"
    }
  })
