package com.ekino.oss.metalastic.docs.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(
    currentPage: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Documentation",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NavigationSection(
            title = "Getting Started",
            items = listOf(
                NavItem("home", "Overview"),
                NavItem("getting-started", "Quick Start"),
                NavItem("configuration", "Configuration")
            ),
            currentPage = currentPage,
            onNavigate = onNavigate
        )

        Spacer(Modifier.height(16.dp))

        NavigationSection(
            title = "Core Concepts",
            items = listOf(
                NavItem("field-types", "Field Types"),
                NavItem("query-dsl", "Query DSL"),
            ),
            currentPage = currentPage,
            onNavigate = onNavigate
        )

        Spacer(Modifier.height(16.dp))

        NavigationSection(
            title = "Resources",
            items = listOf(
                NavItem("examples", "Examples"),
            ),
            currentPage = currentPage,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun NavigationSection(
    title: String,
    items: List<NavItem>,
    currentPage: String,
    onNavigate: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        items.forEach { item ->
            NavigationItem(
                item = item,
                isSelected = currentPage == item.id,
                onClick = { onNavigate(item.id) }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = item.label,
        style = MaterialTheme.typography.bodyMedium,
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

data class NavItem(
    val id: String,
    val label: String
)
