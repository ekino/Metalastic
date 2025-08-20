#!/usr/bin/env kotlin

@file:DependsOn("com.github.pcimcioch:gitlab-ci-kotlin-dsl:1.7.0")

import pcimcioch.gitlabci.dsl.DefaultEnvironment
import pcimcioch.gitlabci.dsl.Duration
import pcimcioch.gitlabci.dsl.GitlabCiDsl
import pcimcioch.gitlabci.dsl.gitlabCi
import pcimcioch.gitlabci.dsl.job.CachePolicy
import pcimcioch.gitlabci.dsl.job.JobDsl
import pcimcioch.gitlabci.dsl.job.WhenRunType
import pcimcioch.gitlabci.dsl.job.WhenUploadType
import java.util.Properties
import java.io.File

/*
  Global utils
*/
@JvmInline
value class RefName(val name: String)

@Suppress("EnumEntryName")
enum class CiPipelineSource {
    api,
    chat,
    external,
    external_pull_request_event,
    merge_request_event,
    parent_pipeline,
    pipeline,
    push,
    schedule,
    trigger,
    web,
    webide,
}

@Suppress("EnumEntryName")
enum class Stages {
    build,
    test,
    publish,
}

val ciCommitRefName by lazy {
    DefaultEnvironment.CI_COMMIT_REF_NAME.value().let(::RefName)
}

val ciCommitTag by lazy {
    DefaultEnvironment.CI_COMMIT_TAG.valueOrNull()?.let(::RefName)
}

val ciCommitSha by lazy {
    DefaultEnvironment.CI_COMMIT_SHA.value().let(::RefName)
}

val ciPipelineSource by lazy {
    DefaultEnvironment.CI_PIPELINE_SOURCE.value().let(CiPipelineSource::valueOf)
}

val ciDefaultBranch by lazy {
    DefaultEnvironment.CI_DEFAULT_BRANCH.value().let(::RefName)
}

fun onPush() = ciPipelineSource == CiPipelineSource.push
fun onMergeRequest() = ciPipelineSource == CiPipelineSource.merge_request_event
fun onMasterBranch() = ciCommitRefName == ciDefaultBranch
fun onFeatureBranch() = !onMasterBranch() && !onReleaseTag()

val semVerRegex by lazy {
    Regex("""^v?(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$""")
}

fun onReleaseTag() = ciCommitTag?.let { semVerRegex.matches(it.name) } == true

fun extractVersionFromGradleProperties(): String? =
    Properties().apply {
        load(File("gradle.properties").inputStream())
    }.getProperty("version")?.takeIf { it.isNotBlank() }

fun String.cleanChars() = replace(Regex("\\W+"), "_")

val gradlewCmd = "./gradlew"

fun anyOf(vararg conditions: () -> Boolean) = conditions.any { it() }

inline fun <reified T : Enum<T>> GitlabCiDsl.stagesOf() = 
    stages(enumValues<T>().asSequence().map { it.name }.toList())

fun <T : Enum<T>> JobDsl.stage(enum: T) {
    stage = enum.name
}

fun DefaultEnvironment.value(): String = System.getenv(name) ?: error("Environment variable $name is not set")
fun DefaultEnvironment.valueOrNull(): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

fun printPipeline(block: GitlabCiDsl.() -> Unit) {
    System.out.writer().use { writer ->
        gitlabCi(writer = writer, block = block)
    }
}

/*
  Pipeline configuration
*/
printPipeline {
    
    // Calculate actual artifact version for job names
    val projectVersion = "1.0-SNAPSHOT" // matches build.gradle.kts version
    val tagName = ciCommitTag?.name
    
    val artifactVersion = when {
        tagName != null -> tagName.removePrefix("v") // v1.2.3 -> 1.2.3  
        else -> projectVersion // 1.0-SNAPSHOT
    }
    
    // Workflow rules for child pipeline - essential for parent_pipeline source
    workflow {
        rules {
            rule {
                ifCondition = "\$CI_PIPELINE_SOURCE == \"parent_pipeline\""
            }
        }
    }
    
    // Global variables
    variables {
        "generatedFile" to ".gitlab-ci-generated.yml"
        "GRADLE_OPTS" to "-Dorg.gradle.daemon=false"
        "GRADLE_USER_HOME" to "\$CI_PROJECT_DIR/.gradle"
    }
    
    // Global defaults
    default {
        image("openjdk:21-jdk-slim")
        
        beforeScript {
            +"chmod +x ./gradlew"
        }
    }
    
    stagesOf<Stages>()
    
    // Build stage
    job("build") {
        stage(Stages.build)
        
        script {
            +"$gradlewCmd build -x test"
        }
        
        artifacts {
            paths(
                "*/build/libs/"
            )
            expireIn = Duration(hours = 1)
        }
        
        cache(
            ".gradle/wrapper",
            ".gradle/caches"
        )
    }
    
    // Test stage
    job("test") {
        stage(Stages.test)
        
        script {
            +"$gradlewCmd test"
        }
        
        artifacts {
            whenUpload = WhenUploadType.ALWAYS
            reports {
                junit(
                    "*/build/test-results/test/TEST-*.xml"
                )
            }
            paths(
                "*/build/test-results/",
                "*/build/reports/"
            )
            expireIn = Duration(days = 7)
        }
        
        cache(
            ".gradle/wrapper",
            ".gradle/caches"
        )
    }
    
    // Publish job - auto for master and tags only
    job("publish ($artifactVersion)") {
        stage(Stages.publish)
        
        script {
            +"$gradlewCmd publish"
        }
        
        rules {
            rule {
                ifCondition = "\$CI_COMMIT_BRANCH == \$CI_DEFAULT_BRANCH"
            }
            rule {
                ifCondition = "\$CI_COMMIT_TAG"
            }
        }
        
        artifacts {
            paths(
                "*/build/libs/"
            )
            expireIn = Duration(days = 7)
        }
        
        cache(
            ".gradle/wrapper",
            ".gradle/caches"
        )
    }
    
    // Manual publish for MRs and feature branches
    job("publish-manual ($artifactVersion)") {
        stage(Stages.publish)
        
        script {
            +"$gradlewCmd publish"
        }
        
        whenRun = WhenRunType.MANUAL
        allowFailure = true
        
        rules {
            rule {
                ifCondition = "\$CI_PIPELINE_SOURCE == \"merge_request_event\""
                whenRun = WhenRunType.MANUAL
            }
            rule {
                ifCondition = "\$CI_COMMIT_BRANCH != \$CI_DEFAULT_BRANCH && \$CI_COMMIT_TAG == null"
                whenRun = WhenRunType.MANUAL
            }
        }
        
        environment("feature-snapshot")
        
        cache(
            ".gradle/wrapper",
            ".gradle/caches"
        )
    }
}