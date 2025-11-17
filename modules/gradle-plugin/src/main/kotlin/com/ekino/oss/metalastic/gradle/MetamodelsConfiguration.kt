/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.gradle

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Configuration for metamodel generation, including package names and class names for different
 * source sets.
 */
abstract class MetamodelsConfiguration @Inject constructor(private val objects: ObjectFactory) {

  /**
   * Global default package for metamodels (default: [PluginConstants.Metamodels.DEFAULT_PACKAGE])
   */
  abstract val packageName: Property<String>

  /**
   * Global default class name for metamodels (default:
   * [PluginConstants.Metamodels.DEFAULT_CLASS_NAME])
   */
  abstract val className: Property<String>

  /**
   * Global default class prefix for generated Q-classes (default:
   * [PluginConstants.Metamodels.DEFAULT_CLASS_PREFIX])
   */
  abstract val classPrefix: Property<String>

  /** Configuration for main source set */
  val main: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for test source set */
  val test: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for integration source set */
  val integration: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for integrationTest source set */
  val integrationTest: SourceSetConfiguration =
    objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for functional source set */
  val functional: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for functionalTest source set */
  val functionalTest: SourceSetConfiguration =
    objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for e2e source set */
  val e2e: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Configuration for e2eTest source set */
  val e2eTest: SourceSetConfiguration = objects.newInstance(SourceSetConfiguration::class.java)

  /** Container for dynamic source set configurations */
  val customSourceSets: NamedDomainObjectContainer<SourceSetConfig> =
    objects.domainObjectContainer(SourceSetConfig::class.java) { name ->
      objects.newInstance(SourceSetConfig::class.java).apply { this.name = name }
    }

  init {
    // Set up defaults
    packageName.convention(PluginConstants.Metamodels.DEFAULT_PACKAGE)
    className.convention(PluginConstants.Metamodels.DEFAULT_CLASS_NAME)
    classPrefix.convention(PluginConstants.Metamodels.DEFAULT_CLASS_PREFIX)
  }

  /** Configure main source set metamodels */
  fun main(action: Action<SourceSetConfiguration>) {
    action.execute(main)
  }

  /** Configure test source set metamodels */
  fun test(action: Action<SourceSetConfiguration>) {
    action.execute(test)
  }

  /** Configure integration source set metamodels */
  fun integration(action: Action<SourceSetConfiguration>) {
    action.execute(integration)
  }

  /** Configure integrationTest source set metamodels */
  fun integrationTest(action: Action<SourceSetConfiguration>) {
    action.execute(integrationTest)
  }

  /** Configure functional source set metamodels */
  fun functional(action: Action<SourceSetConfiguration>) {
    action.execute(functional)
  }

  /** Configure functionalTest source set metamodels */
  fun functionalTest(action: Action<SourceSetConfiguration>) {
    action.execute(functionalTest)
  }

  /** Configure e2e source set metamodels */
  fun e2e(action: Action<SourceSetConfiguration>) {
    action.execute(e2e)
  }

  /** Configure e2eTest source set metamodels */
  fun e2eTest(action: Action<SourceSetConfiguration>) {
    action.execute(e2eTest)
  }

  /** Configure a custom source set metamodels */
  fun sourceSet(name: String, action: Action<SourceSetConfig>) {
    customSourceSets.create(name, action)
  }
}

/** Configuration for a specific source set's metamodel generation. */
abstract class SourceSetConfiguration {

  /** Package name for this source set's metamodels */
  abstract val packageName: Property<String>

  /** Class name for this source set's metamodels */
  abstract val className: Property<String>

  /** Class prefix for this source set's generated Q-classes */
  abstract val classPrefix: Property<String>
}
