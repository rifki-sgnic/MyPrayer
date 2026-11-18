package com.rfksgn.myprayer.presentation.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.presentation.home.compassPointName
import com.rfksgn.myprayer.ui.theme.KaabaBlack
import com.rfksgn.myprayer.ui.theme.SkyStone
import com.rfksgn.myprayer.ui.theme.prayerSkyColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Shortest signed turn from [from] to [to], in -180..180 degrees. Positive means clockwise (turn right). */
internal fun signedTurn(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d < -180f) d += 360f
    if (d > 180f) d -= 360f
    return d
}

private const val AlignedWithinDegrees = 3f
private val PivotBelowStatusBar = 286.dp
private val KaabaDistance = 196.dp
private val ShadowLength = 190.dp

/**
 * Kiblat as Tongkat Istiwa (Rashdul Kiblat): the phone is an upright stick on Home's astrolabe plate.
 * The plate turns with the Qibla bearing, the Ka'bah glows ahead like the sun standing over it, and the
 * phone faces the Qibla when the stick's shadow falls straight down its own dashed line.
 */
@Composable
fun QiblaScreen(
    currentAzimuth: Float,
    qiblaDirection: Float,
    locationName: String,
    nextPrayerName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val signedDiff = signedTurn(currentAzimuth, qiblaDirection)
    val absDiff = abs(signedDiff)
    val isAligned = absDiff <= AlignedWithinDegrees

    LaunchedEffect(isAligned) {
        if (isAligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Unwrapped heading, so the plate never spins the long way round when the sensor crosses north.
    var unwrappedAzimuth by remember { mutableFloatStateOf(currentAzimuth) }
    LaunchedEffect(currentAzimuth) { unwrappedAzimuth += signedTurn(unwrappedAzimuth, currentAzimuth) }
    val azimuth = animateFloatAsState(
        targetValue = unwrappedAzimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "azimuth"
    )

    // Same sky as Home: tinted by the next prayer, secondary text deepened to Sky Stone in light mode
    val baseScheme = MaterialTheme.colorScheme
    val isDark = baseScheme.background.luminance() < 0.5f
    val scheme = remember(baseScheme) { if (isDark) baseScheme else baseScheme.copy(onSurfaceVariant = SkyStone) }
    val (skyTarget, bodyTarget) = prayerSkyColors(nextPrayerName, isDark)
    val sky = animateColorAsState(skyTarget, tween(800), label = "sky")
    val body = animateColorAsState(bodyTarget, tween(800), label = "body")
    val mark = animateColorAsState(if (isAligned) scheme.secondary else scheme.primary, label = "mark")
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    MaterialTheme(colorScheme = scheme) {
        Box(modifier = modifier.fillMaxSize()) {
            IstiwaCanvas(
                rotationDegrees = { qiblaDirection - azimuth.value },
                sky = { sky.value },
                body = { body.value },
                mark = { mark.value },
                aligned = isAligned,
                statusTop = statusTop
            )

            // Location and Qibla bearing
            val bearing = "${"%.1f".format(qiblaDirection)}°"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp)
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        append("Kiblat ")
                        withStyle(SpanStyle(color = scheme.onSurface)) { append(bearing) }
                    },
                    style = ReadoutStyle,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Kiblat $bearing, arah ${compassPointName(qiblaDirection.toDouble())}"
                    }
                )
            }

            // Guidance, on a fade so the shadow runs out under it
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(0f to Color.Transparent, 0.26f to scheme.background))
                    .padding(start = 20.dp, end = 20.dp, top = 64.dp)
            ) {
                Column(
                    modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BasicText(
                        text = if (isAligned) "Lurus" else "${absDiff.toInt()}°",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 0.92.em,
                            letterSpacing = (-0.03).em,
                            fontFeatureSettings = "tnum",
                            color = scheme.onSurface
                        ),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 40.sp, maxFontSize = 80.sp)
                    )
                    if (isAligned) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = scheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bayangan lurus, menghadap kiblat",
                                style = SubLineStyle.copy(fontWeight = FontWeight.SemiBold),
                                color = scheme.onSurface
                            )
                        }
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append("putar ke ")
                                withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Bold)) {
                                    append(if (signedDiff > 0) "kanan" else "kiri")
                                }
                                append(" sampai bayang tongkat lurus")
                            },
                            style = SubLineStyle,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = scheme.outline.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Arah HP ")
                            withStyle(SpanStyle(color = scheme.onSurface)) {
                                append("${((currentAzimuth % 360f + 360f) % 360f).toInt()}°")
                            }
                        },
                        style = ReadoutStyle,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = "Δ ${"%.1f".format(absDiff)}°",
                        style = ReadoutStyle,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private val SubLineStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
private val ReadoutStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")

/**
 * The sky, the turning plate (rings, hour lines, the Qibla axis, the stick's shadow and the Ka'bah) and the
 * fixed parts: the phone's dashed line, its front marker and the stick. State is read in the draw phase only,
 * so the spring animates without recomposing.
 */
