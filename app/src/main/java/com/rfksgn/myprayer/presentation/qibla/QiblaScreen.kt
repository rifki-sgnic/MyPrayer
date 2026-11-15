package com.rfksgn.myprayer.presentation.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.presentation.home.compassPointName
import com.rfksgn.myprayer.ui.theme.CarpetGold
import com.rfksgn.myprayer.ui.theme.CarpetIvory
import com.rfksgn.myprayer.ui.theme.KaabaBlack
import com.rfksgn.myprayer.ui.theme.addPrayerArch
import com.rfksgn.myprayer.ui.theme.carpetColor
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot

/** Shortest signed turn from [from] to [to], in -180..180 degrees. Positive means clockwise (turn right). */
internal fun signedTurn(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d < -180f) d += 360f
    if (d > 180f) d -= 360f
    return d
}

private const val AlignedWithinDegrees = 3f
private const val YouLineFraction = 0.49f
private val RowSpacing = 56.dp
private val ArchColumn = 48.dp
private val KaabaDistance = 250.dp

private val TurnStyle = TextStyle(
    fontSize = 72.sp,
    lineHeight = 66.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.04).em,
    fontFeatureSettings = "tnum"
)

/**
 * Kiblat as a mosque carpet: the prayer rows (shaf) turn with the phone, and the
 * phone faces the qibla when the rows run straight across the fixed line.
 */
@Composable
fun QiblaScreen(
    currentAzimuth: Float,
    qiblaDirection: Float,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val signedDiff = signedTurn(currentAzimuth, qiblaDirection)
    val absDiff = abs(signedDiff)
    val isAligned = absDiff <= AlignedWithinDegrees

    LaunchedEffect(isAligned) {
        if (isAligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Unwrapped heading, so the carpet never spins the long way round when the sensor crosses north.
    var unwrappedAzimuth by remember { mutableFloatStateOf(currentAzimuth) }
    LaunchedEffect(currentAzimuth) { unwrappedAzimuth += signedTurn(unwrappedAzimuth, currentAzimuth) }
    val azimuth = animateFloatAsState(
        targetValue = unwrappedAzimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "azimuth"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val carpet = carpetColor(isDark)
    val carpetDeep = lerp(carpet, Color.Black, 0.25f)
    val rowColor = animateColorAsState(
        if (isAligned) CarpetGold.copy(alpha = 0.85f) else CarpetIvory.copy(alpha = if (isDark) 0.38f else 0.5f),
        label = "row"
    )
    val motifColor = animateColorAsState(
        if (isAligned) CarpetGold.copy(alpha = 0.3f) else CarpetIvory.copy(alpha = if (isDark) 0.14f else 0.2f),
        label = "motif"
    )
    val youColor = animateColorAsState(
        if (isAligned) CarpetGold else CarpetIvory.copy(alpha = 0.7f),
        label = "you"
    )

    Box(modifier = modifier.fillMaxSize().background(carpet)) {
        CarpetCanvas(
            rotationDegrees = { qiblaDirection - azimuth.value },
            rowColor = { rowColor.value },
            motifColor = { motifColor.value },
            youColor = { youColor.value }
        )

        // Location and Qibla bearing
        val bearing = "${"%.1f".format(qiblaDirection)}°"
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(CircleShape)
                .background(carpetDeep)
                .heightIn(min = 36.dp)
                .padding(horizontal = 14.dp)
                .clearAndSetSemantics {
                    contentDescription = "$locationName, kiblat $bearing, arah ${compassPointName(qiblaDirection.toDouble())}"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$locationName · Kiblat",
                style = MaterialTheme.typography.labelLarge,
                color = CarpetIvory.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = bearing,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                color = CarpetIvory
            )
        }

        // Guidance
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.34f to carpetDeep, 1f to carpetDeep))
                .padding(start = 20.dp, end = 20.dp, top = 56.dp)
        ) {
            Column(modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
                Text(
                    text = if (isAligned) "Lurus" else "${absDiff.toInt()}°",
                    style = TurnStyle,
                    color = if (isAligned) CarpetGold else CarpetIvory
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isAligned) {
                        buildAnnotatedString { append("Shaf sudah lurus, menghadap kiblat") }
                    } else {
                        buildAnnotatedString {
                            append("Putar ke ")
                            withStyle(SpanStyle(color = CarpetIvory, fontWeight = FontWeight.ExtraBold)) {
                                append(if (signedDiff > 0) "kanan" else "kiri")
                            }
                            append(" sampai shaf lurus")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
                    color = CarpetIvory.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = CarpetIvory.copy(alpha = 0.22f))
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val readout = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
                Text(
                    text = buildAnnotatedString {
                        append("Arah HP ")
                        withStyle(SpanStyle(color = CarpetIvory, fontWeight = FontWeight.Bold)) {
                            append("${((currentAzimuth % 360f + 360f) % 360f).toInt()}°")
                        }
                    },
                    style = readout,
                    color = CarpetIvory.copy(alpha = 0.72f)
                )
                Text(
                    text = "Δ ${"%.1f".format(absDiff)}°",
                    style = readout,
                    color = CarpetIvory.copy(alpha = 0.72f)
                )
            }
        }
    }
}

