package com.ekino.oss.metalastic.docs.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainLayout(
    currentPage: String,
    darkMode: Boolean,
    onNavigate: (String) -> Unit,
    onToggleTheme: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Top bar
        TopBar(
            darkMode = darkMode,
            onToggleTheme = onToggleTheme
        )

        HorizontalDivider()

        // Main content area with sidebar
        Row(Modifier.fillMaxWidth().weight(1f)) {
            // Sidebar
            Sidebar(
                currentPage = currentPage,
                onNavigate = onNavigate,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )

            HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

            // Page content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(32.dp)
            ) {
                content()
            }
        }
    }
}
