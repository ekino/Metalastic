package com.ekino.oss.metalastic.docs

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun main() {
    // Enable new context menu API for text selection
    ComposeFoundationFlags.isNewContextMenuEnabled = true

    ComposeViewport(document.body!!) {
        App()
    }
}
