package com.ekino.oss.metalastic.elasticsearch.dsl.utils

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatcherBuilder
import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import org.hamcrest.Matcher as HamcrestMatcher
import org.hamcrest.StringDescription
import org.skyscreamer.jsonassert.JSONCompareMode

/**
 * JCV-based JSON testing utilities for Elasticsearch DSL tests.
 *
 * These utilities provide advanced JSON matching capabilities including:
 * - Strict matching (exact structure and values)
 * - Lenient matching (ignoring field order and extra fields)
 * - Template-based matching with placeholders like {json-unit.ignore}
 */

/** Converts a Hamcrest matcher to a Kotest matcher for JSON comparison. */
private fun HamcrestMatcher<String>.toKotestMatcher(expectedJson: String? = null): Matcher<String> =
  object : Matcher<String> {
    override fun test(value: String): MatcherResult {
      val matches = this@toKotestMatcher.matches(value)

      // Get detailed description from Hamcrest matcher
      val description = StringDescription()
      this@toKotestMatcher.describeTo(description)
      val expectedDescription = description.toString()

      // Use provided expectedJson or try to extract from matcher description
      val actualExpectedJson = expectedJson ?: extractExpectedJson(expectedDescription)

      // Get mismatch description if it doesn't match
      val mismatchDescription =
        if (!matches) {
          val mismatchDesc = StringDescription()
          this@toKotestMatcher.describeMismatch(value, mismatchDesc)
          mismatchDesc.toString()
        } else {
          ""
        }

      return MatcherResult(
        matches,
        {
          if (matches) {
            "JSON matched expected structure"
          } else {
            buildString {
              appendLine("JSON did not match expected structure")
              appendLine()
              if (actualExpectedJson != null) {
                appendLine("Expected JSON:")
                appendLine(formatJsonForDisplay(actualExpectedJson))
                appendLine()
              }
              appendLine("Expected description:")
              appendLine(expectedDescription)
              appendLine()
              appendLine("Actual JSON:")
              appendLine(formatJsonForDisplay(value))
              appendLine()
              appendLine("Mismatch details:")
              appendLine(mismatchDescription)
            }
          }
        },
        {
          buildString {
            appendLine("JSON should not have matched expected structure, but it did")
            appendLine()
            if (actualExpectedJson != null) {
              appendLine("Expected JSON NOT to match:")
              appendLine(formatJsonForDisplay(actualExpectedJson))
              appendLine()
            }
            appendLine("Expected description:")
            appendLine(expectedDescription)
            appendLine()
            appendLine("Actual JSON:")
            appendLine(formatJsonForDisplay(value))
          }
        },
      )
    }
  }

/** Formats JSON for display in error messages with basic indentation. */
private fun formatJsonForDisplay(json: String): String {
  return runCatching {
      // Simple formatting - add line breaks for better readability
      json
        .replace(",", ",\n  ")
        .replace("{", "{\n  ")
        .replace("}", "\n}")
        .replace("[", "[\n    ")
        .replace("]", "\n  ]")
    }
    .getOrElse { json }
}

/**
 * Attempts to extract the expected JSON from the Hamcrest matcher description. This works with JCV
 * matchers that include the expected JSON in their description.
 */
private fun extractExpectedJson(description: String): String? {
  val patterns = createJsonExtractionPatterns()

  return patterns.firstNotNullOfOrNull { pattern ->
    pattern.find(description)?.let { match ->
      val json = match.groupValues[1].trim()
      json.takeIf { isValidJsonStructure(it) }?.let { formatJsonSafely(it) }
    }
  }
}

