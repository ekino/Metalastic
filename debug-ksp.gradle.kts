// Add this to your qelasticsearch-test/build.gradle.kts to enable debugging

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            // Enable debugging for KSP
            "-Xdebug",
            // Add more verbose logging
            "-verbose"
        )
    }
}

// Configure KSP with debug options
ksp {
    arg("ksp.incremental", "false") // Disable incremental processing for debugging
    arg("ksp.verbose", "true")      // Enable verbose logging
}
