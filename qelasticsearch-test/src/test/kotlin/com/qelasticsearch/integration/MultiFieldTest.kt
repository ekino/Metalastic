package com.qelasticsearch.integration

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class MultiFieldTest {

    @Test
    fun `should support accessing multifield properties`() {
        // Test that we can access the main field
        assertThat(QJavaTestDocument.multiFieldName).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName.path).isEqualTo("multiFieldName")
        
        // Test that we can access inner fields by suffix - now non-nullable!
        assertThat(QJavaTestDocument.multiFieldName.search).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName.search.path).isEqualTo("multiFieldName.search")
        
        assertThat(QJavaTestDocument.multiFieldName.keyword).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName.keyword.path).isEqualTo("multiFieldName.keyword")
        
        // Test custom suffix access
        assertThat(QJavaTestDocument.multiFieldName["search"]).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName["keyword"]).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName["nonexistent"]).isNull()
    }
    
    @Test
    fun `should be able to traverse multifield search path like user requested`() {
        // This is the exact syntax the user wanted:

        // We can simulate this with our test document - now returns full path!
        val searchFieldPath = QJavaTestDocument.multiFieldName.search.path
        assertThat(searchFieldPath).isEqualTo("multiFieldName.search")
        
        // Test that dynamic property access works and is non-nullable
        assertThat(QJavaTestDocument.multiFieldName.search).isNotNull()
        assertThat(QJavaTestDocument.multiFieldName.keyword).isNotNull()
    }
}