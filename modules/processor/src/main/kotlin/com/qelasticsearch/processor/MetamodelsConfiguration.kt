package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Configuration for Metamodels generation, supporting both automatic detection and future manual
 * configuration via KSP options.
 */
class MetamodelsConfiguration(
  private val logger: KSPLogger,
  private val kspOptions: Map<String, String> = emptyMap(),
) {

  /**
   * Generate the appropriate package and class name for Metamodels based on document classes and
   * source set detection.
   */
  fun generateMetamodelsInfo(documentClasses: List<KSClassDeclaration>): MetamodelsInfo {
    if (documentClasses.isEmpty()) {
      return MetamodelsInfo(
        packageName = CoreConstants.METAMODELS_PACKAGE,
        className = CoreConstants.METAMODELS_CLASS_NAME,
      )
    }

    // Detect source set from file paths
    val sourceSet = detectSourceSet(documentClasses)
    logger.info("Detected source set: $sourceSet")

    // Find common base package
    val basePackage = findCommonBasePackage(documentClasses)
    logger.info("Detected base package: $basePackage")

    // Check for future KSP configuration overrides
    val configuredPackage = getConfiguredPackage(sourceSet)
    val configuredClassName = getConfiguredClassName(sourceSet)

    val finalPackage = configuredPackage ?: generateDefaultPackage(basePackage, sourceSet)
    val finalClassName = configuredClassName ?: CoreConstants.METAMODELS_CLASS_NAME

    logger.info("Generated Metamodels info: $finalPackage.$finalClassName")

    return MetamodelsInfo(packageName = finalPackage, className = finalClassName)
  }

  /** Detect source set from document class file paths. */
  private fun detectSourceSet(documentClasses: List<KSClassDeclaration>): String {
    val sourceSets =
      documentClasses
        .mapNotNull { it.containingFile?.filePath }
        .map { filePath ->
          when {
            "/src/main/" in filePath -> "main"
            "/src/test/" in filePath -> "test"
            else -> "unknown"
          }
        }
        .distinct()

    // All documents should be from the same source set in one KSP run
    return sourceSets.firstOrNull() ?: "main"
  }

  /** Find the common base package from all document classes. */
  private fun findCommonBasePackage(documentClasses: List<KSClassDeclaration>): String {
    val packages = documentClasses.map { it.packageName.asString() }.filter { it.isNotEmpty() }

    if (packages.isEmpty()) {
      return CoreConstants.METAMODELS_PACKAGE
    }

    val commonPrefix =
      if (packages.size == 1) {
        packages.first()
      } else {
        // Find the longest common prefix
        packages.reduce { acc, pkg -> acc.commonPrefixWith(pkg) }
      }

    // Clean up to package boundary (must end with complete package segment)
    return cleanToPackageBoundary(commonPrefix)
  }

  /** Clean package name to end at package boundary. */
  private fun cleanToPackageBoundary(packagePrefix: String): String {
    if (packagePrefix.isEmpty()) {
      return CoreConstants.METAMODELS_PACKAGE
    }

    // If it ends with a dot, remove it
    val cleaned = packagePrefix.trimEnd('.')

    // If empty after cleaning, use fallback
    return cleaned.ifEmpty { CoreConstants.METAMODELS_PACKAGE }
  }

  /** Generate default package name: basePackage.metamodels.sourceSet */
  private fun generateDefaultPackage(basePackage: String, sourceSet: String): String {
    return "$basePackage.metamodels.$sourceSet"
  }

  // Future configuration support methods

  /**
   * Get configured package for source set from KSP options. Future option format:
   * metamodels.main.package=com.example.custom
   */
  private fun getConfiguredPackage(sourceSet: String): String? {
    return kspOptions["metamodels.$sourceSet.package"]
  }

  /**
   * Get configured class name for source set from KSP options. Future option format:
   * metamodels.main.className=CustomMetamodels
   */
  private fun getConfiguredClassName(sourceSet: String): String? {
    return kspOptions["metamodels.$sourceSet.className"]
  }
}

/** Information about where to generate the Metamodels class. */
data class MetamodelsInfo(val packageName: String, val className: String)
