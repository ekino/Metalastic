package com.metalastic.core

/**
 * Base class for object fields that can contain nested fields. Used for both nested objects and the
 * root index.
 */
abstract class ObjectField<T>(
  parent: ObjectField<*>? = null,
  name: String,
  private val nested: Boolean = false,
) : Field<T>(parent, name) {

  fun nested(): kotlin.Boolean = nested

  // Auto field helpers
  inline fun <reified T> auto(fieldName: String): AutoField<T> = AutoField(this, fieldName)

  // Text field helpers
  inline fun <reified T> text(fieldName: String): TextField<T> = TextField(this, fieldName)

  // Keyword field helpers
  inline fun <reified T> keyword(fieldName: String): KeywordField<T> = KeywordField(this, fieldName)

  // Numeric field helpers
  inline fun <reified T> long(fieldName: String): LongField<T> = LongField(this, fieldName)

  inline fun <reified T> integer(fieldName: String): IntegerField<T> = IntegerField(this, fieldName)

  inline fun <reified T> short(fieldName: String): ShortField<T> = ShortField(this, fieldName)

  inline fun <reified T> byte(fieldName: String): ByteField<T> = ByteField(this, fieldName)

  inline fun <reified T> double(fieldName: String): DoubleField<T> = DoubleField(this, fieldName)

  inline fun <reified T> float(fieldName: String): FloatField<T> = FloatField(this, fieldName)

  inline fun <reified T> halfFloat(fieldName: String): HalfFloatField<T> =
    HalfFloatField(this, fieldName)

  inline fun <reified T> scaledFloat(fieldName: String): ScaledFloatField<T> =
    ScaledFloatField(this, fieldName)

  // Date field helpers
  inline fun <reified T> date(fieldName: String): DateField<T> = DateField(this, fieldName)

  inline fun <reified T> dateNanos(fieldName: String): DateNanosField<T> =
    DateNanosField(this, fieldName)

  // Boolean field helper
  inline fun <reified T> boolean(fieldName: String): BooleanField<T> = BooleanField(this, fieldName)

  // Binary field helper
  inline fun <reified T> binary(fieldName: String): BinaryField<T> = BinaryField(this, fieldName)

  // IP field helper
  inline fun <reified T> ip(fieldName: String): IpField<T> = IpField(this, fieldName)

  // Specialized field helpers
  inline fun <reified T> completion(fieldName: String): CompletionField<T> =
    CompletionField(this, fieldName)

  inline fun <reified T> tokenCount(fieldName: String): TokenCountField<T> =
    TokenCountField(this, fieldName)

  inline fun <reified T> percolator(fieldName: String): PercolatorField<T> =
    PercolatorField(this, fieldName)

  inline fun <reified T> rankFeature(fieldName: String): RankFeatureField<T> =
    RankFeatureField(this, fieldName)

  inline fun <reified T> rankFeatures(fieldName: String): RankFeaturesField<T> =
    RankFeaturesField(this, fieldName)

  inline fun <reified T> flattened(fieldName: String): FlattenedField<T> =
    FlattenedField(this, fieldName)

  inline fun <reified T> shape(fieldName: String): ShapeField<T> = ShapeField(this, fieldName)

  inline fun <reified T> point(fieldName: String): PointField<T> = PointField(this, fieldName)

  inline fun <reified T> constantKeyword(fieldName: String): ConstantKeywordField<T> =
    ConstantKeywordField(this, fieldName)

  inline fun <reified T> wildcard(fieldName: String): WildcardField<T> =
    WildcardField(this, fieldName)

  // Range field helpers
  inline fun <reified T> integerRange(fieldName: String): IntegerRangeField<T> =
    IntegerRangeField(this, fieldName)

  inline fun <reified T> floatRange(fieldName: String): FloatRangeField<T> =
    FloatRangeField(this, fieldName)

  inline fun <reified T> longRange(fieldName: String): LongRangeField<T> =
    LongRangeField(this, fieldName)

  inline fun <reified T> doubleRange(fieldName: String): DoubleRangeField<T> =
    DoubleRangeField(this, fieldName)

  inline fun <reified T> dateRange(fieldName: String): DateRangeField<T> =
    DateRangeField(this, fieldName)

  inline fun <reified T> ipRange(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName)

  // Advanced field helpers
  inline fun <reified T> searchAsYouType(fieldName: String): SearchAsYouTypeField<T> =
    SearchAsYouTypeField(this, fieldName)

  inline fun <reified T> denseVector(fieldName: String): DenseVectorField<T> =
    DenseVectorField(this, fieldName)

  inline fun <reified T> alias(fieldName: String): AliasField<T> = AliasField(this, fieldName)

  inline fun <reified T> version(fieldName: String): VersionField<T> = VersionField(this, fieldName)

  inline fun <reified T> murmur3(fieldName: String): Murmur3Field<T> = Murmur3Field(this, fieldName)

  inline fun <reified T> matchOnlyText(fieldName: String): MatchOnlyTextField<T> =
    MatchOnlyTextField(this, fieldName)

  inline fun <reified T> annotatedText(fieldName: String): AnnotatedTextField<T> =
    AnnotatedTextField(this, fieldName)
}
