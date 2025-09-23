plugins { alias(libs.plugins.ksp) }

// Generated code is automatically excluded by Spotless configuration in parent build.gradle.kts

dependencies {
  // DSL runtime needed for generated code
  implementation(project(":modules:core"))
  api(libs.jakarta.annotation.api)
  ksp(project(":modules:processor"))

  // Spring Data Elasticsearch for real document classes
  implementation(libs.spring.data.elasticsearch)

  // Testing dependencies
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.kotlin.compile.testing.ksp)
  testImplementation(libs.mockk)

  // Kotest testing framework
  testImplementation(libs.bundles.kotest.extended)

  // Logging for tests
  testImplementation(libs.kotlin.logging)
}

ksp {
  // Package and Class Name Customization
  arg("metamodels.main.package", "com.example.search.metamodels")
  arg("metamodels.main.className", "MainMetamodels")
  arg("metamodels.test.package", "com.example.test.metamodels")
  arg("metamodels.test.className", "TestMetamodels")

  //     Global Fallbacks
  arg("metamodels.package", "com.example.metamodels")
  //  arg("metamodels.className", "GlobalMetamodels")

  // Feature Toggles
  arg("metalastic.generateJavaCompatibility", "true") // default: true
  // arg("metalastic.generatePrivateClassMetamodels", "true") // default: false
  arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
