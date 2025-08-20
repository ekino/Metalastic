plugins { alias(libs.plugins.ksp) }

// Generated code is automatically excluded by Spotless configuration in parent build.gradle.kts

dependencies {
  // DSL runtime needed for generated code
  implementation(project(":core"))
  api(libs.jakarta.annotation.api)
  ksp(project(":processor"))

  // Spring Data Elasticsearch for real document classes
  implementation(libs.spring.data.elasticsearch)

  // Testing dependencies
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.kotlin.compile.testing.ksp)
  testImplementation(libs.mockk)

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}
