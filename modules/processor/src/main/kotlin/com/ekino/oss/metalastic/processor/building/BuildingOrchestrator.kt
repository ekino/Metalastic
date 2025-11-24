/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.building

import com.ekino.oss.metalastic.processor.model.GenerationResult
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.ekino.oss.metalastic.processor.report.reporter
import com.squareup.kotlinpoet.FileSpec

/**
 * Enhanced BUILDING phase orchestrator with clean architecture.
 *
 * Implements the restructured approach:
 * 1. Import Resolution: Analyze models and resolve imports/conflicts upfront
 * 2. Code Generation: Use V1-compatible strategy for consistent output
 * 3. Metamodels Generation: Clean registry generation with proper imports
 */
class BuildingOrchestrator(
  private val elasticsearchGraph: MetalasticGraph,
  private val options: ProcessorOptions,
) {

  fun build(): GenerationResult {
    val generatedFiles = mutableListOf<FileSpec>()
    // Step 1: Generate files for @Document classes (with inner classes)
    elasticsearchGraph
      .documentModels()
      .filterNot { it.isNested }
      .forEach { documentModel ->
        val fileSpec = QClassGenerator(documentModel, options).buildFieldSpec()
        generatedFiles.add(fileSpec)
        reporter.debug { "Generated document file: ${documentModel.qClassName}" }
      }

    // Step 2: Generate files for object field models discovered recursively
    elasticsearchGraph
      .objectModels()
      .filterNot { it.isNested }
      .forEach { objectModel ->
        val fileSpec = QClassGenerator(objectModel, options).buildFieldSpec()
        generatedFiles.add(fileSpec)
        reporter.debug { "Generated object field file: ${objectModel.qClassName}" }
      }

    // Step 3: Generate Metamodels registry
    val metamodelsFile = MetamodelsBuilder(elasticsearchGraph, options).buildMetamodelsRegistry()

    val result = GenerationResult(qClasses = generatedFiles, metamodels = metamodelsFile)

    reporter.debug {
      "BUILDING phase completed: ${generatedFiles.size} Meta-classes + Metamodels generated"
    }
    return result
  }
}
