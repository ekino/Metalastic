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

  /** Returns `true` if this container is mapped as a `nested` object in Elasticsearch. */
  fun isNested(): Boolean = nested

  /**
   * Resolves a dot-separated [path] relative to this container to the [Metamodel] field it
   * designates, or `null` if no field matches the path.
   */
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
  /** Declares a child [AutoField] named [fieldName], modeling the `auto` field type. */
  inline fun <reified T : Any?> auto(fieldName: String): AutoField<T> =
    AutoField(this, fieldName, typeOf<T>())

  // Text field helpers
  /** Declares a child [TextField] named [fieldName], modeling the `text` field type. */
  inline fun <reified T : Any?> text(fieldName: String): TextField<T> =
    TextField(this, fieldName, typeOf<T>())

  // Keyword field helpers
  /** Declares a child [KeywordField] named [fieldName], modeling the `keyword` field type. */
  inline fun <reified T : Any?> keyword(fieldName: String): KeywordField<T> =
    KeywordField(this, fieldName, typeOf<T>())

  // Numeric field helpers
  /** Declares a child [LongField] named [fieldName], modeling the `long` field type. */
  inline fun <reified T : Any?> long(fieldName: String): LongField<T> =
    LongField(this, fieldName, typeOf<T>())

  /** Declares a child [IntegerField] named [fieldName], modeling the `integer` field type. */
  inline fun <reified T : Any?> integer(fieldName: String): IntegerField<T> =
    IntegerField(this, fieldName, typeOf<T>())

  /** Declares a child [ShortField] named [fieldName], modeling the `short` field type. */
  inline fun <reified T : Any?> short(fieldName: String): ShortField<T> =
    ShortField(this, fieldName, typeOf<T>())

  /** Declares a child [ByteField] named [fieldName], modeling the `byte` field type. */
  inline fun <reified T : Any?> byte(fieldName: String): ByteField<T> =
    ByteField(this, fieldName, typeOf<T>())

  /** Declares a child [DoubleField] named [fieldName], modeling the `double` field type. */
  inline fun <reified T : Any?> double(fieldName: String): DoubleField<T> =
    DoubleField(this, fieldName, typeOf<T>())

  /** Declares a child [FloatField] named [fieldName], modeling the `float` field type. */
  inline fun <reified T : Any?> float(fieldName: String): FloatField<T> =
    FloatField(this, fieldName, typeOf<T>())

  /** Declares a child [HalfFloatField] named [fieldName], modeling the `half_float` field type. */
  inline fun <reified T : Any?> halfFloat(fieldName: String): HalfFloatField<T> =
    HalfFloatField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [ScaledFloatField] named [fieldName], modeling the `scaled_float` field type.
   */
  inline fun <reified T : Any?> scaledFloat(fieldName: String): ScaledFloatField<T> =
    ScaledFloatField(this, fieldName, typeOf<T>())

  // Date field helpers
  /**
   * Declares a child [DateField] named [fieldName], modeling the `date` field type.
   *
   * @param formats the accepted date [formats][DateFormat] for the underlying mapping, empty when
   *   no explicit format was declared
   */
  inline fun <reified T : Any?> date(
    fieldName: String,
    formats: List<DateFormat> = emptyList(),
  ): DateField<T> = DateField(this, fieldName, typeOf<T>(), formats)

  /** Declares a child [DateNanosField] named [fieldName], modeling the `date_nanos` field type. */
  inline fun <reified T : Any?> dateNanos(fieldName: String): DateNanosField<T> =
    DateNanosField(this, fieldName, typeOf<T>())

  // Boolean field helper
  /** Declares a child [BooleanField] named [fieldName], modeling the `boolean` field type. */
  inline fun <reified T : Any?> boolean(fieldName: String): BooleanField<T> =
    BooleanField(this, fieldName, typeOf<T>())

  // Binary field helper
  /** Declares a child [BinaryField] named [fieldName], modeling the `binary` field type. */
  inline fun <reified T : Any?> binary(fieldName: String): BinaryField<T> =
    BinaryField(this, fieldName, typeOf<T>())

  // IP field helper
  /** Declares a child [IpField] named [fieldName], modeling the `ip` field type. */
  inline fun <reified T : Any?> ip(fieldName: String): IpField<T> =
    IpField(this, fieldName, typeOf<T>())

  // Specialized field helpers
  /** Declares a child [CompletionField] named [fieldName], modeling the `completion` field type. */
  inline fun <reified T : Any?> completion(fieldName: String): CompletionField<T> =
    CompletionField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [TokenCountField] named [fieldName], modeling the `token_count` field type.
   */
  inline fun <reified T : Any?> tokenCount(fieldName: String): TokenCountField<T> =
    TokenCountField(this, fieldName, typeOf<T>())

  /** Declares a child [PercolatorField] named [fieldName], modeling the `percolator` field type. */
  inline fun <reified T : Any?> percolator(fieldName: String): PercolatorField<T> =
    PercolatorField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [RankFeatureField] named [fieldName], modeling the `rank_feature` field type.
   */
  inline fun <reified T : Any?> rankFeature(fieldName: String): RankFeatureField<T> =
    RankFeatureField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [RankFeaturesField] named [fieldName], modeling the `rank_features` field
   * type.
   */
  inline fun <reified T : Any?> rankFeatures(fieldName: String): RankFeaturesField<T> =
    RankFeaturesField(this, fieldName, typeOf<T>())

  /** Declares a child [FlattenedField] named [fieldName], modeling the `flattened` field type. */
  inline fun <reified T : Any?> flattened(fieldName: String): FlattenedField<T> =
    FlattenedField(this, fieldName, typeOf<T>())

  /** Declares a child [ShapeField] named [fieldName], modeling the `shape` field type. */
  inline fun <reified T : Any?> shape(fieldName: String): ShapeField<T> =
    ShapeField(this, fieldName, typeOf<T>())

  /** Declares a child [PointField] named [fieldName], modeling the `point` field type. */
  inline fun <reified T : Any?> point(fieldName: String): PointField<T> =
    PointField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [ConstantKeywordField] named [fieldName], modeling the `constant_keyword`
   * field type.
   */
  inline fun <reified T : Any?> constantKeyword(fieldName: String): ConstantKeywordField<T> =
    ConstantKeywordField(this, fieldName, typeOf<T>())

  /** Declares a child [WildcardField] named [fieldName], modeling the `wildcard` field type. */
  inline fun <reified T : Any?> wildcard(fieldName: String): WildcardField<T> =
    WildcardField(this, fieldName, typeOf<T>())

  // Range field helpers
  /**
   * Declares a child [IntegerRangeField] named [fieldName], modeling the `integer_range` field
   * type.
   */
  inline fun <reified T : Any?> integerRange(fieldName: String): IntegerRangeField<T> =
    IntegerRangeField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [FloatRangeField] named [fieldName], modeling the `float_range` field type.
   */
  inline fun <reified T : Any?> floatRange(fieldName: String): FloatRangeField<T> =
    FloatRangeField(this, fieldName, typeOf<T>())

  /** Declares a child [LongRangeField] named [fieldName], modeling the `long_range` field type. */
  inline fun <reified T : Any?> longRange(fieldName: String): LongRangeField<T> =
    LongRangeField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [DoubleRangeField] named [fieldName], modeling the `double_range` field type.
   */
  inline fun <reified T : Any?> doubleRange(fieldName: String): DoubleRangeField<T> =
    DoubleRangeField(this, fieldName, typeOf<T>())

  /** Declares a child [DateRangeField] named [fieldName], modeling the `date_range` field type. */
  inline fun <reified T : Any?> dateRange(fieldName: String): DateRangeField<T> =
    DateRangeField(this, fieldName, typeOf<T>())

  /**
   * Declares a child field named [fieldName] for the `ip_range` field type. Note: this helper
   * currently returns a [MatchOnlyTextField] rather than an [IpRangeField], matching the
   * processor's `FieldType.Ip_Range` mapping.
   */
  inline fun <reified T : Any?> ipRange(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName, typeOf<T>())

  // Advanced field helpers
  /**
   * Declares a child [SearchAsYouTypeField] named [fieldName], modeling the `search_as_you_type`
   * field type.
   */
  inline fun <reified T : Any?> searchAsYouType(fieldName: String): SearchAsYouTypeField<T> =
    SearchAsYouTypeField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [DenseVectorField] named [fieldName], modeling the `dense_vector` field type.
   */
  inline fun <reified T : Any?> denseVector(fieldName: String): DenseVectorField<T> =
    DenseVectorField(this, fieldName, typeOf<T>())

  /** Declares a child [AliasField] named [fieldName], modeling the `alias` field type. */
  inline fun <reified T : Any?> alias(fieldName: String): AliasField<T> =
    AliasField(this, fieldName, typeOf<T>())

  /** Declares a child [VersionField] named [fieldName], modeling the `version` field type. */
  inline fun <reified T : Any?> version(fieldName: String): VersionField<T> =
    VersionField(this, fieldName, typeOf<T>())

  /** Declares a child [Murmur3Field] named [fieldName], modeling the `murmur3` field type. */
  inline fun <reified T : Any?> murmur3(fieldName: String): Murmur3Field<T> =
    Murmur3Field(this, fieldName, typeOf<T>())

  /**
   * Declares a child [MatchOnlyTextField] named [fieldName], modeling the `match_only_text` field
   * type.
   */
  inline fun <reified T : Any?> matchOnlyText(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName, typeOf<T>())

  /**
   * Declares a child [AnnotatedTextField] named [fieldName], modeling the `annotated_text` field
   * type.
   */
  inline fun <reified T : Any?> annotatedText(fieldName: String): AnnotatedTextField<T> =
    AnnotatedTextField(this, fieldName, typeOf<T>())
}
