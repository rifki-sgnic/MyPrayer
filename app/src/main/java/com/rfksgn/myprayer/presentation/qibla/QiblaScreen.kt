package com.rfksgn.myprayer.presentation.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.StayCurrentPortrait
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.sensor.CompassReading
import com.rfksgn.myprayer.presentation.home.LocationPill
import com.rfksgn.myprayer.presentation.home.compassPointName
import com.rfksgn.myprayer.presentation.home.localZoneLabel
import com.rfksgn.myprayer.ui.theme.KaabaBlack
import com.rfksgn.myprayer.ui.theme.SkyStone
import com.rfksgn.myprayer.ui.theme.prayerSkyColors
import com.rfksgn.myprayer.ui.theme.readout
import com.rfksgn.myprayer.ui.theme.subLine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

/** Shortest signed turn from [from] to [to], in -180..180 degrees. Positive means clockwise (turn right). */
internal fun signedTurn(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d < -180f) d += 360f
    if (d > 180f) d -= 360f
    return d
}

private const val AlignedWithinDegrees = 3f
private const val ReleaseBeyondDegrees = 5f
private val PivotBelowStatusBar = 286.dp
private val KaabaDistance = 196.dp
private val ShadowLength = 190.dp
/** A compass that exists but sends nothing for this long is treated as missing. */
private const val SilentCompassMillis = 4000L

/**
 * Kiblat as Tongkat Istiwa (Rashdul Kiblat): the phone is an upright stick on Home's astrolabe plate.
 * The plate turns with the Qibla bearing, the Ka'bah glows ahead like the sun standing over it, and the
 * phone faces the Qibla when the stick's shadow falls straight down its own dashed line.
 *
 * Without a compass the plate stands still as a diagram of Rashdul Kiblat, the Ka'bah ahead and the shadow on
 * its line, with the bearing and the next day the real sun draws that line. [reading] is null until the
 * first heading arrives; an uncalibrated one turns the plate but never claims the lock.
 */
