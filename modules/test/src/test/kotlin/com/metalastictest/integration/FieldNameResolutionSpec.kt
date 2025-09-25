package com.metalastictest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FieldNameResolutionSpec :
  ShouldSpec({
    should("support @Field(name) attribute with conservative property naming") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 1: Conservative approach - @Field(name = "valid") on isValid keeps "isValid" property
      // name
      // but uses "valid" as Elasticsearch field name
      document.isValid shouldNotBe null
      document.isValid.path() shouldBe "valid"
    }

    should("keep original property names when annotation name doesn't improve conventions") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 2: Keep original - @Field(name = "search_content") on searchableText should stay
      // "searchableText"
      document.searchableText shouldNotBe null
      document.searchableText.path() shouldBe
        "search_content" // But ES field name should be from annotation
    }

    should("handle invalid identifiers by keeping original property name") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 3: Invalid identifier - @Field(name = "987987_oi") should keep original "userName"
      document.userName shouldNotBe null
      document.userName.path() shouldBe "987987_oi" // ES field name from annotation
    }

    should("support MultiField with name attribute") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 4: MultiField conservative naming - @MultiField(mainField = @Field(name = "fulltext"))
      // on getText
      // keeps "getText" property name but uses "fulltext" for Elasticsearch
      document.getText shouldNotBe null
      document.getText.path() shouldBe "fulltext"

      // Case 5: MultiField keeping original - description field
      document.description shouldNotBe null
      document.description.path() shouldBe "description_field"
    }

    should("support object and nested fields with name attribute") {
      val document = QFieldNameResolutionTestDocument.fieldNameResolutionTestDocument

      // Case 6: Object field - @Field(name = "user_profile")
      document.profile shouldNotBe null
      document.profile.path() shouldBe "user_profile"

      // Case 7: Nested field - @Field(name = "contact_methods")
      document.contacts shouldNotBe null
      document.contacts.path() shouldBe "contact_methods"

      // Test nested object field names
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
      val isValidField = QFieldNameResolutionTestDocument::class.java.getDeclaredField("isValid")
      isValidField shouldNotBe null

      val searchableTextField =
        QFieldNameResolutionTestDocument::class.java.getDeclaredField("searchableText")
      searchableTextField shouldNotBe null

      val userNameField = QFieldNameResolutionTestDocument::class.java.getDeclaredField("userName")
      userNameField shouldNotBe null
    }
  })
