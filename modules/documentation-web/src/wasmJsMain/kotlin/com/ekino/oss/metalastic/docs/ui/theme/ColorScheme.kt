package com.ekino.oss.metalastic.docs.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Official Metalastic brand colors (from LOGO_GUIDELINES.md)
private val SteelBlue = Color(0xFF2563EB) // Primary brand color
private val ElasticsearchOrange = Color(0xFFF59E0B) // Accent
private val MetamodelGray = Color(0xFF6B7280) // Secondary
private val DarkCharcoal = Color(0xFF1F2937) // Dark theme
private val KotlinPurple = Color(0xFF7C3AED) // Optional accent

// Code syntax highlighting
private val CodeGreen = Color(0xFF86C166)
private val CodeRed = Color(0xFFE06C75)
private val CodeBlue = Color(0xFF61AFEF)

// Light theme
val LightColorScheme = lightColorScheme(
    primary = SteelBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = Color(0xFF001A41),

    secondary = MetamodelGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E7EB),
    onSecondaryContainer = Color(0xFF1F2937),

    tertiary = ElasticsearchOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF4C1D00),

    background = Color(0xFFFDFBFF),
    onBackground = DarkCharcoal,

    surface = Color.White,
    onSurface = DarkCharcoal,
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = MetamodelGray,

    outline = MetamodelGray.copy(alpha = 0.5f),
    outlineVariant = Color(0xFFD1D5DB)
)

// Dark theme
val DarkColorScheme = darkColorScheme(
    primary = SteelBlue.copy(alpha = 0.9f),
    onPrimary = Color(0xFF001A41),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBE7FF),

    secondary = Color(0xFF9CA3AF),
    onSecondary = DarkCharcoal,
    secondaryContainer = Color(0xFF374151),
    onSecondaryContainer = Color(0xFFE5E7EB),

    tertiary = ElasticsearchOrange,
    onTertiary = Color(0xFF4C1D00),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFFEDD5),

    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE5E7EB),

    surface = DarkCharcoal,
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFF9CA3AF),

    outline = MetamodelGray,
    outlineVariant = Color(0xFF374151)
)
