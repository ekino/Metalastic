plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
  // Gradle API is automatically available through kotlin-dsl plugin
  implementation(gradleApi())

  // Note: Removed KSP dependency since we no longer auto-apply KSP
  // Consumer is responsible for applying KSP plugin themselves

  // Testing dependencies
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlin.test)
  testImplementation(gradleTestKit())

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)
}

gradlePlugin {
  website = "https://github.com/ekino/Metalastic"
  vcsUrl = "https://github.com/ekino/Metalastic.git"

  plugins {
    register("metalastic") {
      id = "com.ekino.oss.metalastic"
      implementationClass = "com.ekino.oss.metalastic.gradle.MetalasticPlugin"
      displayName = "Metalastic Plugin"
      description =
        "Gradle plugin for configuring Metalastic annotation processor with type-safe DSL"
      tags = listOf("elasticsearch", "ksp", "annotation-processing", "kotlin")
    }
  }
}

// Generate version properties file for runtime access
val generateVersionPropertiesTask =
  tasks.register("generateVersionProperties") {
    val propertiesDir = layout.buildDirectory.dir("generated/resources")
    val propertiesFile = propertiesDir.get().file("metalastic-plugin.properties")

    // Make task sensitive to version changes
    inputs.property("version", project.version)
    outputs.file(propertiesFile)

    doLast {
      propertiesFile.asFile.parentFile.mkdirs()
      // Use the project's actual version - now consistent between CI and local
      propertiesFile.asFile.writeText("version=${project.version}")
    }
  }

// Include generated resources in the JAR
sourceSets {
  main {
    resources {
      srcDir(generateVersionPropertiesTask.map { it.outputs.files.singleFile.parentFile })
    }
  }
}

// Make sure processResources depends on the version generation task
tasks.processResources { dependsOn(generateVersionPropertiesTask) }

// Plugin marker artifact for easier application
tasks.withType<GenerateModuleMetadata> { enabled = false }
