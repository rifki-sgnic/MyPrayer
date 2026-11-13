package com.rfksgn.myprayer.presentation.home

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.permission.AdzanReadiness
import com.rfksgn.myprayer.core.permission.rememberAdzanReadiness
import com.rfksgn.myprayer.domain.model.ReminderMode
import com.rfksgn.myprayer.ui.theme.SkyStone
import com.rfksgn.myprayer.ui.theme.prayerSky
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefreshLocationClick: () -> Unit,
    onToggleAlarm: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.schedule
    if (schedule == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val readiness = rememberAdzanReadiness()
    val hijriDateString = remember(schedule.dateFormatted) { getFormattedHijriDate() }
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
    val activeReminders = uiState.reminderModes.count { it.value != ReminderMode.SILENT }

    // Secondary text sits on the tinted sky; deepen Stone in light mode so it keeps ≥4.5:1
    val baseScheme = MaterialTheme.colorScheme
    val skyScheme = remember(baseScheme) {
        if (baseScheme.background.luminance() > 0.5f) baseScheme.copy(onSurfaceVariant = SkyStone) else baseScheme
    }
    MaterialTheme(colorScheme = skyScheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // After the scroll so the sky and astrolabe plate scroll with the content
                .prayerSky(schedule.nextPrayerName)
                // The sky runs under the status bar; content starts below it
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HEADER: the location pill wraps under the date when both don't fit
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LocationPill(
                    locationName = uiState.locationName,
                    isUpdating = uiState.isUpdatingLocation,
                    onClick = onRefreshLocationClick
                )
            }

            // First run: the schedule below is Jakarta's until the user shares a location
            if (uiState.isDefaultLocation) {
                LocationSetupCard(
                    cityName = uiState.locationName,
                    isUpdating = uiState.isUpdatingLocation,
                    onUseMyLocation = onRefreshLocationClick
                )
            }

            // 2. NEXT PRAYER: the name leads; time and countdown share one line beneath it
            NextPrayerBlock(
                nextPrayerName = schedule.nextPrayerName,
                nextPrayerTime = nextPrayerTargetTime,
                countdownText = uiState.countdownText,
                qiblaDirection = schedule.qiblaDirection,
                // Location comes first on first run; permissions are surfaced once it is set
                readiness = if (uiState.isDefaultLocation) null else readiness,
                activeReminders = activeReminders,
                onFixReadiness = onOpenSettings
            )

            // 3. SCHEDULE
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ketuk ikon untuk ubah mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

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
    }
}

