/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.writing

import com.ekino.oss.metalastic.processor.model.GenerationResult
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.report.reporter
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import kotlin.time.measureTime

/**
 * FileWriter - Pure KSP file generation component.
 *
 * Responsibilities:
 * - Write KotlinPoet FileSpec objects to actual files using CodeGenerator
 * - Handle proper dependency tracking for incremental compilation
 * - Generate correct file paths and extensions
 * - Report file generation statistics and timing
 */
fun CodeGenerator.writeGeneratedFiles(
  generationResult: GenerationResult,
  elasticsearchGraph: MetalasticGraph,
): WritingResult {
  var filesWritten = 0
  val writingTime = measureTime {
    // Step 1: Write all Meta-class files (documents + object fields)
    generationResult.qClasses.forEach { qClassFile ->
      writeQClassFile(qClassFile, elasticsearchGraph)
      filesWritten++
    }

    // Step 2: Write Metamodels file if present
    generationResult.metamodels?.let { metamodelsFile ->
      writeMetamodelsFile(metamodelsFile, elasticsearchGraph)
      filesWritten++
    }
  }

  reporter.debug {
    "WRITING phase completed: $filesWritten files written in ${writingTime.inWholeMilliseconds}ms"
  }
  return WritingResult(filesWritten = filesWritten, writingTimeMs = writingTime.inWholeMilliseconds)
}

/** Writes a Meta-class FileSpec to disk using the CodeGenerator. */
private fun CodeGenerator.writeQClassFile(
  qClassFile: FileSpec,
  elasticsearchGraph: MetalasticGraph,
) {
  // Use all document sources for dependency tracking since we can't know which specific document
  // generated this Meta-class (could be a document Meta-class or object field Meta-class)
  val sources =
    elasticsearchGraph
      .models()
      .mapNotNull { it.sourceClassDeclaration.containingFile }
      .toTypedArray()
  val dependencies = Dependencies(aggregating = false, sources = sources)

  runCatching {
      val outputFile =
        createNewFile(
          dependencies = dependencies,
          packageName = qClassFile.packageName,
          fileName = qClassFile.name,
        )

      // Replacing redundant 'public' modifiers for cleaner output to match V1
      outputFile.bufferedWriter().use { writer ->
        val fileContent =
          runCatching { qClassFile.toString() }
            .getOrElse { e ->
              reporter.exception(e) {
                "Failed to convert FileSpec to string for ${qClassFile.name}: ${e.message}"
              }
              throw e
            }
        writer.write(fileContent.replace("public ", ""))
      }

      reporter.debug { "Generated Meta-class: ${qClassFile.packageName}.${qClassFile.name}" }
    }
    .onFailure { e ->
      reporter.exception(e) { "Failed to write Meta-class ${qClassFile.name}: ${e.message}" }
      throw e
    }
}

/** Writes the Metamodels FileSpec to disk using the CodeGenerator. */
private fun CodeGenerator.writeMetamodelsFile(
  metamodelsFile: FileSpec,
  elasticsearchGraph: MetalasticGraph,
) {
  val dependencies =
    Dependencies(
      aggregating = true, // Metamodels aggregates all document sources
      sources =
        elasticsearchGraph
          .models()
          .mapNotNull { it.sourceClassDeclaration.containingFile }
          .toTypedArray(),
    )

  runCatching {
      val outputFile =
        createNewFile(
          dependencies = dependencies,
          packageName = metamodelsFile.packageName,
          fileName = metamodelsFile.name,
        )

      // Replacing redundant 'public' modifiers for cleaner output to match V1
      outputFile.bufferedWriter().use { writer ->
        writer.write(metamodelsFile.toString().replace("public ", ""))
      }

      reporter.debug {
        "Generated Metamodels: ${metamodelsFile.packageName}.${metamodelsFile.name}"
      }
    }
    .onFailure { e ->
      reporter.exception(e) { "Failed to write Metamodels ${metamodelsFile.name}: ${e.message}" }
      throw e
    }
}

/** Result of the WRITING phase. */
data class WritingResult(val filesWritten: Int, val writingTimeMs: Long)
