package com.rfksgn.myprayer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quranDataStore by preferencesDataStore(name = "quran_prefs")

data class LastReadQuran(
    val surahNumber: Int = 1,
    val surahName: String = "Al-Fatihah",
    val ayahNumber: Int = 1
)

class QuranPreferences(private val context: Context) {

    companion object {
        private val LAST_SURAH_NUM_KEY = intPreferencesKey("last_read_surah_num")
        private val LAST_SURAH_NAME_KEY = stringPreferencesKey("last_read_surah_name")
        private val LAST_AYAH_NUM_KEY = intPreferencesKey("last_read_ayah_num")
        private val SHOW_LATIN_KEY = booleanPreferencesKey("quran_show_latin")
    }

    val lastReadQuranFlow: Flow<LastReadQuran> = context.quranDataStore.data.map { prefs ->
        LastReadQuran(
            surahNumber = prefs[LAST_SURAH_NUM_KEY] ?: 1,
            surahName = prefs[LAST_SURAH_NAME_KEY] ?: "Al-Fatihah",
            ayahNumber = prefs[LAST_AYAH_NUM_KEY] ?: 1
        )
    }

    val showLatinFlow: Flow<Boolean> = context.quranDataStore.data.map { prefs ->
        prefs[SHOW_LATIN_KEY] ?: true
    }

    suspend fun saveLastReadQuran(surahNumber: Int, surahName: String, ayahNumber: Int) {
        context.quranDataStore.edit { prefs ->
            prefs[LAST_SURAH_NUM_KEY] = surahNumber
            prefs[LAST_SURAH_NAME_KEY] = surahName
            prefs[LAST_AYAH_NUM_KEY] = ayahNumber
        }
    }

    suspend fun setShowLatin(show: Boolean) {
        context.quranDataStore.edit { prefs ->
            prefs[SHOW_LATIN_KEY] = show
        }
    }
}
