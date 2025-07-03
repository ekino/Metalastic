dependencies {
    // Test both modules
    testImplementation(project(":qelasticsearch-dsl"))
    testImplementation(project(":qelasticsearch-processor"))
    
    // Spring Data Elasticsearch for real document classes
    testImplementation("org.springframework.data:spring-data-elasticsearch:5.2.5")
    
    // Annotation processing testing
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    
    // Additional testing utilities
    testImplementation("io.mockk:mockk:1.13.8")
}