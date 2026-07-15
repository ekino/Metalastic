plugins { alias(libs.plugins.ksp) }

kotlin {
  compilerOptions { freeCompilerArgs.add("-opt-in=com.google.devtools.ksp.KspExperimental") }
}

// kctfork pulls the KSP2 implementation (symbol-processing-aa-embeddable, symbol-processing)
// transitively at its own KSP version, while the version catalog only declares the KSP API
// artifacts. Without this, bumping the catalog `ksp` version leaves the implementation behind the
// API and it breaks at runtime (NoSuchMethodError: KSPConfig.getIncrementalLog). Force every KSP
// artifact to the catalog `ksp` version so the API and its implementation always resolve together.
configurations
  .matching { it.name.startsWith("test") }
  .configureEach {
    resolutionStrategy.eachDependency {
      if (requested.group == "com.google.devtools.ksp") {
        useVersion(libs.versions.ksp.get())
      }
    }
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
