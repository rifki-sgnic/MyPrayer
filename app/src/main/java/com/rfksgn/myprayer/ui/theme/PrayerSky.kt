package com.rfksgn.myprayer.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Time-of-day sky for the schedule screen, keyed to the NEXT prayer (DESIGN.md: Astrolabe Plate).
 * [sky] tints the top of the page, [body] is the sun or moon and the colour of the plate's lines.
 * [sunX] is a fraction of the width, [sunUp] how high the sun stands (0 = horizon, 1 = zenith).
 */
private data class PrayerSky(val sky: Color, val body: Color, val sunX: Float, val sunUp: Float)

private fun prayerSky(nextPrayer: String, dark: Boolean): PrayerSky = when (nextPrayer) {
    "Subuh" -> if (dark) PrayerSky(Color(0xFF2A2231), Color(0xFFEAB308), 0.12f, 0f)
    else PrayerSky(Color(0xFFE6DAE6), Color(0xFFC59B27), 0.12f, 0f)
    // Terbit (sunrise) is not a prayer but can be "next": dawn sky, sun just on the horizon
    "Terbit" -> if (dark) PrayerSky(Color(0xFF2A2231), Color(0xFFEAB308), 0.2f, 0.08f)
    else PrayerSky(Color(0xFFE6DAE6), Color(0xFFC59B27), 0.2f, 0.08f)
    "Dzuhur" -> if (dark) PrayerSky(Color(0xFF13262D), Color(0xFFEAB308), 0.5f, 1f)
    else PrayerSky(Color(0xFFDCE7EA), Color(0xFFC59B27), 0.5f, 1f)
    "Ashar" -> if (dark) PrayerSky(Color(0xFF2A2413), Color(0xFFEAB308), 0.7f, 0.75f)
    else PrayerSky(Color(0xFFEFE1C3), Color(0xFFC59B27), 0.7f, 0.75f)
    "Maghrib" -> if (dark) PrayerSky(Color(0xFF321E16), Color(0xFFE08A4A), 0.9f, 0.2f)
    else PrayerSky(Color(0xFFEFD3C3), Color(0xFFC2702F), 0.9f, 0.2f)
    // Isya: night, a pale moon
    else -> if (dark) PrayerSky(Color(0xFF111A2A), Color(0xFFD9DEE6), 0.28f, 0.85f)
    else PrayerSky(Color(0xFFD8DDE6), Color(0xFF8A93A6), 0.28f, 0.85f)
}

/** Secondary text on the tinted sky: Stone deepened so it keeps ≥4.5:1 on every phase in light mode. */
val SkyStone = Color(0xFF5F5C55)

/**
 * Paints the page background, a sky tint fading out by 300dp, and an astrolabe plate (altitude rings every
 * 28dp, hour lines every 15°) centred off-screen top right, with a marker where the sun or moon stands.
 * Apply after verticalScroll so it scrolls with the content.
 */
@Composable
fun Modifier.prayerSky(nextPrayer: String): Modifier {
    val background = MaterialTheme.colorScheme.background
    val target = prayerSky(nextPrayer, dark = background.luminance() < 0.5f)
    val spec = tween<Float>(800)
    val sky by animateColorAsState(target.sky, tween(800), label = "sky")
    val body by animateColorAsState(target.body, tween(800), label = "body")
    val sunX by animateFloatAsState(target.sunX, spec, label = "sunX")
    val sunUp by animateFloatAsState(target.sunUp, spec, label = "sunUp")

    return drawBehind {
        drawRect(background)
        drawRect(
            Brush.verticalGradient(listOf(sky, background), startY = 0f, endY = 300.dp.toPx()),
            size = size.copy(height = minOf(size.height, 300.dp.toPx()))
        )

        // The plate fades in from 40dp, holds to 220dp, and is gone by 360dp from its centre
        val center = Offset(size.width - 60.dp.toPx(), 80.dp.toPx())
        val reach = 360.dp.toPx()
        val line = body.copy(alpha = 0.25f)
        val mask = Brush.radialGradient(
            0f to Color.Transparent,
            (40f / 360f) to Color.Transparent,
            (46f / 360f) to line,
            (220f / 360f) to line,
            1f to Color.Transparent,
            center = center,
            radius = reach
        )
        val hairline = 1.dp.toPx()
        val spacing = 28.dp.toPx()
        var r = spacing
        while (r < reach) {
            drawCircle(mask, radius = r - hairline / 2, center = center, style = Stroke(hairline))
            r += spacing
        }
        for (i in 0 until 24) {
            val a = Math.toRadians(i * 15.0)
            drawLine(
                mask,
                start = center,
                end = Offset(center.x + reach * cos(a).toFloat(), center.y + reach * sin(a).toFloat()),
                strokeWidth = hairline
            )
        }

        // Sun or moon: a 12dp disc with two soft halos. It travels in the free band between the header
        // and the prayer name (160dp at the horizon, 128dp at the zenith) so it never shows through the text
        val sun = Offset(size.width * sunX, (160f - sunUp * 32f).dp.toPx())
        drawCircle(body.copy(alpha = 0.08f), radius = 17.dp.toPx(), center = sun)
        drawCircle(body.copy(alpha = 0.18f), radius = 11.dp.toPx(), center = sun)
        drawCircle(body, radius = 6.dp.toPx(), center = sun)
    }
}
