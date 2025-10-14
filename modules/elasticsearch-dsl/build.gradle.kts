import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Elasticsearch DSL module with Spring Data ES version-aligned versioning
// Format: {spring-data-es-version}-{dsl-version}
// Example: 5.0.12-1.0 means compatible with Spring Data ES 5.0.12, DSL version 1.0

val springDataEsVersion = "5.0.12"
val dslVersion = "1.1"

// Override project version for this module only
version =
  when {
    // CI environment - use git describe strategy with elasticsearch-dsl prefix
    System.getenv("CI") != null -> {
      val tag = System.getenv("CI_COMMIT_TAG")
      when {
        // Handle elasticsearch-dsl specific tags: elasticsearch-dsl-v5.0.12-1.0
        tag?.startsWith("elasticsearch-dsl-v") == true -> tag.removePrefix("elasticsearch-dsl-v")
        // Use git describe with elasticsearch-dsl prefix for snapshot versions
        else -> {
          runCatching {
              val gitDescribe =
                providers
                  .exec {
                    commandLine("git", "describe", "--tags", "--always", "--dirty", "--abbrev=7")
                  }
                  .standardOutput
                  .asText
                  .get()
                  .trim()
              val version = gitDescribe.removePrefix("v")
              if (version.contains("-")) {
                "$springDataEsVersion-$dslVersion-${version}-SNAPSHOT"
              } else {
                "$springDataEsVersion-$dslVersion-SNAPSHOT"
              }
            }
            .getOrElse { "$springDataEsVersion-$dslVersion-SNAPSHOT" }
        }
      }
    }
    // Local development - use spring-data-es versioning format
    else -> {
      val localDslVersion =
        project.findProperty("elasticsearch.dsl.version") as String? ?: dslVersion
      "$springDataEsVersion-$localDslVersion-SNAPSHOT"
    }
  }

configurations.all { resolutionStrategy.force("co.elastic.clients:elasticsearch-java:8.5.3") }

dependencies {
  // Core Metalastic module for Field types
  api(project(":modules:core"))

  // Spring Data Elasticsearch 5.0.12 for compatibility
  api("org.springframework.data:spring-data-elasticsearch:5.0.12")

  // Force elasticsearch-java version to match Spring Data ES 5.0.12
  implementation("co.elastic.clients:elasticsearch-java:8.5.3")!!

  // Google Guava for Range support
  api("com.google.guava:guava:33.3.1-jre")

  // Kotlin reflection for type checking
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  // Logging
  implementation(libs.kotlin.logging)

  // Additional testing for DSL module
  testImplementation(libs.mockk)

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)

  // JCV for advanced JSON testing
  testImplementation("com.ekino.oss.jcv:jcv-hamcrest:1.5.0")
  testImplementation("org.hamcrest:hamcrest:2.2")
  testImplementation("org.skyscreamer:jsonassert:1.5.1")

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}

tasks.test { systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true") }

tasks.register("showVersion") {
  doLast { println("elasticsearch-dsl version: ${project.version}") }
}

tasks.withType<KotlinCompile> {
  compilerOptions { freeCompilerArgs.addAll(listOf("-Xcontext-parameters")) }
}
