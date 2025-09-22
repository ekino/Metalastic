package com.metalastic.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.metalastic.processor.options.ProcessorOptions
import com.metalastic.processor.report.ReporterFactory

class MetalasticSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    val options = ProcessorOptions.fromKspOptions(environment.options)
    ReporterFactory.initialize(options = options, kspLogger = environment.logger)
    return MetalasticSymbolProcessor(
      codeGenerator = environment.codeGenerator,
      options = options,
      logger = environment.logger,
    )
  }
}
