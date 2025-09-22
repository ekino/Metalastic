package com.metalastic.processor.options

import com.metalastic.processor.CoreConstants
import com.metalastic.processor.CoreConstants.ProcessorOptions.Metamodels

data class ProcessorOptions(
  val generateJavaCompatibility: Boolean = true,
  val reportingPath: String? = null,
  val metamodelsConfiguration: MetamodelsConfiguration,
) {

  /** Returns true if reporting is enabled (reportingPath is specified) */
  val isReportingEnabled: Boolean
    get() = !reportingPath.isNullOrBlank()

  companion object {
    fun fromKspOptions(kspOptions: Map<String, String>): ProcessorOptions {
      return ProcessorOptions(
        generateJavaCompatibility =
          kspOptions[CoreConstants.ProcessorOptions.GENERATE_JAVA_COMPATIBILITY]?.toBoolean()
            ?: true,
        reportingPath =
          kspOptions[CoreConstants.ProcessorOptions.REPORTING_PATH]?.takeIf { it.isNotBlank() },
        metamodelsConfiguration = createMetamodelsConfiguration(kspOptions),
      )
    }
  }
}

/** Creates MetamodelsConfiguration with options parsed from KSP options. */
private fun createMetamodelsConfiguration(
  kspOptions: Map<String, String>
): MetamodelsConfiguration {
  return MetamodelsConfiguration(
    packageOverride = kspOptions[Metamodels.PACKAGE_OVERRIDE],
    classNameOverride = kspOptions[Metamodels.CLASS_NAME],
    sourceSetPackageOverrides = extractSourceSetPackageOverrides(kspOptions),
    sourceSetClassNameOverrides = extractSourceSetClassNameOverrides(kspOptions),
  )
}

/** Extract source set specific package overrides from KSP options. */
private fun extractSourceSetPackageOverrides(kspOptions: Map<String, String>): Map<String, String> {
  return kspOptions
    .filterKeys { it.startsWith("metamodels.") && it.endsWith(".package") }
    .mapKeys { (key, _) ->
      // Extract sourceSet from "metamodels.{sourceSet}.package"
      key.removePrefix("metamodels.").removeSuffix(".package")
    }
}

/** Extract source set specific className overrides from KSP options. */
private fun extractSourceSetClassNameOverrides(
  kspOptions: Map<String, String>
): Map<String, String> {
  return kspOptions
    .filterKeys { it.startsWith("metamodels.") && it.endsWith(".className") }
    .mapKeys { (key, _) ->
      // Extract sourceSet from "metamodels.{sourceSet}.className"
      key.removePrefix("metamodels.").removeSuffix(".className")
    }
}
