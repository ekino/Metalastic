package com.metalastic.gradle

import org.gradle.api.provider.Property

/** Configuration for Metalastic debug reporting. */
abstract class ReportingConfiguration {

  /** Enable debug reporting (default: false) */
  abstract val enabled: Property<Boolean>

  /** Output path for debug reports (default: "build/reports/metalastic/processor-report.md") */
  abstract val outputPath: Property<String>

  init {
    // Set up defaults
    enabled.convention(false)
    outputPath.convention("build/reports/metalastic/processor-report.md")
  }
}
