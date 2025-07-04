plugins {
    alias(libs.plugins.ksp)
}

// Disable ktlint for test module that contains generated code
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = false
}

dependencies {
    // DSL runtime needed for generated code
    implementation(project(":qelasticsearch-dsl"))
    ksp(project(":qelasticsearch-processor"))

    // Spring Data Elasticsearch for real document classes
    implementation(libs.spring.data.elasticsearch)

    // Lombok for Java interoperability testing
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing dependencies
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.mockk)
}
