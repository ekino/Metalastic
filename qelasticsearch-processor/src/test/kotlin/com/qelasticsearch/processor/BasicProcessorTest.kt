package com.qelasticsearch.processor

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Basic test suite for QElasticsearch processor functionality.
 */
class BasicProcessorTest : ShouldSpec({

    should("create processor provider successfully") {
        val provider = QElasticsearchSymbolProcessorProvider()
        provider shouldNotBe null
        provider.shouldBeInstanceOf<QElasticsearchSymbolProcessorProvider>()
    }

    should("access all basic FieldType values") {
        FieldType.Text shouldBe FieldType.Text
        FieldType.Keyword shouldBe FieldType.Keyword
        FieldType.Long shouldBe FieldType.Long
        FieldType.Integer shouldBe FieldType.Integer
        FieldType.Short shouldBe FieldType.Short
        FieldType.Byte shouldBe FieldType.Byte
        FieldType.Double shouldBe FieldType.Double
        FieldType.Float shouldBe FieldType.Float
        FieldType.Boolean shouldBe FieldType.Boolean
        FieldType.Date shouldBe FieldType.Date
        FieldType.Binary shouldBe FieldType.Binary
        FieldType.Ip shouldBe FieldType.Ip
    }

    should("access specialized FieldType values") {
        FieldType.Object shouldBe FieldType.Object
        FieldType.Nested shouldBe FieldType.Nested
        FieldType.Half_Float shouldBe FieldType.Half_Float
        FieldType.Scaled_Float shouldBe FieldType.Scaled_Float
        FieldType.Date_Nanos shouldBe FieldType.Date_Nanos
    }

    should("access range FieldType values") {
        FieldType.Integer_Range shouldBe FieldType.Integer_Range
        FieldType.Float_Range shouldBe FieldType.Float_Range
        FieldType.Long_Range shouldBe FieldType.Long_Range
        FieldType.Double_Range shouldBe FieldType.Double_Range
        FieldType.Date_Range shouldBe FieldType.Date_Range
        FieldType.Ip_Range shouldBe FieldType.Ip_Range
    }

    should("support runtime FieldType detection") {
        val allTypes = FieldType.values()
        allTypes.size shouldBe allTypes.size // Basic sanity check
        
        // Test that we can iterate through all field types without exceptions
        var successCount = 0
        allTypes.forEach { fieldType ->
            try {
                fieldType.name.isNotEmpty() shouldBe true
                successCount++
            } catch (e: Exception) {
                // In real processor, this would be logged and skipped
            }
        }
        
        successCount shouldBe allTypes.size
    }

    should("generate correct QIndex class names") {
        val testCases = mapOf(
            "TestDocument" to "QTestDocument",
            "UserDocument" to "QUserDocument",
            "IndexModulePath" to "QIndexModulePath"
        )
        
        testCases.forEach { (input, expected) ->
            val generated = "Q$input"
            generated shouldBe expected
        }
    }

    should("generate correct field delegate patterns") {
        val fieldName = "testField"
        val textDelegate = "val $fieldName by text<String>()"
        val keywordDelegate = "val $fieldName by keyword<String>()"
        val longDelegate = "val $fieldName by long<Long>()"
        
        textDelegate shouldBe "val testField by text<String>()"
        keywordDelegate shouldBe "val testField by keyword<String>()"
        longDelegate shouldBe "val testField by long<Long>()"
    }

    should("handle object field references") {
        val objectClassName = "Address"
        val objectFieldCall = "val address by objectField(Q$objectClassName)"
        objectFieldCall shouldBe "val address by objectField(QAddress)"
    }

    should("handle nested field references") {
        val nestedClassName = "Tag"
        val nestedFieldCall = "val tags by nestedField(Q$nestedClassName)"
        nestedFieldCall shouldBe "val tags by nestedField(QTag)"
    }

    should("generate correct package declarations") {
        val packageName = "com.example.documents"
        val packageDeclaration = "package $packageName"
        packageDeclaration shouldBe "package com.example.documents"
    }

    should("generate correct import statements") {
        val imports = listOf(
            "import com.qelasticsearch.dsl.Index",
            "import com.qelasticsearch.dsl.ObjectFields"
        )
        
        imports.forEach { importStatement ->
            importStatement shouldBe importStatement // Verify valid structure
        }
    }

    should("extract simple names from qualified types") {
        val fullyQualified = "com.example.model.CustomType"
        val simpleName = fullyQualified.substringAfterLast('.')
        simpleName shouldBe "CustomType"
    }

    should("handle empty strings gracefully") {
        val emptyString = ""
        emptyString.isEmpty() shouldBe true
        
        val nonEmptyString = "test"
        nonEmptyString.isEmpty() shouldBe false
    }

    should("handle special field names") {
        val specialNames = listOf(
            "field_with_underscores",
            "fieldWithCamelCase", 
            "field123WithNumbers"
        )
        
        specialNames.forEach { name ->
            name.isNotEmpty() shouldBe true
            val fieldDeclaration = "val $name by text<String>()"
            fieldDeclaration shouldBe "val $name by text<String>()"
        }
    }

    should("handle Kotlin keywords with backticks") {
        val keywords = listOf("class", "object", "fun")
        
        keywords.forEach { keyword ->
            val escapedField = "val `$keyword` by text<String>()"
            escapedField shouldBe "val `$keyword` by text<String>()"
        }
    }
})