package com.qelasticsearch.processor.building

import com.qelasticsearch.core.AliasField
import com.qelasticsearch.core.AnnotatedTextField
import com.qelasticsearch.core.AutoField
import com.qelasticsearch.core.BinaryField
import com.qelasticsearch.core.BooleanField
import com.qelasticsearch.core.ByteField
import com.qelasticsearch.core.ConstantKeywordField
import com.qelasticsearch.core.DateField
import com.qelasticsearch.core.DateNanosField
import com.qelasticsearch.core.DateRangeField
import com.qelasticsearch.core.DenseVectorField
import com.qelasticsearch.core.DoubleField
import com.qelasticsearch.core.DoubleRangeField
import com.qelasticsearch.core.Field
import com.qelasticsearch.core.FlattenedField
import com.qelasticsearch.core.FloatField
import com.qelasticsearch.core.FloatRangeField
import com.qelasticsearch.core.HalfFloatField
import com.qelasticsearch.core.IntegerField
import com.qelasticsearch.core.IntegerRangeField
import com.qelasticsearch.core.IpField
import com.qelasticsearch.core.KeywordField
import com.qelasticsearch.core.LongField
import com.qelasticsearch.core.LongRangeField
import com.qelasticsearch.core.MatchOnlyTextField
import com.qelasticsearch.core.Murmur3Field
import com.qelasticsearch.core.ObjectField
import com.qelasticsearch.core.PercolatorField
import com.qelasticsearch.core.RankFeatureField
import com.qelasticsearch.core.ScaledFloatField
import com.qelasticsearch.core.SearchAsYouTypeField
import com.qelasticsearch.core.ShortField
import com.qelasticsearch.core.TextField
import com.qelasticsearch.core.TokenCountField
import com.qelasticsearch.core.VersionField
import com.qelasticsearch.core.WildcardField
import com.qelasticsearch.processor.report.reporter
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
  private fun <T : Field<*>> MutableMap<FieldType, FieldTypeClass>.safeAddMapping(
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
