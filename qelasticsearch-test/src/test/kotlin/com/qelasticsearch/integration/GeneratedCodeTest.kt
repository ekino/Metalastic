package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

/**
 * Test that demonstrates the generated QTestDocument class works correctly
 */
class GeneratedCodeTest {
    @Test
    fun `generated QTestDocument should have correct index name`() {
        assertThat(QTestDocument.indexName).isEqualTo("test_document")
    }

    @Test
    fun `generated QTestDocument should have all expected fields`() {
        assertThat(QTestDocument.id).isNotNull()
        assertThat(QTestDocument.name).isNotNull()
        assertThat(QTestDocument.age).isNotNull()
        assertThat(QTestDocument.active).isNotNull()
        assertThat(QTestDocument.createdDate).isNotNull()
        assertThat(QTestDocument.address).isNotNull()
        assertThat(QTestDocument.tags).isNotNull()
    }

    @Test
    fun `generated fields should have correct paths`() {
        assertThat(QTestDocument.id.path).isEqualTo("id")
        assertThat(QTestDocument.name.path).isEqualTo("name")
        assertThat(QTestDocument.age.path).isEqualTo("age")
        assertThat(QTestDocument.active.path).isEqualTo("active")
        assertThat(QTestDocument.createdDate.path).isEqualTo("createdDate")
        assertThat(QTestDocument.address.city.path).isEqualTo("address.city")
        assertThat(QTestDocument.tags.name.path).isEqualTo("tags.name")
    }

    @Test
    fun `QTestDocument should extend Index correctly`() {
        assertThat(QTestDocument.path).isEqualTo("")
    }
}
