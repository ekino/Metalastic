package com.metalastic.gradle

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

  /** Class name for this source set's metamodels */
  abstract val className: Property<String>
}
