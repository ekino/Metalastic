import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Elasticsearch DSL module for Spring Data ES 5.0.x

val springDataEsVersion = "5.0.12"
val elasticsearchJavaVersion = "8.5.3"

// Read version from parent shared module
evaluationDependsOn(":modules:elasticsearch-dsl-shared-8.5")

version = project(":modules:elasticsearch-dsl-shared-8.5").version

kotlin {
  sourceSets {
    main { kotlin.srcDir("../elasticsearch-dsl-shared-8.5/src/main/kotlin") }
    test { kotlin.srcDir("../elasticsearch-dsl-shared-8.5/src/test/kotlin") }
  }
}

configurations.all {
  resolutionStrategy.force("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
}

dependencies {
  // Core Metalastic module for Field types
  api(project(":modules:core"))

  // Spring Data Elasticsearch 5.0.12
  api("org.springframework.data:spring-data-elasticsearch:$springDataEsVersion")

  // Force elasticsearch-java version to match Spring Data ES 5.0.12
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
  testImplementation("org.hamcrest:hamcrest:2.2")
  testImplementation("org.skyscreamer:jsonassert:1.5.1")

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}

tasks.test { systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true") }

tasks.register("showVersion") {
  doLast { println("elasticsearch-dsl-5.0 version: ${project.version}") }
}

tasks.withType<KotlinCompile> {
  compilerOptions { freeCompilerArgs.addAll(listOf("-Xcontext-parameters")) }
}
