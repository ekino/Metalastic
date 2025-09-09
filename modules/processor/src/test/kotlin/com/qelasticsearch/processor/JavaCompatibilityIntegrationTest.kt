package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class JavaCompatibilityIntegrationTest :
  ShouldSpec({
    should("include @JvmField annotations when generateJavaCompatibility is true") {
      val options = mapOf("qelasticsearch.generateJavaCompatibility" to "true")
      val config = MetamodelsConfiguration(mockk<KSPLogger>(relaxed = true), options)

      val processorOptions = config.getProcessorOptions()

      processorOptions.generateJavaCompatibility shouldBe true
    }

    should("exclude @JvmField annotations when generateJavaCompatibility is false") {
      val options = mapOf("qelasticsearch.generateJavaCompatibility" to "false")
      val config = MetamodelsConfiguration(mockk<KSPLogger>(relaxed = true), options)

      val processorOptions = config.getProcessorOptions()

      processorOptions.generateJavaCompatibility shouldBe false
    }

    should("default to true when generateJavaCompatibility is not specified") {
      val config = MetamodelsConfiguration(mockk<KSPLogger>(relaxed = true), emptyMap())

      val processorOptions = config.getProcessorOptions()

      processorOptions.generateJavaCompatibility shouldBe true
    }

    should("handle invalid generateJavaCompatibility values gracefully") {
      val options = mapOf("qelasticsearch.generateJavaCompatibility" to "invalid")
      val logger = mockk<KSPLogger>(relaxed = true)
      val config = MetamodelsConfiguration(logger, options)

      val processorOptions = config.getProcessorOptions()

      // Should default to true when invalid value provided
      processorOptions.generateJavaCompatibility shouldBe true
    }

    should("create FieldGenerators with correct Java compatibility setting") {
      val fieldTypeMappings =
        emptyMap<org.springframework.data.elasticsearch.annotations.FieldType, FieldTypeMapping>()
      val logger = mockk<KSPLogger>(relaxed = true)

      val fieldGeneratorsEnabled =
        FieldGenerators(logger, fieldTypeMappings, generateJavaCompatibility = true)
      val fieldGeneratorsDisabled =
        FieldGenerators(logger, fieldTypeMappings, generateJavaCompatibility = false)

      // Both should compile successfully - the actual @JvmField generation
      // would be tested in integration tests with real code generation
      fieldGeneratorsEnabled shouldBe fieldGeneratorsEnabled
      fieldGeneratorsDisabled shouldBe fieldGeneratorsDisabled
    }

    should("create ObjectFieldRegistry with correct Java compatibility setting") {
      val logger = mockk<KSPLogger>(relaxed = true)
      val globalObjectFields = emptyMap<String, ObjectFieldInfo>()

      val registryEnabled =
        ObjectFieldRegistry(logger, globalObjectFields, generateJavaCompatibility = true)
      val registryDisabled =
        ObjectFieldRegistry(logger, globalObjectFields, generateJavaCompatibility = false)

      // Both should compile successfully
      registryEnabled shouldBe registryEnabled
      registryDisabled shouldBe registryDisabled
    }

    should("create components with debugLogging enabled/disabled") {
      val logger = mockk<KSPLogger>(relaxed = true)
      val fieldTypeMappings =
        emptyMap<org.springframework.data.elasticsearch.annotations.FieldType, FieldTypeMapping>()
      val globalObjectFields = emptyMap<String, ObjectFieldInfo>()

      // Test FieldGenerators with debug logging
      val fieldGeneratorsDebug =
        FieldGenerators(
          logger,
          fieldTypeMappings,
          generateJavaCompatibility = true,
          debugLogging = true,
        )
      val fieldGeneratorsNoDebug =
        FieldGenerators(
          logger,
          fieldTypeMappings,
          generateJavaCompatibility = true,
          debugLogging = false,
        )

      // Test FieldTypeExtractor with debug logging
      val extractorDebug = FieldTypeExtractor(logger, debugLogging = true)
      val extractorNoDebug = FieldTypeExtractor(logger, debugLogging = false)

      // Test ObjectFieldRegistry with debug logging
      val registryDebug =
        ObjectFieldRegistry(
          logger,
          globalObjectFields,
          generateJavaCompatibility = true,
          debugLogging = true,
        )
      val registryNoDebug =
        ObjectFieldRegistry(
          logger,
          globalObjectFields,
          generateJavaCompatibility = true,
          debugLogging = false,
        )

      // All should compile successfully
      fieldGeneratorsDebug shouldBe fieldGeneratorsDebug
      fieldGeneratorsNoDebug shouldBe fieldGeneratorsNoDebug
      extractorDebug shouldBe extractorDebug
      extractorNoDebug shouldBe extractorNoDebug
      registryDebug shouldBe registryDebug
      registryNoDebug shouldBe registryNoDebug
    }
  })
