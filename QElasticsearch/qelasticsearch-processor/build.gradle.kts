plugins {
    kotlin("kapt")
}

dependencies {
    // DSL runtime dependency
    implementation(project(":qelasticsearch-dsl"))
    
    // Annotation processing
    implementation("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")
    
    // Code generation
    implementation("com.squareup:kotlinpoet:1.15.3")
    
    // Testing annotation processing
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
}