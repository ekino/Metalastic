plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "metalastic-docs.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.components.resources)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // Compose for Web
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)

                // Markdown rendering
                implementation("com.mikepenz:multiplatform-markdown-renderer:0.30.0")

                // Syntax highlighting
                implementation("dev.snipme:highlights:1.1.0")
            }
        }
    }
}
