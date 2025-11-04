@file:OptIn(ExperimentalCompilerApi::class)

package com.metalastic.processor.testing

import com.metalastic.processor.building.BuildingOrchestrator
import com.metalastic.processor.collecting.GraphBuilder
import com.metalastic.processor.model.GenerationResult
import com.metalastic.processor.model.MetalasticGraph
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Builder for creating KSP component tests during active compilation.
 *
 * Provides a fluent API for setting up test scenarios with access to KSP components (resolver,
 * logger, codeGenerator) without requiring a full processor setup.
 */
class ProcessorTestBuilder {
  private val sources = mutableListOf<SourceFile>()
  private val options = mutableMapOf<String, String>()

  /** Adds a source file to the compilation */
  fun withSource(source: SourceFile): ProcessorTestBuilder {
    sources.add(source)
    return this
  }

  /** Adds a Kotlin source file with the given name and content */
  fun withKotlinSource(name: String, content: String): ProcessorTestBuilder {
    sources.add(SourceFile.kotlin(name, content))
    return this
  }

  /** Adds multiple source files */
  fun withSources(vararg sources: SourceFile): ProcessorTestBuilder {
    this.sources.addAll(sources)
    return this
  }

  /** Sets a KSP option */
  fun withOption(key: String, value: String): ProcessorTestBuilder {
    options[key] = value
    return this
  }

  /** Sets multiple KSP options */
  fun withOptions(options: Map<String, String>): ProcessorTestBuilder {
    this.options.putAll(options)
    return this
  }

  /**
   * Executes the test during active compilation with access to KSP components and returns the
   * result
   */
  fun <T> testReturning(testBlock: (KspTestUtils.KspTestComponents) -> T): T {
    return KspTestUtils.testDuringActiveCompilation(
      sources = sources,
      options = options,
      testBlockProvider = { it },
      testBlock = testBlock,
    )
  }

  /**
   * Executes the test during active compilation with access to KSP components and returns the
   * result
   */
  fun <T> testGraphBuilder(testBlock: (GraphBuilder) -> T): T {
    return KspTestUtils.testDuringActiveCompilation(
      sources = sources,
      options = options,
      testBlockProvider = { GraphBuilder(it.resolver, it.processorOptions) },
      testBlock = testBlock,
    )
  }

  fun testMetalasticGraph(): MetalasticGraph {
    return KspTestUtils.testDuringActiveCompilation(
      sources = sources,
      options = options,
      testBlockProvider = { GraphBuilder(it.resolver, it.processorOptions).build() },
      testBlock = { it },
    )
  }

  fun testBuildingResults(): GenerationResult {
    return KspTestUtils.testDuringActiveCompilation(
      sources = sources,
      options = options,
      testBlockProvider = {
        BuildingOrchestrator(
            GraphBuilder(it.resolver, it.processorOptions).build(),
            it.processorOptions,
          )
          .build()
      },
      testBlock = { it },
    )
  }

  companion object {
    /** Creates a new processor test builder with a source file */
    fun withSource(source: SourceFile): ProcessorTestBuilder =
      ProcessorTestBuilder().withSource(source)

    /** Creates a new processor test builder with a Kotlin source file */
    fun withKotlinSource(name: String, content: String): ProcessorTestBuilder =
      ProcessorTestBuilder().withKotlinSource(name, content)

    /** Creates a new processor test builder with multiple source files */
    fun withSources(vararg sources: SourceFile): ProcessorTestBuilder =
      ProcessorTestBuilder().withSources(*sources)
  }
}
