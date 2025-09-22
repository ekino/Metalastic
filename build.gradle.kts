plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    group = "com.qelasticsearch"
    version = providers.exec {
        commandLine("git", "describe", "--tags", "--always", "--dirty")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim().let { gitVersion ->
        if (gitVersion.isEmpty() || gitVersion.startsWith("fatal:")) {
            "0.0.1-SNAPSHOT" // Fallback for repos without tags
        } else {
            val cleanVersion = gitVersion.removePrefix("v") // Remove 'v' prefix from tags like v1.0.0
            // Add -SNAPSHOT suffix if this is not an exact tag match (contains commits ahead or dirty)
            if (cleanVersion.contains("-") || gitVersion.endsWith("-dirty")) {
                "$cleanVersion-SNAPSHOT"
            } else {
                cleanVersion
            }
        }
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
    val shouldPublish = project.name != "test"

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
                        name.set("QElasticsearch ${project.name}")
                        description.set("A QueryDSL-like library for Elasticsearch in Kotlin")
                        url.set("https://gitlab.ekino.com/iperia/qelasticsearch")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }

                        scm {
                            connection.set("scm:git:git://gitlab.ekino.com/iperia/qelasticsearch.git")
                            developerConnection.set("scm:git:ssh://gitlab.ekino.com/iperia/qelasticsearch.git")
                            url.set("https://gitlab.ekino.com/iperia/qelasticsearch")
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