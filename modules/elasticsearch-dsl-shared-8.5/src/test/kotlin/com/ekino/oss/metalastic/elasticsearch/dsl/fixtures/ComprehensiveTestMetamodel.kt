package com.ekino.oss.metalastic.elasticsearch.dsl.fixtures

import com.ekino.oss.metalastic.core.BooleanField
import com.ekino.oss.metalastic.core.DateField
import com.ekino.oss.metalastic.core.DoubleField
import com.ekino.oss.metalastic.core.FloatField
import com.ekino.oss.metalastic.core.IntegerField
import com.ekino.oss.metalastic.core.KeywordField
import com.ekino.oss.metalastic.core.ObjectField
import com.ekino.oss.metalastic.core.TextField
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import kotlin.reflect.typeOf

/**
 * Comprehensive test metamodel for DSL testing.
 *
 * Provides fields for testing type-specific overloads, collection variants, date types, and edge
 * cases. Only includes fields that are actively used or planned for future test expansion.
 */
object ComprehensiveTestMetamodel : ObjectField<Any>(null, "", false, typeOf<Any>()) {

  // ===== TEXT FIELDS =====
  val name: TextField<String> = TextField(this, "name", typeOf<String>())
  val title: TextField<String> = TextField(this, "title", typeOf<String>())

  // ===== KEYWORD FIELDS =====
  val status: KeywordField<TestStatus> = KeywordField(this, "status", typeOf<TestStatus>())
  val country: KeywordField<String> = KeywordField(this, "country", typeOf<String>())
  val category: KeywordField<String> = KeywordField(this, "category", typeOf<String>())

  // ===== NUMERIC FIELDS =====
  val age: IntegerField<Int> = IntegerField(this, "age", typeOf<Int>())
  val price: DoubleField<Double> = DoubleField(this, "price", typeOf<Double>())
  val rating: FloatField<Float> = FloatField(this, "rating", typeOf<Float>())

  // ===== BOOLEAN FIELD =====
  val active: BooleanField<Boolean> = BooleanField(this, "active", typeOf<Boolean>())

  // ===== DATE FIELDS =====
  val createdAt: DateField<Instant> = DateField(this, "createdAt", typeOf<Instant>())
  val updatedAt: DateField<LocalDateTime> = DateField(this, "updatedAt", typeOf<LocalDateTime>())
  val lastModified: DateField<Date> = DateField(this, "lastModified", typeOf<Date>())

  // ===== COLLECTION FIELDS =====
  val tags: KeywordField<Collection<String>> =
    KeywordField(this, "tags", typeOf<Collection<String>>())
  val scores: IntegerField<Collection<Int>> =
    IntegerField(this, "scores", typeOf<Collection<Int>>())

  // ===== NESTED FIELD =====
  val reviews: ReviewField = ReviewField(this, "reviews", true)
}

/** Nested field for testing nested queries */
class ReviewField(parent: ObjectField<*>?, fieldName: String, nested: Boolean) :
  ObjectField<Any>(parent, fieldName, nested, typeOf<Any>()) {

  val author: KeywordField<String> = KeywordField(this, "author", typeOf<String>())
  val score: DoubleField<Double> = DoubleField(this, "score", typeOf<Double>())
  val verified: BooleanField<Boolean> = BooleanField(this, "verified", typeOf<Boolean>())
}

/** Test enum for testing enum-based queries */
enum class TestStatus {
  ACTIVE,
  INACTIVE,
  PENDING,
  DELETED,
  DRAFT,
}
