/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.elasticsearch.dsl

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.json.JsonData
import com.ekino.oss.metalastic.core.DateField
import com.ekino.oss.metalastic.core.Metamodel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.Date
import org.springframework.data.elasticsearch.annotations.DateFormat

/**
 * Converts various Kotlin types to Elasticsearch FieldValue instances. Null and blank strings are
 * filtered out to avoid empty queries.
 */
internal fun <T> T?.toFieldValue(): FieldValue? =
  when (this) {
    null -> null
    is String -> takeIf { it.isNotBlank() }?.let(FieldValue::of)
    is Long -> FieldValue.of(this)
    is Int -> FieldValue.of(this.toLong())
    is Float -> FieldValue.of(this.toDouble())
    is Double -> FieldValue.of(this)
    is Boolean -> FieldValue.of(this)
    is Enum<*> -> FieldValue.of(this.toString())
    is Date -> FieldValue.of(formatTemporal(emptyList(), toInstant()) { toString() })
    is Temporal -> FieldValue.of(formatTemporal(emptyList(), this) { toString() })
    is FieldValue -> this
    else -> FieldValue.of(this.toString())
  }

internal fun <T : Temporal> T.toEpochMilli() =
  when (this) {
    is Instant -> toEpochMilli()
    is LocalDate -> atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    is LocalDateTime -> toInstant(ZoneOffset.UTC).toEpochMilli()
    is ZonedDateTime -> toInstant().toEpochMilli()
    is OffsetDateTime -> toInstant().toEpochMilli()
    else -> error("Unsupported temporal type: ${this::class.simpleName}")
  }

private const val MILLIS_TO_SECONDS = 1000L

/**
 * Internal implementation that formats a temporal value using the configured date formats.
 *
 * This function implements Elasticsearch's date format fallback chain behavior:
 * - Tries each configured format in order until one successfully formats the value
 * - If no formats are configured, uses Elasticsearch's default: `date_optional_time||epoch_millis`
 * - Handles epoch formats (epoch_millis, epoch_second) as special cases
 * - Falls back to `fallback()` if all format attempts fail
 */
private fun formatTemporal(
  formats: List<DateFormat>,
  value: Temporal,
  fallback: () -> String,
): String {
  // When no formats are configured, use Elasticsearch's default format chain:
  // strict_date_optional_time||epoch_millis (mapped to date_optional_time in Spring Data)
  val formatsToTry =
    formats.ifEmpty { listOf(DateFormat.date_optional_time, DateFormat.epoch_millis) }
  val epochMillis = value.toEpochMilli()
  return formatsToTry
    .asSequence()
    .mapNotNull { format ->
      when (format) {
        // Epoch formats require special handling as they don't use DateTimeFormatter patterns
        DateFormat.epoch_millis -> epochMillis.toString()
        DateFormat.epoch_second -> (epochMillis / MILLIS_TO_SECONDS).toString()
        // All other formats use standard DateTimeFormatter patterns
        else ->
          runCatching { DateTimeFormatter.ofPattern(format.pattern).format(value) }.getOrNull()
      }
    }
    .firstOrNull() ?: fallback()
}

/**
 * Elasticsearch dates are internally converted to UTC (if the time-zone is specified) and stored as
 * a long number representing milliseconds-since-the-epoch.
 *
 * We could also use date formatters, but it might cause issues if it does not match the one (if
 * any) defined in the [org.springframework.data.elasticsearch.annotations.Field] field for the
 * [org.springframework.data.elasticsearch.annotations.Document]
 *
 * [reference](https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/date)
 */
internal fun <T> toJsonData(value: Comparable<T>): Pair<JsonData, DateFormat?>? =
  when (value) {
    is Temporal -> JsonData.of(value.toEpochMilli()) to DateFormat.epoch_millis
    is Date -> JsonData.of(value.time) to DateFormat.epoch_millis
    else -> JsonData.of(value) to null
  }

/**
 * Converts a temporal value to a string representation using the configured date formats.
 *
 * This function implements Elasticsearch's date format fallback chain behavior:
 * - Tries each configured format in order until one successfully formats the value
 * - If no formats are configured, uses Elasticsearch's default: `date_optional_time||epoch_millis`
 * - Handles epoch formats (epoch_millis, epoch_second) as special cases
 * - Falls back to `value.toString()` if all format attempts fail
 * - See [Elasticsearch Date Format](https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/date")
 *
 * [value] The temporal value to format, or null
 *
 * @return The formatted date string, or null if the input value is null
 */
internal fun <T : Temporal> DateField<*>.formatted(value: T?): String? {
  if (value == null) return null
  return formatTemporal(formats, value) { value.toString() }
}

/**
 * Converts a java.util.Date value to a string representation using the configured date formats.
 *
 * This function delegates to the internal implementation by converting the Date to an Instant.
 *
 * [value] The Date value to format, or null
 *
 * @return The formatted date string, or null if the input value is null
 */
internal fun DateField<*>.formatted(value: Date?): String? {
  if (value == null) return null
  return formatTemporal(formats, value.toInstant()) { value.toString() }
}

/**
 * Converts a value to an Elasticsearch FieldValue for regular fields. Uses standard type conversion
 * logic.
 *
 * @param value The value to convert, or null
 * @return The converted FieldValue, or null if the input value is null
 */
fun <T> Metamodel<*>.toFieldValue(value: T?): FieldValue? =
  when (this) {
    is DateField<*> -> toFieldValue(value)
    else -> value?.toFieldValue()
  }

/**
 * Converts a value to an Elasticsearch FieldValue for DateFields. Automatically applies
 * format-aware conversion for Temporal and Date types, respecting the field's configured date
 * formats.
 *
 * @param value The value to convert, or null
 * @return The converted FieldValue, or null if the input value is null
 */
private fun <T> DateField<*>.toFieldValue(value: T?): FieldValue? {
  return when (value) {
    null -> null
    is Temporal -> formatted(value)?.let(FieldValue::of)
    is Date -> formatted(value)?.let(FieldValue::of)
    else -> value.toFieldValue() // Fallback for non-date types
  }
}
