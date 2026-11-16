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
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rfksgn.myprayer.core.permission.checkExactAlarmPermission
import com.rfksgn.myprayer.core.permission.checkNotificationPermission
import com.rfksgn.myprayer.data.local.CustomAdzanAudio
import com.rfksgn.myprayer.domain.model.ReminderMode
import com.rfksgn.myprayer.ui.theme.CarpetCard
import com.rfksgn.myprayer.ui.theme.CarpetCardDark
import com.rfksgn.myprayer.ui.theme.CarpetGold
import com.rfksgn.myprayer.ui.theme.CarpetInk
import com.rfksgn.myprayer.ui.theme.CarpetInkDark
import com.rfksgn.myprayer.ui.theme.CarpetIvory
import com.rfksgn.myprayer.ui.theme.CarpetMute
import com.rfksgn.myprayer.ui.theme.CarpetMuteDark
import com.rfksgn.myprayer.ui.theme.CarpetOnGold
import com.rfksgn.myprayer.ui.theme.CarpetPaper
import com.rfksgn.myprayer.ui.theme.CarpetPaperDark
import com.rfksgn.myprayer.ui.theme.CarpetRed
import com.rfksgn.myprayer.ui.theme.addPrayerArch
import com.rfksgn.myprayer.ui.theme.carpetColor
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    reminderModes: Map<String, ReminderMode>,
    customAudio: CustomAdzanAudio,
    onVolumeChange: (Float) -> Unit,
    onModeChange: (String, ReminderMode) -> Unit,
    onRegularAudioSelected: (String?, String) -> Unit,
    onFajrAudioSelected: (String?, String) -> Unit,
    onTestAdzan: (ReminderMode, Boolean) -> Unit,
    onStopAdzan: () -> Unit,
    modifier: Modifier = Modifier
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val c = remember(isDark) { SiagaColors(isDark) }
    val missing = listOf(hasNotifPerm, hasExactAlarmPerm).count { !it }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.paper)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. KESIAPAN ADZAN: satu vonis, lalu izin yang menentukannya
        ReadinessHero(missing = missing, c = c) {
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
        }

        // 2. SUARA ADZAN
        SiagaSection(title = "Suara adzan", c = c) {
            AudioRow(
                icon = Icons.Default.WbTwilight,
                label = "Subuh",
                fileName = customAudio.fajrFileName,
                isCustom = customAudio.fajrUriString != null,
                pickDescription = "Ganti audio adzan Subuh",
                resetDescription = "Kembalikan audio Subuh bawaan",
                c = c,
                onPick = { fajrAudioPickerLauncher.launch(arrayOf("audio/*")) },
                onReset = { onFajrAudioSelected(null, "Default Subuh") }
            )
            HorizontalDivider(color = c.line)
            AudioRow(
                icon = Icons.Default.MusicNote,
                label = "Dzuhur, Ashar, Maghrib, Isya",
                fileName = customAudio.regularFileName,
                isCustom = customAudio.regularUriString != null,
                pickDescription = "Ganti audio adzan reguler",
                resetDescription = "Kembalikan audio reguler bawaan",
                c = c,
                onPick = { regularAudioPickerLauncher.launch(arrayOf("audio/*")) },
                onReset = { onRegularAudioSelected(null, "Default") }
            )
            HorizontalDivider(color = c.line)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconTile(Icons.AutoMirrored.Filled.VolumeUp, c)
                // Sinkron dua arah dengan volume alarm sistem; disimpan sekali saat jari dilepas
                Slider(
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
                    },
                    valueRange = 0.05f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = c.accent,
                        activeTrackColor = c.accent,
                        inactiveTrackColor = c.accent.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Volume alarm adzan" }
                )
                Text(
                    text = "${(sliderPosition * 100).roundToInt()}%",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                    color = c.ink,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(44.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TestButton("Reguler", "Tes adzan reguler", Icons.Default.PlayArrow, c) { onTestAdzan(ReminderMode.ADZAN, false) }
                TestButton("Subuh", "Tes adzan Subuh", Icons.Default.PlayArrow, c) { onTestAdzan(ReminderMode.ADZAN, true) }
                TestButton("Getar", "Tes getar", Icons.Default.Vibration, c) { onTestAdzan(ReminderMode.VIBRATE, false) }
                Button(
                    onClick = onStopAdzan,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Hentikan adzan", modifier = Modifier.size(20.dp))
                }
            }
        }

        // 3. PENGINGAT TIAP WAKTU
        SiagaSection(title = "Pengingat tiap waktu", c = c) {
            val prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
            prayers.forEachIndexed { index, prayerName ->
                PrayerModeRow(
                    prayerName = prayerName,
                    selectedMode = reminderModes[prayerName] ?: ReminderMode.ADZAN,
                    c = c,
                    onSelectMode = { newMode -> onModeChange(prayerName, newMode) }
                )
                if (index < prayers.lastIndex) HorizontalDivider(color = c.line)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Setelan's own palette: ivory paper under the Kiblat carpet. Gold replaces red as the accent in dark mode. */
private class SiagaColors(dark: Boolean) {
    val paper = if (dark) CarpetPaperDark else CarpetPaper
    val card = if (dark) CarpetCardDark else CarpetCard
    val ink = if (dark) CarpetInkDark else CarpetInk
    val mute = if (dark) CarpetMuteDark else CarpetMute
    val line = ink.copy(alpha = 0.14f)
    val accent = if (dark) CarpetGold else CarpetRed
    val onAccent = if (dark) CarpetOnGold else CarpetIvory
    val hero = carpetColor(dark)
}

private val HeroArchColumn = 48.dp
private val HeroArchRow = 56.dp

@Composable
private fun ReadinessHero(missing: Int, c: SiagaColors, permissions: @Composable () -> Unit) {
    val ready = missing == 0
    val motif = CarpetIvory.copy(alpha = 0.14f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.hero)
            .clipToBounds()
            .drawWithCache {
                val col = HeroArchColumn.toPx()
                val row = HeroArchRow.toPx()
                val arches = Path().apply {
                    var top = 0f
                    while (top < size.height) {
                        var left = 0f
                        while (left < size.width) {
                            addPrayerArch(left, top, col, row)
                            left += col
                        }
                        top += row
                    }
                }
                val stroke = Stroke(width = 1.4.dp.toPx())
                onDrawBehind { drawPath(arches, motif, style = stroke) }
            }
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
    ) {
        Text(
            text = if (ready) {
                AnnotatedString("Adzan siap tepat waktu")
            } else {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = CarpetGold)) { append("$missing") }
                    append(" izin belum aktif")
                }
            },
            style = TextStyle(
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.02).em,
                fontFeatureSettings = "tnum"
            ),
            color = CarpetIvory,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (ready) {
                "Semua izin aktif. Adzan tetap berbunyi saat HP terkunci."
            } else {
                "Tanpa izin ini adzan bisa terlambat atau tidak berbunyi."
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = CarpetIvory.copy(alpha = 0.78f)
        )
        Spacer(Modifier.height(16.dp))
        permissions()
    }
}

