package com.rfksgn.myprayer.presentation.home

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isUpdatingLocation: Boolean = false,
    val reminderModes: Map<String, ReminderMode> = emptyMap(),
    val customAudio: CustomAdzanAudio = CustomAdzanAudio(),
    val adzanVolume: Float = 0.7f
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
                val schedule = getPrayerSchedule(savedLoc.latitude, savedLoc.longitude)
                alarmScheduler.scheduleDailyAlarms(schedule, modesMap)
                Triple(schedule, savedLoc.cityName, modesMap)
            }.collect { (schedule, cityName, modesMap) ->
                _uiState.update {
                    it.copy(
                        schedule = schedule,
                        locationName = cityName,
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
                if (schedule != null) {
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
            }
            _uiState.update { it.copy(isUpdatingLocation = false) }
        }
    }

    fun cycleReminderMode(prayerName: String) {
        viewModelScope.launch {
            val current = _uiState.value.reminderModes[prayerName] ?: ReminderMode.ADZAN
            locationPrefs.setPrayerReminderMode(prayerName, current.next())
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