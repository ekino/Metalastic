dependencies {
    // Spring Data Elasticsearch for annotations
    implementation("org.springframework.data:spring-data-elasticsearch:5.2.5")

    // Kotlin reflection for runtime inspection
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Additional testing for DSL module
    testImplementation("io.mockk:mockk:1.13.8")
}
