package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class QElasticsearchSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    QElasticsearchSymbolProcessor(
      codeGenerator = environment.codeGenerator,
      logger = environment.logger,
    )
}