@Composable
fun QiblaScreen(
    reading: CompassReading?,
    hasCompass: Boolean,
    qiblaDirection: Float,
    latitude: Double,
    longitude: Double,
    locationName: String,
    isDefaultLocation: Boolean,
    isUpdatingLocation: Boolean,
    onRefreshLocation: () -> Unit,
    nextPrayerName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    var silent by remember { mutableStateOf(false) }
    LaunchedEffect(reading == null) {
        if (reading == null) {
            delay(SilentCompassMillis)
            silent = true
        }
    }
    val noCompass = !hasCompass || (reading == null && silent)
    val waiting = reading == null && !noCompass
    val calibrating = reading?.reliable == false

    val reliable = reading?.reliable == true

    // Unwrapped heading, so the plate never spins the long way round when the sensor crosses north.
    // Until a reading arrives the plate rests with the Ka'bah ahead, then turns into place.
    var unwrappedAzimuth by remember { mutableFloatStateOf(reading?.azimuth ?: qiblaDirection) }
    LaunchedEffect(reading?.azimuth) {
        reading?.let { unwrappedAzimuth += signedTurn(unwrappedAzimuth, it.azimuth) }
    }
    val azimuth = animateFloatAsState(
        targetValue = unwrappedAzimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "azimuth"
    )

    // The numbers and the lock read the same smoothed heading as the plate, so nothing jitters ahead of the
    // picture; derived state recomposes only when a shown value changes, not every animation frame.
    val turn by remember(qiblaDirection) {
        derivedStateOf { (signedTurn(azimuth.value, qiblaDirection) * 10f).roundToInt() / 10f }
    }
    val shownHeading by remember { derivedStateOf { ((azimuth.value % 360f + 360f) % 360f).roundToInt() % 360 } }
    val signedDiff = if (reading == null) 0f else turn
    val absDiff = abs(signedDiff)

    // Lock within 3°, let go only past 5°: noise at the edge never flickers "Lurus" or buzzes twice
    var isAligned by remember { mutableStateOf(false) }
    LaunchedEffect(reliable, qiblaDirection) {
        snapshotFlow { abs(signedTurn(azimuth.value, qiblaDirection)) }.collect { off ->
            isAligned = reliable && off <= if (isAligned) ReleaseBeyondDegrees else AlignedWithinDegrees
        }
    }
    LaunchedEffect(isAligned) {
        if (isAligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Same sky as Home: tinted by the next prayer, secondary text deepened to Sky Stone in light mode
    val baseScheme = MaterialTheme.colorScheme
    val isDark = baseScheme.background.luminance() < 0.5f
    val scheme = remember(baseScheme) { if (isDark) baseScheme else baseScheme.copy(onSurfaceVariant = SkyStone) }
    val (skyTarget, bodyTarget) = prayerSkyColors(nextPrayerName, isDark)
    // Keyed on the theme: a theme flip snaps the sky with the background; only a new prayer fades it
    val (sky, body) = key(isDark) {
        animateColorAsState(skyTarget, tween(800), label = "sky") to animateColorAsState(bodyTarget, tween(800), label = "body")
    }
    val mark = animateColorAsState(if (isAligned) scheme.secondary else scheme.primary, label = "mark")
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    MaterialTheme(colorScheme = scheme) {
        Box(modifier = modifier.fillMaxSize()) {
            IstiwaCanvas(
                rotationDegrees = { if (reading == null) 0f else qiblaDirection - azimuth.value },
                sky = { sky.value },
                body = { body.value },
                mark = { mark.value },
                aligned = isAligned,
                phone = reading != null,
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
                // Home's location link: the one place to fix the location this bearing is for
                Box(Modifier.weight(1f)) {
                    LocationPill(locationName, isUpdatingLocation, onRefreshLocation)
                }
                Text(
                    text = buildAnnotatedString {
                        append("Kiblat ")
                        withStyle(SpanStyle(color = scheme.onSurface)) { append(bearing) }
                    },
                    style = MaterialTheme.typography.readout,
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
                val side = if (signedDiff > 0) "kanan" else "kiri"
                val bearingWhole = qiblaDirection.roundToInt() % 360
                val point = compassPointName(qiblaDirection.toDouble()).lowercase()
                // TalkBack hears only meaningful changes, never every sensor tick
                val spoken = when {
                    noCompass -> "Kiblat $bearingWhole derajat dari utara, ke arah $point. Kompas HP ini tidak bisa dibaca."
                    waiting -> "Membaca kompas"
                    isAligned -> "Lurus, menghadap kiblat"
                    absDiff < 10f -> "Hampir lurus, putar sedikit ke $side"
                    else -> "Putar ke $side sekitar ${(absDiff / 10f).roundToInt() * 10} derajat"
                }
                Column(
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = spoken
                        liveRegion = LiveRegionMode.Polite
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BasicText(
                        text = when {
                            noCompass -> "$bearingWhole°"
                            waiting -> "—"
                            isAligned -> "Lurus"
                            else -> "${absDiff.roundToInt()}°"
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (waiting) scheme.onSurfaceVariant else scheme.onSurface
                        ),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 40.sp, maxFontSize = 80.sp)
                    )
                    when {
                        noCompass -> Text(
                            text = "dari utara, ke arah $point",
                            style = MaterialTheme.typography.subLine,
                            color = scheme.onSurfaceVariant
                        )
                        waiting -> Text(
                            text = "Membaca kompas…",
                            style = MaterialTheme.typography.subLine,
                            color = scheme.onSurfaceVariant
                        )
                        isAligned -> Row(
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
                                text = "Bayangan di garis, menghadap kiblat",
                                style = MaterialTheme.typography.subLine,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                        }
                        else -> Text(
                            text = buildAnnotatedString {
                                append("putar ke ")
                                withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Bold)) {
                                    append(side)
                                }
                                append(" sampai bayangan tongkat jatuh di garis")
                            },
                            style = MaterialTheme.typography.subLine,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = scheme.outline.copy(alpha = 0.6f))
                when {
                    noCompass -> NoCompassNote(latitude, longitude)
                    calibrating -> GuideNote(
                        icon = Icons.Outlined.Explore,
                        lead = "Kompas belum akurat.",
                        rest = "Gerakkan HP membentuk angka 8 sampai pesan ini hilang."
                    )
                    reading?.flat == false -> GuideNote(
                        icon = Icons.Outlined.StayCurrentPortrait,
                        lead = "Letakkan HP mendatar,",
                        rest = "layar menghadap ke atas. Arah kompas goyah saat HP berdiri."
                    )
                    isDefaultLocation -> DefaultLocationNote(locationName, isUpdatingLocation, onRefreshLocation)
                    else -> Text(
                        text = buildAnnotatedString {
                            append("Arah HP ")
                            withStyle(SpanStyle(color = scheme.onSurface)) {
                                append(if (reading == null) "—" else "$shownHeading°")
                            }
                        },
                        style = MaterialTheme.typography.readout,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight()
                    )
                }
            }
        }
    }
}

/**
 * A quiet instruction in the footer slot: calibration (the lock waits until it clears) or holding the phone
 * flat. It announces itself once when it appears, never on each reading.
 */
@Composable
private fun GuideNote(icon: ImageVector, lead: String, rest: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 10.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = scheme.onSurface, fontWeight = FontWeight.SemiBold)) {
                    append(lead)
                }
                append(" ")
                append(rest)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

/** The bearing is still for the default city: say so, and offer the fix in the same row. */
@Composable
private fun DefaultLocationNote(locationName: String, isUpdating: Boolean, onRefresh: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                enabled = !isUpdating,
                role = Role.Button,
                onClickLabel = "Gunakan lokasi saya",
                onClick = onRefresh
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Arah ini masih untuk $locationName.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (isUpdating) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = scheme.primary)
        } else {
            Text(
                text = "Gunakan lokasi saya",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
        }
    }
}

