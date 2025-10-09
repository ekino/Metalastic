package com.metalastic.gradle

import org.gradle.api.provider.Property

/** Configuration for Metalastic debug reporting. */
abstract class ReportingConfiguration {

  /** Enable debug reporting (default: [PluginConstants.Reporting.DEFAULT_ENABLED]) */
  abstract val enabled: Property<Boolean>

  /** Output path for debug reports (default: [PluginConstants.Reporting.DEFAULT_OUTPUT_PATH]) */
  abstract val outputPath: Property<String>

  init {
    // Set up defaults
    enabled.convention(PluginConstants.Reporting.DEFAULT_ENABLED)
    outputPath.convention(PluginConstants.Reporting.DEFAULT_OUTPUT_PATH)
  }
}
