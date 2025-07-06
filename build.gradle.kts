plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "maven-publish")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.file("detekt.yml"))
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.6.0")
        debug.set(true)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        filter {
            exclude("**/build/**")
        }
        additionalEditorconfig.set(
            mapOf(
                "max_line_length" to "160",
                "ktlint_standard_max-line-length" to "enabled"
            )
        )
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}