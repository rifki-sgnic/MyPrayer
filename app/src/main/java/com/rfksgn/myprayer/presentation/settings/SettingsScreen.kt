package com.rfksgn.myprayer.presentation.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rfksgn.myprayer.core.permission.checkExactAlarmPermission
import com.rfksgn.myprayer.core.permission.checkNotificationPermission
import com.rfksgn.myprayer.data.local.CustomAdzanAudio
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import com.rfksgn.myprayer.domain.model.ReminderMode
import com.rfksgn.myprayer.presentation.quran.EaseOutExpo
import com.rfksgn.myprayer.ui.theme.SkyStone
import com.rfksgn.myprayer.ui.theme.prayerSkyColors
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SettingsScreen(
    schedule: PrayerSchedule?,
    reminderModes: Map<String, ReminderMode>,
    customAudio: CustomAdzanAudio,
    onVolumeChange: (Float) -> Unit,
    onModeChange: (String, ReminderMode) -> Unit,
    onRegularAudioSelected: (String?, String) -> Unit,
    onFajrAudioSelected: (String?, String) -> Unit,
    onTestAdzan: (ReminderMode, Boolean) -> Unit,
    onStopAdzan: () -> Unit,
    modifier: Modifier = Modifier,
    // hoisted so re-tapping Setelan in the navigation bar can scroll back to the dial
    scrollState: ScrollState = rememberScrollState()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasNotifPerm by remember { mutableStateOf(checkNotificationPermission(context)) }
    var hasExactAlarmPerm by remember { mutableStateOf(checkExactAlarmPermission(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotifPerm = checkNotificationPermission(context)
                hasExactAlarmPerm = checkExactAlarmPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted
    }

    // Launcher 1: Pilih Audio Reguler (Dzuhur - Isya)
    val regularAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val fileName = getFileNameFromUri(context, uri) ?: "Audio Adzan Kustom"
            onRegularAudioSelected(uri.toString(), fileName)
        }
    }

    // Launcher 2: Pilih Audio Khusus Subuh
    val fajrAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val fileName = getFileNameFromUri(context, uri) ?: "Audio Subuh Kustom"
            onFajrAudioSelected(uri.toString(), fileName)
        }
    }

    // --- SINKRONISASI 2 ARAH VOLUME ALARM SISTEM & SLIDER UI ---
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxAlarmVolume =
        remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1) }

    // State lokal agar slider bergerak 60fps super mulus tanpa menunggu DataStore
    var sliderPosition by remember {
        val currentSysVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        mutableFloatStateOf(currentSysVol.toFloat() / maxAlarmVolume.toFloat())
    }
    var isUserDraggingSlider by remember { mutableStateOf(false) }

    // Pantau jika user menekan tombol volume fisik di HP (Volume Sistem berubah -> Slider ikut berubah)
    DisposableEffect(context) {
        val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                if (!isUserDraggingSlider) {
                    val sysVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    val newFraction = (sysVol.toFloat() / maxAlarmVolume.toFloat()).coerceIn(0f, 1f)
                    sliderPosition = newFraction
                    onVolumeChange(newFraction)
                }
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
        onDispose {
            context.contentResolver.unregisterContentObserver(volumeObserver)
        }
    }

    // The Jadwal sky, keyed to the next prayer; secondary text in Sky Stone by day (DESIGN.md: Setelan Rete)
    val baseScheme = MaterialTheme.colorScheme
    val isDark = baseScheme.background.luminance() < 0.5f
    val scheme = remember(baseScheme) { if (isDark) baseScheme else baseScheme.copy(onSurfaceVariant = SkyStone) }
    val nextPrayer = schedule?.nextPrayerName.orEmpty()
    val (skyTarget, bodyTarget) = prayerSkyColors(nextPrayer, isDark)
    // Keyed on the theme: a theme flip snaps the sky with the background; only a new prayer fades it
    val (skyState, bodyState) = key(isDark) {
        animateColorAsState(skyTarget, tween(800), label = "sky") to animateColorAsState(bodyTarget, tween(800), label = "body")
    }
    val sky by skyState
    val body by bodyState

    val times = schedule?.let { listOf(it.fajr, it.dhuhr, it.asr, it.maghrib, it.isha) } ?: FallbackTimes
    val modeOf = { prayer: String -> reminderModes[prayer] ?: ReminderMode.ADZAN }
    val active = Prayers.count { modeOf(it) != ReminderMode.SILENT }
    val missing = listOf(hasNotifPerm, hasExactAlarmPerm).count { !it }

    MaterialTheme(colorScheme = scheme) {
        val background = scheme.background
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                // After the scroll so the sky scrolls with the content; it runs under the status bar
                .drawBehind {
                    drawRect(background)
                    val skyHeight = 300.dp.toPx()
                    drawRect(
                        Brush.verticalGradient(listOf(sky, background), startY = 0f, endY = skyHeight),
                        size = size.copy(height = minOf(size.height, skyHeight))
                    )
                }
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Setelan",
                    style = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "$active dari 5 pengingat aktif",
                    style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
                    color = scheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))

            // 1. PENGINGAT: the five prayers on the astrolabe's hour scale; a tap cycles adzan, getar, mati
            ReteDial(
                times = times,
                modeOf = modeOf,
                nextPrayer = nextPrayer,
                missing = missing,
                body = body,
                onCycle = { prayer -> onModeChange(prayer, modeOf(prayer).next()) }
            )
            Text(
                text = "Ketuk waktu untuk berganti: adzan, getar, mati.",
                style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                // 2. KESIAPAN ADZAN: one verdict, then the permissions that decide it
                Spacer(Modifier.height(24.dp))
                Text(
                    text = if (missing == 0) "Adzan siap tepat waktu" else "$missing izin belum aktif",
                    style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = if (missing == 0) {
                        "Semua izin aktif. Adzan tetap berbunyi saat HP terkunci."
                    } else {
                        "Tanpa izin ini adzan bisa terlambat atau tidak berbunyi."
                    },
                    style = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                PermissionLine(
                    title = "Notifikasi",
                    hint = "Agar tombol hentikan adzan muncul",
                    isGranted = hasNotifPerm,
                    onActivate = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openAppNotificationSettings(context)
                        }
                    }
                )
                PermissionLine(
                    title = "Alarm tepat waktu",
                    hint = "Agar adzan tidak terlambat",
                    isGranted = hasExactAlarmPerm,
                    onActivate = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    }
                )

                // 3. SUARA
                Text(
                    text = "Suara",
                    style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 32.dp, bottom = 4.dp).semantics { heading() }
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Volume alarm adzan",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${(sliderPosition * 100).roundToInt()}%",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum")
                    )
                }
                // Sinkron dua arah dengan volume alarm sistem; disimpan sekali saat jari dilepas
                VolumeRuler(
                    value = sliderPosition,
                    onValueChange = { newValue ->
                        isUserDraggingSlider = true
                        sliderPosition = newValue
                        val targetIndex = (newValue * maxAlarmVolume).roundToInt().coerceIn(1, maxAlarmVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetIndex, 0)
                    },
                    onValueChangeFinished = {
                        isUserDraggingSlider = false
                        onVolumeChange(sliderPosition)
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TestKey("Reguler", "Tes adzan reguler", Icons.Default.PlayArrow) { onTestAdzan(ReminderMode.ADZAN, false) }
                    TestKey("Subuh", "Tes adzan Subuh", Icons.Default.WbTwilight) { onTestAdzan(ReminderMode.ADZAN, true) }
                    TestKey("Getar", "Tes getar", Icons.Default.Vibration) { onTestAdzan(ReminderMode.VIBRATE, false) }
                    TestKey("Hentikan", "Hentikan adzan", Icons.Default.Stop, stop = true, onClick = onStopAdzan)
                }
                Spacer(Modifier.height(20.dp))
                AudioRow(
                    label = "Subuh",
                    fileName = customAudio.fajrFileName,
                    isCustom = customAudio.fajrUriString != null,
                    pickDescription = "Ganti audio adzan Subuh",
                    resetDescription = "Kembalikan audio Subuh bawaan",
                    onPick = { fajrAudioPickerLauncher.launch(arrayOf("audio/*")) },
                    onReset = { onFajrAudioSelected(null, "Default Subuh") }
                )
                AudioRow(
                    label = "Dzuhur sampai Isya",
                    fileName = customAudio.regularFileName,
                    isCustom = customAudio.regularUriString != null,
                    pickDescription = "Ganti audio adzan reguler",
                    resetDescription = "Kembalikan audio reguler bawaan",
                    onPick = { regularAudioPickerLauncher.launch(arrayOf("audio/*")) },
                    onReset = { onRegularAudioSelected(null, "Default") }
                )
            }
        }
    }
}

