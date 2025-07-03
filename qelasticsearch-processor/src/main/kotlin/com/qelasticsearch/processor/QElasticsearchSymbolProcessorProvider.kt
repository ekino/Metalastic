package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.*

class QElasticsearchSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return QElasticsearchSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}