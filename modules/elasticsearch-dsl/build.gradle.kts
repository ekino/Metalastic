import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Elasticsearch DSL module - Rolling Release (Latest Spring Data ES 5.x)
// This artifact tracks the latest Spring Data ES 5.x versions.
// Currently supports: Spring Data ES 5.4.x - 5.5.x
// For frozen compatibility (5.0-5.3), use metalastic-elasticsearch-dsl-5.3 instead.

val springDataEsVersion = "5.5.6" // Latest Spring Data ES 5.x

version = "1.0"

dependencies {
  // Core Metalastic module for Field types
  api(project(":modules:core"))

  // Spring Data Elasticsearch (transitive, brings elasticsearch-java)
  api("org.springframework.data:spring-data-elasticsearch:$springDataEsVersion")

  // Google Guava for Range support
  api("com.google.guava:guava:33.5.0-jre")

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
