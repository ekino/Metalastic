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
            QJavaTestDocument.multiFieldName.path().path shouldBe "multiFieldName"

            // Test that we can access inner fields by suffix - now non-nullable!
            QJavaTestDocument.multiFieldName.search.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.search
                .path()
                .path shouldBe "multiFieldName.search"

            QJavaTestDocument.multiFieldName.keyword.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.keyword
                .path()
                .path shouldBe "multiFieldName.keyword"

            // Test custom suffix access
            QJavaTestDocument.multiFieldName["search"].shouldNotBeNull()
            QJavaTestDocument.multiFieldName["keyword"].shouldNotBeNull()
            QJavaTestDocument.multiFieldName["nonexistent"].shouldBeNull()
        }

        should("be able to traverse multifield search path like user requested") {
            // This is the exact syntax the user wanted:

            // We can simulate this with our test document - now returns full path!
            val searchFieldPath =
                QJavaTestDocument.multiFieldName.search
                    .path()
                    .path
            searchFieldPath shouldBe "multiFieldName.search"

            // Test that dynamic property access works and is non-nullable
            QJavaTestDocument.multiFieldName.search.shouldNotBeNull()
            QJavaTestDocument.multiFieldName.keyword.shouldNotBeNull()
        }

        should("provide access to main field") {
            // Test that we can access the main field directly
            val mainField = QJavaTestDocument.multiFieldName.main()
            mainField.shouldNotBeNull()
            mainField.path().path shouldBe "multiFieldName"

            // The main field should be the primary field (in this case, TextField)
            mainField.shouldNotBeNull()
        }
    })
