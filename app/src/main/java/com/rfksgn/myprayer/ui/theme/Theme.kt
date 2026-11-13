package com.rfksgn.myprayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Every role a component reads is set here; unset roles fall back to Material's baseline purple.
private val DarkColorScheme = darkColorScheme(
    primary = JadePrimary,
    onPrimary = Color(0xFF052E16),
    primaryContainer = JadeContainer,
    onPrimaryContainer = CrispWhite,
    inversePrimary = CypressPrimary,
    secondary = AmberGold,
    onSecondary = Color(0xFF1C1917),
    secondaryContainer = JadeMist,
    onSecondaryContainer = JadePrimary,
    background = DarkBackground,
    onBackground = CrispWhite,
    surface = DarkSurface,
    onSurface = CrispWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SageMuted,
    surfaceTint = JadePrimary,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = Color(0xFF0F1613),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF22302A),
    inverseSurface = CrispWhite,
    inverseOnSurface = Color(0xFF181816),
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val LightColorScheme = lightColorScheme(
    primary = CypressPrimary,
    onPrimary = Color.White,
    primaryContainer = CypressContainer,
    onPrimaryContainer = Color(0xFFF4F1EA),
    inversePrimary = JadePrimary,
    secondary = BrassGold,
    onSecondary = Color.White,
    secondaryContainer = CypressMist,
    onSecondaryContainer = CypressPrimary,
    background = LightBackground,
    onBackground = InkCharcoal,
    surface = LightSurface,
    onSurface = InkCharcoal,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = StoneMuted,
    surfaceTint = CypressPrimary,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = Color(0xFFFAF8F4),
    surfaceContainer = Color(0xFFF3F0EA),
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = Color(0xFFE7E2D8),
    inverseSurface = CypressContainer,
    inverseOnSurface = Color(0xFFF4F1EA),
    outline = LightBorder,
    outlineVariant = LightBorder,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

/**
 * MyPrayer always renders its own palette (DESIGN.md: The Brand Palette Rule).
 * Dynamic Color is intentionally not supported.
 */
@Composable
fun MyPrayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