@Composable
private fun PermissionLine(title: String, hint: String, isGranted: Boolean, onActivate: () -> Unit) {
    HorizontalDivider(color = CarpetIvory.copy(alpha = 0.18f))
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isGranted) CarpetIvory.copy(alpha = 0.7f) else CarpetGold,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                fontWeight = FontWeight.SemiBold,
                color = CarpetIvory
            )
            if (!isGranted) {
                Text(text = hint, style = MaterialTheme.typography.bodySmall, color = CarpetIvory.copy(alpha = 0.7f))
            }
        }
        if (isGranted) {
            Text(text = "Aktif", style = MaterialTheme.typography.labelMedium, color = CarpetIvory.copy(alpha = 0.7f))
        } else {
            Button(
                onClick = onActivate,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = CarpetIvory, contentColor = CarpetRed),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.heightIn(min = 36.dp).semantics { contentDescription = "Aktifkan $title" }
            ) {
                Text("Aktifkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SiagaSection(title: String, c: SiagaColors, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
            fontWeight = FontWeight.Bold,
            color = c.ink,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun IconTile(icon: ImageVector, c: SiagaColors) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(c.accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = c.accent, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AudioRow(
    icon: ImageVector,
    label: String,
    fileName: String,
    isCustom: Boolean,
    pickDescription: String,
    resetDescription: String,
    c: SiagaColors,
    onPick: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(icon, c)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = c.mute)
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                fontWeight = FontWeight.SemiBold,
                color = c.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isCustom) {
            IconButton(onClick = onReset) {
                Icon(Icons.Default.Restore, contentDescription = resetDescription, tint = c.mute)
            }
        }
        TextButton(onClick = onPick, modifier = Modifier.semantics { contentDescription = pickDescription }) {
            Text("Ganti", fontWeight = FontWeight.Bold, color = c.accent)
        }
    }
}

@Composable
private fun RowScope.TestButton(label: String, description: String, icon: ImageVector, c: SiagaColors, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, c.line),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = c.card, contentColor = c.ink),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .semantics { contentDescription = description }
    ) {
        Icon(icon, contentDescription = null, tint = c.accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun PrayerModeRow(
    prayerName: String,
    selectedMode: ReminderMode,
    c: SiagaColors,
    onSelectMode: (ReminderMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = prayerName,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.Bold,
                color = if (selectedMode == ReminderMode.SILENT) c.mute else c.ink
            )
            Text(text = selectedMode.label, style = MaterialTheme.typography.labelMedium, color = c.mute)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(c.ink.copy(alpha = 0.06f))
                .padding(3.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ReminderMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                val fill by animateColorAsState(if (isSelected) c.accent else Color.Transparent, label = "mode_fill")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(fill)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelectMode(mode) })
                        .semantics { contentDescription = "Pengingat $prayerName: ${mode.label}" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (mode) {
                            ReminderMode.ADZAN -> Icons.AutoMirrored.Filled.VolumeUp
                            ReminderMode.VIBRATE -> Icons.Default.Vibration
                            ReminderMode.SILENT -> Icons.Default.NotificationsOff
                        },
                        contentDescription = null,
                        tint = if (isSelected) c.onAccent else c.mute,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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