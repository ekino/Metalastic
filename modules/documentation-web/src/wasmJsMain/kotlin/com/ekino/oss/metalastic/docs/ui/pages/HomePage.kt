package com.ekino.oss.metalastic.docs.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekino.oss.metalastic.docs.ui.components.SyntaxHighlightedCode
import dev.snipme.highlights.model.SyntaxLanguage

@Composable
fun HomePage(onNavigate: (String) -> Unit) {
    SelectionContainer {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Metalastic",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Type-safe metamodel generator for Elasticsearch in Kotlin",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Features",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            FeatureItem("Zero Runtime Overhead", "All magic happens at compile time")
            FeatureItem("Full IDE Support", "Autocomplete and refactoring")
            FeatureItem("Multi-Version Support", "Spring Data ES 5.0-5.5")
            FeatureItem("Java Compatible", "Works with both Kotlin and Java")

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Quick Example",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            SyntaxHighlightedCode(
                code = """
                    package com.ekino.iperia.elastic.search.catalog

                    import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
                    import com.ekino.iperia.elastic.model.catalog.IndexBloc
                    import com.ekino.iperia.elastic.model.catalog.MetaIndexBloc
                    import com.ekino.iperia.elastic.search.SearchCriteriaToQueryTemplate
                    import com.ekino.iperia.elastic.search.catalog.criteria.BlocSearchCriteria
                    import com.ekino.oss.metalastic.core.Field
                    import com.ekino.oss.metalastic.core.Metamodel
                    import com.ekino.oss.metalastic.elasticsearch.dsl.boolQueryDsl

                    class BlocSearchCriteriaQuery :
                      SearchCriteriaToQueryTemplate<BlocSearchCriteria, IndexBloc, MetaIndexBloc<IndexBloc>>(
                        criteriaType = BlocSearchCriteria::class,
                        indexType = IndexBloc::class,
                        metamodel = MetaIndexBloc.indexBloc,
                      ) {

                      override fun sortingPathAliases(alias: String): List<Field<*>>? =
                        when (alias) {
                          "code" -> metamodel.stringCode.mainField()
                          else -> null
                        }?.let(::listOf)

                      override fun MetaIndexBloc<IndexBloc>.textQueryFields(): List<Metamodel<*>>? =
                        listOf(
                          stringCode.search,
                          description.search,
                        )

                      override fun BoolQuery.Builder.withCriteriaQuery(criteria: BlocSearchCriteria) {
                        with(metamodel) {
                          boolQueryDsl {
                            must + {
                              stringCode.search matchPhrase criteria.code
                              description.search matchPhrase criteria.description
                              criteria.date.mustBeBetween(startDate, endDate)
                            }
                            withCatalogCriteria(availabilityStartDate, availabilityEndDate, criteria)
                          }
                        }
                      }
                    }
                """.trimIndent(),
                language = SyntaxLanguage.KOTLIN
            )

            Spacer(Modifier.height(32.dp))

            Button(onClick = { onNavigate("getting-started") }) {
                Text("Get Started")
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate to Getting Started",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .height(60.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Feature",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
