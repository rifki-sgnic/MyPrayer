package com.rfksgn.myprayer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp

/** The mosque-carpet red for the current scheme: dark mode darkens it 45% toward black. Shared by Kiblat and Setelan. */
fun carpetColor(dark: Boolean): Color = if (dark) lerp(CarpetRed, Color.Black, 0.45f) else CarpetRed

/**
 * Adds one prayer-space arch to the path, drawn in a [width] x [height] tile whose base is the tile's bottom edge
 * (48x56 design units: sides at 8 and 40, apex at 3).
 */
fun Path.addPrayerArch(left: Float, top: Float, width: Float, height: Float) {
    val ux = width / 48f
    val uy = height / 56f
    moveTo(left + 8 * ux, top + 56 * uy)
    lineTo(left + 8 * ux, top + 26 * uy)
    cubicTo(left + 8 * ux, top + 14 * uy, left + 18 * ux, top + 8 * uy, left + 24 * ux, top + 3 * uy)
    cubicTo(left + 30 * ux, top + 8 * uy, left + 40 * ux, top + 14 * uy, left + 40 * ux, top + 26 * uy)
    lineTo(left + 40 * ux, top + 56 * uy)
}
