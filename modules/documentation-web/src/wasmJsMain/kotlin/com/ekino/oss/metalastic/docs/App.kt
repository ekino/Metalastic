package com.ekino.oss.metalastic.docs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ekino.oss.metalastic.docs.ui.layout.MainLayout
import com.ekino.oss.metalastic.docs.ui.pages.ConfigurationPage
import com.ekino.oss.metalastic.docs.ui.pages.ExamplesPage
import com.ekino.oss.metalastic.docs.ui.pages.FieldTypesPage
import com.ekino.oss.metalastic.docs.ui.pages.GettingStartedPage
import com.ekino.oss.metalastic.docs.ui.pages.HomePage
import com.ekino.oss.metalastic.docs.ui.pages.NotFoundPage
import com.ekino.oss.metalastic.docs.ui.pages.QueryDslPage
import com.ekino.oss.metalastic.docs.ui.theme.DarkColorScheme
import com.ekino.oss.metalastic.docs.ui.theme.LightColorScheme

@Composable
fun App() {
    var darkMode by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf("home") }

    MaterialTheme(
        colorScheme = if (darkMode) DarkColorScheme else LightColorScheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainLayout(
                currentPage = currentPage,
                darkMode = darkMode,
                onNavigate = { currentPage = it },
                onToggleTheme = { darkMode = !darkMode }
            ) {
                when (currentPage) {
                    "home" -> HomePage(onNavigate = { currentPage = it })
                    "getting-started" -> GettingStartedPage()
                    "configuration" -> ConfigurationPage()
                    "field-types" -> FieldTypesPage()
                    "query-dsl" -> QueryDslPage()
                    "examples" -> ExamplesPage()
                    else -> NotFoundPage()
                }
            }
        }
    }
}
