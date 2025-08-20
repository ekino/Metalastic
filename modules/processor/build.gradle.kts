plugins { alias(libs.plugins.ksp) }

dependencies {
  // DSL runtime dependency
  implementation(project(":modules:core"))

  // Spring Data Elasticsearch for annotations
  implementation(libs.spring.data.elasticsearch)

  // KSP and Code generation
  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(libs.jakarta.annotation.api)

  // Testing annotation processing
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.kotlin.compile.testing.ksp)
  testImplementation(libs.mockk)

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}