@Composable
private fun IstiwaCanvas(
    rotationDegrees: () -> Float,
    sky: () -> Color,
    body: () -> Color,
    mark: () -> Color,
    aligned: Boolean,
    statusTop: Dp
) {
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val brass = MaterialTheme.colorScheme.secondary
    val shade = ink.copy(alpha = if (background.luminance() < 0.5f) 0.2f else 0.3f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .drawWithCache {
                // On short screens the stick moves up so the shadow still clears the guidance panel
                val pivot = Offset(
                    size.width / 2f,
                    minOf((statusTop + PivotBelowStatusBar).toPx(), size.height - 300.dp.toPx())
                )
                val hairline = 1.dp.toPx()
                val reach = 340.dp.toPx()
                val kaabaCenter = Offset(pivot.x, pivot.y - KaabaDistance.toPx())
                val kaabaSize = 18.dp.toPx()
                val shadowLength = ShadowLength.toPx()
                val shadow = Path().apply {
                    val top = 5.3.dp.toPx()
                    val tip = 1.7.dp.toPx()
                    moveTo(pivot.x - top, pivot.y)
                    lineTo(pivot.x + top, pivot.y)
                    lineTo(pivot.x + tip, pivot.y + shadowLength)
                    lineTo(pivot.x - tip, pivot.y + shadowLength)
                    close()
                }
                val shadowBrush = Brush.verticalGradient(listOf(shade, Color.Transparent), pivot.y, pivot.y + shadowLength)
                val axisBrush = Brush.verticalGradient(
                    listOf(brass.copy(alpha = 0.5f), Color.Transparent),
                    kaabaCenter.y + 22.dp.toPx(),
                    pivot.y - 26.dp.toPx()
                )
                val guideTop = (statusTop + 76.dp).toPx()
                val guideBottom = guideTop + 400.dp.toPx()
                val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
                val front = Path().apply {
                    val tipY = (statusTop + 64.dp).toPx()
                    moveTo(pivot.x, tipY)
                    lineTo(pivot.x - 6.dp.toPx(), tipY + 9.dp.toPx())
                    lineTo(pivot.x + 6.dp.toPx(), tipY + 9.dp.toPx())
                    close()
                }

                onDrawBehind {
                    drawRect(background)
                    drawRect(
                        Brush.verticalGradient(listOf(sky(), background), startY = 0f, endY = 300.dp.toPx()),
                        size = size.copy(height = minOf(size.height, 300.dp.toPx()))
                    )

                    val m = mark()
                    if (aligned) {
                        drawLine(m, Offset(pivot.x, guideTop), Offset(pivot.x, guideBottom), strokeWidth = 1.5.dp.toPx())
                    } else {
                        drawLine(
                            m.copy(alpha = 0.55f), Offset(pivot.x, guideTop), Offset(pivot.x, guideBottom),
                            strokeWidth = 1.5.dp.toPx(), pathEffect = dash
                        )
                    }

                    rotate(degrees = rotationDegrees(), pivot = pivot) {
                        // Home's astrolabe plate, centred on the stick: rings every 28dp, hour lines every 15°
                        val line = body().copy(alpha = 0.25f)
                        val mask = Brush.radialGradient(
                            0f to Color.Transparent,
                            (40f / 340f) to Color.Transparent,
                            (46f / 340f) to line,
                            (230f / 340f) to line,
                            1f to Color.Transparent,
                            center = pivot,
                            radius = reach
                        )
                        val spacing = 28.dp.toPx()
                        var r = spacing
                        while (r < reach) {
                            drawCircle(mask, radius = r - hairline / 2, center = pivot, style = Stroke(hairline))
                            r += spacing
                        }
                        for (i in 0 until 24) {
                            val a = Math.toRadians(i * 15.0)
                            drawLine(
                                mask, pivot,
                                Offset(pivot.x + reach * cos(a).toFloat(), pivot.y + reach * sin(a).toFloat()),
                                strokeWidth = hairline
                            )
                        }

                        drawLine(
                            axisBrush,
                            Offset(pivot.x, kaabaCenter.y + 22.dp.toPx()),
                            Offset(pivot.x, pivot.y - 26.dp.toPx()),
                            strokeWidth = hairline
                        )
                        drawPath(shadow, shadowBrush)

                        // The Ka'bah with Home's sun halos, as if the sun stood over it
                        for ((grow, alpha) in listOf(15.dp to 0.08f, 7.dp to 0.18f)) {
                            val s = kaabaSize + grow.toPx() * 2
                            drawRoundRect(
                                brass.copy(alpha = alpha),
                                Offset(kaabaCenter.x - s / 2, kaabaCenter.y - s / 2),
                                Size(s, s),
                                CornerRadius(2.dp.toPx() + grow.toPx())
                            )
                        }
                        val kaabaTopLeft = Offset(kaabaCenter.x - kaabaSize / 2, kaabaCenter.y - kaabaSize / 2)
                        drawRoundRect(KaabaBlack, kaabaTopLeft, Size(kaabaSize, kaabaSize), CornerRadius(2.dp.toPx()))
                        drawRect(brass, kaabaTopLeft + Offset(0f, 4.dp.toPx()), Size(kaabaSize, 2.5.dp.toPx()))
                        drawRoundRect(
                            brass, kaabaTopLeft + Offset(hairline / 2, hairline / 2),
                            Size(kaabaSize - hairline, kaabaSize - hairline), CornerRadius(2.dp.toPx()),
                            style = Stroke(hairline)
                        )
                    }

                    // The phone: its front marker and the stick it stands as
                    drawPath(front, m)
                    drawCircle(m.copy(alpha = 0.18f), radius = 14.dp.toPx(), center = pivot)
                    drawCircle(surface, radius = 8.dp.toPx(), center = pivot)
                    drawCircle(ink, radius = 7.dp.toPx(), center = pivot, style = Stroke(2.dp.toPx()))
                    drawCircle(m, radius = 3.dp.toPx(), center = pivot)
                }
            }
    )
}
