package com.ekino.oss.metalastic.gradle

import org.gradle.api.provider.Property

/** Configuration for Metalastic feature toggles. */
abstract class FeaturesConfiguration {

  /**
   * Generate @JvmField annotations for Java interoperability (default:
   * [PluginConstants.Features.DEFAULT_GENERATE_JAVA_COMPATIBILITY])
   */
  abstract val generateJavaCompatibility: Property<Boolean>

  /**
   * Generate metamodels for private @Document classes (default:
   * [PluginConstants.Features.DEFAULT_GENERATE_PRIVATE_CLASS_METAMODELS])
   */
  abstract val generatePrivateClassMetamodels: Property<Boolean>

  init {
    // Set up defaults to match existing processor behavior
    generateJavaCompatibility.convention(
      PluginConstants.Features.DEFAULT_GENERATE_JAVA_COMPATIBILITY
    )
    generatePrivateClassMetamodels.convention(
      PluginConstants.Features.DEFAULT_GENERATE_PRIVATE_CLASS_METAMODELS
    )
  }
}
