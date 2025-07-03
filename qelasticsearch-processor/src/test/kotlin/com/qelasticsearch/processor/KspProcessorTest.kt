package com.qelasticsearch.processor

import assertk.assertThat
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

class KspProcessorTest {

    @Test
    fun `processor classes should be available`() {
        val provider = QElasticsearchSymbolProcessorProvider()
        assertThat(provider).isNotNull()
    }
}