private fun createJsonExtractionPatterns(): List<Regex> =
  listOf(
    // Pattern for JCV matchers: "expected JSON: {...}"
    Regex("""expected JSON:\s*(.+)""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
    // Pattern for JSONAssert-style matchers that might include JSON
    Regex("""Expected:\s*(\{.+\})""", setOf(RegexOption.DOT_MATCHES_ALL)),
    // Pattern for matchers that have JSON in quotes
    Regex(""""(.+)"""", setOf(RegexOption.DOT_MATCHES_ALL)),
  )

private fun isValidJsonStructure(json: String): Boolean =
  json.startsWith("{") || json.startsWith("[")

private fun formatJsonSafely(json: String): String {
  return runCatching {
      json
        .replace(",", ",\n  ")
        .replace("{", "{\n  ")
        .replace("}", "\n}")
        .replace("[", "[\n    ")
        .replace("]", "\n  ]")
    }
    .getOrElse { json }
}

/**
 * Creates a standard JSON matcher that handles common differences like field ordering. This is the
 * most commonly used matcher for general JSON validation.
 */
fun jsonMatcher(expectedJson: String): Matcher<String> =
  JsonMatchers.jsonMatcher(expectedJson).toKotestMatcher(expectedJson)

/**
 * Creates a strict JSON matcher that requires exact match including field order. Use this when you
 * need to verify the exact structure and order.
 */
fun jsonStrictMatcher(expectedJson: String): Matcher<String> =
  JsonMatcherBuilder.create()
    .mode(JSONCompareMode.STRICT)
    .build(expectedJson)
    .toKotestMatcher(expectedJson)

/**
 * Creates a lenient JSON matcher that ignores field order and allows extra fields. Use this when
 * you only care about specific fields being present with correct values.
 */
fun jsonLenientMatcher(expectedJson: String): Matcher<String> =
  JsonMatcherBuilder.create()
    .mode(JSONCompareMode.LENIENT)
    .build(expectedJson)
    .toKotestMatcher(expectedJson)

/**
 * Creates a non-extensible JSON matcher that allows field reordering but not extra fields. Use this
 * when you want flexible field ordering but strict field presence.
 */
fun jsonNonExtensibleMatcher(expectedJson: String): Matcher<String> =
  JsonMatcherBuilder.create()
    .mode(JSONCompareMode.NON_EXTENSIBLE)
    .build(expectedJson)
    .toKotestMatcher(expectedJson)

/**
 * Extension function to create template-based expected JSON for Elasticsearch queries. Supports JCV
 * placeholders for dynamic values. This produces just the query structure to match
 * Query.toJsonString().
 */
fun elasticsearchQueryTemplate(
  mustQueries: List<String> = emptyList(),
  mustNotQueries: List<String> = emptyList(),
  shouldQueries: List<String> = emptyList(),
  filterQueries: List<String> = emptyList(),
): String {
  val parts = mutableListOf<String>()

  if (mustQueries.isNotEmpty()) {
    parts.add(""""must":[${mustQueries.joinToString(",")}]""")
  }

  if (mustNotQueries.isNotEmpty()) {
    parts.add(""""must_not":[${mustNotQueries.joinToString(",")}]""")
  }

  if (shouldQueries.isNotEmpty()) {
    parts.add(""""should":[${shouldQueries.joinToString(",")}]""")
  }

  if (filterQueries.isNotEmpty()) {
    parts.add(""""filter":[${filterQueries.joinToString(",")}]""")
  }

  val boolContent = if (parts.isEmpty()) "" else parts.joinToString(",")

  return """
    {
      "bool": {
        $boolContent
      }
    }
  """
    .trimIndent()
}

/**
 * Extension function to create template-based expected JSON for NativeQuery search requests. This
 * produces the full search request structure with aggregations, query, and sort.
 */
fun elasticsearchSearchRequestTemplate(
  mustQueries: List<String> = emptyList(),
  mustNotQueries: List<String> = emptyList(),
  shouldQueries: List<String> = emptyList(),
  filterQueries: List<String> = emptyList(),
): String {
  val parts = mutableListOf<String>()

  if (mustQueries.isNotEmpty()) {
    parts.add(""""must":[${mustQueries.joinToString(",")}]""")
  }

  if (mustNotQueries.isNotEmpty()) {
    parts.add(""""must_not":[${mustNotQueries.joinToString(",")}]""")
  }

  if (shouldQueries.isNotEmpty()) {
    parts.add(""""should":[${shouldQueries.joinToString(",")}]""")
  }

  if (filterQueries.isNotEmpty()) {
    parts.add(""""filter":[${filterQueries.joinToString(",")}]""")
  }

  val boolContent = if (parts.isEmpty()) "" else parts.joinToString(",")

  return """
    {
      "aggregations": {},
      "query": {
        "bool": {
          $boolContent
        }
      },
      "sort": []
    }
  """
    .trimIndent()
}
