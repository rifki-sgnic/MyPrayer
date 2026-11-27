package com.rfksgn.myprayer.presentation.home

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.permission.AdzanReadiness
import com.rfksgn.myprayer.core.permission.rememberAdzanReadiness
import com.rfksgn.myprayer.data.local.ThemeMode
import com.rfksgn.myprayer.domain.model.ReminderMode
import com.rfksgn.myprayer.ui.theme.SkyStone
import com.rfksgn.myprayer.ui.theme.badge
import com.rfksgn.myprayer.ui.theme.brassText
import com.rfksgn.myprayer.ui.theme.overline
import com.rfksgn.myprayer.ui.theme.launchEntrance
import com.rfksgn.myprayer.ui.theme.prayerSky
import com.rfksgn.myprayer.ui.theme.readout
import com.rfksgn.myprayer.ui.theme.subLine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
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
    onOpenQibla: () -> Unit,
    themeMode: ThemeMode,
    onCycleThemeMode: () -> Unit,
    onOpenAlKahf: () -> Unit,
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

    val isFriday = remember(schedule.dateFormatted) {
        LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY
    }

    val showAlKahfBanner = remember(schedule.maghrib, uiState.countdownText) {
        isAlKahfSunnahWindow(schedule.maghrib)
    }

    val readiness = rememberAdzanReadiness()
    val hijriDateString = remember(schedule.dateFormatted) { getFormattedHijriDate() }
    val currentTime = remember(uiState.countdownText) { LocalTime.now() }

    val prayers = remember(schedule, isFriday) {
        listOf(
            PrayerRowData("Subuh", schedule.fajr, isSunnah = false),
            PrayerRowData("Terbit", schedule.sunrise, isSunnah = true),
            PrayerRowData(if (isFriday) "Jumat" else "Dzuhur", schedule.dhuhr, isSunnah = false, keyName = "Dzuhur"),
            PrayerRowData("Ashar", schedule.asr, isSunnah = false),
            PrayerRowData("Maghrib", schedule.maghrib, isSunnah = false),
            PrayerRowData("Isya", schedule.isha, isSunnah = false)
        )
    }

    val nextPrayerItem = prayers.find { it.keyName == schedule.nextPrayerName }
    val nextPrayerTargetName = nextPrayerItem?.name ?: schedule.nextPrayerName
    val nextPrayerTargetTime = nextPrayerItem?.time ?: ""

    val reminderKeys = prayers.filterNot { it.isSunnah }.map { it.keyName }
    val modeOf = { key: String -> uiState.reminderModes[key] ?: ReminderMode.ADZAN }
    val activeReminders = reminderKeys.count { modeOf(it) != ReminderMode.SILENT }
    // Terbit has no reminder, so it has no mode to report
    val nextMode = nextPrayerItem?.takeUnless { it.isSunnah }?.let { modeOf(it.keyName) }
    // The hint teaches the reminder icons until the first change; after that the undo snackbar has taught it
    val showModeHint = reminderKeys.all { modeOf(it) == ReminderMode.ADZAN }

    // Secondary text sits on the tinted sky; deepen Stone in light mode so it keeps ≥4.5:1
    val baseScheme = MaterialTheme.colorScheme
    val skyScheme = remember(baseScheme) {
        if (baseScheme.background.luminance() > 0.5f) baseScheme.copy(onSurfaceVariant = SkyStone) else baseScheme
    }
    MaterialTheme(colorScheme = skyScheme) {
      // The page fits the screen and does not scroll; scrolling only switches on when it cannot fit
      // (large font scale, a short screen, or the first-run location card).
      BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        val scrollState = rememberScrollState()
        // The plate centres on the theme hub: status bar, the 8dp top padding, then half the 48dp hub
        val hubCenterY = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp + 24.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState, enabled = scrollState.maxValue > 0)
                // After the scroll so the sky and astrolabe plate scroll with the content
                .prayerSky(schedule.nextPrayerName, hubCenterY)
                .heightIn(min = viewportHeight)
                // The sky runs under the status bar; content starts below it
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            // Two blocks: the hero at the top, the schedule at the bottom; spare height on tall screens opens between them
            verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 1. HEADER: dates with the location under them, and the theme hub at the plate's centre
            Row(modifier = Modifier.fillMaxWidth().launchEntrance(0), verticalAlignment = Alignment.Top) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = hijriDateString.uppercase(),
                            style = MaterialTheme.typography.overline,
                            color = MaterialTheme.colorScheme.brassText
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
                Spacer(modifier = Modifier.width(12.dp))
                ThemeHub(mode = themeMode, onClick = onCycleThemeMode)
            }

            // First run: the schedule below is Jakarta's until the user shares a location
            if (uiState.isDefaultLocation) {
                Box(Modifier.launchEntrance(1)) {
                    LocationSetupCard(
                        cityName = uiState.locationName,
                        isUpdating = uiState.isUpdatingLocation,
                        onUseMyLocation = onRefreshLocationClick
                    )
                }
            }

            // 2. NEXT PRAYER: the name leads; time and countdown share one line beneath it
            Box(Modifier.launchEntrance(2)) {
                NextPrayerBlock(
                    nextPrayerName = nextPrayerTargetName,
                    nextPrayerTime = nextPrayerTargetTime,
                    countdownText = uiState.countdownText,
                    qiblaDirection = schedule.qiblaDirection,
                    // Location comes first on first run; permissions are surfaced once it is set
                    readiness = if (uiState.isDefaultLocation) null else readiness,
                    activeReminders = activeReminders,
                    nextMode = nextMode,
                    onFixReadiness = onOpenSettings,
                    onOpenQibla = onOpenQibla
                )
            }
          }

            // Banner Sunnah Jumat: Muncul otomatis Kamis ba'da Maghrib s/d Jumat sebelum Maghrib
            if (showAlKahfBanner) {
                Box(Modifier.launchEntrance(3)) {
                    AlKahfSunnahCard(onClick = onOpenAlKahf)
                }
            }

            // 3. SCHEDULE. No header; the reminder hint shows above the rows until the first change.
            val scheduleOffset = if (showAlKahfBanner) 4 else 3
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showModeHint) {
                    Text(
                        text = "Ketuk lonceng untuk berganti: adzan, getar, mati.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .launchEntrance(scheduleOffset)
                            .padding(horizontal = 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    prayers.forEachIndexed { index, item ->
                        val isNext = item.keyName == schedule.nextPrayerName
                        val isPassed = isPrayerPassed(item.time, currentTime, isNext)
                        val mode = uiState.reminderModes[item.keyName] ?: ReminderMode.ADZAN

                        Box(Modifier.launchEntrance(scheduleOffset + 1 + index)) {
                            PrayerTimelineItem(
                                name = item.name,
                                time = item.time,
                                isNext = isNext,
                                isPassed = isPassed,
                                isSunnah = item.isSunnah,
                                reminderMode = mode,
                                onToggleAlarm = { onToggleAlarm(item.keyName) }
                            )
                        }
                    }
                }
            }
        }
      }
    }
}

