package com.metalastic.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.metalastic.processor.CoreConstants.PRODUCT_NAME
import com.metalastic.processor.building.BuildingOrchestrator
import com.metalastic.processor.collecting.GraphBuilder
import com.metalastic.processor.options.ProcessorOptions
import com.metalastic.processor.report.reporter
import com.metalastic.processor.writing.writeGeneratedFiles
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * Metalastic Symbol Processor with clean SOLID architecture.
 *
 * Follows a three-phase approach:
 * 1. COLLECTING: Pure data discovery and analysis
 * 2. BUILDING: Pure KotlinPoet code generation
 * 3. WRITING: Pure file I/O operations
 */
class MetalasticSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val options: ProcessorOptions,
  private val logger: KSPLogger,
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    measureTimedValue {
        runCatching {
          reporter.debug { "Starting $PRODUCT_NAME annotation processing" }

          // Phase 1: COLLECTING - Discover and analyze all @Document classes
          val elasticsearchModels =
            measureTimedValue { GraphBuilder(resolver, options).build() }
              .let { (graph, duration) ->
                reporter.debug { "🔬 COLLECTING phase completed in $duration 📊" }
                graph
              }

          // Phase 2: BUILDING - Generate KotlinPoet specifications
          val buildingResult =
            measureTimedValue { BuildingOrchestrator(elasticsearchModels, options).build() }
              .let { (generationResult, duration) ->
                reporter.debug { "👷️ BUILDING phase completed in $duration 📊" }
                generationResult
              }

          // Phase 3: WRITING - Write files to filesystem and generate reports
          measureTime { codeGenerator.writeGeneratedFiles(buildingResult, elasticsearchModels) }
            .also { duration -> reporter.debug { "📝 WRITING phase completed in $duration 📊" } }
        }
      }
      .also { (result, duration) ->
        result
          .onSuccess {
            val message = "🏁 $PRODUCT_NAME processor completed in $duration 📊"
            reporter.debug { message }
            logger.warn(message)
          }
          .onFailure { error ->
            val message = "❌ $PRODUCT_NAME processor failed after $duration 📊"
            reporter.exception(error) { message }
            logger.exception(error)
          }
      }
    // Write the debug report if enabled
    reporter.writeReport()

    return emptyList() // No deferred symbols
  }
}