/** No compass: the bearing stands in the hero, and the sun gives the line on Rashdul Kiblat. */
@Composable
private fun NoCompassNote(latitude: Double, longitude: Double) {
    val scheme = MaterialTheme.colorScheme
    val sunHighEnough = remember(latitude, longitude) {
        sunAltitudeAtRashdul(latitude, longitude) >= RashdulMinSunAltitude
    }
    val moment = remember {
        nextRashdulKiblat(Instant.now()).atZone(ZoneId.systemDefault()).format(RashdulFormat) + " " + localZoneLabel()
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Kompas HP ini tidak bisa dibaca.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface
        )
        Text(
            text = if (sunHighEnough) {
                buildAnnotatedString {
                    append("Matahari tepat di atas Ka'bah pada ")
                    withStyle(SpanStyle(color = scheme.onSurface, fontWeight = FontWeight.SemiBold)) {
                        append(moment)
                    }
                    append(". Saat itu, arah dari ujung bayangan benda tegak ke bendanya adalah arah kiblat.")
                }
            } else {
                buildAnnotatedString {
                    append("Saat matahari tepat di atas Ka'bah, di tempat Anda ia sudah terlalu rendah untuk ")
                    append("membentuk bayangan. Tanyakan arah kiblat di masjid terdekat.")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

private val RashdulFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))

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
    phone: Boolean,
    statusTop: Dp
) {
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val brass = MaterialTheme.colorScheme.secondary
    val shade = ink.copy(alpha = if (background.luminance() < 0.5f) 0.34f else 0.46f)

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
                // Holds its full tone for half its length before fading, so the line it must meet stays readable
                val shadowBrush = Brush.verticalGradient(
                    0f to shade, 0.5f to shade, 1f to Color.Transparent,
                    startY = pivot.y, endY = pivot.y + shadowLength
                )
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
                    // Without a phone in the picture the plate is a diagram, with no line for the phone to meet
                    if (phone) {
                        drawLine(
                            if (aligned) m else m.copy(alpha = 0.55f),
                            Offset(pivot.x, guideTop), Offset(pivot.x, guideBottom),
                            strokeWidth = 1.5.dp.toPx(), pathEffect = if (aligned) null else dash
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
                    if (phone) drawPath(front, m)
                    drawCircle(m.copy(alpha = 0.18f), radius = 14.dp.toPx(), center = pivot)
                    drawCircle(surface, radius = 8.dp.toPx(), center = pivot)
                    drawCircle(ink, radius = 7.dp.toPx(), center = pivot, style = Stroke(2.dp.toPx()))
                    drawCircle(m, radius = 3.dp.toPx(), center = pivot)
                }
            }
    )
}
