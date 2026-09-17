/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.core

import kotlin.reflect.KType
import org.springframework.data.elasticsearch.annotations.DateFormat

/**
 * Base class for leaf metamodel fields, i.e. fields that do not contain nested fields.
 *
 * A [Field] always belongs to a [Container] (an [ObjectField]/[Document] or a [MultiField]) that it
 * registers itself with on construction, which makes path traversal and parent lookup possible.
 *
 * @param T the Kotlin type carried by this field
 */
sealed class Field<T>(private val parent: Container<*>, fieldName: String, fieldType: KType) :
  Metamodel<T>(fieldName, fieldType) {

  init {
    parent.register(this)
  }

  override fun parent(): Container<*> = parent
}

/**
 * Metamodel field for the Elasticsearch `auto` field type, mapped from `FieldType.Auto`.
 *
 * @param T the Kotlin type carried by this field
 */
class AutoField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Text fields
/**
 * Metamodel field for the Elasticsearch `text` field type, mapped from `FieldType.Text`.
 *
 * @param T the Kotlin type carried by this field
 */
class TextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `keyword` field type, mapped from `FieldType.Keyword`.
 *
 * @param T the Kotlin type carried by this field
 */
class KeywordField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Numeric fields
/**
 * Metamodel field for the Elasticsearch `long` field type, mapped from `FieldType.Long`.
 *
 * @param T the Kotlin type carried by this field
 */
class LongField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `integer` field type, mapped from `FieldType.Integer`.
 *
 * @param T the Kotlin type carried by this field
 */
class IntegerField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `short` field type, mapped from `FieldType.Short`.
 *
 * @param T the Kotlin type carried by this field
 */
class ShortField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `byte` field type, mapped from `FieldType.Byte`.
 *
 * @param T the Kotlin type carried by this field
 */
class ByteField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `double` field type, mapped from `FieldType.Double`.
 *
 * @param T the Kotlin type carried by this field
 */
class DoubleField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `float` field type, mapped from `FieldType.Float`.
 *
 * @param T the Kotlin type carried by this field
 */
class FloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `half_float` field type, mapped from
 * `FieldType.Half_Float`.
 *
 * @param T the Kotlin type carried by this field
 */
class HalfFloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `scaled_float` field type, mapped from
 * `FieldType.Scaled_Float`.
 *
 * @param T the Kotlin type carried by this field
 */
class ScaledFloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Date fields
/**
 * Metamodel field for the Elasticsearch `date` field type, mapped from `FieldType.Date`.
 *
 * Optionally carries the [DateFormat] values declared on the source `@Field` annotation so
 * consumers can inspect which date formats the underlying Elasticsearch mapping accepts.
 *
 * @param T the Kotlin type carried by this field
 * @param formats the accepted date formats, empty when no explicit format was declared
 */
class DateField<T : Any?>(
  parent: Container<*>,
  fieldName: String,
  fieldType: KType,
  val formats: List<DateFormat> = emptyList(),
) : Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `date_nanos` field type, mapped from
 * `FieldType.Date_Nanos`.
 *
 * @param T the Kotlin type carried by this field
 */
class DateNanosField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Boolean field
/**
 * Metamodel field for the Elasticsearch `boolean` field type, mapped from `FieldType.Boolean`.
 *
 * @param T the Kotlin type carried by this field
 */
class BooleanField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Binary field
/**
 * Metamodel field for the Elasticsearch `binary` field type, mapped from `FieldType.Binary`.
 *
 * @param T the Kotlin type carried by this field
 */
class BinaryField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `ip` field type, mapped from `FieldType.Ip`.
 *
 * @param T the Kotlin type carried by this field
 */
class IpField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Specialized fields
/**
 * Metamodel field for the Elasticsearch `completion` field type, used for completion suggesters.
 *
 * @param T the Kotlin type carried by this field
 */
class CompletionField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `token_count` field type, mapped from
 * `FieldType.TokenCount`.
 *
 * @param T the Kotlin type carried by this field
 */
class TokenCountField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `percolator` field type, mapped from
 * `FieldType.Percolator`.
 *
 * @param T the Kotlin type carried by this field
 */
class PercolatorField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `rank_feature` field type, mapped from
 * `FieldType.Rank_Feature`.
 *
 * @param T the Kotlin type carried by this field
 */
class RankFeatureField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `rank_features` field type, mapped from
 * `FieldType.Rank_Features`.
 *
 * @param T the Kotlin type carried by this field
 */
class RankFeaturesField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `flattened` field type, mapped from `FieldType.Flattened`.
 *
 * @param T the Kotlin type carried by this field
 */
class FlattenedField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `shape` field type, used to model arbitrary two dimensional
 * geometries.
 *
 * @param T the Kotlin type carried by this field
 */
class ShapeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `point` field type, used to model arbitrary `x, y`
 * cartesian points.
 *
 * @param T the Kotlin type carried by this field
 */
class PointField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `constant_keyword` field type, mapped from
 * `FieldType.Constant_Keyword`.
 *
 * @param T the Kotlin type carried by this field
 */
class ConstantKeywordField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `wildcard` field type, mapped from `FieldType.Wildcard`.
 *
 * @param T the Kotlin type carried by this field
 */
class WildcardField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Range fields
/**
 * Metamodel field for the Elasticsearch `integer_range` field type, mapped from
 * `FieldType.Integer_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class IntegerRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `float_range` field type, mapped from
 * `FieldType.Float_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class FloatRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `long_range` field type, mapped from
 * `FieldType.Long_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class LongRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `double_range` field type, mapped from
 * `FieldType.Double_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class DoubleRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `date_range` field type, mapped from
 * `FieldType.Date_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class DateRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `ip_range` field type, mapped from `FieldType.Ip_Range`.
 *
 * @param T the Kotlin type carried by this field
 */
class IpRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Advanced fields
/**
 * Metamodel field for the Elasticsearch `search_as_you_type` field type, mapped from
 * `FieldType.Search_As_You_Type`.
 *
 * @param T the Kotlin type carried by this field
 */
class SearchAsYouTypeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `dense_vector` field type, mapped from
 * `FieldType.Dense_Vector`.
 *
 * @param T the Kotlin type carried by this field
 */
class DenseVectorField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `alias` field type, mapped from `FieldType.Alias`.
 *
 * @param T the Kotlin type carried by this field
 */
class AliasField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `version` field type, mapped from `FieldType.Version`.
 *
 * @param T the Kotlin type carried by this field
 */
class VersionField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `murmur3` field type, mapped from `FieldType.Murmur3`.
 *
 * @param T the Kotlin type carried by this field
 */
class Murmur3Field<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `match_only_text` field type, mapped from
 * `FieldType.Match_Only_Text`.
 *
 * @param T the Kotlin type carried by this field
 */
class MatchOnlyTextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

/**
 * Metamodel field for the Elasticsearch `annotated_text` field type, mapped from
 * `FieldType.Annotated_Text`.
 *
 * @param T the Kotlin type carried by this field
 */
class AnnotatedTextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)
