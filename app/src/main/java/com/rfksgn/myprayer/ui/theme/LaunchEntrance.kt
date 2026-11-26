package com.rfksgn.myprayer.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/*
 * The launch cascade (DESIGN.md: Launch). As the splash fades, Jadwal settles in from the top: each block
 * fades in and rises over 220ms. Indices: 0 header, 1 location card (first run), 2 next prayer,
 * 3 schedule label, 4-9 the six rows. When the Al-Kahf banner shows it takes 3 and everything below shifts by one.
 */
private const val CascadeItemMs = 220f
// When each block starts (ms). Gaps shrink down the page so the group reads as one wave, not a metronome.
private val CascadeDelayMs = floatArrayOf(0f, 30f, 50f, 70f, 80f, 88f, 94f, 98f, 100f, 100f, 100f)
// The next prayer leads, so it travels furthest; everything else only settles
private const val LeadIndex = 2
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** The whole cascade, for the clock that drives [LocalLaunchProgress] from 0 to 1. */
val LaunchCascadeMs = (CascadeDelayMs.last() + CascadeItemMs).toInt()

/** Launch progress from 0 to 1; read in the draw phase only. Settled (1) whenever no launch is playing. */
val LocalLaunchProgress = staticCompositionLocalOf<() -> Float> { { 1f } }

/** Places this block in the launch cascade at [index] (see the index list above). */
@Composable
fun Modifier.launchEntrance(index: Int): Modifier {
    val progress = LocalLaunchProgress.current
    val rise = with(LocalDensity.current) { (if (index == LeadIndex) 12.dp else 6.dp).toPx() }
    // A decorative cascade must never crash the screen: extra blocks just join the last beat
    val delay = CascadeDelayMs.getOrElse(index) { CascadeDelayMs.last() }
    return graphicsLayer {
        val elapsed = progress() * LaunchCascadeMs - delay
        val e = EmphasizedDecelerate.transform((elapsed / CascadeItemMs).coerceIn(0f, 1f))
        alpha = e
        translationY = (1f - e) * rise
    }
}