/**
 * The plate's centre pin: a 40dp paper disc with a 1dp primary ring and a primary glyph, in a 48dp target.
 * Quiet on purpose, so the prayer name stays the strongest mark. Each tap moves Otomatis, Terang, Gelap, then back.
 */
@Composable
private fun ThemeHub(mode: ThemeMode, onClick: () -> Unit) {
    val label = when (mode) {
        ThemeMode.AUTO -> "Otomatis"
        ThemeMode.LIGHT -> "Terang"
        ThemeMode.DARK -> "Gelap"
    }
    val disc by animateColorAsState(MaterialTheme.colorScheme.surface, tween(300), label = "hubDisc")
    val glyph by animateColorAsState(MaterialTheme.colorScheme.primary, tween(300), label = "hubGlyph")
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = "Ganti tema", onClick = onClick)
            .semantics { contentDescription = "Tema: " + label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(disc, CircleShape)
                .border(1.dp, glyph, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = mode, animationSpec = tween(200), label = "hubIcon") { m ->
                Icon(
                    imageVector = when (m) {
                        ThemeMode.AUTO -> Icons.Filled.Contrast
                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                        ThemeMode.DARK -> Icons.Filled.DarkMode
                    },
                    contentDescription = null,
                    tint = glyph,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun LocationPill(
    locationName: String,
    isUpdating: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = !isUpdating,
                role = Role.Button,
                onClickLabel = "Perbarui lokasi",
                onClick = onClick
            )
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
    nextMode: ReminderMode?,
    onFixReadiness: () -> Unit,
    onOpenQibla: () -> Unit
) {
    val zoneLabel = remember { localZoneLabel() }
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        // Name, time and countdown read as one group ("Isya, pukul 19:12 WIB, 00:24:30 lagi")
        Column(
            modifier = Modifier
                // 24dp of air above the name: the 16dp gap below the header plus 8dp
                .padding(top = 8.dp, bottom = 16.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Shrinks rather than clips for long names (Maghrib) at large font scales
            BasicText(
                text = nextPrayerName,
                style = MaterialTheme.typography.displayLarge.copy(color = onSurface),
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
                style = MaterialTheme.typography.subLine,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                nextPrayerName = nextPrayerName,
                nextMode = nextMode,
                onFix = onFixReadiness,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            val degrees = qiblaDirection.roundToInt()
            Text(
                text = "Kiblat $degrees°",
                style = MaterialTheme.typography.readout,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClickLabel = "Buka Kiblat", onClick = onOpenQibla)
                    .heightIn(min = 48.dp)
                    .wrapContentHeight()
                    .padding(start = 4.dp)
                    .semantics {
                        contentDescription =
                            "Arah kiblat $degrees derajat, ${compassPointName(qiblaDirection)}"
                    }
            )
        }
    }
}

/**
 * Answers "will my next adzan sound?" in one line, most urgent first: notifications off, exact alarms off
 * (tappable, to Setelan), then the next prayer muted, then the day's count. It never shows a check while
 * the next reminder is off.
 */
@Composable
private fun ReminderStatus(
    readiness: AdzanReadiness?,
    activeReminders: Int,
    nextPrayerName: String,
    nextMode: ReminderMode?,
    onFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val problem = when {
        readiness == null -> null
        !readiness.notifications -> "Notifikasi adzan belum aktif"
        !readiness.exactAlarm -> "Adzan bisa terlambat"
        else -> null
    }
    val textStyle = MaterialTheme.typography.readout
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
                color = MaterialTheme.colorScheme.brassText,
                maxLines = 1
            )
        }
    } else {
        val nextMuted = nextMode == ReminderMode.SILENT && activeReminders > 0
        val status = when {
            activeReminders == 0 -> "Semua pengingat nonaktif"
            nextMuted -> "Pengingat $nextPrayerName nonaktif"
            activeReminders == 5 -> "Pengingat aktif · 5 waktu"
            else -> "Pengingat aktif · $activeReminders dari 5 waktu"
        }
        val muted = activeReminders == 0 || nextMuted
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (muted) Icons.Default.NotificationsOff else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (muted) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = status,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    val backgroundColor =
        if (isPassed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface

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
                .padding(
                    start = 18.dp,
                    end = 4.dp,
                    top = if (isNext) 6.dp else 2.dp,
                    bottom = if (isNext) 6.dp else 2.dp
                ),
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
                            style = MaterialTheme.typography.badge,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
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
                                reminderMode == ReminderMode.SILENT -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )

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
    val isSunnah: Boolean,
    val keyName: String = name
)

private fun isPrayerPassed(timeStr: String, now: LocalTime, isNext: Boolean): Boolean {
    return !isNext && try {
        val parts = timeStr.split(":")
        val prayerTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
        prayerTime.isBefore(now)
    } catch (e: Exception) {
        false
    }
}

/** WIB / WITA / WIT from the device's current offset; GMT±h anywhere else. */
internal fun localZoneLabel(): String {
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

@Composable
private fun AlKahfSunnahCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sunnah Hari Jumat",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.brassText
                    )
                )
                Text(
                    text = "Baca Surah Al-Kahfi (QS. 18)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Buka Surah Al-Kahfi",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun isAlKahfSunnahWindow(maghribTimeStr: String): Boolean {
    return try {
        val now = LocalDateTime.now()
        val parts = maghribTimeStr.split(":")
        val maghribToday = LocalTime.of(parts[0].toInt(), parts[1].toInt())

        when (now.dayOfWeek) {
            DayOfWeek.THURSDAY -> !now.toLocalTime().isBefore(maghribToday) // Kamis >= Maghrib
            DayOfWeek.FRIDAY -> now.toLocalTime().isBefore(maghribToday)    // Jumat < Maghrib
            else -> false
        }
    } catch (e: Exception) {
        false
    }
}
