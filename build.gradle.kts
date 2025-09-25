plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    group = "com.metalastic"
    version = when {
        // CI environment - use git describe for consistent versioning with CI pipeline
        System.getenv("CI") != null -> {
            val tag = System.getenv("CI_COMMIT_TAG")
            tag?.removePrefix("v") // v1.2.3 -> 1.2.3
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
                    // Fallback to CI_COMMIT_SHA if git describe fails
                    val sha = System.getenv("CI_COMMIT_SHA") ?: "unknown"
                    "${sha.take(7)}-SNAPSHOT"
                }
        }
        // Local development - ALWAYS use localVersion from gradle.properties
        else -> project.findProperty("localVersion") as String? ?: "2.0.1-SNAPSHOT"
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

    // Skip publishing for test module - it's only for integration testing
    // gradle-plugin has its own special publication setup
    val shouldPublish = project.name != "test" && project.name != "gradle-plugin"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    if (shouldPublish) {
        apply(plugin = "maven-publish")
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

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.file("detekt.yml"))
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
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    // Enable sources jar for better IDE support
                    artifact(tasks.named("kotlinSourcesJar"))

                    pom {
                        name.set("Metalastic ${project.name}")
                        description.set("A type-safe metamodel library for Elasticsearch in Kotlin")
                        url.set("https://gitlab.ekino.com/iperia/metalastic")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }

                        scm {
                            connection.set("scm:git:git://gitlab.ekino.com/iperia/metalastic.git")
                            developerConnection.set("scm:git:ssh://gitlab.ekino.com/iperia/metalastic.git")
                            url.set("https://gitlab.ekino.com/iperia/metalastic")
                        }
                    }
                }
            }

            repositories {
                maven {
                    name = "GitLab"
                    url = uri("https://gitlab.ekino.com/api/v4/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                    credentials(HttpHeaderCredentials::class) {
                        name = "Job-Token"
                        value = System.getenv("CI_JOB_TOKEN")
                    }
                    authentication {
                        create("header", HttpHeaderAuthentication::class)
                    }
                }
            }
        }
    }
}