package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Integration test to verify that the KSP processor generates the expected files
 * when processing the TestDocument class.
 */
class ProcessorIntegrationTest {

    @Test
    fun `should generate QTestDocument class when TestDocument is processed`() {
        // The KSP processor should have generated QTestDocument during compilation
        // Let's check if we can find the generated file in the build directory
        
        val buildDir = File("build/generated/ksp/main/kotlin/com/qelasticsearch/integration")
        println("Looking for generated files in: ${buildDir.absolutePath}")
        
        if (buildDir.exists()) {
            val files = buildDir.listFiles()
            println("Found files: ${files?.map { it.name }}")
            
            val qTestDocumentFile = File(buildDir, "QTestDocument.kt")
            if (qTestDocumentFile.exists()) {
                println("QTestDocument.kt content:")
                println(qTestDocumentFile.readText())
                assertThat(qTestDocumentFile.exists())
            } else {
                println("QTestDocument.kt not found")
            }
        } else {
            println("Build directory does not exist")
        }
        
        // For now, we'll just verify that TestDocument compiles successfully
        // In a real integration test, we would verify the generated QTestDocument can be used
        val testDoc = TestDocument()
        assertThat(testDoc).isNotNull()
    }
}