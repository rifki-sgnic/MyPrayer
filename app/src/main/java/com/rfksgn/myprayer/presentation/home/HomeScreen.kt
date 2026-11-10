package com.rfksgn.myprayer.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.domain.model.ReminderMode
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefreshLocationClick: () -> Unit,
    onToggleAlarm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.schedule ?: return
    val hijriDateString = remember { getFormattedHijriDate() }
    val currentTime = remember(uiState.countdownText) { LocalTime.now() }

    val prayers = remember(schedule) {
        listOf(
            PrayerRowData("Subuh", schedule.fajr, isSunnah = false),
            PrayerRowData("Terbit", schedule.sunrise, isSunnah = true),
            PrayerRowData("Dzuhur", schedule.dhuhr, isSunnah = false),
            PrayerRowData("Ashar", schedule.asr, isSunnah = false),
            PrayerRowData("Maghrib", schedule.maghrib, isSunnah = false),
            PrayerRowData("Isya", schedule.isha, isSunnah = false)
        )
    }

    val nextPrayerTargetTime = prayers.find { it.name == schedule.nextPrayerName }?.time ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. TOP EDITORIAL HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = hijriDateString.uppercase(),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    text = schedule.dateFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Location Pill + Refresh Button
            Surface(
                onClick = onRefreshLocationClick,
                enabled = !uiState.isUpdatingLocation,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = uiState.locationName.take(18),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.isUpdatingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Perbarui GPS",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 2. ARCHITECTURAL HERO COUNTDOWN CARD
        HeroCountdownCard(
            nextPrayerName = schedule.nextPrayerName,
            nextPrayerTime = nextPrayerTargetTime,
            countdownText = uiState.countdownText,
            qiblaDirection = schedule.qiblaDirection
        )

        // 3. SECTION HEADER JADWAL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "JADWAL WAKTU SHOLAT",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "Ketuk ikon untuk ubah mode",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 4. PRAYER TIMELINE LIST
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prayers.forEach { item ->
                val isNext = item.name == schedule.nextPrayerName
                val isPassed = isPrayerPassed(item.time, currentTime, isNext)
                val mode = uiState.reminderModes[item.name] ?: ReminderMode.ADZAN

                PrayerTimelineItem(
                    name = item.name,
                    time = item.time,
                    isNext = isNext,
                    isPassed = isPassed,
                    isSunnah = item.isSunnah,
                    reminderMode = mode,
                    onToggleAlarm = { onToggleAlarm(item.name) }
                )
            }
        }
    }
}

@Composable
private fun HeroCountdownCard(
    nextPrayerName: String,
    nextPrayerTime: String,
    countdownText: String,
    qiblaDirection: Double
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val goldAccent = MaterialTheme.colorScheme.secondary

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle Celestial / Astrolabe Geometry Background (Anti-AI Slop)
            Canvas(modifier = Modifier.matchParentSize()) {
                val rightCenter = Offset(size.width * 0.9f, size.height * 0.2f)
                drawCircle(
                    color = goldAccent.copy(alpha = 0.07f),
                    radius = size.width * 0.45f,
                    center = rightCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = goldAccent.copy(alpha = 0.05f),
                    radius = size.width * 0.65f,
                    center = rightCenter,
                    style = Stroke(width = 1.dp.toPx())
                )
                // Horizon arc at bottom
                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(goldAccent.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(-size.width * 0.2f, size.height * 0.65f),
                    size = size.copy(width = size.width * 1.4f, height = size.height)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Row: Badge & Target Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(goldAccent)
                        )
                        Text(
                            text = "MENUJU ${nextPrayerName.uppercase()}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Text(
                        text = "$nextPrayerTime WIB",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                            color = goldAccent
                        )
                    )
                }

                // Center: Large Tabular Countdown
                Text(
                    text = countdownText,
                    style = TextStyle(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        fontFeatureSettings = "tnum",
                        color = contentColor
                    )
                )

                HorizontalDivider(color = contentColor.copy(alpha = 0.1f))

                // Bottom Metadata: Qibla Azimuth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sudut Kiblat Lokasi Ini",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${"%.1f".format(qiblaDirection)}° Barat-Barat Laut",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                            color = contentColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimelineItem(
    name: String,
    time: String,
    isNext: Boolean,
    isPassed: Boolean,
    isSunnah: Boolean,
    reminderMode: ReminderMode,
    onToggleAlarm: () -> Unit
) {
    val backgroundColor = when {
        isNext -> MaterialTheme.colorScheme.surface
        isPassed -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.surface
    }

    val borderStroke = when {
        isNext -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        isPassed -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val textAlpha = if (isPassed) 0.45f else 1f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = borderStroke,
        shadowElevation = if (isNext) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = if (isNext) 18.dp else 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status Indicator + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Timeline Node
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(16.dp)
                ) {
                    if (isNext) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (isPassed) 0.25f else 0.5f
                                    )
                                )
                        )
                    }
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                )

                if (isNext) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "BERIKUTNYA",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Right: Tabular Time + Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = time,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                        color = if (isNext) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                        }
                    )
                )

                if (isSunnah) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Waktu Terbit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha * 0.6f)
                        )
                    }
                } else {
                    IconButton(onClick = onToggleAlarm, modifier = Modifier.size(40.dp)) {
                        val icon = when (reminderMode) {
                            ReminderMode.ADZAN -> Icons.Default.NotificationsActive
                            ReminderMode.VIBRATE -> Icons.Default.Vibration
                            ReminderMode.SILENT -> Icons.Default.NotificationsOff
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Mode Pengingat $name",
                            modifier = Modifier.size(18.dp),
                            tint = when {
                                reminderMode == ReminderMode.SILENT -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                isNext -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class PrayerRowData(
    val name: String,
    val time: String,
    val isSunnah: Boolean
)

private fun isPrayerPassed(timeStr: String, now: LocalTime, isNext: Boolean): Boolean {
    if (isNext) return false
    return try {
        val parts = timeStr.split(":")
        val prayerTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
        prayerTime.isBefore(now)
    } catch (e: Exception) {
        false
    }
}

private fun getFormattedHijriDate(): String {
    return try {
        val hijri = HijrahDate.now()
        val day = hijri.get(ChronoField.DAY_OF_MONTH)
        val month = hijri.get(ChronoField.MONTH_OF_YEAR)
        val year = hijri.get(ChronoField.YEAR)
        val monthNames = listOf(
            "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir",
            "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
        )
        "$day ${monthNames.getOrElse(month - 1) { "" }} $year H"
    } catch (e: Exception) {
        "Kalender Hijriah"
    }
}