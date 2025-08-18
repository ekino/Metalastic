plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    group = "com.qelasticsearch"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "maven-publish")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
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
            ktlint("1.6.0")
                .editorConfigOverride(
                    mapOf(
                        "max_line_length" to "160",
                        "ktlint_standard_max-line-length" to "enabled"
                    )
                )
            trimTrailingWhitespace()
            endWithNewline()
        }

        java {
            removeUnusedImports()
            removeWildcardImports()
        }

        kotlinGradle {
            ktlint("1.6.0")
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

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                
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
                url = uri("https://gitlab.ekino.com/api/v4/projects/\${System.getenv("CI_PROJECT_ID")}/packages/maven")
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