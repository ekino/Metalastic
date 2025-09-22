package com.metalastictest.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MultiFieldSpec :
  ShouldSpec({
    should("support accessing multifield properties including main field") {
      // Test that we can access the main multifield object
      QJavaTestDocument.javaTestDocument.multiFieldName.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"

      // The multifield itself IS the main field through delegation - no .main property needed!
      // This is the beauty of the new MultiField<T : Field> architecture

      // Test that we can access inner fields by suffix - now with correct paths!
      QJavaTestDocument.javaTestDocument.multiFieldName.search.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.search.path() shouldBe
        "multiFieldName.search"

      QJavaTestDocument.javaTestDocument.multiFieldName.keyword.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.keyword.path() shouldBe
        "multiFieldName.keyword"

      // Test the new 'somethingElse' field that was previously not accessible
      QJavaTestDocument.javaTestDocument.multiFieldName.somethingElse.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.somethingElse.path() shouldBe
        "multiFieldName.somethingElse"
    }

    should("be able to traverse multifield search path like user requested") {
      // This is the exact syntax the user wanted with correct paths:

      // Now returns the full dotted path as expected!
      val searchFieldPath = QJavaTestDocument.javaTestDocument.multiFieldName.search.path()
      searchFieldPath shouldBe "multiFieldName.search"

      // Test that type-safe property access works and is non-nullable
      QJavaTestDocument.javaTestDocument.multiFieldName.search.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.keyword.shouldNotBeNull()

      // Test the new field that was defined but previously inaccessible
      QJavaTestDocument.javaTestDocument.multiFieldName.somethingElse.shouldNotBeNull()
    }

    should("support type-safe multifield access with actual @InnerField definitions") {
      // Test that we can access all the actually defined inner fields

      // The multifield itself IS the main field (mainField = @Field(type = FieldType.Text))
      // No .main property needed - that's the elegance of MultiField<T : Field> delegation!
      QJavaTestDocument.javaTestDocument.multiFieldName.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"

      // All fields defined in @InnerField annotations should be accessible:
      // @InnerField(suffix = "keyword", type = FieldType.Keyword)
      QJavaTestDocument.javaTestDocument.multiFieldName.keyword.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.keyword.path() shouldBe
        "multiFieldName.keyword"

      // @InnerField(suffix = "search", type = FieldType.Text, analyzer = "standard")
      QJavaTestDocument.javaTestDocument.multiFieldName.search.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.search.path() shouldBe
        "multiFieldName.search"

      // @InnerField(suffix = "somethingElse", type = FieldType.Text, analyzer = "standard")
      QJavaTestDocument.javaTestDocument.multiFieldName.somethingElse.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.multiFieldName.somethingElse.path() shouldBe
        "multiFieldName.somethingElse"

      // The multifield object itself represents both the container AND the main field
      QJavaTestDocument.javaTestDocument.multiFieldName.path() shouldBe "multiFieldName"
    }

    should("support different main field types in multifields") {
      // Test description2 which has a Keyword main field instead of Text
      // @MultiField(mainField = @Field(type = FieldType.Keyword)) - no otherFields

      QJavaTestDocument.javaTestDocument.description2.shouldNotBeNull()
      QJavaTestDocument.javaTestDocument.description2.path() shouldBe "description2"

      // The multifield itself IS the main KeywordField (not TextField like multiFieldName)
      // Through MultiField<KeywordField<String>> delegation
      // No inner fields defined for description2
    }
  })
