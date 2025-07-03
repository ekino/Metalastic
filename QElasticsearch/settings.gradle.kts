plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "QElasticsearch"

include(
    ":qelasticsearch-dsl",
    ":qelasticsearch-processor", 
    ":qelasticsearch-test"
)