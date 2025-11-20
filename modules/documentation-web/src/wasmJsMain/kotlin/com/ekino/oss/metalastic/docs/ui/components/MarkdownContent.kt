package com.ekino.oss.metalastic.docs.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

@Composable
fun MarkdownContent(content: String) {
    SelectionContainer {
        Markdown(
            content = content,
            colors = DefaultMarkdownColors(
                text = MaterialTheme.colorScheme.onBackground,
                codeText = Color(0xFF86C166), // Green for code in blocks
                codeBackground = Color(0xFF1E1E1E), // Dark background for code blocks
                linkText = MaterialTheme.colorScheme.primary,
                inlineCodeText = Color(0xFFE06C75), // Red for inline code
                inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                tableText = MaterialTheme.colorScheme.onSurface,
                tableBackground = Color.Transparent
            ),
            typography = DefaultMarkdownTypography(
                h1 = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                h2 = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                h3 = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                h4 = MaterialTheme.typography.headlineMedium,
                h5 = MaterialTheme.typography.headlineSmall,
                h6 = MaterialTheme.typography.titleLarge,
                text = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp
                ),
                code = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                quote = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                paragraph = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp
                ),
                ordered = MaterialTheme.typography.bodyLarge,
                bullet = MaterialTheme.typography.bodyLarge,
                list = MaterialTheme.typography.bodyLarge,
                link = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                inlineCode = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        )
    }
}
