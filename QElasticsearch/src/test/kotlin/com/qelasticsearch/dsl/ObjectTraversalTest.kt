package com.qelasticsearch.dsl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test

class ObjectTraversalTest {
    
    @Test
    fun `should enable direct field traversal through object fields`() {
        // Given - Nested object structure
        class AddressFields : ObjectFields() {
            val street by text()
            val city by text()
            val country by keyword()
            val zipCode by keyword()
        }
        
        class CompanyFields : ObjectFields() {
            val name by text()
            val address by objectField(AddressFields())
        }
        
        val person = object : Index("person") {
            val name by text()
            val age by integer()
            val address by objectField(AddressFields())
            val company by nestedField(CompanyFields())
        }
        
        // Then - Direct field traversal should work
        assertThat(person.name).isInstanceOf(TextField::class)
        assertThat(person.name.path).isEqualTo("name")
        
        assertThat(person.age).isInstanceOf(IntegerField::class)  
        assertThat(person.age.path).isEqualTo("age")
        
        // Direct traversal through object fields
        assertThat(person.address.street).isInstanceOf(TextField::class)
        assertThat(person.address.street.path).isEqualTo("address.street")
        
        assertThat(person.address.city).isInstanceOf(TextField::class)
        assertThat(person.address.city.path).isEqualTo("address.city")
        
        assertThat(person.address.country).isInstanceOf(KeywordField::class)
        assertThat(person.address.country.path).isEqualTo("address.country")
        
        assertThat(person.address.zipCode).isInstanceOf(KeywordField::class)
        assertThat(person.address.zipCode.path).isEqualTo("address.zipCode")
        
        // Direct traversal through nested fields  
        assertThat(person.company.name).isInstanceOf(TextField::class)
        assertThat(person.company.name.path).isEqualTo("company.name")
        
        // Deep traversal through nested object fields
        assertThat(person.company.address.street).isInstanceOf(TextField::class)
        assertThat(person.company.address.street.path).isEqualTo("company.address.street")
        
        assertThat(person.company.address.city).isInstanceOf(TextField::class)
        assertThat(person.company.address.city.path).isEqualTo("company.address.city")
        
        assertThat(person.company.address.country).isInstanceOf(KeywordField::class)
        assertThat(person.company.address.country.path).isEqualTo("company.address.country")
    }
    
    @Test  
    fun `should support the exact syntax from user requirements`() {
        // Given - User's exact test case
        class AddressFields : ObjectFields() {
            val city by text()
            val country by keyword()
        }

        class Job : ObjectFields() {
            val title by text()
            val salary by double()
        }

        val addressFields = AddressFields()
        val jobFields = Job()

        val person = object : Index("person") {
            val name by text()
            val age by integer()
            val bio by text()
            val address by objectField(addressFields)
            val job by objectField(jobFields, nested = true)
        }

        // Then - Exact assertions from user requirements
        assertThat(person.address.city.path).isEqualTo("address.city")
        assertThat(person.job.title.path).isEqualTo("job.title")
        
        // Additional verification
        assertThat(person.address.country.path).isEqualTo("address.country")
        assertThat(person.job.salary.path).isEqualTo("job.salary")
    }
    
    @Test
    fun `should maintain type safety with direct traversal`() {
        // Given - Complex nested structure
        class MetricsFields : ObjectFields() {
            val views by long()
            val score by double()
            val active by boolean()
        }
        
        class ProductFields : ObjectFields() {
            val name by text()
            val price by double()
            val category by keyword()
            val metrics by objectField(MetricsFields())
        }
        
        val index = object : Index("shop") {
            val product by objectField(ProductFields())
        }
        
        // Then - Type safety maintained through traversal
        assertThat(index.product.name).isInstanceOf(TextField::class)
        assertThat(index.product.price).isInstanceOf(DoubleField::class)
        assertThat(index.product.category).isInstanceOf(KeywordField::class)
        
        assertThat(index.product.metrics.views).isInstanceOf(LongField::class)
        assertThat(index.product.metrics.score).isInstanceOf(DoubleField::class)
        assertThat(index.product.metrics.active).isInstanceOf(BooleanField::class)
        
        // Path construction works correctly
        assertThat(index.product.name.path).isEqualTo("product.name")
        assertThat(index.product.metrics.views.path).isEqualTo("product.metrics.views")
        assertThat(index.product.metrics.score.path).isEqualTo("product.metrics.score")
        assertThat(index.product.metrics.active.path).isEqualTo("product.metrics.active")
    }
}