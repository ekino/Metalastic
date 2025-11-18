import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Elasticsearch DSL module for Spring Data ES 5.0-5.3 (Frozen)
// This artifact supports Spring Data ES 5.0.x through 5.3.x and will not change its compatibility
// range.
// For latest Spring Data ES 5.x support, use metalastic-elasticsearch-dsl instead.

val springDataEsVersion = "5.3.13" // Latest in 5.0-5.3 range

// Version inherited from root project (set from git tags)

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
  doLast { println("elasticsearch-dsl-5.3 version: ${project.version}") }
}

tasks.withType<KotlinCompile> {
  compilerOptions { freeCompilerArgs.addAll(listOf("-Xcontext-parameters")) }
}
