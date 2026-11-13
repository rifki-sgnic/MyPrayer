package com.rfksgn.myprayer.presentation.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfksgn.myprayer.core.alarm.AdzanAlarmScheduler
import com.rfksgn.myprayer.core.location.LocationTracker
import com.rfksgn.myprayer.core.service.AdzanForegroundService
import com.rfksgn.myprayer.data.local.CustomAdzanAudio
import com.rfksgn.myprayer.data.local.LocationPreferences
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import com.rfksgn.myprayer.domain.model.ReminderMode
import com.rfksgn.myprayer.domain.usecase.GetPrayerScheduleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class HomeUiState(
    val schedule: PrayerSchedule? = null,
    val locationName: String = "Memuat lokasi...",
    val countdownText: String = "--:--:--",
    val isDefaultLocation: Boolean = false,
    val isUpdatingLocation: Boolean = false,
    val reminderModes: Map<String, ReminderMode> = emptyMap(),
    val customAudio: CustomAdzanAudio = CustomAdzanAudio(),
    val adzanVolume: Float = 0.7f
)

/** One-off feedback shown as a snackbar; the action (undo, retry, open settings) is optional. */
data class HomeMessage(
    val text: String,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)

class HomeViewModel(
    private val appContext: Context,
    private val locationPrefs: LocationPreferences,
    private val locationTracker: LocationTracker,
    private val alarmScheduler: AdzanAlarmScheduler,
    private val getPrayerSchedule: GetPrayerScheduleUseCase = GetPrayerScheduleUseCase()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _messages = Channel<HomeMessage>(Channel.BUFFERED)
    val messages: Flow<HomeMessage> = _messages.receiveAsFlow()

    // The day the schedule was computed for, so it rolls over at midnight
    private var scheduleDate: LocalDate = LocalDate.now()

    init {
        observePreferences()
        startCountdownTicker()
    }

    private fun observePreferences() {
        // 1. Hanya hitung jadwal & setel ulang AlarmManager jika Lokasi atau Mode Pengingat berubah
        viewModelScope.launch {
            combine(
                locationPrefs.locationFlow,
                locationPrefs.reminderModesFlow
            ) { savedLoc, modesMap ->
                scheduleDate = LocalDate.now()
                val schedule = getPrayerSchedule(savedLoc.latitude, savedLoc.longitude)
                alarmScheduler.scheduleDailyAlarms(schedule, modesMap)
                Triple(schedule, savedLoc, modesMap)
            }.collect { (schedule, savedLoc, modesMap) ->
                _uiState.update {
                    it.copy(
                        schedule = schedule,
                        locationName = savedLoc.cityName,
                        isDefaultLocation = savedLoc.isDefault,
                        reminderModes = modesMap
                    )
                }
            }
        }

        // 2. Pantau perubahan File Audio & Volume secara ringan (Tanpa membebani AlarmManager!)
        viewModelScope.launch {
            combine(
                locationPrefs.customAudioFlow,
                locationPrefs.adzanVolumeFlow
            ) { customAudio, volume ->
                customAudio to volume
            }.collect { (customAudio, volume) ->
                _uiState.update {
                    it.copy(
                        customAudio = customAudio,
                        adzanVolume = volume
                    )
                }
            }
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                val schedule = _uiState.value.schedule
                if (schedule != null && LocalDate.now() != scheduleDate) {
                    // New day: refresh the date header and the day's times
                    scheduleDate = LocalDate.now()
                    val loc = locationPrefs.locationFlow.first()
                    _uiState.update {
                        it.copy(schedule = getPrayerSchedule(loc.latitude, loc.longitude))
                    }
                } else if (schedule != null) {
                    val diffMillis = schedule.nextPrayerTimeMillis - System.currentTimeMillis()
                    if (diffMillis > 0) {
                        val totalSeconds = diffMillis / 1000
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        _uiState.update {
                            it.copy(countdownText = "%02d:%02d:%02d".format(hours, minutes, seconds))
                        }
                    } else {
                        // Waktu sholat telah tiba, hitung ulang untuk jadwal berikutnya
                        val loc = locationPrefs.locationFlow.first()
                        _uiState.update {
                            it.copy(schedule = getPrayerSchedule(loc.latitude, loc.longitude))
                        }
                    }
                }
                delay(1000L.milliseconds)
            }
        }
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingLocation = true) }
            val result = locationTracker.getCurrentLocation()
            if (result != null) {
                locationPrefs.saveLocation(result.first, result.second, result.third)
                _messages.send(HomeMessage("Jadwal diperbarui untuk ${result.third}"))
            } else {
                _messages.send(
                    HomeMessage(
                        text = "Lokasi belum ditemukan. Pastikan GPS aktif, lalu coba lagi.",
                        actionLabel = "Coba Lagi",
                        action = ::refreshGpsLocation
                    )
                )
            }
            _uiState.update { it.copy(isUpdatingLocation = false) }
        }
    }

    fun onLocationPermissionDenied() {
        val schedulePlace = if (_uiState.value.isDefaultLocation) "Jakarta" else "lokasi terakhir"
        _messages.trySend(
            HomeMessage(
                text = "Izin lokasi ditolak. Jadwal tetap memakai $schedulePlace.",
                actionLabel = "Buka Setelan",
                action = ::openAppSettings
            )
        )
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", appContext.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun cycleReminderMode(prayerName: String) {
        viewModelScope.launch {
            val current = _uiState.value.reminderModes[prayerName] ?: ReminderMode.ADZAN
            val next = current.next()
            locationPrefs.setPrayerReminderMode(prayerName, next)
            // A stray tap can silence a prayer, so every change is announced and undoable
            val text = when (next) {
                ReminderMode.SILENT -> "Pengingat $prayerName dinonaktifkan"
                else -> "$prayerName: ${next.label}"
            }
            _messages.send(
                HomeMessage(
                    text = text,
                    actionLabel = "Urungkan",
                    action = { setReminderMode(prayerName, current) }
                )
            )
        }
    }

    fun setReminderMode(prayerName: String, mode: ReminderMode) {
        viewModelScope.launch {
            locationPrefs.setPrayerReminderMode(prayerName, mode)
        }
    }

    fun updateCustomRegularAudio(uriString: String?, fileName: String) {
        viewModelScope.launch {
            locationPrefs.saveCustomRegularAudio(uriString, fileName)
        }
    }

    fun updateCustomFajrAudio(uriString: String?, fileName: String) {
        viewModelScope.launch {
            locationPrefs.saveCustomFajrAudio(uriString, fileName)
        }
    }

    fun setAdzanVolume(volume: Float) {
        viewModelScope.launch {
            locationPrefs.setAdzanVolume(volume)
        }
    }

    fun testPlayAdzan(
        mode: ReminderMode = ReminderMode.ADZAN,
        isFajr: Boolean = false
    ) {
        val label = if (isFajr) "Simulasi Subuh" else "Simulasi Adzan"
        val intent = Intent(appContext, AdzanForegroundService::class.java).apply {
            action = AdzanForegroundService.ACTION_START_ADZAN
            putExtra(AdzanForegroundService.EXTRA_PRAYER_NAME, label)
            putExtra(AdzanForegroundService.EXTRA_REMINDER_MODE, mode.name)
            putExtra(AdzanForegroundService.EXTRA_IS_TEST, true)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    fun stopAdzan() {
        val intent = Intent(appContext, AdzanForegroundService::class.java).apply {
            action = AdzanForegroundService.ACTION_STOP_ADZAN
        }
        appContext.startService(intent)
    }

}