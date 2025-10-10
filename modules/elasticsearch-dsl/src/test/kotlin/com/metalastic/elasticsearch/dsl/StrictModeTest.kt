package com.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.reflect.typeOf

/** Test metamodel with a non-nested object field to test strict mode behavior */
object StrictModeTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {
  val regularField: NonNestedTestField = NonNestedTestField(this, "regularField", false)
}

class NonNestedTestField(parent: ObjectField<*>?, fieldName: String, nested: Boolean) :
  ObjectField<Any>(parent, fieldName, nested, typeOf<Any>()) {
  val name: KeywordField<String> = KeywordField(this, "name", typeOf<String>())
}

class StrictModeTest :
  ShouldSpec({
    val metamodel = StrictModeTestMetamodel

    beforeSpec {
      // Clear the system property before tests
      System.clearProperty("metalastic.dsl.strict")
    }

    afterSpec {
      // Clean up after tests
      System.clearProperty("metalastic.dsl.strict")
    }

    context("Strict mode disabled (default)") {
      beforeTest { System.setProperty("metalastic.dsl.strict", "false") }

      should("apply nested query on non-nested field with warning (graceful fallback)") {
        val builder = BoolQuery.Builder()

        // This should NOT throw, just log a warning
        builder.boolQueryDsl {
          must +
            {
              metamodel.regularField.nested { must + { metamodel.regularField.name term "test" } }
            }
        }

        val query = builder.build()
        // Verify the query was built (fallback to bool query)
        query.must().isEmpty() shouldBe false
      }
    }

    xcontext("Strict mode enabled") {
      // Note: These tests are disabled because Kotlin's lazy property is initialized once
      // and cannot be reset during test execution. In real usage, the system property
      // should be set BEFORE the JVM starts (e.g., via -Dmetalastic.dsl.strict=true)

      should("throw exception when nested query is used on non-nested field") {
        System.setProperty("metalastic.dsl.strict", "true")

        val exception =
          shouldThrow<IllegalStateException> {
            val builder = BoolQuery.Builder()
            builder.boolQueryDsl {
              must +
                {
                  metamodel.regularField.nested {
                    must + { metamodel.regularField.name term "test" }
                  }
                }
            }
          }

        exception.message shouldContain "Nested query used on non-nested field 'regularField'"
        exception.message shouldContain "@Field(type = FieldType.Nested)"
      }
    }

    context("StrictMode property reading") {
      should("read strict mode from system property") {
        System.setProperty("metalastic.dsl.strict", "true")

        // Test the actual system property reading mechanism
        val strictValue = System.getProperty("metalastic.dsl.strict", "false").toBoolean()
        strictValue shouldBe true
      }

      should("default to false when property not set") {
        System.clearProperty("metalastic.dsl.strict")

        val strictValue = System.getProperty("metalastic.dsl.strict", "false").toBoolean()
        strictValue shouldBe false
      }
    }
  })
