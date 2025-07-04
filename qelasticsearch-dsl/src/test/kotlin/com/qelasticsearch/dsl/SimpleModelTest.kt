package com.qelasticsearch.dsl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test

class SimpleModelTest {
    @Test
    fun `simple field should have correct name and path`() {
        // Given
        val field = TextField<String>("name", "")

        // Then
        assertThat(field.name).isEqualTo("name")
        assertThat(field.path).isEqualTo("name")
    }

    @Test
    fun `simple field with parent path should have dotted path`() {
        // Given
        val field = TextField<String>("city", "address")

        // Then
        assertThat(field.name).isEqualTo("city")
        assertThat(field.path).isEqualTo("address.city")
    }

    @Test
    fun `index should have correct name and empty path`() {
        // Given
        val index =
            object : Index("test-index") {
                val name by text<String>()
                val age by integer<Int>()
            }

        // Then
        assertThat(index.indexName).isEqualTo("test-index")
        assertThat(index.path).isEqualTo("")
        assertThat(index.name.name).isEqualTo("name")
        assertThat(index.name.path).isEqualTo("name")
        assertThat(index.name).isInstanceOf(TextField::class)
        assertThat(index.age.name).isEqualTo("age")
        assertThat(index.age.path).isEqualTo("age")
        assertThat(index.age).isInstanceOf(IntegerField::class)
    }

    @Test
    fun `all field types should be created correctly`() {
        // Given
        val index =
            object : Index("test") {
                val textField by text<String>()
                val keywordField by keyword<String>()
                val longField by long<Long>()
                val intField by integer<Int>()
                val shortField by short<Short>()
                val byteField by byte<Byte>()
                val doubleField by double<Double>()
                val floatField by float<Float>()
                val halfFloatField by halfFloat()
                val scaledFloatField by scaledFloat()
                val dateField by date<java.util.Date>()
                val dateNanosField by dateNanos()
                val booleanField by boolean<Boolean>()
                val binaryField by binary()
                val ipField by ip()
                val geoPointField by geoPoint()
                val geoShapeField by geoShape()
                val completionField by completion()
                val tokenCountField by tokenCount()
                val percolatorField by percolator()
                val rankFeatureField by rankFeature()
                val rankFeaturesField by rankFeatures()
                val flattenedField by flattened()
                val shapeField by shape()
                val pointField by point()
                val constantKeywordField by constantKeyword()
                val wildcardField by wildcard()
                val integerRangeField by integerRange()
                val floatRangeField by floatRange()
                val longRangeField by longRange()
                val doubleRangeField by doubleRange()
                val dateRangeField by dateRange()
                val ipRangeField by ipRange()
            }

        // Then - Check field types using instance checks
        assertThat(index.textField).isInstanceOf(TextField::class)
        assertThat(index.keywordField).isInstanceOf(KeywordField::class)
        assertThat(index.longField).isInstanceOf(LongField::class)
        assertThat(index.intField).isInstanceOf(IntegerField::class)
        assertThat(index.shortField).isInstanceOf(ShortField::class)
        assertThat(index.byteField).isInstanceOf(ByteField::class)
        assertThat(index.doubleField).isInstanceOf(DoubleField::class)
        assertThat(index.floatField).isInstanceOf(FloatField::class)
        assertThat(index.halfFloatField).isInstanceOf(HalfFloatField::class)
        assertThat(index.scaledFloatField).isInstanceOf(ScaledFloatField::class)
        assertThat(index.dateField).isInstanceOf(DateField::class)
        assertThat(index.dateNanosField).isInstanceOf(DateNanosField::class)
        assertThat(index.booleanField).isInstanceOf(BooleanField::class)
        assertThat(index.binaryField).isInstanceOf(BinaryField::class)
        assertThat(index.ipField).isInstanceOf(IpField::class)
        assertThat(index.geoPointField).isInstanceOf(GeoPointField::class)
        assertThat(index.geoShapeField).isInstanceOf(GeoShapeField::class)
        assertThat(index.completionField).isInstanceOf(CompletionField::class)
        assertThat(index.tokenCountField).isInstanceOf(TokenCountField::class)
        assertThat(index.percolatorField).isInstanceOf(PercolatorField::class)
        assertThat(index.rankFeatureField).isInstanceOf(RankFeatureField::class)
        assertThat(index.rankFeaturesField).isInstanceOf(RankFeaturesField::class)
        assertThat(index.flattenedField).isInstanceOf(FlattenedField::class)
        assertThat(index.shapeField).isInstanceOf(ShapeField::class)
        assertThat(index.pointField).isInstanceOf(PointField::class)
        assertThat(index.constantKeywordField).isInstanceOf(ConstantKeywordField::class)
        assertThat(index.wildcardField).isInstanceOf(WildcardField::class)
        assertThat(index.integerRangeField).isInstanceOf(IntegerRangeField::class)
        assertThat(index.floatRangeField).isInstanceOf(FloatRangeField::class)
        assertThat(index.longRangeField).isInstanceOf(LongRangeField::class)
        assertThat(index.doubleRangeField).isInstanceOf(DoubleRangeField::class)
        assertThat(index.dateRangeField).isInstanceOf(DateRangeField::class)
        assertThat(index.ipRangeField).isInstanceOf(IpRangeField::class)
    }

    @Test
    fun `basic path traversal should work as specified`() {
        // Given - Recreate the exact scenario from requirements
        class AddressFields : ObjectFields() {
            val city by text<String>()
            val country by keyword<String>()
        }

        val addressFields = AddressFields()

        val person =
            object : Index("person") {
                val name by text<String>()
                val age by integer<Int>()
                val bio by text<String>()
                val address by objectField(addressFields)
            }

        // Then - Test exact requirements
        assertThat(person.path).isEqualTo("")
        assertThat(person.name.path).isEqualTo("name")
        assertThat(person.age.path).isEqualTo("age")
        assertThat(person.bio.path).isEqualTo("bio")

        // The nested field access should work - it should return the ObjectFields instance directly
        assertThat(person.address).isInstanceOf(ObjectFields::class)
    }

    @Test
    fun `should be able to travers object and nested objects`() {
        class AddressFields : ObjectFields() {
            val city by text<String>()
            val country by keyword<String>()
        }

        class Job : ObjectFields() {
            val title by text<String>()
            val salary by double<Double>()
        }

        val addressFields = AddressFields()
        val jobFields = Job()

        val person =
            object : Index("person") {
                val name by text<String>()
                val age by integer<Int>()
                val bio by text<String>()
                val address by objectField(addressFields)
                val job by objectField(jobFields, nested = true)
            }

        assertThat(person.address.city.path).isEqualTo("address.city")
        assertThat(person.job.title.path).isEqualTo("job.title")
    }
}
