package com.rfksgn.myprayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JadePrimary,
    onPrimary = Color(0xFF052E16),
    primaryContainer = JadeContainer,
    onPrimaryContainer = CrispWhite,
    secondary = AmberGold,
    onSecondary = Color(0xFF1C1917),
    background = DarkBackground,
    onBackground = CrispWhite,
    surface = DarkSurface,
    onSurface = CrispWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SageMuted,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CypressPrimary,
    onPrimary = Color.White,
    primaryContainer = CypressContainer,
    onPrimaryContainer = Color(0xFFF4F1EA),
    secondary = BrassGold,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = InkCharcoal,
    surface = LightSurface,
    onSurface = InkCharcoal,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = StoneMuted,
    outline = LightBorder
)

@Composable
fun MyPrayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}