package com.ekino.oss.metalastic.dsl

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object VersionCheck {
  private val SUPPORTED_MAJOR_MINORS = setOf("6.0")

  init {
    checkVersion()
  }

  @Suppress("TooGenericExceptionCaught")
  private fun checkVersion() {
    try {
      val version = getSpringDataEsVersion()
      if (version != "unknown") {
        val majorMinor = version.substringBeforeLast(".")

        if (majorMinor !in SUPPORTED_MAJOR_MINORS) {
          logger.warn {
            """
                        |
                        |═══════════════════════════════════════════════════════════════════════════════
                        | ⚠️  Spring Data Elasticsearch Version Mismatch Detected
                        |═══════════════════════════════════════════════════════════════════════════════
                        | Detected version: $version
                        | Supported versions: 6.0.x
                        |
                        | You are using: metalastic-elasticsearch-dsl (rolling release)
                        | For Spring Data ES 5.0-5.3 please use: metalastic-elasticsearch-dsl-5.3
                        | For Spring Data ES 5.4-5.5 please use: metalastic-elasticsearch-dsl-5.5
                        |═══════════════════════════════════════════════════════════════════════════════
                        |
            """
              .trimMargin()
          }
        }
      }
    } catch (e: Exception) {
      logger.debug(e) { "Could not detect Spring Data Elasticsearch version" }
    }
  }

  @Suppress("TooGenericExceptionCaught", "SwallowedException")
  private fun getSpringDataEsVersion(): String {
    return try {
      Class.forName("org.springframework.data.elasticsearch.core.Version")
        .getDeclaredMethod("getVersion")
        .invoke(null) as String
    } catch (e: Exception) {
      "unknown"
    }
  }
}
