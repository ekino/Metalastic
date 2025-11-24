/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

@file:OptIn(ExperimentalCompilerApi::class)

package com.ekino.oss.metalastic.processor

import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.model.MultiFieldModel
import com.ekino.oss.metalastic.processor.testing.ProcessorTestBuilder
import com.ekino.oss.metalastic.processor.testing.TestDataSources
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.sequences.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Tests for the enhanced collecting phase using the new unified MetalasticModel approach.
 *
 * This test validates:
 * - Document Discovery: Finding @Document classes correctly
 * - Recursive Type Exploration: Building complete model trees with parent-child relationships
 * - Field Analysis: Extracting fields with originalName property
 * - Nested Class Handling: Proper handling of Object/Nested field types
 */
class CollectingPhaseTestSpec :
  ShouldSpec({
    should("successfully collect simple document with all fields") {
      val graph =
        ProcessorTestBuilder.withSource(
            TestDataSources.simpleDocumentWithAllFieldTypeExceptObjectAndNested()
          )
          .testMetalasticGraph()

      graph.documentModels().shouldHaveSize(1)

      val documentModel = graph.documentModels().first()
      documentModel.shouldBeInstanceOf<MetalasticGraph.DocumentClass> {
        it.qClassName shouldBe "MetaSimpleDocument"
        it.fullyQualifiedName shouldBe "com.example.test.MetaSimpleDocument"
      }

      documentModel.fields shouldHaveSize
        FieldType.entries.filterNot { it == FieldType.Object || it == FieldType.Nested }.size

      fun FieldType.toFieldName() = "field${this.name}"

      documentModel.fields
        .associateBy { fieldModel ->
          FieldType.entries.first { it.toFieldName() == fieldModel.name }
        }
        .onEach { (fieldType, fieldModel) ->
          fieldModel.name shouldBe fieldType.toFieldName()
          fieldModel.fieldType shouldBe fieldType
        }
    }

    should("successfully collect complex document with multiple nesting levels") {
      val graph =
        ProcessorTestBuilder.withSource(TestDataSources.complexDocument()).testMetalasticGraph()

      graph.documentModels().shouldHaveSize(1)
      graph.nestedModels() shouldHaveSize 5

      val documentModel = graph.documentModels().first()

      documentModel.qClassName shouldBe "MetaComplexDocument"
      documentModel.fields shouldHaveSize 3 // id, person, orders

      // Verify deep nested class structure is discovered
      documentModel.nestedClasses().size shouldBe 2
      documentModel
        .nestedClasses()
        .map { it.sourceClassDeclaration.simpleName.asString() }
        .shouldContainInOrder("Person", "Order")

      val person =
        documentModel.nestedClasses().firstOrNull {
          it.sourceClassDeclaration.simpleName.asString() == "Person"
        }

      val order =
        documentModel.nestedClasses().firstOrNull {
          it.sourceClassDeclaration.simpleName.asString() == "Order"
        }

      person.shouldNotBeNull {
        nestedClasses().size shouldBe 1
        nestedClasses().first().should { contact ->
          contact.sourceClassDeclaration.simpleName.asString() shouldBe "Contact"
          contact.nestedClasses().size shouldBe 1
          contact.nestedClasses().first().should { address ->
            address.sourceClassDeclaration.simpleName.asString() shouldBe "Address"
            address.nestedClasses().shouldBeEmpty()
          }
        }
      }
      order.shouldNotBeNull {
        nestedClasses().size shouldBe 1
        nestedClasses().first().should { orderItem ->
          orderItem.sourceClassDeclaration.simpleName.asString() shouldBe "OrderItem"
          orderItem.nestedClasses().shouldBeEmpty()
        }
      }
    }

    should("handle MultiField annotations correctly with enhanced field models") {
      val graph =
        ProcessorTestBuilder.withSource(TestDataSources.multiFieldDocument()).testMetalasticGraph()

      graph.documentModels() shouldHaveSize 1
      val documentModel = graph.documentModels().first()

      documentModel.qClassName shouldBe "MetaMultiFieldDocument"
      documentModel.fields shouldHaveSize 3 // id, multiName, multiCode

      // Find MultiField
      val multiNameField = documentModel.fields.find { it.name == "multiName" }
      multiNameField.shouldNotBeNull {
        this should beInstanceOf<MultiFieldModel>()
        this.sourceDeclaration.simpleName.asString() shouldBe "multiName"
      }
    }

    should("handle inner class annotated with @Document correctly") {
      val graph =
        ProcessorTestBuilder.withSource(TestDataSources.outerDocument()).testMetalasticGraph()

      graph.documentModels() shouldHaveSize 2

      val outerDocumentModel =
        graph.documentModels().find {
          it.fullyQualifiedName == "com.example.dataset.MetaOuterDocument"
        }
      val innerDocumentModel =
        graph.documentModels().find {
          it.fullyQualifiedName == "com.example.dataset.MetaOuterDocument.MetaInnerDocument"
        }

      outerDocumentModel.shouldNotBeNull {
        this.shouldBeInstanceOf<MetalasticGraph.DocumentClass> {
          it.qClassName shouldBe "MetaOuterDocument"
          it.fields shouldHaveSize 1 //
        }
      }
      innerDocumentModel.shouldNotBeNull {
        this.shouldBeInstanceOf<MetalasticGraph.DocumentClass> {
          it.qClassName shouldBe "MetaInnerDocument" // Nested classes use simple names
          it.fields shouldHaveSize 2
          // Test that the inner document has a parent class (is nested)
          it.sourceParentClass.shouldNotBeNull().qualifiedName?.asString() shouldBe
            outerDocumentModel!!.sourceClassDeclaration.qualifiedName?.asString()
          it.isNested shouldBe true
          it.nestedClasses().singleOrNull() shouldNotBeNull
            {
              shouldBeInstanceOf<MetalasticGraph.ObjectClass> { nestedObject ->
                nestedObject.fields.shouldHaveSize(1)
                nestedObject.sourceParentClass.shouldNotBeNull()
                nestedObject.sourceParentClass!!.simpleName.asString() shouldBe "InnerDocument"
                nestedObject.qClassName shouldBe "NestedObject"
                nestedObject.isNested shouldBe true
              }
            }
        }
      }
    }

    should("test SimpleDocument field collecting") {
      val graph =
        ProcessorTestBuilder.withSources(
            TestDataSources.simpleDocument(),
            TestDataSources.objectClass(),
            TestDataSources.otherObjectClass(),
          )
          .testMetalasticGraph()

      graph.documentModels() shouldHaveSize 1
    }

    should("test ExempleDocument field collecting") {
      val graph =
        ProcessorTestBuilder.withSources(
            TestDataSources.exampleDocument(),
            TestDataSources.nameCollision(),
          )
          .testMetalasticGraph()

      graph.documentModels() shouldHaveSize 2
    }

    should("test TestDocument field collecting") {
      val graph =
        ProcessorTestBuilder.withSources(TestDataSources.testDocument()).testMetalasticGraph()

      graph.documentModels() shouldHaveSize 1
    }

    should("test IndexPerson field collecting") {
      val graph =
        ProcessorTestBuilder.withSources(TestDataSources.indexPerson()).testMetalasticGraph()

      graph.documentModels() shouldHaveSize 1
    }

    should("test JavaTestDocument field collecting") {
      val graph =
        ProcessorTestBuilder.withSources(TestDataSources.javaTestDocument()).testMetalasticGraph()

      graph.documentModels() shouldHaveSize 1

      val javaTestDocument = graph.documentModels().first()
      javaTestDocument.qClassName shouldBe "MetaJavaTestDocument"
      // JavaTestDocument should have only 1 inner class: JavaTag
      val trueInnerClasses =
        javaTestDocument.nestedClasses().filter {
          it.sourceParentClass == javaTestDocument.sourceClassDeclaration
        }

      trueInnerClasses shouldHaveSize 1 // Only JavaTag
      trueInnerClasses.first().qClassName shouldBe "JavaTag"

      // WithoutAnnotatedField and SomeInnerClass are inner classes of JavaAddress (external
      // class)
      // They should be in objectFields as they get their own Meta-class files
      val withoutAnnotatedFieldModel =
        graph.objectModels().find { it.qClassName == "WithoutAnnotatedField" }
      withoutAnnotatedFieldModel.shouldNotBeNull()

      val someInnerClassModel = graph.objectModels().find { it.qClassName == "SomeInnerClass" }
      someInnerClassModel.shouldNotBeNull()

      // JavaAddress should also be in objectFields
      val javaAddressModel = graph.objectModels().find { it.qClassName == "MetaJavaAddress" }
      javaAddressModel.shouldNotBeNull()
    }

    should("exclude private classes by default") {
      val graph =
        ProcessorTestBuilder.withKotlinSource(
            "TestPrivateClass.kt",
            """
            package com.example.test
            import org.springframework.data.elasticsearch.annotations.Document
            import org.springframework.data.elasticsearch.annotations.Field
            import org.springframework.data.elasticsearch.annotations.FieldType

            @Document(indexName = "public_doc")
            class PublicDocument {
                @Field(type = FieldType.Keyword)
                val id: String = ""
            }

            @Document(indexName = "private_doc")
            private class PrivateDocument {
                @Field(type = FieldType.Keyword)
                val id: String = ""
            }
            """
              .trimIndent(),
          )
          .testMetalasticGraph()

      // Should only have the public document, private document should be excluded
      graph.documentModels() shouldHaveSize 1
      graph.documentModels().first().qClassName shouldBe "MetaPublicDocument"

      // Verify private document is not present
      graph.models().find { it.qClassName == "MetaPrivateDocument" } shouldBe null
    }

    should("include private classes when option is enabled") {
      val graph =
        ProcessorTestBuilder.withKotlinSource(
            "TestPrivateClass.kt",
            """
            package com.example.test
            import org.springframework.data.elasticsearch.annotations.Document
            import org.springframework.data.elasticsearch.annotations.Field
            import org.springframework.data.elasticsearch.annotations.FieldType

            @Document(indexName = "public_doc")
            class PublicDocument {
                @Field(type = FieldType.Keyword)
                val id: String = ""
            }

            @Document(indexName = "private_doc")
            private class PrivateDocument {
                @Field(type = FieldType.Keyword)
                val id: String = ""
            }
            """
              .trimIndent(),
          )
          .withOption("metalastic.generatePrivateClassMetamodels", "true")
          .testMetalasticGraph()

      // Should have both documents when option is enabled
      graph.documentModels() shouldHaveSize 2

      val publicDoc = graph.documentModels().find { it.qClassName == "MetaPublicDocument" }
      publicDoc.shouldNotBeNull()

      val privateDoc = graph.documentModels().find { it.qClassName == "MetaPrivateDocument" }
      privateDoc.shouldNotBeNull()
    }
  })
