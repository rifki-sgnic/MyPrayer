package com.rfksgn.myprayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
    error = QuranNightError,
    onError = QuranNightOnError,
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

// Quran tab in dark mode: Malam Hangat. Unlisted roles keep DarkColorScheme's values.
private val QuranNightColorScheme = DarkColorScheme.copy(
    primary = QuranNightJade,
    onPrimary = QuranNightBackground,
    primaryContainer = QuranNightContainer,
    onPrimaryContainer = QuranNightInk,
    secondary = QuranNightAmber,
    onSecondary = QuranNightBackground,
    secondaryContainer = QuranNightMist,
    onSecondaryContainer = QuranNightJade,
    tertiary = QuranNightAmber,
    background = QuranNightBackground,
    onBackground = QuranNightInk,
    surface = QuranNightSurface,
    onSurface = QuranNightInk,
    surfaceVariant = QuranNightVariant,
    onSurfaceVariant = QuranNightMuted,
    surfaceTint = QuranNightJade,
    surfaceContainerLowest = QuranNightBackground,
    surfaceContainerLow = Color(0xFF141510),
    surfaceContainer = QuranNightSurface,
    surfaceContainerHigh = QuranNightVariant,
    surfaceContainerHighest = Color(0xFF2C2A22),
    outline = QuranNightBorder,
    outlineVariant = QuranNightBorder
)

private val QuranDayColorScheme = LightColorScheme.copy(tertiary = CypressPrimary)

/**
 * Brass for words. Brass Gold reads at about 2:1 on limestone and the skies, so by day text takes Brass Ink;
 * icons, dots, rings and the plate keep `secondary`. At night Lit Amber already reads, so it is unchanged.
 */
val ColorScheme.brassText: Color
    get() = if (secondary == BrassGold) BrassInk else secondary

/**
 * The Quran tab's palette: Malam Hangat at night, the light scheme by day, whatever the system theme.
 * `tertiary` is the ayah-end medallion and surah-name colour: cypress by day, amber at night.
 */
fun quranColorScheme(night: Boolean): ColorScheme = if (night) QuranNightColorScheme else QuranDayColorScheme

/** Reader colours that have no Material role. Set by [QuranTheme]; screens read them, never branch on the scheme. */
@Immutable
data class QuranReadingColors(
    val latin: Color,      // transliteration line
    val openFill: Color,   // background of the ayah whose actions are open
    val pillEdge: Color    // edge of the "Lanjut ke ayat" pill
)

private val QuranDayReadingColors = QuranReadingColors(
    latin = CypressPrimary,
    openFill = CypressPrimary.copy(alpha = 0.05f),
    pillEdge = LightBorder
)

// Warm ivory, not jade, for the Latin line, so the translation still reads first; the shadow can't show on near-black, so the pill's edge carries it
private val QuranNightReadingColors = QuranReadingColors(
    latin = QuranNightLatin,
    openFill = QuranNightInk.copy(alpha = 0.06f),
    pillEdge = QuranNightInk.copy(alpha = 0.18f)
)

val LocalQuranReadingColors = staticCompositionLocalOf { QuranDayReadingColors }

@Composable
fun QuranTheme(night: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalQuranReadingColors provides if (night) QuranNightReadingColors else QuranDayReadingColors) {
        MaterialTheme(
            colorScheme = quranColorScheme(night),
            typography = MaterialTheme.typography,
            content = content
        )
    }
}

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
