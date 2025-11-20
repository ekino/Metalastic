package com.ekino.oss.metalastic.docs.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ekino.oss.documentation_web.generated.resources.Res

@Composable
fun MarkdownPage(resourcePath: String) {
    var content by remember { mutableStateOf("Loading...") }

    LaunchedEffect(resourcePath) {
        try {
            val bytes = Res.readBytes(resourcePath)
            content = bytes.decodeToString()
        } catch (e: Exception) {
            content = "Error loading content: ${e.message}"
        }
    }

    MarkdownContent(content = content)
}
