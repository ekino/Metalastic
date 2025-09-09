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

    // Check for KSP configuration overrides
    val configuredPackage = getConfiguredPackage(sourceSet)
    val configuredClassName = getConfiguredClassName(sourceSet)

    val finalPackage = configuredPackage ?: generateDefaultPackage(basePackage, sourceSet)
    val finalClassName = configuredClassName ?: CoreConstants.METAMODELS_CLASS_NAME

    // Log configuration resolution summary
    if (configuredPackage != null || configuredClassName != null) {
      logger.info("Applied KSP configuration overrides for source set '$sourceSet'")
    } else {
      logger.info(
        "Using default configuration for source set '$sourceSet' (no KSP overrides found)"
      )
    }

    logger.info("Final Metamodels configuration: $finalPackage.$finalClassName")

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
   * Get configured package for source set from KSP options. Supports both source set specific and
   * global fallback options:
   * - metamodels.main.package=com.example.custom (source set specific)
   * - metamodels.package=com.example.global (global fallback)
   */
  private fun getConfiguredPackage(sourceSet: String): String? {
    // Try source set specific configuration first
    val sourceSetSpecific = kspOptions["metamodels.$sourceSet.package"]
    if (sourceSetSpecific != null) {
      return validateAndLogPackage(sourceSetSpecific, "metamodels.$sourceSet.package")
    }

    // Fall back to global configuration
    val global = kspOptions["metamodels.package"]
    return global?.let { validateAndLogPackage(it, "metamodels.package") }
  }

  /**
   * Get configured class name for source set from KSP options. Supports both source set specific
   * and global fallback options:
   * - metamodels.main.className=CustomMetamodels (source set specific)
   * - metamodels.className=GlobalMetamodels (global fallback)
   */
  private fun getConfiguredClassName(sourceSet: String): String? {
    // Try source set specific configuration first
    val sourceSetSpecific = kspOptions["metamodels.$sourceSet.className"]
    if (sourceSetSpecific != null) {
      return validateAndLogClassName(sourceSetSpecific, "metamodels.$sourceSet.className")
    }

    // Fall back to global configuration
    val global = kspOptions["metamodels.className"]
    return global?.let { validateAndLogClassName(it, "metamodels.className") }
  }

  /**
   * Get additional configuration options for the processor. Supports various feature toggles and
   * customization options.
   */
  fun getProcessorOptions(): ProcessorOptions {
    return ProcessorOptions(
      generateJavaCompatibility =
        getBooleanOption("qelasticsearch.generateJavaCompatibility", true),
      debugLogging = getBooleanOption("qelasticsearch.debugLogging", false),
    )
  }

  /** Helper to parse boolean options with default fallback. */
  private fun getBooleanOption(key: String, default: Boolean): Boolean {
    return when (val value = kspOptions[key]?.trim()?.lowercase()) {
      "true" -> {
        logger.info("Enabled feature '$key'")
        true
      }
      "false" -> {
        logger.info("Disabled feature '$key'")
        false
      }
      null -> {
        logger.info("Using default value for '$key': $default")
        default
      }
      else -> {
        logger.warn("Invalid boolean value '$value' for option '$key', using default: $default")
        default
      }
    }
  }

  /**
   * Validates a package name and logs the configuration resolution. Returns the validated package
   * name or null if invalid.
   */
  private fun validateAndLogPackage(packageName: String, optionKey: String): String? {
    val trimmed = packageName.trim()
    return when {
      trimmed.isEmpty() -> {
        logger.warn("Invalid empty package name for option '$optionKey', ignoring")
        null
      }
      !isValidPackageName(trimmed) -> {
        logger.warn("Invalid package name '$trimmed' for option '$optionKey', ignoring")
        null
      }
      else -> {
        logger.info("Using configured package '$trimmed' from option '$optionKey'")
        trimmed
      }
    }
  }

  /**
   * Validates a class name and logs the configuration resolution. Returns the validated class name
   * or null if invalid.
   */
  private fun validateAndLogClassName(className: String, optionKey: String): String? {
    val trimmed = className.trim()
    return when {
      trimmed.isEmpty() -> {
        logger.warn("Invalid empty class name for option '$optionKey', ignoring")
        null
      }
      !isValidClassName(trimmed) -> {
        logger.warn("Invalid class name '$trimmed' for option '$optionKey', ignoring")
        null
      }
      else -> {
        logger.info("Using configured class name '$trimmed' from option '$optionKey'")
        trimmed
      }
    }
  }

  /**
   * Validates if a string is a valid Java/Kotlin package name. Package names must consist of valid
   * identifiers separated by dots.
   */
  private fun isValidPackageName(packageName: String): Boolean {
    return packageName.isNotEmpty() &&
      !packageName.startsWith('.') &&
      !packageName.endsWith('.') &&
      !packageName.contains("..") &&
      packageName.split('.').all { segment -> segment.isNotEmpty() && isValidIdentifier(segment) }
  }

  /**
   * Validates if a string is a valid Java/Kotlin class name. Class names must be valid identifiers
   * and follow naming conventions.
   */
  private fun isValidClassName(className: String): Boolean {
    return className.isNotEmpty() && isValidIdentifier(className) && className[0].isUpperCase()
  }

  /**
   * Validates if a string is a valid Java/Kotlin identifier. Must start with letter or underscore,
   * followed by letters, digits, or underscores.
   */
  private fun isValidIdentifier(identifier: String): Boolean {
    return identifier.isNotEmpty() &&
      identifier[0].isJavaIdentifierStart() &&
      identifier.drop(1).all { it.isJavaIdentifierPart() }
  }
}

/** Information about where to generate the Metamodels class. */
data class MetamodelsInfo(val packageName: String, val className: String)

/** Additional processor configuration options from KSP arguments. */
data class ProcessorOptions(val generateJavaCompatibility: Boolean, val debugLogging: Boolean)