/**
 * The carpet: arch prayer-spaces and shaf lines, rotated so the carpet's front faces the Qibla,
 * with the Ka'bah marker ahead and the phone's own line fixed across the screen.
 * State is read in the draw phase only, so the spring animates without recomposing.
 */
@Composable
private fun CarpetCanvas(
    rotationDegrees: () -> Float,
    rowColor: () -> Color,
    motifColor: () -> Color,
    youColor: () -> Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .drawWithCache {
                val row = RowSpacing.toPx()
                val col = ArchColumn.toPx()
                val pivot = Offset(size.width / 2f, size.height * YouLineFraction)
                val reach = hypot(size.width, size.height)
                val rows = ceil(reach / row).toInt()
                val cols = ceil(reach / col).toInt()

                // One arch per prayer space, its base sitting on the shaf line.
                val arches = Path().apply {
                    for (r in -rows..rows) {
                        for (c in -cols..cols) {
                            addPrayerArch(pivot.x + c * col - col / 2f, pivot.y + r * row - row, col, row)
                        }
                    }
                }
                val archStroke = Stroke(width = 1.4.dp.toPx())
                val lineWidth = 2.dp.toPx()

                val kaabaSize = 30.dp.toPx()
                val kaabaTopLeft = Offset(pivot.x - kaabaSize / 2f, pivot.y - KaabaDistance.toPx() - kaabaSize / 2f)
                val pointer = Path().apply {
                    val tipY = kaabaTopLeft.y - 14.dp.toPx()
                    moveTo(pivot.x, tipY)
                    lineTo(pivot.x - 6.dp.toPx(), tipY + 9.dp.toPx())
                    lineTo(pivot.x + 6.dp.toPx(), tipY + 9.dp.toPx())
                    close()
                }

                onDrawBehind {
                    rotate(degrees = rotationDegrees(), pivot = pivot) {
                        drawPath(arches, color = motifColor(), style = archStroke)
                        val rowLine = rowColor()
                        for (r in -rows..rows) {
                            val y = pivot.y + r * row
                            drawLine(rowLine, Offset(pivot.x - reach, y), Offset(pivot.x + reach, y), strokeWidth = lineWidth)
                        }
                        drawPath(pointer, color = CarpetGold)
                        drawRoundRect(KaabaBlack, kaabaTopLeft, Size(kaabaSize, kaabaSize), CornerRadius(3.dp.toPx()))
                        drawRect(
                            CarpetGold,
                            Offset(kaabaTopLeft.x, kaabaTopLeft.y + kaabaSize * 0.2f),
                            Size(kaabaSize, kaabaSize * 0.125f)
                        )
                        drawRoundRect(
                            CarpetGold, kaabaTopLeft, Size(kaabaSize, kaabaSize), CornerRadius(3.dp.toPx()),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    }

                    // The phone's own line: aligned when the shaf runs along it.
                    val you = youColor()
                    drawLine(you, Offset(0f, pivot.y), Offset(size.width, pivot.y), strokeWidth = lineWidth)
                    drawCircle(you.copy(alpha = you.alpha * 0.25f), radius = 12.dp.toPx(), center = pivot)
                    drawCircle(you, radius = 7.dp.toPx(), center = pivot)
                }
            }
    )
}
