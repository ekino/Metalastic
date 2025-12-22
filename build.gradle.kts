plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.gradle.maven.publish.plugin) apply false
}

allprojects {
    group = "com.ekino.oss"
    version = when {
        // CI environment - GitHub Actions
        System.getenv("GITHUB_ACTIONS") != null -> {
            // Use GITHUB_REF_NAME for tag-based releases (unified versioning: only v* tags)
            val tag = System.getenv("GITHUB_REF_NAME")?.takeIf {
                it.startsWith("v")
            }

            tag?.removePrefix("v") // v1.0.0 -> 1.0.0
                ?: // Use git describe to match CI pipeline versioning exactly
                runCatching {
                    val gitDescribe = providers.exec {
                        commandLine("git", "describe", "--tags", "--always", "--dirty", "--abbrev=7")
                    }.standardOutput.asText.get().trim()
                    val version = gitDescribe.removePrefix("v")
                    if (version.contains("-")) {
                        "$version-SNAPSHOT"
                    } else {
                        "$version-SNAPSHOT"
                    }
                } .getOrElse {
                    // Fallback to commit SHA
                    val sha = System.getenv("GITHUB_SHA") ?: "unknown"
                    "${sha.take(7)}-SNAPSHOT"
                }
        }
        // Local development - ALWAYS use localVersion from gradle.properties
        else -> project.findProperty("localVersion") as String? ?: "1.2.0-SNAPSHOT"
    }

    repositories {
        mavenCentral()
    }

}

subprojects {
    // Skip configuration for intermediate directories like 'modules'
    if (project.path == ":modules") {
        // Don't apply plugins to the modules directory itself
        return@subprojects
    }

    // BOM module has its own special setup (java-platform), skip all standard configuration
    if (project.name == "bom") {
        return@subprojects
    }

    // Skip publishing for test module - it's only for integration testing
    // gradle-plugin has its own special publication setup
    val shouldPublish = project.name != "test" &&
                        project.name != "gradle-plugin"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "dev.detekt")

    if (shouldPublish) {
        apply(plugin = "com.vanniktech.maven.publish")
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    dependencies {
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<dev.detekt.gradle.extensions.DetektExtension>("detekt") {
            buildUponDefaultConfig.set(true)
            allRules.set(false)
            config.setFrom(rootProject.file("detekt.yml"))
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/generated/**")
            ktfmt(libs.versions.ktfmt.get()).googleStyle()
            trimTrailingWhitespace()
            endWithNewline()
        }

        java {
            removeUnusedImports()
        }

        kotlinGradle {
            ktfmt().googleStyle().configure {
                it.setRemoveUnusedImports(true)
            }
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("markdown") {
            target("**/*.md")
            prettier()
                .configFile(rootProject.file(".prettierrc.json"))
        }

        yaml {
            target("**/*.yml", "**/*.yaml")
            prettier()
                .configFile(rootProject.file(".prettierrc.json"))
        }
    }

    if (shouldPublish) {
        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            // Set Maven coordinates: group:artifactId:version
            coordinates(
                groupId = project.group.toString(),
                artifactId = "metalastic-${project.name}",
                version = project.version.toString()
            )

            // Publish to Maven Central via Central Portal
            // v0.34.0+ supports both SNAPSHOTs and Releases
            publishToMavenCentral(automaticRelease = true)

            // Automatically sign all publications
            signAllPublications()

            // Configure POM metadata
            pom {
                name.set("Metalastic ${project.name}")
                description.set("A type-safe metamodel library for Elasticsearch in Kotlin")
                url.set("https://github.com/ekino/Metalastic")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("Benoit.Havret")
                        name.set("Benoît Havret")
                        email.set("benoit.havret@ekino.com")
                        organization.set("ekino")
                        organizationUrl.set("https://github.com/ekino")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/ekino/Metalastic.git")
                    developerConnection.set("scm:git:ssh://github.com/ekino/Metalastic.git")
                    url.set("https://github.com/ekino/Metalastic")
                }
            }
        }
    }
}