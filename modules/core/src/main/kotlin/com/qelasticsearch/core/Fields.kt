package com.qelasticsearch.core
class AutoField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Text fields
class TextField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class KeywordField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Numeric fields
class LongField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class IntegerField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class ShortField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class ByteField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class DoubleField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class FloatField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class HalfFloatField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class ScaledFloatField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Date fields
class DateField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class DateNanosField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Boolean field
class BooleanField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Binary field
class BinaryField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class IpField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Specialized fields
class CompletionField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class TokenCountField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class PercolatorField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class RankFeatureField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class RankFeaturesField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class FlattenedField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class ShapeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class PointField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class ConstantKeywordField<T>(parent: ObjectField<*>, fieldName: String) :
  Field<T>(parent, fieldName)

class WildcardField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Range fields
class IntegerRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class FloatRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class LongRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class DoubleRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class DateRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class IpRangeField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

// Advanced fields
class SearchAsYouTypeField<T>(parent: ObjectField<*>, fieldName: String) :
  Field<T>(parent, fieldName)

class DenseVectorField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class AliasField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class VersionField<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class Murmur3Field<T>(parent: ObjectField<*>, fieldName: String) : Field<T>(parent, fieldName)

class MatchOnlyTextField<T>(parent: ObjectField<*>, fieldName: String) :
  Field<T>(parent, fieldName)

class AnnotatedTextField<T>(parent: ObjectField<*>, fieldName: String) :
  Field<T>(parent, fieldName)