private val Prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")

// Only until the first schedule exists, so the dial never shows empty
private val FallbackTimes = listOf("04:30", "12:00", "15:15", "18:00", "19:15")

/** The dial is laid out on a 328-unit square (the 360dp design width minus gutters) and scales from there. */
private const val DialUnits = 328f
private const val DialCenter = DialUnits / 2
private const val MarkerRadius = 134f
private const val LabelRadius = 88f
private const val HubRadius = 30f

/** A "HH:mm" time on the astrolabe's hour scale: 12:00 at the top, midnight at the bottom, clockwise. */
internal fun hourAngle(time: String): Float {
    val parts = time.split(":")
    val minutes = (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    return (minutes / 1440f * 360f + 180f) % 360f
}

/** A point on the dial, in dial units, at [degrees] clockwise from the top and [radius] from the centre. */
private fun dialPoint(degrees: Float, radius: Float): Offset {
    val a = Math.toRadians(degrees.toDouble())
    return Offset(DialCenter + radius * sin(a).toFloat(), DialCenter - radius * cos(a).toFloat())
}

@Composable
private fun ReteDial(
    times: List<String>,
    modeOf: (String) -> ReminderMode,
    nextPrayer: String,
    missing: Int,
    body: Color,
    onCycle: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val brass = scheme.secondary
    val angles = times.map(::hourAngle)
    val nextAngle = Prayers.indexOf(nextPrayer).takeIf { it >= 0 }?.let { angles[it] }
    val hubFill by animateColorAsState(if (missing == 0) scheme.primary else brass, tween(300), label = "hub")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .drawBehind {
                val u = size.width / DialUnits
                val hair = 1.dp.toPx()
                val line = body.copy(alpha = 0.25f)
                val tick = body.copy(alpha = 0.6f)
                fun DrawScope.radial(deg: Float, from: Float, to: Float, color: Color, width: Float) =
                    drawLine(color, dialPoint(deg, from) * u, dialPoint(deg, to) * u, width)

                drawCircle(body.copy(alpha = 0.45f), radius = DialCenter * u - hair / 2, style = Stroke(hair))
                // The plate: rings every 28 and hour lines every 15°, clear of the hub and the rim
                for (r in 56..140 step 28) drawCircle(line, radius = r * u, style = Stroke(hair))
                for (i in 0 until 24) radial(i * 15f, 35f, 146f, line, hair)
                // The rim: a tick every quarter hour, longer every hour, brass at 00, 06, 12 and 18
                for (i in 0 until 96) radial(i * 3.75f, if (i % 4 == 0) 152f else 157f, 163f, tick, hair)
                for (i in 0 until 4) radial(i * 90f, 146f, 163f, brass, 2.dp.toPx())
                // The alidade: a brass rule from the hub to the next prayer, fading toward the hub
                if (nextAngle != null) {
                    val start = dialPoint(nextAngle, 36f) * u
                    val end = dialPoint(nextAngle, 114f) * u
                    drawLine(
                        Brush.linearGradient(listOf(brass.copy(alpha = 0.1f), brass), start = start, end = end),
                        start, end, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round
                    )
                }
                drawCircle(body.copy(alpha = 0.14f), radius = (HubRadius + 7f) * u)
            }
    ) {
        val unit: Dp = maxWidth / DialUnits

        // The hub carries the verdict: a check when ready, the missing-permission count otherwise
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(unit * HubRadius * 2)
                .background(hubFill, CircleShape)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = missing,
                transitionSpec = {
                    (fadeIn(tween(200, delayMillis = 100)) +
                            scaleIn(tween(200, delayMillis = 100, easing = EaseOutExpo), initialScale = 0.9f)) togetherWith
                            fadeOut(tween(100)) using
                            SizeTransform(clip = false)
                },
                contentAlignment = Alignment.Center,
                label = "hubGlyph"
            ) { count ->
                if (count == 0) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = scheme.background, modifier = Modifier.size(26.dp))
                } else {
                    Text(
                        text = "$count",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                        color = scheme.background
                    )
                }
            }
        }

        Prayers.forEachIndexed { i, prayer ->
            val mode = modeOf(prayer)
            val isNext = prayer == nextPrayer
            val label = dialPoint(angles[i], LabelRadius)
            Column(
                modifier = Modifier
                    .offset(unit * label.x - 32.dp, unit * label.y - 14.dp)
                    .width(64.dp)
                    .clearAndSetSemantics {},
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = prayer,
                    style = TextStyle(fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = if (mode == ReminderMode.SILENT) scheme.onSurfaceVariant else scheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = times[i],
                    style = TextStyle(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isNext) brass else scheme.onSurfaceVariant
                )
            }
            val mark = dialPoint(angles[i], MarkerRadius)
            ReteMarker(
                prayer = prayer,
                time = times[i],
                mode = mode,
                isNext = isNext,
                onClick = { onCycle(prayer) },
                modifier = Modifier.offset(unit * mark.x - 24.dp, unit * mark.y - 24.dp)
            )
        }
    }
}

