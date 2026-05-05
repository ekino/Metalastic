/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
@file:OptIn(ExperimentalCompilerApi::class)

package com.ekino.oss.metalastic.processor.testing

import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.Ksp2PrecursorTool
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import io.kotest.matchers.shouldNotBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Utility for testing KSP components during active compilation.
 *
 * This ensures that testing occurs when ResolverImpl.instance is properly initialized, avoiding NPE
 * issues when using getSymbolsWithAnnotation() and other resolver methods.
 */
object KspTestUtils {

  /** Captured KSP components from active compilation */
  data class KspTestComponents(
    val logger: KSPLogger,
    val codeGenerator: CodeGenerator,
    val resolver: Resolver,
    val options: Map<String, String>,
  ) {
    val processorOptions by lazy { ProcessorOptions.fromKspOptions(options) }
  }

  /**
   * Executes a test function during active compilation with real KSP components and returns the
   * result.
   *
   * @param sources List of source files to compile
   * @param options KSP options to pass to the compilation
   * @param testBlock Function to execute during compilation with captured components
   */
  fun <T, U> testDuringActiveCompilation(
    sources: List<SourceFile> = emptyList(),
    options: Map<String, String> = defaultOptions(),
    testBlockProvider: (testComponents: KspTestComponents) -> U,
    testBlock: (U) -> T,
  ): T {
    var capturedEnvironment: SymbolProcessorEnvironment? = null
    var capturedResolver: Resolver? = null
    var testResult: Result<T>? = null

    val testProvider =
      object : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
          capturedEnvironment = environment

          return object : SymbolProcessor {
            override fun process(resolver: Resolver): List<KSAnnotated> {
              capturedResolver = resolver
              val components =
                KspTestComponents(
                  logger = environment.logger,
                  codeGenerator = environment.codeGenerator,
                  resolver = resolver,
                  options = options,
                )

              // Execute the test during active compilation
              testResult = runCatching { testBlock(testBlockProvider(components)) }

              return emptyList()
            }
          }
        }
      }

    val compilation = createCompilation(sources, options, listOf(testProvider))
    compilation.compile()

    // Verify components were captured
    capturedEnvironment shouldNotBe null
    capturedResolver shouldNotBe null

    // Return the test result
    return testResult?.getOrThrow() ?: error("Test block was not executed")
  }

  //    /**
  //     * Executes a test function during active compilation with real KSP components (Unit
  // version).
  //     *
  //     * @param sources List of source files to compile
  //     * @param options KSP options to pass to the compilation
  //     * @param testBlock Function to execute during compilation with captured components
  //     */
  //    fun testDuringActiveCompilation(
  //        sources: List<SourceFile> = defaultSources(),
  //        options: Map<String, String> = defaultOptions(),
  //        testBlock: (KspTestComponents) -> Unit,
  //    ) {
  //        testDuringActiveCompilation<Unit>(sources, options) { components ->
  //            testBlock(components)
  //            Unit
  //        }
  //    }

  /** Creates a compilation with the specified sources, options, and providers */
  private fun createCompilation(
    sources: List<SourceFile>,
    options: Map<String, String>,
    providers: List<SymbolProcessorProvider>,
  ): KotlinCompilation {
    return KotlinCompilation().apply {
      useKsp2()
      this.sources = sources
      symbolProcessorProviders = providers.toMutableList()
      inheritClassPath = true
      messageOutputStream = System.out

      kspProcessorOptions.putAll(options)
      // Java records and Java 16+ syntax need an explicit JVM target; otherwise javac may fall
      // back to a target that doesn't understand records and silently strips/fails parsing.
      jvmTarget = "21"

      // kctfork's Ksp2PrecursorTool defaults source roots to each source file's parent directory.
      // For files written under their package layout (e.g. <sourcesDir>/com/example/dataset/X.java)
      // that yields the package directory, not the package root — javac/KSP then can't resolve the
      // declared package. Override to point at the single sources root instead.
      val ksp2 = precursorTools["ksp2"] as Ksp2PrecursorTool
      ksp2.onBuilder = {
        val sourcesRoot = projectBaseDir.parentFile.resolve("sources").absoluteFile
        sourceRoots = listOf(sourcesRoot)
        javaSourceRoots = listOf(sourcesRoot)
      }
    }
  }

  /** Default KSP options for testing */
  private fun defaultOptions(): Map<String, String> =
    mapOf("ksp.incremental" to "false", "metalastic.generateJavaCompatibility" to "true")
}
