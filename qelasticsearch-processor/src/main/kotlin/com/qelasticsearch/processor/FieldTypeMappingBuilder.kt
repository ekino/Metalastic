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
    safeAddMapping(mappings, "Text", "text()", "TextField")
    safeAddMapping(mappings, "Keyword", "keyword()", "KeywordField")
    safeAddMapping(mappings, "Binary", "binary()", "BinaryField")

    // Numeric field types - available in all versions
    safeAddMapping(mappings, "Long", "long()", "LongField")
    safeAddMapping(mappings, "Integer", "integer()", "IntegerField")
    safeAddMapping(mappings, "Short", "short()", "ShortField")
    safeAddMapping(mappings, "Byte", "byte()", "ByteField")
    safeAddMapping(mappings, "Double", "double()", "DoubleField")
    safeAddMapping(mappings, "Float", "float()", "FloatField")
    safeAddMapping(mappings, "Half_Float", "halfFloat()", "HalfFloatField")
    safeAddMapping(mappings, "Scaled_Float", "scaledFloat()", "ScaledFloatField")

    // Date/time field types
    safeAddMapping(mappings, "Date", "date()", "DateField")
    safeAddMapping(mappings, "Date_Nanos", "dateNanos()", "DateNanosField")

    // Boolean field type
    safeAddMapping(mappings, "Boolean", "boolean()", "BooleanField")

    // Range field types
    safeAddMapping(mappings, "Integer_Range", "integerRange()", "IntegerRangeField")
    safeAddMapping(mappings, "Float_Range", "floatRange()", "FloatRangeField")
    safeAddMapping(mappings, "Long_Range", "longRange()", "LongRangeField")
    safeAddMapping(mappings, "Double_Range", "doubleRange()", "DoubleRangeField")
    safeAddMapping(mappings, "Date_Range", "dateRange()", "DateRangeField")
    safeAddMapping(mappings, "Ip_Range", "ipRange()", "IpRangeField")

    // Specialized field types
    safeAddMapping(mappings, "Object", "objectField()", "ObjectField")
    safeAddMapping(mappings, "Nested", "nestedField()", "NestedField")
    safeAddMapping(mappings, "Ip", "ip()", "IpField")
    safeAddMapping(mappings, "TokenCount", "tokenCount()", "TokenCountField")
    safeAddMapping(mappings, "Percolator", "percolator()", "PercolatorField")
    safeAddMapping(mappings, "Flattened", "flattened()", "FlattenedField")
    safeAddMapping(mappings, "Search_As_You_Type", "searchAsYouType()", "SearchAsYouTypeField")

    // Geo types
    safeAddMapping(mappings, "Geo_Point", "geoPoint()", "GeoPointField")
    safeAddMapping(mappings, "Geo_Shape", "geoShape()", "GeoShapeField")

    // Advanced field types - may not exist in older versions
    safeAddMapping(mappings, "Auto", "auto()", "AutoField")
    safeAddMapping(mappings, "Rank_Feature", "rankFeature()", "RankFeatureField")
    safeAddMapping(mappings, "Rank_Features", "rankFeatures()", "RankFeaturesField")
    safeAddMapping(mappings, "Wildcard", "wildcard()", "WildcardField")
    safeAddMapping(mappings, "Dense_Vector", "denseVector()", "DenseVectorField")
    safeAddMapping(mappings, "Sparse_Vector", "sparseVector()", "SparseVectorField")
    safeAddMapping(mappings, "Constant_Keyword", "constantKeyword()", "ConstantKeywordField")
    safeAddMapping(mappings, "Alias", "alias()", "AliasField")
    safeAddMapping(mappings, "Version", "version()", "VersionField")
    safeAddMapping(mappings, "Murmur3", "murmur3()", "Murmur3Field")
    safeAddMapping(mappings, "Match_Only_Text", "matchOnlyText()", "MatchOnlyTextField")
    safeAddMapping(mappings, "Annotated_Text", "annotatedText()", "AnnotatedTextField")
    safeAddMapping(mappings, "Completion", "completion()", "CompletionField")
    safeAddMapping(mappings, "Join", "join()", "JoinField")

    return mappings.toMap()
  }

  /** Safely adds a FieldType mapping if the enum value exists in the current version. */
  private fun safeAddMapping(
    mappings: MutableMap<FieldType, FieldTypeMapping>,
    fieldTypeName: String,
    delegate: String,
    className: String,
  ) {
    try {
      val fieldType = FieldType.valueOf(fieldTypeName)
      mappings[fieldType] = FieldTypeMapping(delegate, className)
    } catch (e: IllegalArgumentException) {
      // FieldType enum value doesn't exist in this version - skip it
      logger.info("Skipping unsupported FieldType: $fieldTypeName (${e.message})")
    }
  }
}
