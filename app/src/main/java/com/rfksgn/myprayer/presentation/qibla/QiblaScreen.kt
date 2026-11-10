package com.rfksgn.myprayer.presentation.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    currentAzimuth: Float,
    qiblaDirection: Float,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Selisih sudut terdekat (-180..180)
    val signedDiff = remember(currentAzimuth, qiblaDirection) {
        var diff = (qiblaDirection - currentAzimuth) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        diff
    }
    val absDiff = abs(signedDiff)
    val isAligned = absDiff <= 3.0f

    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "azimuth_rot"
    )

    val instrumentAccent by animateColorAsState(
        targetValue = if (isAligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        label = "accent_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP HEADER & DUAL INSTRUMENT HUD
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "INSTRUMEN ARAH KIBLAT",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Dual Metric Readout Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Phone Heading
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ARAH HP (UTARA)",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentAzimuth.toInt()}°",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxHeight()
                    )

                    // Right: Target Qibla Bearing
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TITIK KIBLAT",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(qiblaDirection)}°",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                                color = instrumentAccent
                            )
                        )
                    }
                }
            }
        }

        // 2. ASTROLABE PRECISION COMPASS DIAL
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(330.dp)
                .padding(8.dp)
        ) {
            AstrolabeCompassCanvas(
                azimuth = animatedAzimuth,
                qiblaAngle = qiblaDirection,
                isAligned = isAligned,
                accentColor = instrumentAccent,
                goldColor = MaterialTheme.colorScheme.secondary,
                surfaceColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline,
                textColor = MaterialTheme.colorScheme.onSurface,
                mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 3. TACTILE ALIGNMENT STATUS BAR
        val guidanceText = when {
            isAligned -> "Kiblat Terkunci • Posisi Lurus"
            signedDiff > 0 -> "Putar HP ke Kanan ${absDiff.toInt()}°"
            else -> "Putar HP ke Kiri ${absDiff.toInt()}°"
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isAligned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                width = if (isAligned) 1.5.dp else 1.dp,
                color = if (isAligned) instrumentAccent else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(instrumentAccent)
                    )
                    Text(
                        text = guidanceText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAligned) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Text(
                    text = "Δ ${"%.1f".format(absDiff)}°",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = if (isAligned) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun AstrolabeCompassCanvas(
    azimuth: Float,
    qiblaAngle: Float,
    isAligned: Boolean,
    accentColor: Color,
    goldColor: Color,
    surfaceColor: Color,
    borderColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 12.dp.toPx()
        val dialRadius = outerRadius - 10.dp.toPx()
        val innerReticleRadius = dialRadius * 0.42f

        // A. STATIC TOP LUBBER POINTER (Penanda Moncong HP di 0 Derajat Atas)
        val lubberPath = Path().apply {
            moveTo(center.x, 2.dp.toPx())
            lineTo(center.x - 6.dp.toPx(), -8.dp.toPx())
            lineTo(center.x + 6.dp.toPx(), -8.dp.toPx())
            close()
        }
        drawPath(path = lubberPath, color = accentColor)

        // B. OUTER INSTRUMENT CASE & BEZEL
        drawCircle(
            color = surfaceColor,
            radius = outerRadius,
            center = center
        )
        drawCircle(
            color = if (isAligned) accentColor else borderColor,
            radius = outerRadius,
            center = center,
            style = Stroke(width = if (isAligned) 2.5.dp.toPx() else 1.5.dp.toPx())
        )

        // C. ROTATING COMPASS ROSE (-azimuth)
        rotate(degrees = -azimuth, pivot = center) {
            // 1. 180 Fine Instrument Ticks (Setiap 2 derajat)
            for (deg in 0 until 360 step 2) {
                val isCardinal = deg % 90 == 0
                val isThirty = deg % 30 == 0
                val isTen = deg % 10 == 0

                val tickLength = when {
                    isCardinal -> 14.dp.toPx()
                    isThirty -> 10.dp.toPx()
                    isTen -> 6.dp.toPx()
                    else -> 3.dp.toPx()
                }

                val strokeWidth = when {
                    isCardinal -> 2.dp.toPx()
                    isThirty -> 1.5.dp.toPx()
                    else -> 1.dp.toPx()
                }

                val tickColor = when {
                    deg == 0 -> Color(0xFFDC2626) // Utara Merah
                    isThirty -> textColor.copy(alpha = 0.75f)
                    isTen -> mutedTextColor.copy(alpha = 0.45f)
                    else -> mutedTextColor.copy(alpha = 0.22f)
                }

                val rad = Math.toRadians((deg - 90).toDouble())
                val startR = dialRadius
                val endR = dialRadius - tickLength

                drawLine(
                    color = tickColor,
                    start = Offset(
                        center.x + startR * cos(rad).toFloat(),
                        center.y + startR * sin(rad).toFloat()
                    ),
                    end = Offset(
                        center.x + endR * cos(rad).toFloat(),
                        center.y + endR * sin(rad).toFloat()
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 2. Degree Numerals (Setiap 30 derajat, kecuali titik Cardinal)
            for (deg in 0 until 360 step 30) {
                if (deg % 90 != 0) {
                    val rad = Math.toRadians((deg - 90).toDouble())
                    val numRadius = dialRadius - 22.dp.toPx()
                    val measured = textMeasurer.measure(
                        text = "$deg",
                        style = TextStyle(
                            color = mutedTextColor.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFeatureSettings = "tnum"
                        )
                    )
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            center.x + numRadius * cos(rad).toFloat() - measured.size.width / 2f,
                            center.y + numRadius * sin(rad).toFloat() - measured.size.height / 2f
                        )
                    )
                }
            }

            // 3. Cardinal Letters (U, T, S, B)
            val cardinals = listOf(0 to "U", 90 to "T", 180 to "S", 270 to "B")
            cardinals.forEach { (deg, label) ->
                val rad = Math.toRadians((deg - 90).toDouble())
                val charRadius = dialRadius - 26.dp.toPx()
                val measured = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = if (deg == 0) Color(0xFFDC2626) else textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        center.x + charRadius * cos(rad).toFloat() - measured.size.width / 2f,
                        center.y + charRadius * sin(rad).toFloat() - measured.size.height / 2f
                    )
                )
            }

            // 4. QIBLA POINTER & KAABA EMBLEM (Pada sudut qiblaAngle)
            rotate(degrees = qiblaAngle, pivot = center) {
                val pointerTipY = center.y - dialRadius + 2.dp.toPx()
                val kaabaCenterY = center.y - dialRadius + 46.dp.toPx()

                // Garis poros dari titik tengah menuju Ka'bah
                drawLine(
                    color = accentColor,
                    start = Offset(center.x, center.y + innerReticleRadius * 0.5f),
                    end = Offset(center.x, pointerTipY + 8.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Ujung Panah Presisi di Tepi Skala Kiblat
                val arrowHead = Path().apply {
                    moveTo(center.x, pointerTipY)
                    lineTo(center.x - 6.dp.toPx(), pointerTipY + 12.dp.toPx())
                    lineTo(center.x + 6.dp.toPx(), pointerTipY + 12.dp.toPx())
                    close()
                }
                drawPath(path = arrowHead, color = accentColor)

                // Ikon Minimalis Ka'bah (Kotak Obsidian + Sabuk Emas Kiswah)
                val boxSize = 18.dp.toPx()
                val boxTopLeft = Offset(center.x - boxSize / 2f, kaabaCenterY - boxSize / 2f)

                // Lingkaran halo di belakang Ka'bah
                drawCircle(
                    color = surfaceColor,
                    radius = 14.dp.toPx(),
                    center = Offset(center.x, kaabaCenterY)
                )
                drawCircle(
                    color = goldColor,
                    radius = 14.dp.toPx(),
                    center = Offset(center.x, kaabaCenterY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Bodi Ka'bah
                drawRoundRect(
                    color = Color(0xFF141413),
                    topLeft = boxTopLeft,
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                // Pita Emas Kiswah
                drawLine(
                    color = goldColor,
                    start = Offset(boxTopLeft.x + 2.dp.toPx(), boxTopLeft.y + boxSize * 0.32f),
                    end = Offset(boxTopLeft.x + boxSize - 2.dp.toPx(), boxTopLeft.y + boxSize * 0.32f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // D. STATIC ASTROLABE INNER RETICLE & CROSSHAIR
        drawCircle(
            color = borderColor,
            radius = innerReticleRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Garis bidik salib halus (Crosshair)
        val crossLen = innerReticleRadius - 8.dp.toPx()
        drawLine(
            color = borderColor.copy(alpha = 0.7f),
            start = Offset(center.x - crossLen, center.y),
            end = Offset(center.x + crossLen, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = borderColor.copy(alpha = 0.7f),
            start = Offset(center.x, center.y - crossLen),
            end = Offset(center.x, center.y + crossLen),
            strokeWidth = 1.dp.toPx()
        )

        // Titik Poros Tengah
        drawCircle(
            color = surfaceColor,
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = accentColor,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}