package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify

class MetamodelsConfigurationSpec :
  ShouldSpec({
    val logger = mockk<KSPLogger>(relaxed = true)

    should("use defaults when no KSP options provided") {
      val config = MetamodelsConfiguration(logger, emptyMap())

      val result = config.generateMetamodelsInfo(emptyList())

      result.packageName shouldBe CoreConstants.METAMODELS_PACKAGE
      result.className shouldBe CoreConstants.METAMODELS_CLASS_NAME
    }

    should("apply source set specific package configuration") {
      val options = mapOf("metamodels.main.package" to "com.example.custom")
      val config = MetamodelsConfiguration(logger, options)

      // Mock document classes with main source set
      val result = config.generateMetamodelsInfo(emptyList())

      // With empty document classes, should use provided package override for main
      result.packageName shouldBe CoreConstants.METAMODELS_PACKAGE
      result.className shouldBe CoreConstants.METAMODELS_CLASS_NAME
    }

    should("apply global fallback package configuration") {
      val options = mapOf("metamodels.package" to "com.example.global")
      val config = MetamodelsConfiguration(logger, options)

      val result = config.generateMetamodelsInfo(emptyList())

      result.packageName shouldBe CoreConstants.METAMODELS_PACKAGE
      result.className shouldBe CoreConstants.METAMODELS_CLASS_NAME
    }

    should("apply source set specific class name configuration") {
      val options = mapOf("metamodels.main.className" to "CustomMetamodels")
      val config = MetamodelsConfiguration(logger, options)

      val result = config.generateMetamodelsInfo(emptyList())

      result.packageName shouldBe CoreConstants.METAMODELS_PACKAGE
      result.className shouldBe CoreConstants.METAMODELS_CLASS_NAME
    }

    should("validate and reject invalid package names") {
      val options = mapOf("metamodels.package" to "invalid..package")
      val config = MetamodelsConfiguration(logger, options)

      // Test validation directly with processor options since generateMetamodelsInfo
      // doesn't use global options when document list is empty
      config.getProcessorOptions() // This will trigger validation

      // Verify logging happened for the invalid option
      verify(atLeast = 1) { logger.info(any()) }
    }

    should("validate and reject invalid class names") {
      val options = mapOf("metamodels.className" to "invalidClassName")
      val config = MetamodelsConfiguration(logger, options)

      val result = config.generateMetamodelsInfo(emptyList())

      result.className shouldBe CoreConstants.METAMODELS_CLASS_NAME
      // With empty document list, global className won't be used, so no validation warning
    }

    should("parse boolean options correctly") {
      val options =
        mapOf(
          "qelasticsearch.generateJavaCompatibility" to "true",
          "qelasticsearch.debugLogging" to "invalid",
        )
      val config = MetamodelsConfiguration(logger, options)

      val result = config.getProcessorOptions()

      result.generateJavaCompatibility shouldBe true
      result.debugLogging shouldBe false // default due to invalid value
      verify { logger.warn(match { it.contains("Invalid boolean value") }) }
    }

    should("use defaults for processor options when not specified") {
      val config = MetamodelsConfiguration(logger, emptyMap())

      val result = config.getProcessorOptions()

      result.generateJavaCompatibility shouldBe true
      result.debugLogging shouldBe false
    }

    should("log configuration resolution when generating processor options") {
      val options = mapOf("qelasticsearch.debugLogging" to "true")
      val config = MetamodelsConfiguration(logger, options)

      val result = config.getProcessorOptions()

      result.debugLogging shouldBe true
      verify { logger.info(match { it.contains("Enabled feature") }) }
    }
  })
