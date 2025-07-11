package com.qelasticsearch.integration

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MultiFieldSpec :
    ShouldSpec({

        should("support accessing multifield properties") {
            // Test that we can access the main field
            QJavaTestDocument.multiFieldName.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.path() shouldBe "multiFieldName"

            // Test that we can access inner fields by suffix - now non-nullable!
            QJavaTestDocument.multiFieldName.search.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.search.path() shouldBe "search"

            QJavaTestDocument.multiFieldName.keyword.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.keyword.path() shouldBe "keyword"

            // Test custom suffix access
            QJavaTestDocument.multiFieldName["search"].shouldNotBeNull()
            QJavaTestDocument.multiFieldName["keyword"].shouldNotBeNull()
            QJavaTestDocument.multiFieldName["nonexistent"].shouldBeNull()
        }

        should("be able to traverse multifield search path like user requested") {
            // This is the exact syntax the user wanted:

            // We can simulate this with our test document - returns local field name!
            val searchFieldPath = QJavaTestDocument.multiFieldName.search.path()
            searchFieldPath shouldBe "search"

            // Test that dynamic property access works and is non-nullable
            QJavaTestDocument.multiFieldName.search.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.keyword.shouldNotBeNull()
        }

        should("provide access to main field") {
            // Test that we can access the main field directly
            val mainField = QJavaTestDocument.multiFieldName.main()
            mainField.shouldNotBeNull()
            mainField.path() shouldBe "multiFieldName"

            // The main field should be the primary field (in this case, TextField)
            mainField.shouldNotBeNull()
        }
    })
