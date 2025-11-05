plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.gradle.maven.publish.plugin)
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
    create("metalastic") {
      id = "com.ekino.oss.metalastic"
      implementationClass = "com.ekino.oss.metalastic.gradle.MetalasticPlugin"
      displayName = "Metalastic Plugin"
      description =
        "Gradle plugin for configuring Metalastic annotation processor with type-safe DSL"
      tags = listOf("elasticsearch", "querydsl", "ksp", "annotation-processing", "kotlin")
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

// Publishing configuration for the plugin
mavenPublishing {
  // Publish to Maven Central via S01 OSSRH
  // Note: Central Portal doesn't support SNAPSHOTs, so we use S01 which supports both
  publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, automaticRelease = true)

  // Automatically sign all publications
  signAllPublications()

  // Configure POM metadata
  pom {
    name.set("Metalastic Gradle Plugin")
    description.set(
      "Gradle plugin for configuring Metalastic annotation processor with type-safe DSL"
    )
    url.set("https://github.com/ekino/Metalastic")

    licenses {
      license {
        name.set("MIT License")
        url.set("https://opensource.org/licenses/MIT")
      }
    }

    developers {
      developer {
        id.set("ekino")
        name.set("ekino")
        email.set("opensource@ekino.com")
        organization.set("ekino")
        organizationUrl.set("https://github.com/ekino")
      }
    }

    scm {
      connection.set("scm:git:git://github.com/ekino/Metalastic.git")
      developerConnection.set("scm:git:ssh://github.com/ekino/Metalastic.git")
      url.set("https://github.com/ekino/Metalastic")
    }
  }
}
