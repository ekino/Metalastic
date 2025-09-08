package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.KSPLogger
import org.springframework.data.elasticsearch.annotations.FieldType

/** Builds field type mappings with runtime detection for version compatibility. */
class FieldTypeMappingBuilder(private val logger: KSPLogger) {
  /**
   * Builds field type mappings with runtime detection for version compatibility. Only includes
   * FieldType enum values that exist in the current Spring Data Elasticsearch version.
   */
  fun build(): Map<FieldType, FieldTypeMapping> {
    val mappings = mutableMapOf<FieldType, FieldTypeMapping>()

    // Core field types - available in all versions
    safeAddMapping(mappings, "Text", "TextField")
    safeAddMapping(mappings, "Keyword", "KeywordField")
    safeAddMapping(mappings, "Binary", "BinaryField")

    // Numeric field types - available in all versions
    safeAddMapping(mappings, "Long", "LongField")
    safeAddMapping(mappings, "Integer", "IntegerField")
    safeAddMapping(mappings, "Short", "ShortField")
    safeAddMapping(mappings, "Byte", "ByteField")
    safeAddMapping(mappings, "Double", "DoubleField")
    safeAddMapping(mappings, "Float", "FloatField")
    safeAddMapping(mappings, "Half_Float", "HalfFloatField")
    safeAddMapping(mappings, "Scaled_Float", "ScaledFloatField")

    // Date/time field types
    safeAddMapping(mappings, "Date", "DateField")
    safeAddMapping(mappings, "Date_Nanos", "DateNanosField")

    // Boolean field type
    safeAddMapping(mappings, "Boolean", "BooleanField")

    // Range field types
    safeAddMapping(mappings, "Integer_Range", "IntegerRangeField")
    safeAddMapping(mappings, "Float_Range", "FloatRangeField")
    safeAddMapping(mappings, "Long_Range", "LongRangeField")
    safeAddMapping(mappings, "Double_Range", "DoubleRangeField")
    safeAddMapping(mappings, "Date_Range", "DateRangeField")
    safeAddMapping(mappings, "Ip_Range", "IpRangeField")

    // Specialized field types
    safeAddMapping(mappings, "Object", "ObjectField")
    safeAddMapping(mappings, "Nested", "ObjectField")
    safeAddMapping(mappings, "Ip", "IpField")
    safeAddMapping(mappings, "TokenCount", "TokenCountField")
    safeAddMapping(mappings, "Percolator", "PercolatorField")
    safeAddMapping(mappings, "Flattened", "FlattenedField")
    safeAddMapping(mappings, "Search_As_You_Type", "SearchAsYouTypeField")

    // Geo types
    safeAddMapping(mappings, "Geo_Point", "GeoPointField")
    safeAddMapping(mappings, "Geo_Shape", "GeoShapeField")

    // Advanced field types - may not exist in older versions
    safeAddMapping(mappings, "Auto", "AutoField")
    safeAddMapping(mappings, "Rank_Feature", "RankFeatureField")
    safeAddMapping(mappings, "Rank_Features", "RankFeaturesField")
    safeAddMapping(mappings, "Wildcard", "WildcardField")
    safeAddMapping(mappings, "Dense_Vector", "DenseVectorField")
    safeAddMapping(mappings, "Sparse_Vector", "SparseVectorField")
    safeAddMapping(mappings, "Constant_Keyword", "ConstantKeywordField")
    safeAddMapping(mappings, "Alias", "AliasField")
    safeAddMapping(mappings, "Version", "VersionField")
    safeAddMapping(mappings, "Murmur3", "Murmur3Field")
    safeAddMapping(mappings, "Match_Only_Text", "MatchOnlyTextField")
    safeAddMapping(mappings, "Annotated_Text", "AnnotatedTextField")
    safeAddMapping(mappings, "Completion", "CompletionField")
    safeAddMapping(mappings, "Join", "JoinField")

    return mappings.toMap()
  }

  /** Safely adds a FieldType mapping if the enum value exists in the current version. */
  private fun safeAddMapping(
    mappings: MutableMap<FieldType, FieldTypeMapping>,
    fieldTypeName: String,
    className: String,
  ) {
    try {
      val fieldType = FieldType.valueOf(fieldTypeName)
      mappings[fieldType] = FieldTypeMapping(className)
    } catch (e: IllegalArgumentException) {
      // FieldType enum value doesn't exist in this version - skip it
      logger.info("Skipping unsupported FieldType: $fieldTypeName (${e.message})")
    }
  }
}
