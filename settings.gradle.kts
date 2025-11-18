plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Metalastic"

include(
    ":modules:core",
    ":modules:processor",
    ":modules:gradle-plugin",
    ":modules:bom",
    ":modules:elasticsearch-dsl-5.3",
    ":modules:elasticsearch-dsl",
    ":modules:test"
)