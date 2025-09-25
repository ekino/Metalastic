package com.metalastic.core

import kotlin.reflect.KType

class AutoField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Text fields
class TextField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class KeywordField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Numeric fields
class LongField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IntegerField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ShortField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ByteField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DoubleField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FloatField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class HalfFloatField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ScaledFloatField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Date fields
class DateField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DateNanosField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Boolean field
class BooleanField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Binary field
class BinaryField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IpField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Specialized fields
class CompletionField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class TokenCountField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class PercolatorField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class RankFeatureField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class RankFeaturesField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FlattenedField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ShapeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class PointField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class ConstantKeywordField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class WildcardField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Range fields
class IntegerRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class FloatRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class LongRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DoubleRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DateRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class IpRangeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

// Advanced fields
class SearchAsYouTypeField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class DenseVectorField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class AliasField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class VersionField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class Murmur3Field<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class MatchOnlyTextField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)

class AnnotatedTextField<T : Any?>(parent: ObjectField<*>, fieldName: String, fieldType: KType) :
  Field<T>(parent, fieldName, fieldType)
