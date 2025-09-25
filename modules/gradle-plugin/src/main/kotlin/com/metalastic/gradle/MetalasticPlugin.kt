package com.metalastic.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies

/**
 * Gradle plugin for configuring Metalastic annotation processor with a type-safe DSL.
 *
 * This plugin:
 * 1. Adds Metalastic core and processor dependencies automatically
 * 2. Provides a type-safe DSL for configuration
 * 3. Converts DSL configuration to KSP args
 *
 * Consumer only needs:
 * 1. Apply KSP plugin: id("com.google.devtools.ksp") version "X.Y.Z"
 * 2. Apply this plugin: id("com.metalastic") version "VERSION"
 */
class MetalasticPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    // Create the metalastic extension
    val extension = project.extensions.create<MetalasticExtension>("metalastic")

    // Add dependencies immediately - KSP needs them early
    addMetalasticDependencies(project)

    // Configure KSP args after project evaluation
    project.afterEvaluate {
      // Configure KSP args based on extension (only if KSP plugin is available)
      if (project.plugins.hasPlugin("com.google.devtools.ksp")) {
        configureKspArgs(project, extension)
      } else {
        project.logger.warn(
          "Metalastic: KSP plugin not found. Please add it to your plugins block: id(\"com.google.devtools.ksp\") version \"<version>\""
        )
      }
    }
  }

  private fun addMetalasticDependencies(project: Project) {
    val version = getMetalasticVersion()

    // Only add dependencies if not in a Metalastic project itself
    if (project.group.toString() != "com.metalastic") {
      project.dependencies {
        // Add core implementation dependency
        add("implementation", "com.metalastic:core:$version")
        project.logger.info("Metalastic: Added core dependency: com.metalastic:core:$version")

        // Add processor KSP dependency (now safe without version conflicts)
        add("ksp", "com.metalastic:processor:$version")
        project.logger.info(
          "Metalastic: Added processor dependency: ksp(\"com.metalastic:processor:$version\")"
        )
      }
    } else {
      project.logger.info("Metalastic: Skipping dependency addition for Metalastic project itself")
    }
  }

  private fun getMetalasticVersion(): String {
    // Try JAR manifest first (for published versions)
    this::class.java.`package`.implementationVersion?.let {
      return it
    }

    // Try embedded properties file (for development)
    this::class.java.classLoader.getResourceAsStream("metalastic-plugin.properties")?.use {
      val properties = java.util.Properties()
      properties.load(it)
      properties.getProperty("version")?.let { version ->
        return version
      }
    }

    // Should not happen in production
    error("Plugin version could not be determined")
  }

  @Suppress("LongParameterList")
  private fun configureSourceSet(
    argMethod: java.lang.reflect.Method,
    kspExtension: Any,
    project: Project,
    sourceSetName: String,
    config: SourceSetConfiguration,
    defaultPackage: String? = null,
    defaultClassName: String? = null,
  ) {
    val packageName =
      if (config.packageName.isPresent) {
        config.packageName.get()
      } else {
        defaultPackage
      }

    val className =
      if (config.className.isPresent) {
        config.className.get()
      } else {
        defaultClassName
      }

    if (packageName != null) {
      argMethod.invoke(kspExtension, "metamodels.$sourceSetName.package", packageName)
      project.logger.info(
        "Metalastic: Set KSP arg metamodels.$sourceSetName.package = $packageName"
      )
    }

    if (className != null) {
      argMethod.invoke(kspExtension, "metamodels.$sourceSetName.className", className)
      project.logger.info(
        "Metalastic: Set KSP arg metamodels.$sourceSetName.className = $className"
      )
    }
  }

  private fun configureCustomSourceSets(
    argMethod: java.lang.reflect.Method,
    kspExtension: Any,
    project: Project,
    customSourceSets: org.gradle.api.NamedDomainObjectContainer<SourceSetConfig>,
  ) {
    customSourceSets.forEach { sourceSetConfig ->
      val sourceSetName = sourceSetConfig.name

      val packageName =
        if (sourceSetConfig.packageName.isPresent) {
          sourceSetConfig.packageName.get()
        } else null

      val className =
        if (sourceSetConfig.className.isPresent) {
          sourceSetConfig.className.get()
        } else null

      if (packageName != null) {
        argMethod.invoke(kspExtension, "metamodels.$sourceSetName.package", packageName)
        project.logger.info(
          "Metalastic: Set KSP arg metamodels.$sourceSetName.package = $packageName"
        )
      }

      if (className != null) {
        argMethod.invoke(kspExtension, "metamodels.$sourceSetName.className", className)
        project.logger.info(
          "Metalastic: Set KSP arg metamodels.$sourceSetName.className = $className"
        )
      }
    }
  }

  private fun configureKspArgs(project: Project, extension: MetalasticExtension) {
    project.logger.info("Metalastic: Configuring KSP arguments")

    runCatching {
        // Access KSP extension through reflection to avoid direct dependency
        val kspExtension = project.extensions.findByName("ksp")
        if (kspExtension == null) {
          project.logger.warn("Metalastic: KSP extension not found")
          return
        }

        // Get the arg method through reflection
        val argMethod =
          kspExtension::class.java.getMethod("arg", String::class.java, String::class.java)

        // Metamodels configuration
        val metamodels = extension.metamodels

        // Global fallbacks
        if (metamodels.fallbackPackage.isPresent) {
          argMethod.invoke(kspExtension, "metamodels.package", metamodels.fallbackPackage.get())
        }
        if (metamodels.fallbackClassName.isPresent) {
          argMethod.invoke(kspExtension, "metamodels.className", metamodels.fallbackClassName.get())
        }

        // Source set specific configurations - use defaults if not configured
        configureSourceSet(
          argMethod,
          kspExtension,
          project,
          "main",
          metamodels.main,
          defaultPackage = "${project.group}.metamodels",
          defaultClassName = "Metamodels",
        )

        configureSourceSet(argMethod, kspExtension, project, "test", metamodels.test)
        configureSourceSet(argMethod, kspExtension, project, "integration", metamodels.integration)
        configureSourceSet(
          argMethod,
          kspExtension,
          project,
          "integrationTest",
          metamodels.integrationTest,
        )
        configureSourceSet(argMethod, kspExtension, project, "functional", metamodels.functional)
        configureSourceSet(
          argMethod,
          kspExtension,
          project,
          "functionalTest",
          metamodels.functionalTest,
        )
        configureSourceSet(argMethod, kspExtension, project, "e2e", metamodels.e2e)
        configureSourceSet(argMethod, kspExtension, project, "e2eTest", metamodels.e2eTest)

        // Configure custom source sets
        configureCustomSourceSets(argMethod, kspExtension, project, metamodels.customSourceSets)

        // Features configuration
        val features = extension.features
        if (features.generateJavaCompatibility.isPresent) {
          argMethod.invoke(
            kspExtension,
            "metalastic.generateJavaCompatibility",
            features.generateJavaCompatibility.get().toString(),
          )
          project.logger.info(
            "Metalastic: Set KSP arg metalastic.generateJavaCompatibility = ${features.generateJavaCompatibility.get()}"
          )
        }
        if (features.generatePrivateClassMetamodels.isPresent) {
          argMethod.invoke(
            kspExtension,
            "metalastic.generatePrivateClassMetamodels",
            features.generatePrivateClassMetamodels.get().toString(),
          )
        }

        // Reporting configuration
        val reporting = extension.reporting
        if (reporting.enabled.get() && reporting.outputPath.isPresent) {
          argMethod.invoke(kspExtension, "metalastic.reportingPath", reporting.outputPath.get())
          project.logger.info(
            "Metalastic: Set KSP arg metalastic.reportingPath = ${reporting.outputPath.get()}"
          )
        }

        project.logger.info("Metalastic: KSP configuration complete")
      }
      .onFailure { error ->
        project.logger.error("Metalastic: Failed to configure KSP args: ${error.message}")
      }
  }
}
