/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.other.integration

import com.example.test.metamodels.TestMetamodels
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Verifies that Java records annotated with `@Document` and record components reached via
 * `@Field(Object)` / `@Field(Nested)` produce a complete generated metamodel.
 */
class JavaRecordDocumentSpec :
  ShouldSpec({
    should("generate metamodel for @Document Java record with all record components") {
      MetaDocumentInTest.documentInTest.indexName() shouldBe "document_in_test"

      MetaDocumentInTest.documentInTest.id shouldNotBe null
      MetaDocumentInTest.documentInTest.title shouldNotBe null
      MetaDocumentInTest.documentInTest.code shouldNotBe null
      MetaDocumentInTest.documentInTest.address shouldNotBe null
      MetaDocumentInTest.documentInTest.tags shouldNotBe null
    }

    should("ignore record components that are not @Field-annotated") {
      val instanceFields =
        MetaDocumentInTest::class
          .java
          .declaredFields
          .filter { field ->
            java.lang.reflect.Modifier.isPublic(field.modifiers) &&
              !java.lang.reflect.Modifier.isStatic(field.modifiers) &&
              !field.name.contains('$')
          }
          .map { it.name }
          .toSet()
      instanceFields shouldBe setOf("id", "title", "code", "address", "tags")
    }

    should("produce correct paths for simple record components") {
      MetaDocumentInTest.documentInTest.id.path() shouldBe "id"
      MetaDocumentInTest.documentInTest.title.path() shouldBe "title"
      MetaDocumentInTest.documentInTest.code.path() shouldBe "code"
    }

    should("traverse into a nested record reached via @Field(Object)") {
      MetaDocumentInTest.documentInTest.address.street.path() shouldBe "address.street"
      MetaDocumentInTest.documentInTest.address.city.path() shouldBe "address.city"
      MetaDocumentInTest.documentInTest.address.city.isNestedPath() shouldBe false
    }

    should("traverse into a nested record reached via @Field(Nested)") {
      MetaDocumentInTest.documentInTest.tags.name.path() shouldBe "tags.name"
      MetaDocumentInTest.documentInTest.tags.weight.path() shouldBe "tags.weight"
      MetaDocumentInTest.documentInTest.tags.name.isNestedPath() shouldBe true
      MetaDocumentInTest.documentInTest.tags.name.nestedPaths().toList() shouldBe listOf("tags")
    }

    should("expose multi-field inner variants from a record component") {
      MetaDocumentInTest.documentInTest.code.search.path() shouldBe "code.search"
    }

    should("expose the parent chain for nested record fields") {
      MetaDocumentInTest.documentInTest.address.city.parents().toList() shouldContain
        MetaDocumentInTest.documentInTest.address
    }

    should("register the record document in the Metamodels registry") {
      val registered = TestMetamodels.entries().toList()
      registered shouldContain MetaDocumentInTest.documentInTest
      // Nested records are not @Document and must stay out of the registry
      registered.map { it.indexName() } shouldNotBe listOf("address", "tags")
    }
  })
