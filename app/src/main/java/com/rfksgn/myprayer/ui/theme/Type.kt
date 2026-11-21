package com.rfksgn.myprayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.R

// Scheherazade New (SIL, OFL; license in assets/licenses): Quranic naskh for every Arabic text in the Quran feature
val QuranFontFamily = FontFamily(Font(R.font.scheherazade_new_regular))

/**
 * Material roles the app sets, plus two of its own. Every size is in sp, so all of it follows the system
 * font scale. Unset Material roles keep their defaults.
 */
val Typography = Typography(
    // The one dominant mark on Jadwal and Kiblat: the next prayer's name, or the degrees still to turn.
    // Screens shrink it step by step to 40sp (TextAutoSize) rather than clip it at large font scales.
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 80.sp,
        lineHeight = 0.92.em,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = "tnum"
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * The line under the display mark: the next prayer's time and countdown, or Kiblat's turn guidance.
 * Balanced breaks, so a wrap at large font scales never strands one word.
 */
val Typography.subLine: TextStyle
    get() = SubLine

/** Small tabular readouts beside the display: "Kiblat 295°", "Arah HP 12°", the reminder status. */
val Typography.readout: TextStyle
    get() = Readout

private val SubLine = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    fontFeatureSettings = "tnum",
    lineBreak = LineBreak.Heading
)

private val Readout = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    fontFeatureSettings = "tnum"
)
