package com.metalastic.gradle

import org.gradle.api.provider.Property

/** Configuration for Metalastic feature toggles. */
abstract class FeaturesConfiguration {

  /** Generate @JvmField annotations for Java interoperability (default: true) */
  abstract val generateJavaCompatibility: Property<Boolean>

  /** Generate metamodels for private @Document classes (default: false) */
  abstract val generatePrivateClassMetamodels: Property<Boolean>

  init {
    // Set up defaults to match existing processor behavior
    generateJavaCompatibility.convention(true)
    generatePrivateClassMetamodels.convention(false)
  }
}
