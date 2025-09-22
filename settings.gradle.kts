plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "Metalastic"

include(
    ":modules:core",
    ":modules:processor",
    ":modules:test"
)