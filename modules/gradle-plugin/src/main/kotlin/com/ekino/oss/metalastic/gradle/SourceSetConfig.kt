/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.gradle

import org.gradle.api.provider.Property

/**
 * Configuration for a dynamically named source set's metamodel generation. This is used for custom
 * source sets defined via sourceSet("name") { ... }
 */
abstract class SourceSetConfig {

  /** The name of the source set */
  lateinit var name: String

  /** Package name for this source set's metamodels */
  abstract val packageName: Property<String>

  /**
   * Name of the centralized Metamodels registry class for this source set. Does NOT affect
   * individual Meta* class names - use [classPrefix] for those.
   */
  abstract val registryClassName: Property<String>

  /**
   * Class prefix for this source set's generated metamodel classes (e.g., "Meta" for MetaProduct)
   */
  abstract val classPrefix: Property<String>
}
