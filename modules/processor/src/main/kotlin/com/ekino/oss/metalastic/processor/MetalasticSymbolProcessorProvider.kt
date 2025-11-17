/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor

import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.ekino.oss.metalastic.processor.report.ReporterFactory
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class MetalasticSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    environment.logger.info("🔍 MetalasticSymbolProcessorProvider.create() called")
    val options = ProcessorOptions.fromKspOptions(environment.options)
    environment.logger.info("🔍 Processor options: $options")
    ReporterFactory.initialize(options = options, kspLogger = environment.logger)
    return MetalasticSymbolProcessor(
      codeGenerator = environment.codeGenerator,
      options = options,
      logger = environment.logger,
    )
  }
}
