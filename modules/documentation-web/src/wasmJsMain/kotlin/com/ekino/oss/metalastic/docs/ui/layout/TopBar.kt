package com.ekino.oss.metalastic.docs.ui.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import com.ekino.oss.documentation_web.generated.resources.Res
import com.ekino.oss.documentation_web.generated.resources.metalastic_logo

@Composable
fun TopBar(
    darkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.metalastic_logo),
                contentDescription = "Metalastic Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Metalastic",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
