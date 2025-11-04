package com.metalastic.processor.building

import com.metalastic.core.AliasField
import com.metalastic.core.AnnotatedTextField
import com.metalastic.core.AutoField
import com.metalastic.core.BinaryField
import com.metalastic.core.BooleanField
import com.metalastic.core.ByteField
import com.metalastic.core.ConstantKeywordField
import com.metalastic.core.DateField
import com.metalastic.core.DateNanosField
import com.metalastic.core.DateRangeField
import com.metalastic.core.DenseVectorField
import com.metalastic.core.DoubleField
import com.metalastic.core.DoubleRangeField
import com.metalastic.core.FlattenedField
import com.metalastic.core.FloatField
import com.metalastic.core.FloatRangeField
import com.metalastic.core.HalfFloatField
import com.metalastic.core.IntegerField
import com.metalastic.core.IntegerRangeField
import com.metalastic.core.IpField
import com.metalastic.core.KeywordField
import com.metalastic.core.LongField
import com.metalastic.core.LongRangeField
import com.metalastic.core.MatchOnlyTextField
import com.metalastic.core.Metamodel
import com.metalastic.core.Murmur3Field
import com.metalastic.core.ObjectField
import com.metalastic.core.PercolatorField
import com.metalastic.core.RankFeatureField
import com.metalastic.core.ScaledFloatField
import com.metalastic.core.SearchAsYouTypeField
import com.metalastic.core.ShortField
import com.metalastic.core.TextField
import com.metalastic.core.TokenCountField
import com.metalastic.core.VersionField
import com.metalastic.core.WildcardField
import com.metalastic.processor.report.reporter
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass
import org.springframework.data.elasticsearch.annotations.FieldType

/** Builds field type mappings with runtime detection for version compatibility. */
object FieldTypeMappings {

  fun classOf(fieldType: FieldType): FieldTypeClass =
    safeMappings[fieldType]
      ?: run {
        // This should never happen due to safeMapping construction, but just in case
        val message = "Unsupported field type: $fieldType"
        val exception = IllegalArgumentException("Unsupported field type: $fieldType")
        reporter.exception(exception) { message }
        throw exception
      }

  /**
   * Builds field type mappings with runtime detection for version compatibility. Only includes
   * FieldType enum values that exist in the current Spring Data Elasticsearch version.
   */
  private val safeMappings by lazy {
    val mappings = mutableMapOf<FieldType, FieldTypeClass>()
    // Core field types - available in all versions
    mappings.safeAddMapping("Text", TextField::class)
    mappings.safeAddMapping("Keyword", KeywordField::class)
    mappings.safeAddMapping("Binary", BinaryField::class)

    // Numeric field types - available in all versions
    mappings.safeAddMapping("Long", LongField::class)
    mappings.safeAddMapping("Integer", IntegerField::class)
    mappings.safeAddMapping("Short", ShortField::class)
    mappings.safeAddMapping("Byte", ByteField::class)
    mappings.safeAddMapping("Double", DoubleField::class)
    mappings.safeAddMapping("Float", FloatField::class)
    mappings.safeAddMapping("Half_Float", HalfFloatField::class)
    mappings.safeAddMapping("Scaled_Float", ScaledFloatField::class)

    // Date/time field types
    mappings.safeAddMapping("Date", DateField::class)
    mappings.safeAddMapping("Date_Nanos", DateNanosField::class)

    // Boolean field type
    mappings.safeAddMapping("Boolean", BooleanField::class)

    // Range field types
    mappings.safeAddMapping("Integer_Range", IntegerRangeField::class)
    mappings.safeAddMapping("Float_Range", FloatRangeField::class)
    mappings.safeAddMapping("Long_Range", LongRangeField::class)
    mappings.safeAddMapping("Double_Range", DoubleRangeField::class)
    mappings.safeAddMapping("Date_Range", DateRangeField::class)
    mappings.safeAddMapping("Ip_Range", MatchOnlyTextField::class)

    // Specialized field types
    mappings.safeAddMapping("Object", ObjectField::class)
    mappings.safeAddMapping("Nested", ObjectField::class)
    mappings.safeAddMapping("Ip", IpField::class)
    mappings.safeAddMapping("TokenCount", TokenCountField::class)
    mappings.safeAddMapping("Percolator", PercolatorField::class)
    mappings.safeAddMapping("Flattened", FlattenedField::class)
    mappings.safeAddMapping("Search_As_You_Type", SearchAsYouTypeField::class)

    // Advanced field types
    mappings.safeAddMapping("Auto", AutoField::class)
    mappings.safeAddMapping("Rank_Feature", RankFeatureField::class)
    mappings.safeAddMapping("Rank_Features", RankFeatureField::class)
    mappings.safeAddMapping("Wildcard", WildcardField::class)
    mappings.safeAddMapping("Dense_Vector", DenseVectorField::class)
    mappings.safeAddMapping("Constant_Keyword", ConstantKeywordField::class)
    mappings.safeAddMapping("Alias", AliasField::class)
    mappings.safeAddMapping("Version", VersionField::class)
    mappings.safeAddMapping("Murmur3", Murmur3Field::class)
    mappings.safeAddMapping("Match_Only_Text", MatchOnlyTextField::class)
    mappings.safeAddMapping("Annotated_Text", AnnotatedTextField::class)
    mappings.toMap()
  }

  /** Safely adds a FieldType mapping if the enum value exists in the current version. */
  private fun <T : Metamodel<*>> MutableMap<FieldType, FieldTypeClass>.safeAddMapping(
    fieldTypeName: String,
    fieldClass: KClass<T>,
  ) {
    FieldType.entries
      .firstOrNull { it.name == fieldTypeName }
      ?.also { put(it, FieldTypeClass(fieldClass.asClassName())) }
      ?: reporter.debug {
        // FieldType enum value doesn't exist in this version - skip it
        "Skipping unsupported FieldType: $fieldTypeName"
      }
  }
}
