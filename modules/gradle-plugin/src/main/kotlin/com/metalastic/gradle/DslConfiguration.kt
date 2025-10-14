package com.metalastic.gradle

import org.gradle.api.provider.Property

/** Configuration for Metalastic elasticsearch-dsl module. */
abstract class DslConfiguration {

  /**
   * Enable strict mode for nested queries. When enabled, throws an exception if `.nested()` is used
   * on a non-nested field instead of just logging a warning (default: false)
   *
   * This setting configures test tasks and JavaExec tasks (including application run tasks) to pass
   * `-Dmetalastic.dsl.strict=true` as a system property.
   */
  abstract val strictMode: Property<Boolean>

  init {
    strictMode.convention(false)
  }
}
