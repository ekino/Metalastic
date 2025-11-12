import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Shared DSL source for elasticsearch-java 8.5-8.13 (Spring Data ES 5.0-5.3)
// This module is NOT published - only the version-specific variants (5.0, 5.1, 5.2, 5.3) are
// published
//
// DSL version for all 8.15-based variants (5.4, 5.5)
version = "1.0"

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
