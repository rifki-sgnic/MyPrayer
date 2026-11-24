package com.rfksgn.myprayer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rfksgn.myprayer.domain.model.QuranIndex
import com.rfksgn.myprayer.domain.model.Wirid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quranDataStore by preferencesDataStore(name = "quran_prefs")

data class LastReadQuran(
    val surahNumber: Int = 1,
    val surahName: String = "Al-Fatihah",
    val ayahNumber: Int = 1,
    /** Epoch millis; 0 when it was saved before this was recorded, or never. */
    val readAt: Long = 0L,
    /** Read as part of the khatam, so "continue" means the khatam's next ayah. */
    val inKhatam: Boolean = false
)

/**
 * The khatam's own place, apart from the last read: reading Al-Mulk at night must not move it.
 * [position] is the last ayah read in order, as a global index (0 = not started). [dayStart] is where it stood
 * when [day] (epoch day) began, which fixes today's juz.
 */
data class QuranKhatam(
    val position: Int = 0,
    val round: Int = 1,
    val day: Long = -1L,
    val dayStart: Int = 0
)

data class WiridState(
    val active: List<Wirid> = emptyList(),
    /** The epoch day each wirid was last ticked. */
    val doneOn: Map<Wirid, Long> = emptyMap()
)

/** Arabic ayah sizes in sp, picked by index in the reader's Tampilan sheet. */
val QuranArabicSizes = listOf(26, 30, 34, 38)
const val QuranArabicSizeDefault = 1

class QuranPreferences(private val context: Context) {

    companion object {
        private val LAST_SURAH_NUM_KEY = intPreferencesKey("last_read_surah_num")
        private val LAST_SURAH_NAME_KEY = stringPreferencesKey("last_read_surah_name")
        private val LAST_AYAH_NUM_KEY = intPreferencesKey("last_read_ayah_num")
        private val LAST_READ_AT_KEY = longPreferencesKey("last_read_at")
        private val LAST_READ_KHATAM_KEY = booleanPreferencesKey("last_read_in_khatam")
        private val SHOW_LATIN_KEY = booleanPreferencesKey("quran_show_latin")
        private val ARABIC_SIZE_KEY = intPreferencesKey("quran_arabic_size")
        private val KHATAM_POSITION_KEY = intPreferencesKey("khatam_position")
        private val KHATAM_ROUND_KEY = intPreferencesKey("khatam_round")
        private val KHATAM_DAY_KEY = longPreferencesKey("khatam_day")
        private val KHATAM_DAY_START_KEY = intPreferencesKey("khatam_day_start")
        private val WIRID_ACTIVE_KEY = stringPreferencesKey("wirid_active")
        private val WIRID_DONE_KEY = stringSetPreferencesKey("wirid_done")
    }

    val arabicSizeFlow: Flow<Int> = context.quranDataStore.data.map { prefs ->
        (prefs[ARABIC_SIZE_KEY] ?: QuranArabicSizeDefault).coerceIn(QuranArabicSizes.indices)
    }

    suspend fun setArabicSize(index: Int) {
        context.quranDataStore.edit { it[ARABIC_SIZE_KEY] = index.coerceIn(QuranArabicSizes.indices) }
    }

    val lastReadQuranFlow: Flow<LastReadQuran> = context.quranDataStore.data.map { prefs ->
        LastReadQuran(
            surahNumber = prefs[LAST_SURAH_NUM_KEY] ?: 1,
            surahName = prefs[LAST_SURAH_NAME_KEY] ?: "Al-Fatihah",
            ayahNumber = prefs[LAST_AYAH_NUM_KEY] ?: 1,
            readAt = prefs[LAST_READ_AT_KEY] ?: 0L,
            inKhatam = prefs[LAST_READ_KHATAM_KEY] ?: false
        )
    }

    val showLatinFlow: Flow<Boolean> = context.quranDataStore.data.map { prefs ->
        prefs[SHOW_LATIN_KEY] ?: true
    }

    suspend fun saveLastReadQuran(surahNumber: Int, surahName: String, ayahNumber: Int, inKhatam: Boolean) {
        context.quranDataStore.edit { prefs ->
            prefs[LAST_SURAH_NUM_KEY] = surahNumber
            prefs[LAST_SURAH_NAME_KEY] = surahName
            prefs[LAST_AYAH_NUM_KEY] = ayahNumber
            prefs[LAST_READ_AT_KEY] = System.currentTimeMillis()
            prefs[LAST_READ_KHATAM_KEY] = inKhatam
            if (inKhatam) {
                // Only forward: scrolling back to re-read an ayah doesn't undo the khatam's progress
                val g = QuranIndex.globalIndex(surahNumber, ayahNumber)
                if (g > (prefs[KHATAM_POSITION_KEY] ?: 0)) prefs[KHATAM_POSITION_KEY] = g
            }
        }
    }

    suspend fun setShowLatin(show: Boolean) {
        context.quranDataStore.edit { prefs ->
            prefs[SHOW_LATIN_KEY] = show
        }
    }

    val khatamFlow: Flow<QuranKhatam> = context.quranDataStore.data.map { prefs ->
        QuranKhatam(
            position = prefs[KHATAM_POSITION_KEY] ?: 0,
            round = prefs[KHATAM_ROUND_KEY] ?: 1,
            day = prefs[KHATAM_DAY_KEY] ?: -1L,
            dayStart = prefs[KHATAM_DAY_START_KEY] ?: 0
        )
    }

    /** Sets the khatam's place outright: a manual tick, its undo, or a new round from Al-Fatihah. */
    suspend fun setKhatam(position: Int, round: Int) {
        context.quranDataStore.edit { prefs ->
            prefs[KHATAM_POSITION_KEY] = position.coerceIn(0, QuranIndex.TOTAL)
            prefs[KHATAM_ROUND_KEY] = round
        }
    }

    suspend fun startKhatamDay(day: Long, position: Int) {
        context.quranDataStore.edit { prefs ->
            prefs[KHATAM_DAY_KEY] = day
            prefs[KHATAM_DAY_START_KEY] = position
        }
    }

    val wiridFlow: Flow<WiridState> = context.quranDataStore.data.map { prefs ->
        WiridState(
            active = prefs[WIRID_ACTIVE_KEY].orEmpty().split(',').mapNotNull { name ->
                Wirid.entries.firstOrNull { it.name == name }
            },
            doneOn = prefs[WIRID_DONE_KEY].orEmpty().mapNotNull { entry ->
                val wirid = Wirid.entries.firstOrNull { it.name == entry.substringBefore(':') }
                val day = entry.substringAfter(':').toLongOrNull()
                if (wirid != null && day != null) wirid to day else null
            }.toMap()
        )
    }

    suspend fun setWiridActive(active: List<Wirid>) {
        context.quranDataStore.edit { it[WIRID_ACTIVE_KEY] = active.joinToString(",") { w -> w.name } }
    }

    /** Ticks [wirid] for [day], or clears its tick when [day] is null. Only the latest day per wirid is kept. */
    suspend fun setWiridDone(wirid: Wirid, day: Long?) {
        context.quranDataStore.edit { prefs ->
            val others = prefs[WIRID_DONE_KEY].orEmpty().filterNot { it.substringBefore(':') == wirid.name }
            prefs[WIRID_DONE_KEY] = if (day == null) others.toSet() else others.toSet() + "${wirid.name}:$day"
        }
    }
}
