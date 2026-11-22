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
 * fades in and rises 12dp over 280ms, starting 60ms in and 28ms after the block above it.
 */
private const val CascadeStartMs = 60f
private const val CascadeStepMs = 28f
private const val CascadeItemMs = 280f
private const val CascadeItems = 10
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** The whole cascade, for the clock that drives [LocalLaunchProgress] from 0 to 1. */
const val LaunchCascadeMs = (CascadeStartMs + (CascadeItems - 1) * CascadeStepMs + CascadeItemMs).toInt()

/** Launch progress from 0 to 1; read in the draw phase only. Settled (1) whenever no launch is playing. */
val LocalLaunchProgress = staticCompositionLocalOf<() -> Float> { { 1f } }

/** Places this block in the launch cascade at [index] (0 is the top of Jadwal). */
@Composable
fun Modifier.launchEntrance(index: Int): Modifier {
    val progress = LocalLaunchProgress.current
    val rise = with(LocalDensity.current) { 12.dp.toPx() }
    return graphicsLayer {
        val elapsed = progress() * LaunchCascadeMs - (CascadeStartMs + index * CascadeStepMs)
        val e = EmphasizedDecelerate.transform((elapsed / CascadeItemMs).coerceIn(0f, 1f))
        alpha = e
        translationY = (1f - e) * rise
    }
}
