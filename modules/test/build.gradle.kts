plugins {
  alias(libs.plugins.ksp)
  // Note: Cannot use id("com.metalastic") within the same project
  // Will be available once published: id("com.metalastic") version "PROJECT_VERSION"
}

// Generated code is automatically excluded by Spotless configuration in parent build.gradle.kts

dependencies {
  // Dependencies need to be added manually since we can't use the plugin within the same project
  implementation(project(":modules:core"))
  ksp(project(":modules:processor"))

  // Spring Data Elasticsearch for real document classes
  implementation(libs.spring.data.elasticsearch)
  api(libs.jakarta.annotation.api)

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

// Demonstration of what the plugin DSL will look like for consumers:
/*
metalastic {
  metamodels {
    main {
      packageName = "com.example.search.metamodels"
      className = "MainMetamodels"
    }
    test {
      packageName = "com.example.test.metamodels"
      className = "TestMetamodels"
    }
    fallbackPackage = "com.example.metamodels"
  }

  features {
    generateJavaCompatibility = true
    generatePrivateClassMetamodels = false
  }

  reporting {
    enabled = true
    outputPath = "build/reports/metalastic/processor-report.md"
  }
}
*/

// Manual KSP configuration (equivalent to the plugin DSL above)
ksp {
  arg("metamodels.main.package", "com.example.search.metamodels")
  arg("metamodels.main.className", "MainMetamodels")
  arg("metamodels.test.package", "com.example.test.metamodels")
  arg("metamodels.test.className", "TestMetamodels")
  arg("metamodels.package", "com.example.metamodels")
  arg("metalastic.generateJavaCompatibility", "true")
  arg("metalastic.reportingPath", "build/reports/metalastic/processor-report.md")
}
