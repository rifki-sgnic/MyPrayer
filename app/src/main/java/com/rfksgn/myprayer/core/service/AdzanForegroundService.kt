package com.rfksgn.myprayer.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.rfksgn.myprayer.MainActivity
import com.rfksgn.myprayer.R
import com.rfksgn.myprayer.data.local.LocationPreferences
import com.rfksgn.myprayer.domain.model.ReminderMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

class AdzanForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var serviceJob: Job? = null

    companion object {
        const val CHANNEL_ID = "adzan_playback_channel_v3"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_ADZAN = "ACTION_START_ADZAN"
        const val ACTION_STOP_ADZAN = "ACTION_STOP_ADZAN"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_REMINDER_MODE = "EXTRA_REMINDER_MODE"
        const val EXTRA_IS_TEST = "EXTRA_IS_TEST"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ADZAN -> {
                stopAdzanAndService()
            }
            ACTION_START_ADZAN -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Sholat"
                val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)
                val fallbackModeStr = intent.getStringExtra(EXTRA_REMINDER_MODE) ?: ReminderMode.ADZAN.name

                startForegroundWithNotification(prayerName, ReminderMode.ADZAN)
                acquireWakeLock()

                stopPlaybackAndVibration()
                serviceJob = CoroutineScope(Dispatchers.Main).launch {
                    val prefs = LocationPreferences(applicationContext)

                    val actualMode = if (isTest) {
                        runCatching { ReminderMode.valueOf(fallbackModeStr) }.getOrDefault(ReminderMode.ADZAN)
                    } else {
                        val savedModes = prefs.reminderModesFlow.first()
                        savedModes[prayerName] ?: ReminderMode.ADZAN
                    }

                    startForegroundWithNotification(prayerName, actualMode)

                    when (actualMode) {
                        ReminderMode.SILENT -> {
                            stopAdzanAndService()
                        }
                        ReminderMode.VIBRATE -> {
                            startVibrationOnlyMode()
                        }
                        ReminderMode.ADZAN -> {
                            val customAudio = prefs.customAudioFlow.first()
                            val savedVolume = prefs.adzanVolumeFlow.first()

                            // Sinkronkan Volume Sistem STREAM_ALARM dengan preferensi terakhir
                            syncSystemAlarmVolume(savedVolume)

                            // KUNCI FIX SUBUH: Pisahkan secara tegas URI Subuh dan URI Reguler!
                            val isFajr = prayerName.contains("Subuh", ignoreCase = true)
                            val customUriToUse = if (isFajr) {
                                customAudio.fajrUriString // Hanya gunakan jika user memang memilih custom Subuh
                            } else {
                                customAudio.regularUriString
                            }

                            val defaultRawUri = resolveDefaultAdzanUri(isFajr)
                            val primaryUri = if (!customUriToUse.isNullOrBlank()) {
                                customUriToUse.toUri()
                            } else {
                                defaultRawUri
                            }

                            val started = tryStartMediaPlayer(primaryUri)
                            if (!started) {
                                tryStartMediaPlayer(defaultRawUri)
                            }
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun syncSystemAlarmVolume(volumeFraction: Float) {
        runCatching {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val targetIndex = (volumeFraction * maxVol).roundToInt().coerceIn(1, maxVol)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetIndex, 0)
        }
    }

    private fun startForegroundWithNotification(prayerName: String, mode: ReminderMode) {
        createNotificationChannel()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdzanForegroundService::class.java).apply {
            action = ACTION_STOP_ADZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = if (mode == ReminderMode.VIBRATE) {
            "Mode Getar Aktif • Geser hapus notifikasi atau ketuk tombol untuk berhenti."
        } else {
            "Geser hapus notifikasi ini atau ketuk tombol di bawah untuk menghentikan adzan."
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Waktu $prayerName Telah Tiba")
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Hentikan",
                stopPendingIntent
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun startVibrationOnlyMode() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 800, 400, 800, 1000)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))

        delay(15_000L.milliseconds)
        stopAdzanAndService()
    }

    private fun tryStartMediaPlayer(targetUri: Uri): Boolean {
        return try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(applicationContext, targetUri)
                // Volume sepenuhnya mengikuti hardware STREAM_ALARM sehingga 100% sinkron dengan sistem HP!
                setVolume(1.0f, 1.0f)
                isLooping = false
                setOnPreparedListener { it.start() }
                setOnCompletionListener { stopAdzanAndService() }
                prepareAsync()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Pemanggilan langsung R.raw.adzan_subuh & R.raw.adzan yang pasti akurat
    private fun resolveDefaultAdzanUri(isFajr: Boolean): Uri {
        val rawResId = if (isFajr) {
            R.raw.adzan_subuh
        } else {
            R.raw.adzan
        }
        return runCatching {
            "android.resource://$packageName/$rawResId".toUri()
        }.getOrElse {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyDeen::AdzanWakeLock"
        ).apply {
            acquire(5 * 60 * 1000L)
        }
    }

    private fun stopPlaybackAndVibration() {
        serviceJob?.cancel()
        serviceJob = null

        runCatching {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        mediaPlayer = null

        runCatching { vibrator?.cancel() }
        vibrator = null
    }

    private fun stopAdzanAndService() {
        stopPlaybackAndVibration()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopPlaybackAndVibration()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Panggilan Adzan & Pengingat Sholat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Menampilkan kontrol aktif saat adzan atau getar waktu sholat berjalan"
            setSound(null, null)
            enableVibration(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}