dependencies {
    // Spring Data Elasticsearch for annotations
    implementation(libs.spring.data.elasticsearch)

    // Kotlin reflection for runtime inspection
    implementation(libs.kotlin.reflect)

    // Additional testing for DSL module
    testImplementation(libs.mockk)

    // Kotest testing framework
    testImplementation(libs.bundles.kotest.extended)

    // Logging for tests
    testImplementation(libs.kotlin.logging)
}
