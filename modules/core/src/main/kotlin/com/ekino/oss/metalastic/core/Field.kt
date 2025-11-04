package com.ekino.oss.metalastic.core

import kotlin.reflect.KType
import org.springframework.data.elasticsearch.annotations.DateFormat

sealed class Field<T>(private val parent: Container<*>, fieldName: String, fieldType: KType) :
  Metamodel<T>(fieldName, fieldType) {

  init {
    parent.register(this)
  }

  override fun parent(): Container<*> = parent
}

class AutoField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Text fields
class TextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class KeywordField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Numeric fields
class LongField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IntegerField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ShortField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ByteField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DoubleField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class HalfFloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ScaledFloatField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Date fields
class DateField<T : Any?>(
  parent: Container<*>,
  fieldName: String,
  fieldType: KType,
  val formats: List<DateFormat> = emptyList(),
) : Field<T>(parent, fieldName, fieldType)

class DateNanosField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Boolean field
class BooleanField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Binary field
class BinaryField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IpField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Specialized fields
class CompletionField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class TokenCountField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class PercolatorField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class RankFeatureField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class RankFeaturesField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FlattenedField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ShapeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class PointField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ConstantKeywordField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class WildcardField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Range fields
class IntegerRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FloatRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class LongRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DoubleRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DateRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IpRangeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Advanced fields
class SearchAsYouTypeField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DenseVectorField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class AliasField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class VersionField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class Murmur3Field<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class MatchOnlyTextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class AnnotatedTextField<T : Any?>(parent: Container<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)