@Composable
private fun ReteMarker(
    prayer: String,
    time: String,
    mode: ReminderMode,
    isNext: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val brass = scheme.secondary
    val fill by animateColorAsState(
        when (mode) {
            ReminderMode.ADZAN -> scheme.primary
            ReminderMode.VIBRATE -> scheme.secondaryContainer
            ReminderMode.SILENT -> scheme.background
        },
        tween(150), label = "marker_fill"
    )
    val tint by animateColorAsState(
        when (mode) {
            ReminderMode.ADZAN -> scheme.onPrimary
            ReminderMode.VIBRATE -> scheme.primary
            ReminderMode.SILENT -> scheme.onSurfaceVariant
        },
        tween(150), label = "marker_tint"
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = "mengganti pengingat", onClick = onClick)
            .semantics { contentDescription = "Pengingat $prayer $time: ${mode.label}" }
            // The next prayer wears a brass ring, 2dp clear of its disc
            .drawBehind { if (isNext) drawCircle(brass, radius = 19.75.dp.toPx(), style = Stroke(1.5.dp.toPx())) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(fill, CircleShape)
                .then(if (mode == ReminderMode.SILENT) Modifier.border(1.dp, scheme.outline, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (mode) {
                    ReminderMode.ADZAN -> Icons.Default.NotificationsActive
                    ReminderMode.VIBRATE -> Icons.Default.Vibration
                    ReminderMode.SILENT -> Icons.Default.NotificationsOff
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PermissionLine(title: String, hint: String, isGranted: Boolean, onActivate: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    HorizontalDivider(color = scheme.outline.copy(alpha = 0.7f))
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isGranted) scheme.onSurfaceVariant else scheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(text = title, style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold))
            if (!isGranted) {
                Text(text = hint, style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp), color = scheme.onSurfaceVariant)
            }
        }
        if (isGranted) {
            Text(text = "Aktif", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        } else {
            Button(
                onClick = onActivate,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.heightIn(min = 36.dp).semantics { contentDescription = "Aktifkan $title" }
            ) {
                Text("Aktifkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * The volume as a graduated rule: a tick every 5%, a long one every 25%, the level in primary and a brass
 * index at the value. An invisible Material slider on top does the input and the accessibility.
 */
@Composable
private fun VolumeRuler(value: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val tick = scheme.onSurfaceVariant.copy(alpha = 0.55f)
    val primary = scheme.primary
    val brass = scheme.secondary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                val hair = 1.dp.toPx()
                val base = size.height - 14.dp.toPx()
                for (i in 0..20) {
                    val x = i / 20f * (size.width - hair) + hair / 2
                    val length = if (i % 5 == 0) 20.dp.toPx() else 8.dp.toPx()
                    drawLine(tick, Offset(x, base - length), Offset(x, base), hair)
                }
                val x = size.width * value
                drawRect(primary, topLeft = Offset(0f, base - 2.dp.toPx()), size = Size(x, 2.dp.toPx()))
                drawRoundRect(
                    brass,
                    topLeft = Offset(x - 1.5.dp.toPx(), base + 4.dp.toPx() - 28.dp.toPx()),
                    size = Size(3.dp.toPx(), 28.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0.05f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize().semantics { contentDescription = "Volume alarm adzan" }
        )
    }
}

@Composable
private fun TestKey(label: String, description: String, icon: ImageVector, stop: Boolean = false, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (stop) scheme.errorContainer else scheme.surface, CircleShape)
                .then(if (stop) Modifier else Modifier.border(1.dp, scheme.outline, CircleShape)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (stop) scheme.onErrorContainer else scheme.primary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}

@Composable
private fun AudioRow(
    label: String,
    fileName: String,
    isCustom: Boolean,
    pickDescription: String,
    resetDescription: String,
    onPick: () -> Unit,
    onReset: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    HorizontalDivider(color = scheme.outline.copy(alpha = 0.7f))
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Text(
                text = fileName,
                style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isCustom) {
            IconButton(onClick = onReset) {
                Icon(Icons.Default.Restore, contentDescription = resetDescription, tint = scheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onPick, modifier = Modifier.semantics { contentDescription = pickDescription }) {
            Text("Ganti", fontWeight = FontWeight.Bold, color = scheme.primary)
        }
    }
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else null
        }
    }.getOrNull()
}
