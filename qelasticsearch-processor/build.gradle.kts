plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    // DSL runtime dependency
    implementation(project(":qelasticsearch-dsl"))

    // Spring Data Elasticsearch for annotations
    implementation(libs.spring.data.elasticsearch)

    // KSP and Code generation
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    // Testing annotation processing
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
    
    // Kotest testing framework
    testImplementation(libs.bundles.kotest.extended)
}
