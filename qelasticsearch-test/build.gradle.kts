plugins {
    id("com.google.devtools.ksp")
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
    implementation("org.springframework.data:spring-data-elasticsearch:5.2.5")

    // Lombok for Java interoperability testing
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // QueryDSL for comparison and compatibility testing
    implementation("com.querydsl:querydsl-core:5.0.0")

    // KSP testing
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")

    // Additional testing utilities
    testImplementation("io.mockk:mockk:1.13.8")
}
