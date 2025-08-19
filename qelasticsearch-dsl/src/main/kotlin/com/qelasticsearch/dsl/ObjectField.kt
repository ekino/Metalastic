package com.qelasticsearch.dsl

import com.qelasticsearch.dsl.delegation.FieldDelegate

/**
 * Base class for object fields that can contain nested fields. Used for both nested objects and the
 * root index.
 */
abstract class ObjectField(
  parent: ObjectField? = null,
  path: String = "",
  protected val nested: Boolean = false,
) : Field(parent, path) {
  @YellowColor fun nested(): Boolean = nested

  // Text field delegates
  inline fun <reified T> text() = FieldDelegate { parent, path -> TextField<T>(parent, path) }

  // Keyword field delegates
  inline fun <reified T> keyword() = FieldDelegate { parent, path -> KeywordField<T>(parent, path) }

  // Numeric field delegates
  inline fun <reified T> long() = FieldDelegate { parent, path -> LongField<T>(parent, path) }

  inline fun <reified T> integer() = FieldDelegate { parent, path -> IntegerField<T>(parent, path) }

  inline fun <reified T> short() = FieldDelegate { parent, path -> ShortField<T>(parent, path) }

  inline fun <reified T> byte() = FieldDelegate { parent, path -> ByteField<T>(parent, path) }

  inline fun <reified T> double() = FieldDelegate { parent, path -> DoubleField<T>(parent, path) }

  inline fun <reified T> float() = FieldDelegate { parent, path -> FloatField<T>(parent, path) }

  fun halfFloat() = FieldDelegate { parent, path -> HalfFloatField(parent, path) }

  fun scaledFloat() = FieldDelegate { parent, path -> ScaledFloatField(parent, path) }

  // Date field delegates
  inline fun <reified T> date() = FieldDelegate { parent, path -> DateField<T>(parent, path) }

  fun dateNanos() = FieldDelegate { parent, path -> DateNanosField(parent, path) }

  // Boolean field delegate
  inline fun <reified T> boolean() = FieldDelegate { parent, path -> BooleanField<T>(parent, path) }

  // Binary field delegate
  fun binary() = FieldDelegate { parent, path -> BinaryField(parent, path) }

  // Geo field delegates
  fun ip() = FieldDelegate { parent, path -> IpField(parent, path) }

  fun geoPoint() = FieldDelegate { parent, path -> GeoPointField(parent, path) }

  fun geoShape() = FieldDelegate { parent, path -> GeoShapeField(parent, path) }

  // Specialized field delegates
  fun completion() = FieldDelegate { parent, path -> CompletionField(parent, path) }

  fun tokenCount() = FieldDelegate { parent, path -> TokenCountField(parent, path) }

  fun percolator() = FieldDelegate { parent, path -> PercolatorField(parent, path) }

  fun rankFeature() = FieldDelegate { parent, path -> RankFeatureField(parent, path) }

  fun rankFeatures() = FieldDelegate { parent, path -> RankFeaturesField(parent, path) }

  fun flattened() = FieldDelegate { parent, path -> FlattenedField(parent, path) }

  fun shape() = FieldDelegate { parent, path -> ShapeField(parent, path) }

  fun point() = FieldDelegate { parent, path -> PointField(parent, path) }

  fun constantKeyword() = FieldDelegate { parent, path -> ConstantKeywordField(parent, path) }

  fun wildcard() = FieldDelegate { parent, path -> WildcardField(parent, path) }

  // Range field delegates
  fun integerRange() = FieldDelegate { parent, path -> IntegerRangeField(parent, path) }

  fun floatRange() = FieldDelegate { parent, path -> FloatRangeField(parent, path) }

  fun longRange() = FieldDelegate { parent, path -> LongRangeField(parent, path) }

  fun doubleRange() = FieldDelegate { parent, path -> DoubleRangeField(parent, path) }

  fun dateRange() = FieldDelegate { parent, path -> DateRangeField(parent, path) }

  fun ipRange() = FieldDelegate { parent, path -> IpRangeField(parent, path) }
}
