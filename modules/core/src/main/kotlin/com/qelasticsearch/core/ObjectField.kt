package com.qelasticsearch.core

/**
 * Base class for object fields that can contain nested fields. Used for both nested objects and the
 * root index.
 */
abstract class ObjectField(
  parent: ObjectField? = null,
  fieldName: String = "",
  private val nested: Boolean = false,
) : Field(parent, fieldName) {
  @YellowColor fun nested(): Boolean = nested

  // Field creation helper methods for cleaner generated code

  // Text field helpers
  inline fun <reified T> textField(fieldName: String): TextField<T> = TextField(this, fieldName)

  // Keyword field helpers
  inline fun <reified T> keywordField(fieldName: String): KeywordField<T> =
    KeywordField(this, fieldName)

  // Numeric field helpers
  inline fun <reified T> longField(fieldName: String): LongField<T> = LongField(this, fieldName)

  inline fun <reified T> integerField(fieldName: String): IntegerField<T> =
    IntegerField(this, fieldName)

  inline fun <reified T> shortField(fieldName: String): ShortField<T> = ShortField(this, fieldName)

  inline fun <reified T> byteField(fieldName: String): ByteField<T> = ByteField(this, fieldName)

  inline fun <reified T> doubleField(fieldName: String): DoubleField<T> =
    DoubleField(this, fieldName)

  inline fun <reified T> floatField(fieldName: String): FloatField<T> = FloatField(this, fieldName)

  fun halfFloatField(fieldName: String): HalfFloatField = HalfFloatField(this, fieldName)

  fun scaledFloatField(fieldName: String): ScaledFloatField = ScaledFloatField(this, fieldName)

  // Date field helpers
  inline fun <reified T> dateField(fieldName: String): DateField<T> = DateField(this, fieldName)

  fun dateNanosField(fieldName: String): DateNanosField = DateNanosField(this, fieldName)

  // Boolean field helper
  inline fun <reified T> booleanField(fieldName: String): BooleanField<T> =
    BooleanField(this, fieldName)

  // Binary field helper
  fun binaryField(fieldName: String): BinaryField = BinaryField(this, fieldName)

  // Geo field helpers
  fun ipField(fieldName: String): IpField = IpField(this, fieldName)

  fun geoPointField(fieldName: String): GeoPointField = GeoPointField(this, fieldName)

  fun geoShapeField(fieldName: String): GeoShapeField = GeoShapeField(this, fieldName)

  // Specialized field helpers
  fun completionField(fieldName: String): CompletionField = CompletionField(this, fieldName)

  fun tokenCountField(fieldName: String): TokenCountField = TokenCountField(this, fieldName)

  fun percolatorField(fieldName: String): PercolatorField = PercolatorField(this, fieldName)

  fun rankFeatureField(fieldName: String): RankFeatureField = RankFeatureField(this, fieldName)

  fun rankFeaturesField(fieldName: String): RankFeaturesField = RankFeaturesField(this, fieldName)

  fun flattenedField(fieldName: String): FlattenedField = FlattenedField(this, fieldName)

  fun shapeField(fieldName: String): ShapeField = ShapeField(this, fieldName)

  fun pointField(fieldName: String): PointField = PointField(this, fieldName)

  fun constantKeywordField(fieldName: String): ConstantKeywordField =
    ConstantKeywordField(this, fieldName)

  fun wildcardField(fieldName: String): WildcardField = WildcardField(this, fieldName)

  // Range field helpers
  fun integerRangeField(fieldName: String): IntegerRangeField = IntegerRangeField(this, fieldName)

  fun floatRangeField(fieldName: String): FloatRangeField = FloatRangeField(this, fieldName)

  fun longRangeField(fieldName: String): LongRangeField = LongRangeField(this, fieldName)

  fun doubleRangeField(fieldName: String): DoubleRangeField = DoubleRangeField(this, fieldName)

  fun dateRangeField(fieldName: String): DateRangeField = DateRangeField(this, fieldName)

  fun ipRangeField(fieldName: String): IpRangeField = IpRangeField(this, fieldName)

  inline fun <reified T> dynamicField(fieldName: String): DynamicField<T> =
    DynamicField(this, fieldName)
}
