package com.rfksgn.myprayer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rfksgn.myprayer.domain.model.ReminderMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_location_prefs")

data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    // True until a GPS location has been saved; the schedule is then still Jakarta's
    val isDefault: Boolean = false
)

data class CustomAdzanAudio(
    val regularUriString: String? = null,
    val regularFileName: String = "Default (Bawaan Aplikasi)",
    val fajrUriString: String? = null,
    val fajrFileName: String = "Default Subuh (Bawaan Aplikasi)"
)

class LocationPreferences(private val context: Context) {

    companion object {
        private val LAT_KEY = doublePreferencesKey("latitude")
        private val LNG_KEY = doublePreferencesKey("longitude")
        private val CITY_KEY = stringPreferencesKey("city_name")

        // Audio Reguler (Dzuhur, Ashar, Maghrib, Isya)
        private val CUSTOM_URI_KEY = stringPreferencesKey("custom_adzan_uri")
        private val CUSTOM_NAME_KEY = stringPreferencesKey("custom_adzan_name")

        // Audio Khusus Subuh
        private val CUSTOM_FAJR_URI_KEY = stringPreferencesKey("custom_fajr_adzan_uri")
        private val CUSTOM_FAJR_NAME_KEY = stringPreferencesKey("custom_fajr_adzan_name")

        private val ADZAN_VOLUME_KEY = floatPreferencesKey("adzan_volume")
        fun modeKeyFor(prayerName: String) = stringPreferencesKey("reminder_mode_$prayerName")
    }

    // Default ke Jakarta jika belum pernah ambil GPS
    val locationFlow: Flow<SavedLocation> = context.dataStore.data.map { prefs ->
        SavedLocation(
            latitude = prefs[LAT_KEY] ?: -6.1754,
            longitude = prefs[LNG_KEY] ?: 106.8272,
            cityName = prefs[CITY_KEY] ?: "Jakarta Pusat",
            isDefault = prefs[LAT_KEY] == null
        )
    }

    // Menyimpan mode per waktu sholat (ADZAN, VIBRATE, atau SILENT)
    val reminderModesFlow: Flow<Map<String, ReminderMode>> = context.dataStore.data.map { prefs ->
        listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya").associateWith { name ->
            val saved = prefs[modeKeyFor(name)]
            runCatching { ReminderMode.valueOf(saved ?: "ADZAN") }.getOrDefault(ReminderMode.ADZAN)
        }
    }

    // Menyimpan file audio MP3/WAV kustom pilihan user
    val customAudioFlow: Flow<CustomAdzanAudio> = context.dataStore.data.map { prefs ->
        CustomAdzanAudio(
            regularUriString = prefs[CUSTOM_URI_KEY],
            regularFileName = prefs[CUSTOM_NAME_KEY] ?: "Default (Bawaan Aplikasi)",
            fajrUriString = prefs[CUSTOM_FAJR_URI_KEY],
            fajrFileName = prefs[CUSTOM_FAJR_NAME_KEY] ?: "Default Subuh (Bawaan Aplikasi)"
        )
    }

    // Flow Volume Adzan (0.0f sampai 1.0f, default 0.7f / 70%)
    val adzanVolumeFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[ADZAN_VOLUME_KEY] ?: 0.7f
    }

    suspend fun saveLocation(lat: Double, lng: Double, city: String) {
        context.dataStore.edit { prefs ->
            prefs[LAT_KEY] = lat
            prefs[LNG_KEY] = lng
            prefs[CITY_KEY] = city
        }
    }

    suspend fun setPrayerReminderMode(prayerName: String, mode: ReminderMode) {
        context.dataStore.edit { prefs ->
            prefs[modeKeyFor(prayerName)] = mode.name
        }
    }

    suspend fun saveCustomRegularAudio(uriString: String?, fileName: String) {
        context.dataStore.edit { prefs ->
            if (uriString != null) {
                prefs[CUSTOM_URI_KEY] = uriString
                prefs[CUSTOM_NAME_KEY] = fileName
            } else {
                prefs.remove(CUSTOM_URI_KEY)
                prefs[CUSTOM_NAME_KEY] = "Default (Bawaan Aplikasi)"
            }
        }
    }

    suspend fun saveCustomFajrAudio(uriString: String?, fileName: String) {
        context.dataStore.edit { prefs ->
            if (uriString != null) {
                prefs[CUSTOM_FAJR_URI_KEY] = uriString
                prefs[CUSTOM_FAJR_NAME_KEY] = fileName
            } else {
                prefs.remove(CUSTOM_FAJR_URI_KEY)
                prefs[CUSTOM_FAJR_NAME_KEY] = "Default Subuh (Bawaan Aplikasi)"
            }
        }
    }

    suspend fun setAdzanVolume(volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[ADZAN_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }
}