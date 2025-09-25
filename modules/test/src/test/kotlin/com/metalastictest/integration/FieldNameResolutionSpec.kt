package com.metalastictest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FieldNameResolutionSpec :
  ShouldSpec({
    should("use annotation name when it's a valid identifier") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 1: Valid identifier - @Field(name = "valid") on isValid should use "valid" as property
      // name
      document.valid shouldNotBe null
      document.valid.path() shouldBe "valid"
    }

    should("keep original property name when annotation name doesn't follow conventions") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 2: Valid identifier but non-conventional (underscore) - @Field(name =
      // "search_content")
      // should keep original "searchableText" property name but use "search_content" as ES field
      // name
      document.searchableText shouldNotBe null
      document.searchableText.path() shouldBe "search_content"
    }

    should("handle invalid identifiers by keeping original property name") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 3: Invalid identifier - @Field(name = "987987_oi") should keep original "userName"
      document.userName shouldNotBe null
      document.userName.path() shouldBe "987987_oi" // ES field name from annotation
    }

    should("support MultiField with name attribute") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 4: MultiField with valid conventional name - @MultiField(mainField = @Field(name =
      // "fulltext"))
      // should use "fulltext" as property name
      document.fulltext shouldNotBe null
      document.fulltext.path() shouldBe "fulltext"

      // Case 5: MultiField with non-conventional name (underscore) - should keep original property
      // name
      document.description shouldNotBe null
      document.description.path() shouldBe "description_field"
    }

    should("support object and nested fields with name attribute") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 6: Object field with non-conventional name (underscore) - should keep original
      // property name
      document.profile shouldNotBe null
      document.profile.path() shouldBe "user_profile"

      // Case 7: Nested field with non-conventional name (underscore) - should keep original
      // property name
      document.contacts shouldNotBe null
      document.contacts.path() shouldBe "contact_methods"

      // Test nested object field names - also non-conventional, keep original names
      document.profile.name.path() shouldBe "user_profile.display_name"
      document.profile.email.path() shouldBe "user_profile.email_addr"

      document.contacts.type.path() shouldBe "contact_methods.contact_type"
      document.contacts.value.path() shouldBe "contact_methods.contact_value"
    }

    should("use default behavior when no name attribute is provided") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 8: No name attribute - should use property name
      document.age shouldNotBe null
      document.age.path() shouldBe "age"
      document.id.path() shouldBe "id"
    }

    should("maintain proper Java compatibility") {
      // Since we use @JvmField, properties should be accessible as static fields from Java
      val validField = QFieldNameResolutionTestDocument::class.java.getDeclaredField("valid")
      validField shouldNotBe null

      val searchableTextField =
        QFieldNameResolutionTestDocument::class.java.getDeclaredField("searchableText")
      searchableTextField shouldNotBe null

      val userNameField = QFieldNameResolutionTestDocument::class.java.getDeclaredField("userName")
      userNameField shouldNotBe null
    }
  })
