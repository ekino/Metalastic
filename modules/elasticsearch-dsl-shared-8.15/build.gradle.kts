import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Shared DSL source for elasticsearch-java 8.15+ (Spring Data ES 5.4-5.5)
// This module is NOT published - only the version-specific variants (5.4, 5.5) are published
//
// DSL version for all 8.15-based variants (5.4, 5.5)
version = "1.0"

val springDataEsVersion = "5.5.5" // For compilation/testing only
val elasticsearchJavaVersion = "8.18.8"

configurations.all {
  resolutionStrategy.force("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
}

dependencies {
  // Core Metalastic module for Field types
  api(project(":modules:core"))

  // Spring Data Elasticsearch (for compilation/testing only - variants have their own versions)
  api("org.springframework.data:spring-data-elasticsearch:$springDataEsVersion")

  // Force elasticsearch-java version
  implementation("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")!!

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
  testImplementation("org.hamcrest:hamcrest:3.0")
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
