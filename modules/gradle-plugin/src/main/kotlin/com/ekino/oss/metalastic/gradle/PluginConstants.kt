/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.gradle

/**
 * Central configuration constants for the Metalastic Gradle plugin.
 *
 * This object contains all default values and configuration keys used throughout the plugin,
 * providing a single source of truth for plugin configuration.
 */
object PluginConstants {

  // Plugin identification
  const val PLUGIN_ID = "com.ekino.oss.metalastic"
  const val PLUGIN_GROUP = "com.ekino.oss"

  /** Metamodels configuration defaults and KSP argument keys. */
  object Metamodels {
    const val DEFAULT_PACKAGE = "com.ekino.oss.metalastic"
    const val DEFAULT_CLASS_NAME = "Metamodels"
    const val DEFAULT_CLASS_PREFIX = "Meta"

    // KSP argument keys
    const val KSP_ARG_PACKAGE = "metamodels.package"
    const val KSP_ARG_REGISTRY_CLASS_NAME = "metamodels.registryClassName"
    const val KSP_ARG_CLASS_PREFIX = "metamodels.classPrefix"

    // Source set specific KSP argument keys
    fun kspArgPackage(sourceSet: String) = "metamodels.$sourceSet.package"

    fun kspArgRegistryClassName(sourceSet: String) = "metamodels.$sourceSet.registryClassName"

    fun kspArgClassPrefix(sourceSet: String) = "metamodels.$sourceSet.classPrefix"
  }

  /** Features configuration defaults and KSP argument keys. */
  object Features {
    const val DEFAULT_GENERATE_JAVA_COMPATIBILITY = true
    const val DEFAULT_GENERATE_PRIVATE_CLASS_METAMODELS = false

    // KSP argument keys
    const val KSP_ARG_JAVA_COMPATIBILITY = "metalastic.generateJavaCompatibility"
    const val KSP_ARG_PRIVATE_CLASS_METAMODELS = "metalastic.generatePrivateClassMetamodels"
  }

  /** Reporting configuration defaults and KSP argument keys. */
  object Reporting {
    const val DEFAULT_ENABLED = false
    const val DEFAULT_OUTPUT_PATH = "build/reports/metalastic/processor-report.md"

    // KSP argument keys
    const val KSP_ARG_REPORTING_PATH = "metalastic.reportingPath"
  }

  /** Standard source set names. */
  object SourceSets {
    const val MAIN = "main"
    const val TEST = "test"
    const val INTEGRATION = "integration"
    const val INTEGRATION_TEST = "integrationTest"
    const val FUNCTIONAL = "functional"
    const val FUNCTIONAL_TEST = "functionalTest"
    const val E2E = "e2e"
    const val E2E_TEST = "e2eTest"
  }

  /** Dependency artifact names. */
  object Dependencies {
    const val CORE_ARTIFACT = "com.ekino.oss:metalastic-core"
    const val PROCESSOR_ARTIFACT = "com.ekino.oss:metalastic-processor"
  }
}
