plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    group = "com.metalastic"
    version = when {
        // CI environment - works with both GitLab CI and GitHub Actions
        System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null -> {
            // Support both GitLab CI_COMMIT_TAG and GitHub GITHUB_REF_NAME
            val tag = System.getenv("CI_COMMIT_TAG")
                ?: System.getenv("GITHUB_REF_NAME")?.takeIf { it.startsWith("v") || it.startsWith("elasticsearch-dsl-v") }

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
                    // Fallback to commit SHA (works with both GitLab and GitHub)
                    val sha = System.getenv("CI_COMMIT_SHA")
                        ?: System.getenv("GITHUB_SHA")
                        ?: "unknown"
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
                        url.set("https://github.com/ekino/Metalastic")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }

                        developers {
                            developer {
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

            repositories {
                // GitHub Packages - Primary target
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/ekino/Metalastic")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                            ?: project.findProperty("gpr.user") as String?
                        password = System.getenv("GITHUB_TOKEN")
                            ?: project.findProperty("gpr.token") as String?
                    }
                }

                // GitLab Maven Registry - Only when running in GitLab CI
                // Conditional: only added if CI_PROJECT_ID and CI_JOB_TOKEN are available
                val gitlabProjectId = System.getenv("CI_PROJECT_ID")
                val gitlabJobToken = System.getenv("CI_JOB_TOKEN")
                if (gitlabProjectId != null && gitlabJobToken != null) {
                    maven {
                        name = "GitLab"
                        url = uri("https://gitlab.ekino.com/api/v4/projects/${gitlabProjectId}/packages/maven")
                        credentials(HttpHeaderCredentials::class) {
                            name = "Job-Token"
                            value = gitlabJobToken
                        }
                        authentication {
                            create("header", HttpHeaderAuthentication::class)
                        }
                    }
                }
            }
        }
    }
}