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
                .editorConfigOverride(mapOf(
                    "max_line_length" to "160",
                    "ktlint_standard_max-line-length" to "enabled"
                ))
            trimTrailingWhitespace()
            endWithNewline()
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
            }
        }
    }
}