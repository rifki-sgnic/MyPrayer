package com.rfksgn.myprayer.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.data.local.LastReadQuran
import com.rfksgn.myprayer.data.local.QuranArabicSizeDefault
import com.rfksgn.myprayer.data.local.QuranPreferences
import com.rfksgn.myprayer.data.local.QuranReadingMode
import com.rfksgn.myprayer.data.repository.QuranRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranUiState(
    val surahs: List<SurahEntity> = emptyList(),
    val bookmarkedAyahs: List<AyahEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTabIndex: Int = 0, // 0 = Daftar Surah, 1 = Bookmark Tersimpan
    val lastRead: LastReadQuran = LastReadQuran(),
    val showLatin: Boolean = true,
    val readingMode: QuranReadingMode = QuranReadingMode.AUTO,
    val arabicSize: Int = QuranArabicSizeDefault,
    // State Halaman Baca Surah
    val activeSurahNumber: Int? = null,
    val activeSurah: SurahEntity? = null,
    val activeAyahs: List<AyahEntity> = emptyList(),
    val initialScrollAyah: Int = 1,
    val isLoadingSurah: Boolean = false,
    val surahLoadError: String? = null,
    // State Unduh 30 Juz Offline
    val isDownloadingFullQuran: Boolean = false,
    val downloadProgressText: String? = null
)

class QuranViewModel(
    private val repository: QuranRepository,
    private val preferences: QuranPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private var activeSurahJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        viewModelScope.launch {
            combine(
                repository.allSurahsFlow,
                repository.bookmarkedAyahsFlow,
                preferences.lastReadQuranFlow,
                preferences.showLatinFlow
            ) { surahs, bookmarks, lastRead, showLatin ->
                QuranCombinedData(surahs, bookmarks, lastRead, showLatin)
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        surahs = data.surahs,
                        bookmarkedAyahs = data.bookmarks,
                        lastRead = data.lastRead,
                        showLatin = data.showLatin
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(preferences.readingModeFlow, preferences.arabicSizeFlow, ::Pair).collect { (mode, size) ->
                _uiState.update { it.copy(readingMode = mode, arabicSize = size) }
            }
        }
    }

    fun setReadingMode(mode: QuranReadingMode) {
        viewModelScope.launch { preferences.setReadingMode(mode) }
    }

    fun setArabicSize(index: Int) {
        viewModelScope.launch { preferences.setArabicSize(index) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun openSurah(surahNumber: Int, targetAyah: Int = 1) {
        activeSurahJob?.cancel()
        activeSurahJob = viewModelScope.launch {
            val cachedSurah = repository.getSurahByNumber(surahNumber)
                ?: _uiState.value.surahs.find { it.number == surahNumber }

            _uiState.update {
                it.copy(
                    activeSurahNumber = surahNumber,
                    activeSurah = cachedSurah,
                    activeAyahs = emptyList(),
                    isLoadingSurah = cachedSurah == null,
                    surahLoadError = null,
                    initialScrollAyah = targetAyah
                )
            }

            // Immediately observe local database cache
            launch {
                repository.getAyahsFlow(surahNumber).collect { ayahs ->
                    _uiState.update { state ->
                        state.copy(
                            activeAyahs = ayahs,
                            isLoadingSurah = ayahs.isEmpty() && state.isLoadingSurah
                        )
                    }
                }
            }

            // Synchronize with remote API if verses are missing locally
            val loadSuccess = repository.ensureSurahAyahsLoaded(surahNumber)
            val updatedSurah = repository.getSurahByNumber(surahNumber) ?: cachedSurah

            _uiState.update { state ->
                val hasContent = state.activeAyahs.isNotEmpty() || (updatedSurah != null && updatedSurah.versesCount == 0)
                state.copy(
                    activeSurah = updatedSurah,
                    isLoadingSurah = false,
                    surahLoadError = if (!loadSuccess && !hasContent) {
                        "Gagal memuat Surah. Periksa koneksi internet Anda."
                    } else null
                )
            }
        }
    }

    fun closeActiveSurah() {
        activeSurahJob?.cancel()
        _uiState.update {
            it.copy(
                activeSurahNumber = null,
                activeSurah = null,
                activeAyahs = emptyList(),
                isLoadingSurah = false,
                surahLoadError = null
            )
        }
    }

    fun markAsLastRead(surah: SurahEntity, ayahNumber: Int) {
        viewModelScope.launch {
            preferences.saveLastReadQuran(surah.number, surah.nameLatin, ayahNumber)
        }
    }

    fun toggleBookmark(ayah: AyahEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(ayah)
        }
    }

    fun toggleShowLatin() {
        viewModelScope.launch {
            preferences.setShowLatin(!_uiState.value.showLatin)
        }
    }

    fun downloadAll30JuzOffline() {
        if (_uiState.value.isDownloadingFullQuran) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloadingFullQuran = true,
                    downloadProgressText = "Menyiapkan..."
                )
            }
            val success = repository.downloadFullQuranOffline { current, total ->
                _uiState.update {
                    it.copy(downloadProgressText = "Mengunduh Surah $current/$total")
                }
            }
            _uiState.update {
                it.copy(
                    isDownloadingFullQuran = false,
                    downloadProgressText = if (success) "30 Juz Tersimpan Offline ✓" else "Gagal mengunduh, cek koneksi"
                )
            }
        }
    }

    private data class QuranCombinedData(
        val surahs: List<SurahEntity>,
        val bookmarks: List<AyahEntity>,
        val lastRead: LastReadQuran,
        val showLatin: Boolean
    )
}