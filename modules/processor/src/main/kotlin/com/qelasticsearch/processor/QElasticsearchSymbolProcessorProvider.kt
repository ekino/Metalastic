package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.qelasticsearch.processor.options.ProcessorOptions
import com.qelasticsearch.processor.report.ReporterFactory

class QElasticsearchSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    val options = ProcessorOptions.fromKspOptions(environment.options)
    ReporterFactory.initialize(options = options, kspLogger = environment.logger)
    return QElasticsearchSymbolProcessor(
      codeGenerator = environment.codeGenerator,
      options = options,
      logger = environment.logger,
    )
  }
}
