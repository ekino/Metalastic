package com.ekino.oss.metalastic.dsl

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object VersionCheck {
  private val SUPPORTED_MAJOR_MINORS = setOf("5.4", "5.5")

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
                        | Supported versions: 5.4.x - 5.5.x
                        |
                        | You are using: metalastic-elasticsearch-dsl-5.5 (frozen)
                        | For latest Spring Data ES 6.x please use: metalastic-elasticsearch-dsl
                        | For Spring Data ES 5.0-5.3 please use: metalastic-elasticsearch-dsl-5.3
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
