package com.ekino.oss.metalastic.docs.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NotFoundPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "404",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Page not found",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
