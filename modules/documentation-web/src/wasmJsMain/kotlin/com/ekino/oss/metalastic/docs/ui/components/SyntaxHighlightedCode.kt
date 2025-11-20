package com.ekino.oss.metalastic.docs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes

@Composable
fun SyntaxHighlightedCode(
    code: String,
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
    modifier: Modifier = Modifier
) {
    // Detect dark mode by checking if background is dark (luminance < 0.5)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val highlights = remember(code, language, isDark) {
        Highlights.Builder()
            .code(code)
            .theme(SyntaxThemes.darcula(darkMode = isDark))
            .language(language)
            .build()
            .getHighlights()
    }

    val backgroundColor = if (isDark) {
        Color(0xFF1E1E1E)  // Dark background for dark mode
    } else {
        MaterialTheme.colorScheme.surfaceVariant  // Light background for light mode
    }

    val annotatedString = buildAnnotatedString {
        append(code)

        // Apply color highlights - rgb is an Int in format 0x00RRGGBB
        highlights.filterIsInstance<ColorHighlight>().forEach { highlight ->
            val rgbInt = highlight.rgb

            // Extract RGB components (0-255) and normalize to 0.0-1.0
            val red = ((rgbInt shr 16) and 0xFF) / 255f
            val green = ((rgbInt shr 8) and 0xFF) / 255f
            val blue = (rgbInt and 0xFF) / 255f

            addStyle(
                style = SpanStyle(color = Color(red, green, blue)),
                start = highlight.location.start,
                end = highlight.location.end
            )
        }

        // Apply bold highlights
        highlights.filterIsInstance<BoldHighlight>().forEach { highlight ->
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = highlight.location.start,
                end = highlight.location.end
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)  // Max height for scrolling
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        SelectionContainer {
            Text(
                text = annotatedString,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
