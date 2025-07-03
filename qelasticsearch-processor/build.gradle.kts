plugins {
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

dependencies {
    // DSL runtime dependency
    implementation(project(":qelasticsearch-dsl"))
    
    // Spring Data Elasticsearch for annotations
    implementation("org.springframework.data:spring-data-elasticsearch:5.2.5")
    
    // KSP API
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.21-2.0.2")
    
    // Code generation
    implementation("com.squareup:kotlinpoet:1.15.3")
    implementation("com.squareup:kotlinpoet-ksp:1.15.3")
    
    // Testing annotation processing
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
}