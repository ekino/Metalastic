plugins { alias(libs.plugins.ksp) }

kotlin {
  compilerOptions { freeCompilerArgs.add("-opt-in=com.google.devtools.ksp.KspExperimental") }
}

dependencies {
  // DSL runtime dependency
  implementation(project(":modules:core"))

  // Spring Data Elasticsearch for annotations (needed at KSP runtime for annotation processing)
  implementation(libs.spring.data.elasticsearch)

  // KSP and Code generation
  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(libs.jakarta.annotation.api)

  // Testing annotation processing
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.kotlin.compile.testing.ksp)
  // Needed at compile-time for KSPJvmConfig.Builder used in onBuilder hook;
  // kctfork declares it only at runtime scope.
  testImplementation(libs.ksp.common.deps)
  testImplementation(libs.mockk)

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}
