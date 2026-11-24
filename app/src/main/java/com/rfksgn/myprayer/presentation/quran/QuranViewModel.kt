package com.rfksgn.myprayer.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.data.local.LastReadQuran
import com.rfksgn.myprayer.data.local.QuranArabicSizeDefault
import com.rfksgn.myprayer.data.local.QuranKhatam
import com.rfksgn.myprayer.data.local.QuranPreferences
import com.rfksgn.myprayer.data.local.WiridState
import com.rfksgn.myprayer.data.repository.QuranRepository
import com.rfksgn.myprayer.domain.model.QuranIndex
import com.rfksgn.myprayer.domain.model.Wirid
import com.rfksgn.myprayer.presentation.home.HomeMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuranUiState(
    val surahs: List<SurahEntity> = emptyList(),
    val bookmarkedAyahs: List<AyahEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTabIndex: Int = 0, // 0 = Surah, 1 = Juz
    val lastRead: LastReadQuran = LastReadQuran(),
    /** The last read ayah's text, once its surah is cached; the card shows the surah's name until then. */
    val lastReadAyah: AyahEntity? = null,
    val khatam: QuranKhatam = QuranKhatam(),
    /** Today's khatam portion: the juz the khatam stood in when the day began. */
    val todayJuz: Int = 1,
    val wirid: WiridState = WiridState(),
    val showLatin: Boolean = true,
    val arabicSize: Int = QuranArabicSizeDefault,
    // State Halaman Baca Surah
    val activeSurahNumber: Int? = null,
    val activeSurah: SurahEntity? = null,
    val activeAyahs: List<AyahEntity> = emptyList(),
    val initialScrollAyah: Int = 1,
    val isLoadingSurah: Boolean = false,
    val surahLoadError: String? = null,
    /** The open reader moves the khatam forward as it saves. */
    val readingKhatam: Boolean = false,
    // State Unduh 30 Juz Offline
    val isDownloadingFullQuran: Boolean = false,
    val downloadProgressText: String? = null
) {
    val isReaderActive: Boolean
        get() = activeSurahNumber != null || activeSurah != null || isLoadingSurah || surahLoadError != null

    val khatamDone: Boolean get() = khatam.position >= QuranIndex.TOTAL

    fun isWiridDone(w: Wirid, today: Long = LocalDate.now().toEpochDay()): Boolean =
        if (w == Wirid.KHATAM) khatam.position >= QuranIndex.juzEndGlobal(todayJuz) else wirid.doneOn[w] == today
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(
    private val repository: QuranRepository,
    private val preferences: QuranPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private val _messages = Channel<HomeMessage>(Channel.BUFFERED)
    val messages: Flow<HomeMessage> = _messages.receiveAsFlow()

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
            preferences.arabicSizeFlow.collect { size ->
                _uiState.update { it.copy(arabicSize = size) }
            }
        }

        viewModelScope.launch {
            preferences.lastReadQuranFlow
                .map { it.surahNumber to it.ayahNumber }
                .distinctUntilChanged()
                .flatMapLatest { (surah, ayah) -> repository.getAyahFlow(surah, ayah) }
                .collect { ayah -> _uiState.update { it.copy(lastReadAyah = ayah) } }
        }

        viewModelScope.launch {
            preferences.khatamFlow.collect { khatam ->
                val today = LocalDate.now().toEpochDay()
                // The first look on a new day fixes that day's portion
                if (khatam.day != today) preferences.startKhatamDay(today, khatam.position)
                val start = if (khatam.day == today) khatam.dayStart else khatam.position
                _uiState.update {
                    it.copy(khatam = khatam, todayJuz = QuranIndex.juzOf((start + 1).coerceAtMost(QuranIndex.TOTAL)))
                }
            }
        }

        viewModelScope.launch {
            preferences.wiridFlow.collect { wirid -> _uiState.update { it.copy(wirid = wirid) } }
        }
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

    fun openSurah(surahNumber: Int, targetAyah: Int = 1, khatam: Boolean = false) {
        activeSurahJob?.cancel()
        // Switch to the reader now, so the open transition doesn't wait on the database read below
        val listedSurah = _uiState.value.surahs.find { it.number == surahNumber }
        _uiState.update {
            it.copy(
                activeSurahNumber = surahNumber,
                activeSurah = listedSurah,
                activeAyahs = emptyList(),
                isLoadingSurah = listedSurah == null,
                surahLoadError = null,
                initialScrollAyah = targetAyah,
                readingKhatam = khatam
            )
        }
        activeSurahJob = viewModelScope.launch {
            val cachedSurah = repository.getSurahByNumber(surahNumber) ?: listedSurah
            _uiState.update { it.copy(activeSurah = cachedSurah, isLoadingSurah = cachedSurah == null) }

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

    /** The reader's previous/next surah keeps the reading mode, so a khatam runs on into the next surah. */
    fun navigateSurah(surahNumber: Int) = openSurah(surahNumber, 1, khatam = _uiState.value.readingKhatam)

    /** Opens at the ayah after the khatam's place. */
    fun continueKhatam() {
        val next = _uiState.value.khatam.position + 1
        if (next > QuranIndex.TOTAL) return
        val (surah, ayah) = QuranIndex.fromGlobal(next)
        openSurah(surah, ayah, khatam = true)
    }

    /** Continues wherever the last read was: the khatam's next ayah, or the same ayah of the surah read freely. */
    fun resumeLastRead() {
        val state = _uiState.value
        val last = state.lastRead
        if (last.inKhatam && !state.khatamDone) continueKhatam() else openSurah(last.surahNumber, last.ayahNumber)
    }

    fun closeActiveSurah() {
        activeSurahJob?.cancel()
        _uiState.update {
            it.copy(
                activeSurahNumber = null,
                activeSurah = null,
                activeAyahs = emptyList(),
                isLoadingSurah = false,
                surahLoadError = null,
                readingKhatam = false
            )
        }
    }

    fun markAsLastRead(surah: SurahEntity, ayahNumber: Int) {
        val inKhatam = _uiState.value.readingKhatam
        viewModelScope.launch {
            preferences.saveLastReadQuran(surah.number, surah.nameLatin, ayahNumber, inKhatam)
            // Reaching a surah's last ayah ticks the wirid that reads it
            if (ayahNumber >= surah.versesCount) {
                val today = LocalDate.now().toEpochDay()
                _uiState.value.wirid.active.filter { it.surah == surah.number }.forEach {
                    preferences.setWiridDone(it, today)
                }
            }
        }
    }

    /** A tick by hand, for a reading done from a paper mushaf. Each one can be undone from the snackbar. */
    fun toggleWiridDone(wirid: Wirid) {
        val state = _uiState.value
        viewModelScope.launch {
            if (wirid == Wirid.KHATAM) {
                if (state.isWiridDone(Wirid.KHATAM)) {
                    _messages.send(HomeMessage("Juz ${state.todayJuz} sudah selesai lewat posisi baca khatam."))
                    return@launch
                }
                val before = state.khatam
                preferences.setKhatam(QuranIndex.juzEndGlobal(state.todayJuz), before.round)
                _messages.send(HomeMessage("Juz ${state.todayJuz} ditandai sudah dibaca", "Urungkan") {
                    viewModelScope.launch { preferences.setKhatam(before.position, before.round) }
                })
            } else {
                val today = LocalDate.now().toEpochDay()
                val previous = state.wirid.doneOn[wirid]
                val done = previous == today
                preferences.setWiridDone(wirid, if (done) null else today)
                _messages.send(
                    HomeMessage(
                        if (done) "Centang ${wirid.label} dilepas" else "${wirid.label} ditandai sudah dibaca",
                        "Urungkan"
                    ) { viewModelScope.launch { preferences.setWiridDone(wirid, previous) } }
                )
            }
        }
    }

    fun toggleWiridActive(wirid: Wirid) {
        val active = _uiState.value.wirid.active
        viewModelScope.launch {
            preferences.setWiridActive(if (wirid in active) active - wirid else active + wirid)
        }
    }

    fun startNewKhatam() {
        val round = _uiState.value.khatam.round + 1
        viewModelScope.launch {
            preferences.setKhatam(0, round)
            preferences.startKhatamDay(LocalDate.now().toEpochDay(), 0)
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
                    it.copy(downloadProgressText = "Menyimpan surah $current dari $total")
                }
            }
            _uiState.update {
                it.copy(
                    isDownloadingFullQuran = false,
                    downloadProgressText = if (success) "Semua surah tersimpan di HP" else "Unduhan berhenti. Sambungkan internet, lalu coba lagi."
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