@Composable
private fun LocationPill(
    locationName: String,
    isUpdating: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isUpdating, role = Role.Button, onClickLabel = "Perbarui lokasi", onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = locationName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun LocationSetupCard(
    cityName: String,
    isUpdating: Boolean,
    onUseMyLocation: () -> Unit
) {
    // Android 13+ asks for notifications in the same step, so say so before the dialogs appear
    val reason = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        "Izinkan lokasi agar waktu sholat sesuai kota Anda, dan notifikasi agar adzan bisa dihentikan."
    } else {
        "Izinkan lokasi agar waktu sholat dan adzan sesuai kota Anda."
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Jadwal ini masih untuk $cityName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onUseMyLocation,
                enabled = !isUpdating,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mencari lokasi…")
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gunakan Lokasi Saya", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun NextPrayerBlock(
    nextPrayerName: String,
    nextPrayerTime: String,
    countdownText: String,
    qiblaDirection: Double,
    readiness: AdzanReadiness?,
    activeReminders: Int,
    onFixReadiness: () -> Unit
) {
    val zoneLabel = remember { localZoneLabel() }
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        // Name, time and countdown read as one group ("Isya, pukul 19:12 WIB, 00:24:30 lagi")
        Column(
            modifier = Modifier
                // 44dp of air above the name: the screen's 20dp gap plus 24dp
                .padding(top = 24.dp, bottom = 32.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Shrinks rather than clips for long names (Maghrib) at large font scales
            BasicText(
                text = nextPrayerName,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 0.92.em,
                    letterSpacing = (-0.03).em,
                    color = onSurface
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 40.sp, maxFontSize = 80.sp)
            )
            Text(
                text = buildAnnotatedString {
                    append("pukul ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primary)) {
                        append(nextPrayerTime)
                    }
                    append(" $zoneLabel  ·  ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = onSurface)) {
                        append(countdownText)
                    }
                    append(" lagi")
                },
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFeatureSettings = "tnum",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

        Row(
            // Same height whether or not the status is tappable, so the layout never jumps
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReminderStatus(
                readiness = readiness,
                activeReminders = activeReminders,
                onFix = onFixReadiness,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            val degrees = qiblaDirection.roundToInt()
            Text(
                text = "Kiblat $degrees°",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription =
                        "Arah kiblat $degrees derajat, ${compassPointName(qiblaDirection)}"
                }
            )
        }
    }
}

/** The adzan's readiness in one line; tappable when something needs fixing. */
@Composable
private fun ReminderStatus(
    readiness: AdzanReadiness?,
    activeReminders: Int,
    onFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val problem = when {
        readiness == null -> null
        readiness.mayBeLate -> "Adzan bisa terlambat"
        !readiness.notifications -> "Notifikasi adzan belum aktif"
        else -> null
    }
    val textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    val goldAccent = MaterialTheme.colorScheme.secondary

    if (problem != null) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClickLabel = "Buka Setelan izin", onClick = onFix)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = goldAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = problem,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = "Perbaiki",
                style = textStyle.copy(fontWeight = FontWeight.Bold),
                color = goldAccent,
                maxLines = 1
            )
        }
    } else {
        val status = when (activeReminders) {
            0 -> "Semua pengingat nonaktif"
            5 -> "Pengingat aktif · 5 waktu"
            else -> "Pengingat aktif · $activeReminders dari 5 waktu"
        }
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (activeReminders == 0) Icons.Default.NotificationsOff else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (activeReminders == 0) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(text = status, style = textStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val backgroundColor = if (isPassed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface

    val borderStroke = when {
        isNext -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        isPassed -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    // Passed prayers recede but stay readable (≥4.5:1 on the background)
    val textAlpha = if (isPassed) 0.62f else 1f

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
                .padding(start = 18.dp, end = 4.dp, top = if (isNext) 8.dp else 4.dp, bottom = if (isNext) 8.dp else 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                )

                if (isNext) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
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

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Waktu terbit, tanpa adzan",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    IconButton(onClick = onToggleAlarm, modifier = Modifier.size(48.dp)) {
                        val icon = when (reminderMode) {
                            ReminderMode.ADZAN -> Icons.Default.NotificationsActive
                            ReminderMode.VIBRATE -> Icons.Default.Vibration
                            ReminderMode.SILENT -> Icons.Default.NotificationsOff
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Pengingat $name: ${reminderMode.label}. Ketuk untuk mengganti.",
                            modifier = Modifier.size(18.dp),
                            tint = when {
                                reminderMode == ReminderMode.SILENT -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

/** WIB / WITA / WIT from the device's current offset; GMT±h anywhere else. */
private fun localZoneLabel(): String {
    val offsetSeconds = ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds
    return when (offsetSeconds) {
        7 * 3600 -> "WIB"
        8 * 3600 -> "WITA"
        9 * 3600 -> "WIT"
        else -> {
            val hours = offsetSeconds / 3600
            val minutes = kotlin.math.abs(offsetSeconds % 3600) / 60
            val sign = if (offsetSeconds >= 0) "+" else "-"
            "GMT$sign${kotlin.math.abs(hours)}" + if (minutes > 0) ":%02d".format(minutes) else ""
        }
    }
}

/** 16-point compass name in Indonesian, for the screen-reader description of the Qibla bearing. */
internal fun compassPointName(degrees: Double): String {
    val points = listOf(
        "Utara", "Utara-Timur Laut", "Timur Laut", "Timur-Timur Laut",
        "Timur", "Timur-Tenggara", "Tenggara", "Selatan-Tenggara",
        "Selatan", "Selatan-Barat Daya", "Barat Daya", "Barat-Barat Daya",
        "Barat", "Barat-Barat Laut", "Barat Laut", "Utara-Barat Laut"
    )
    val index = (((degrees % 360) + 360) % 360 / 22.5).roundToInt() % 16
    return points[index]
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
