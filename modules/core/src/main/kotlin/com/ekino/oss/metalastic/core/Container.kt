/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.core

import kotlin.reflect.KType
import kotlin.reflect.typeOf
import org.springframework.data.elasticsearch.annotations.DateFormat

/**
 * Base class for object fields that can contain nested fields. Used for both nested objects and the
 * root index.
 */
abstract class Container<T : Any?>(
  name: String,
  private val nested: Boolean = false,
  fieldType: KType,
) : Metamodel<T>(name, fieldType) {

  private val fields: MutableList<Metamodel<*>> = mutableListOf()

  internal fun register(field: Metamodel<*>) {
    fields.add(field)
  }

  fun isNested(): Boolean = nested

  fun fieldBy(path: String): Metamodel<*>? {
    if (path.isBlank()) {
      return null
    }
    val fieldName = path.substringBefore(".")
    val remainingPath = path.substringAfter(".").takeIf { it != path }
    val field = fields.firstOrNull { it.name() == fieldName }

    return when {
      field == null -> null
      remainingPath == null -> field
      field is Container<*> -> field.fieldBy(remainingPath)
      else -> field.takeIf { remainingPath.isEmpty() }
    }
  }

  // Auto field helpers
  inline fun <reified T : Any?> auto(fieldName: String): AutoField<T> =
    AutoField(this, fieldName, typeOf<T>())

  // Text field helpers
  inline fun <reified T : Any?> text(fieldName: String): TextField<T> =
    TextField(this, fieldName, typeOf<T>())

  // Keyword field helpers
  inline fun <reified T : Any?> keyword(fieldName: String): KeywordField<T> =
    KeywordField(this, fieldName, typeOf<T>())

  // Numeric field helpers
  inline fun <reified T : Any?> long(fieldName: String): LongField<T> =
    LongField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> integer(fieldName: String): IntegerField<T> =
    IntegerField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> short(fieldName: String): ShortField<T> =
    ShortField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> byte(fieldName: String): ByteField<T> =
    ByteField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> double(fieldName: String): DoubleField<T> =
    DoubleField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> float(fieldName: String): FloatField<T> =
    FloatField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> halfFloat(fieldName: String): HalfFloatField<T> =
    HalfFloatField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> scaledFloat(fieldName: String): ScaledFloatField<T> =
    ScaledFloatField(this, fieldName, typeOf<T>())

  // Date field helpers
  inline fun <reified T : Any?> date(
    fieldName: String,
    formats: List<DateFormat> = emptyList(),
  ): DateField<T> = DateField(this, fieldName, typeOf<T>(), formats)

  inline fun <reified T : Any?> dateNanos(fieldName: String): DateNanosField<T> =
    DateNanosField(this, fieldName, typeOf<T>())

  // Boolean field helper
  inline fun <reified T : Any?> boolean(fieldName: String): BooleanField<T> =
    BooleanField(this, fieldName, typeOf<T>())

  // Binary field helper
  inline fun <reified T : Any?> binary(fieldName: String): BinaryField<T> =
    BinaryField(this, fieldName, typeOf<T>())

  // IP field helper
  inline fun <reified T : Any?> ip(fieldName: String): IpField<T> =
    IpField(this, fieldName, typeOf<T>())

  // Specialized field helpers
  inline fun <reified T : Any?> completion(fieldName: String): CompletionField<T> =
    CompletionField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> tokenCount(fieldName: String): TokenCountField<T> =
    TokenCountField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> percolator(fieldName: String): PercolatorField<T> =
    PercolatorField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> rankFeature(fieldName: String): RankFeatureField<T> =
    RankFeatureField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> rankFeatures(fieldName: String): RankFeaturesField<T> =
    RankFeaturesField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> flattened(fieldName: String): FlattenedField<T> =
    FlattenedField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> shape(fieldName: String): ShapeField<T> =
    ShapeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> point(fieldName: String): PointField<T> =
    PointField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> constantKeyword(fieldName: String): ConstantKeywordField<T> =
    ConstantKeywordField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> wildcard(fieldName: String): WildcardField<T> =
    WildcardField(this, fieldName, typeOf<T>())

  // Range field helpers
  inline fun <reified T : Any?> integerRange(fieldName: String): IntegerRangeField<T> =
    IntegerRangeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> floatRange(fieldName: String): FloatRangeField<T> =
    FloatRangeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> longRange(fieldName: String): LongRangeField<T> =
    LongRangeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> doubleRange(fieldName: String): DoubleRangeField<T> =
    DoubleRangeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> dateRange(fieldName: String): DateRangeField<T> =
    DateRangeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> ipRange(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName, typeOf<T>())

  // Advanced field helpers
  inline fun <reified T : Any?> searchAsYouType(fieldName: String): SearchAsYouTypeField<T> =
    SearchAsYouTypeField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> denseVector(fieldName: String): DenseVectorField<T> =
    DenseVectorField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> alias(fieldName: String): AliasField<T> =
    AliasField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> version(fieldName: String): VersionField<T> =
    VersionField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> murmur3(fieldName: String): Murmur3Field<T> =
    Murmur3Field(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> matchOnlyText(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName, typeOf<T>())

  inline fun <reified T : Any?> annotatedText(fieldName: String): AnnotatedTextField<T> =
    AnnotatedTextField(this, fieldName, typeOf<T>())
}
