plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Metalastic"

include(
    ":modules:core",
    ":modules:processor",
    ":modules:gradle-plugin",
    ":modules:elasticsearch-dsl-shared-8.5",
    ":modules:elasticsearch-dsl-shared-8.15",
    ":modules:elasticsearch-dsl-5.0",
    ":modules:elasticsearch-dsl-5.1",
    ":modules:elasticsearch-dsl-5.2",
    ":modules:elasticsearch-dsl-5.3",
    ":modules:elasticsearch-dsl-5.4",
    ":modules:elasticsearch-dsl-5.5",
    ":modules:test"
)