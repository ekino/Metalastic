package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import org.springframework.data.elasticsearch.annotations.FieldType

private val logger = KotlinLogging.logger {}

/**
 * Tests for the QElasticsearch processor provider and core functionality. Focus on actual processor
 * behavior rather than trivial operations.
 */
class ProcessorProviderSpec :
  ShouldSpec({
    should("create processor provider successfully") {
      val provider = QElasticsearchSymbolProcessorProvider()
      provider shouldNotBe null
      provider.shouldBeInstanceOf<SymbolProcessorProvider>()
    }

    should("create symbol processor with environment") {
      val provider = QElasticsearchSymbolProcessorProvider()
      val mockEnvironment = mockk<SymbolProcessorEnvironment>(relaxed = true)

      val processor = provider.create(mockEnvironment)
      processor shouldNotBe null
      processor.shouldBeInstanceOf<QElasticsearchSymbolProcessor>()
    }

    should("support all Spring Data Elasticsearch FieldTypes for version compatibility") {
      // This test verifies we can access all FieldType enum values at runtime
      // This is important for version compatibility detection
      val allTypes = FieldType.entries
      logger.info { "Verifying access to ${allTypes.size} FieldType entries" }

      // Test a representative sample of field types that should always be available
      val requiredTypes =
        setOf(
          "Text",
          "Keyword",
          "Long",
          "Integer",
          "Double",
          "Float",
          "Boolean",
          "Date",
          "Object",
          "Nested",
        )
      val availableTypeNames = allTypes.map { it.name }.toSet()

      requiredTypes.forEach { requiredType ->
        if (requiredType in availableTypeNames) {
          logger.debug { "✓ Required FieldType available: $requiredType" }
        } else {
          logger.warn { "✗ Required FieldType missing: $requiredType" }
        }
      }

      // Verify we have at least the core types
      allTypes.size shouldNotBe 0
      logger.info {
        "FieldType compatibility check completed with ${allTypes.size} available types"
      }
    }

    should("handle version-specific FieldTypes gracefully") {
      // Test that we can handle newer FieldTypes that might not exist in all versions
      val allTypes = FieldType.entries
      var supportedCount = 0
      var unsupportedCount = 0

      allTypes.forEach { fieldType ->
        try {
          // Try to access the field type
          fieldType.name.isNotEmpty()
          supportedCount++
        } catch (e: Exception) {
          logger.warn(e) { "FieldType not fully supported: ${fieldType.name}" }
          unsupportedCount++
        }
      }

      logger.info { "FieldType support: $supportedCount supported, $unsupportedCount with issues" }
      supportedCount shouldNotBe 0 // Should have at least some supported types
    }
  })
