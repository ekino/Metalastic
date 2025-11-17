/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.options

import com.ekino.oss.metalastic.processor.CoreConstants
import com.ekino.oss.metalastic.processor.CoreConstants.ProcessorOptions.Metamodels
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.report.reporter
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Configuration for Metamodels generation, supporting both automatic detection and manual
 * configuration via specific options.
 */
class MetamodelsConfiguration(
  val packageOverride: String? = null,
  val classNameOverride: String? = null,
  val classPrefixOverride: String? = null,
  val sourceSetPackageOverrides: Map<String, String> = emptyMap(),
  val sourceSetClassNameOverrides: Map<String, String> = emptyMap(),
  val sourceSetClassPrefixOverrides: Map<String, String> = emptyMap(),
) {

  /**
   * Generate the appropriate package and class name for Metamodels based on document classes and
   * source set detection.
   */
  fun generateMetamodelsInfo(graph: MetalasticGraph): MetamodelsInfo {
    // Detect source set from file paths
    val sourceSet = detectSourceSet(graph.models().first().sourceClassDeclaration)
    reporter.debug { "Detected source set: $sourceSet" }

    // Check for configuration overrides
    val configuredPackage = getConfiguredPackage(sourceSet)
    val configuredClassName = getConfiguredClassName(sourceSet)
    val configuredClassPrefix = getConfiguredClassPrefix(sourceSet)

    // Use common ancestor package as default
    val finalPackage = configuredPackage ?: generateDefaultPackage(graph)
    val finalClassName = configuredClassName ?: CoreConstants.Metamodels.SIMPLE_NAME
    val finalClassPrefix = configuredClassPrefix ?: CoreConstants.META_PREFIX

    // Log configuration resolution summary
    if (configuredPackage != null || configuredClassName != null || configuredClassPrefix != null) {
      reporter.debug { "Applied configuration overrides for source set '$sourceSet'" }
    } else {
      reporter.debug {
        "Using default configuration for source set '$sourceSet' (no overrides found)"
      }
    }

    reporter.debug {
      "Final Metamodels configuration: $finalPackage.$finalClassName (prefix: $finalClassPrefix)"
    }

    return MetamodelsInfo(
      packageName = finalPackage,
      className = finalClassName,
      classPrefix = finalClassPrefix,
    )
  }

  /** Detect source set from document class file paths. */
  private fun detectSourceSet(documentClass: KSClassDeclaration): String {
    return documentClass.containingFile?.filePath?.let { extractSourceSetFromPath(it) } ?: "main"
  }

  /**
   * Extract source set name from file path by finding the directory before 'java' or 'kotlin'.
   * Example: /path/to/src/test/java/com/example/Class.java → "test"
   */
  private fun extractSourceSetFromPath(filePath: String): String? {
    val pathSegments = filePath.split('/')

    // Find the index of 'java' or 'kotlin' directory
    val languageDirIndex = pathSegments.indexOfFirst { it == "java" || it == "kotlin" }
    if (languageDirIndex <= 0) return null // No language dir found or it's the first segment

    // The source set is the directory immediately before the language directory
    val sourceSetName = pathSegments[languageDirIndex - 1]
    return if (sourceSetName.isNotEmpty() && isValidSourceSetName(sourceSetName)) {
      sourceSetName
    } else {
      null
    }
  }

  /**
   * Validates if a string is a valid source set name. Source set names should be valid identifiers.
   */
  private fun isValidSourceSetName(sourceSetName: String): Boolean {
    return sourceSetName.isNotEmpty() &&
      sourceSetName.all { it.isLetterOrDigit() || it == '_' || it == '-' } &&
      sourceSetName[0].isLetter()
  }

  /**
   * Generate default package name based on common ancestor of document classes. Example:
   * com.example.test.MyClass + com.example.OtherTest → com.example
   */
  private fun generateDefaultPackage(graph: MetalasticGraph): String {
    // Extract package names from all root document classes (non-nested)
    val packageNames =
      graph.rootModels().map { it.packageName }.filter { it.isNotBlank() }.distinct().toList()

    return when {
      packageNames.isEmpty() -> Metamodels.FALLBACK_METAMODELS_PACKAGE
      packageNames.size == 1 -> packageNames.first()
      else -> {
        val commonAncestor = findCommonPackageAncestor(packageNames)
        commonAncestor.takeIf { it.isNotBlank() } ?: Metamodels.FALLBACK_METAMODELS_PACKAGE
      }
    }
  }

  /**
   * Find the common ancestor package from a list of package names. Example:
   * ["com.example.test", "com.example.other"] → "com.example"
   */
  private fun findCommonPackageAncestor(packageNames: List<String>): String {
    return when {
      packageNames.isEmpty() -> ""
      packageNames.size == 1 -> packageNames.first()
      else -> {
        // Split all packages into segments
        val packageSegments = packageNames.map { it.split('.') }

        // Find the minimum number of segments
        val minSegments = packageSegments.minOfOrNull { it.size } ?: 0

        if (minSegments == 0) {
          ""
        } else {
          // Find common prefix segments
          val commonSegments =
            (0 until minSegments)
              .takeWhile { i ->
                val segmentAtIndex = packageSegments.first()[i]
                packageSegments.all { it[i] == segmentAtIndex }
              }
              .map { i -> packageSegments.first()[i] }

          commonSegments.joinToString(".")
        }
      }
    }
  }

  // Configuration support methods

  /**
   * Get configured package for source set. Supports both source set specific and global fallback
   * options.
   */
  private fun getConfiguredPackage(sourceSet: String): String? {
    // Try source set specific configuration first
    val sourceSetSpecific = sourceSetPackageOverrides[sourceSet]
    if (sourceSetSpecific != null) {
      return validateAndLogPackage(sourceSetSpecific, "sourceSet-specific package for '$sourceSet'")
    }

    // Fall back to global configuration
    return packageOverride?.let { validateAndLogPackage(it, "global package override") }
  }

  /**
   * Get configured class name for source set. Supports both source set specific and global fallback
   * options.
   */
  private fun getConfiguredClassName(sourceSet: String): String? {
    // Try source set specific configuration first
    val sourceSetSpecific = sourceSetClassNameOverrides[sourceSet]
    if (sourceSetSpecific != null) {
      return validateAndLogClassName(
        sourceSetSpecific,
        "sourceSet-specific className for '$sourceSet'",
      )
    }

    // Fall back to global configuration
    return classNameOverride?.let { validateAndLogClassName(it, "global className override") }
  }

  /**
   * Get configured class prefix for source set. Supports both source set specific and global
   * fallback options.
   */
  private fun getConfiguredClassPrefix(sourceSet: String): String? {
    // Try source set specific configuration first
    val sourceSetSpecific = sourceSetClassPrefixOverrides[sourceSet]
    if (sourceSetSpecific != null) {
      return validateAndLogClassPrefix(
        sourceSetSpecific,
        "sourceSet-specific classPrefix for '$sourceSet'",
      )
    }

    // Fall back to global configuration
    return classPrefixOverride?.let { validateAndLogClassPrefix(it, "global classPrefix override") }
  }

  /**
   * Validates a package name and logs the configuration resolution. Returns the validated package
   * name or null if invalid.
   */
  private fun validateAndLogPackage(packageName: String, optionKey: String): String? {
    val trimmed = packageName.trim()
    return when {
      trimmed.isEmpty() -> {
        reporter.debug { "Invalid empty package name for option '$optionKey', ignoring" }
        null
      }
      !isValidPackageName(trimmed) -> {
        reporter.debug { "Invalid package name '$trimmed' for option '$optionKey', ignoring" }
        null
      }
      else -> {
        reporter.debug { "Using configured package '$trimmed' from option '$optionKey'" }
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
        reporter.debug { "Invalid empty class name for option '$optionKey', ignoring" }
        null
      }
      !isValidClassName(trimmed) -> {
        reporter.debug { "Invalid class name '$trimmed' for option '$optionKey', ignoring" }
        null
      }
      else -> {
        reporter.debug { "Using configured class name '$trimmed' from option '$optionKey'" }
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
   * Validates a class prefix and logs the configuration resolution. Returns the validated prefix or
   * null if invalid.
   */
  private fun validateAndLogClassPrefix(classPrefix: String, optionKey: String): String? {
    val trimmed = classPrefix.trim()
    return when {
      trimmed.isEmpty() -> {
        reporter.debug { "Invalid empty class prefix for option '$optionKey', ignoring" }
        null
      }
      !isValidClassPrefix(trimmed) -> {
        reporter.debug { "Invalid class prefix '$trimmed' for option '$optionKey', ignoring" }
        null
      }
      !trimmed[0].isUpperCase() -> {
        reporter.debug {
          "Class prefix '$trimmed' for option '$optionKey' should start with uppercase letter (convention)"
        }
        reporter.debug { "Using class prefix '$trimmed' from option '$optionKey'" }
        trimmed
      }
      else -> {
        reporter.debug { "Using class prefix '$trimmed' from option '$optionKey'" }
        trimmed
      }
    }
  }

  /**
   * Validates if a string is a valid class prefix. Must be a valid Java identifier (can be used as
   * part of a class name).
   */
  private fun isValidClassPrefix(classPrefix: String): Boolean {
    return classPrefix.isNotEmpty() && isValidIdentifier(classPrefix)
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
data class MetamodelsInfo(val packageName: String, val className: String, val classPrefix: String)
