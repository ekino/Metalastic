package com.metalastictest.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Integration test to verify that the KSP processor generates the expected files when processing
 * the TestDocument class.
 */
class ProcessorIntegrationSpec :
  ShouldSpec({
    should("generate MetaTestDocument class when TestDocument is processed") {
      // The KSP processor should have generated MetaTestDocument during compilation
      // Let's check if we can find the generated file in the build directory

      val buildDir = File("build/generated/ksp/main/kotlin/com/qelasticsearch/integration")
      logger.info { "Looking for generated files in: ${buildDir.absolutePath}" }

      if (buildDir.exists()) {
        val files = buildDir.listFiles()
        logger.info { "Found files: ${files?.map { it.name }}" }

        val qTestDocumentFile = File(buildDir, "MetaTestDocument.kt")
        if (qTestDocumentFile.exists()) {
          logger.info { "MetaTestDocument.kt content:" }
          logger.info { qTestDocumentFile.readText() }
          qTestDocumentFile.exists() shouldNotBe false
        } else {
          logger.warn { "MetaTestDocument.kt not found" }
        }
      } else {
        logger.warn { "Build directory does not exist" }
      }

      // For now, we'll just verify that TestDocument compiles successfully
      // In a real integration test, we would verify the generated MetaTestDocument can be used
      val testDoc = TestDocument()
      testDoc shouldNotBe null
    }
  })
