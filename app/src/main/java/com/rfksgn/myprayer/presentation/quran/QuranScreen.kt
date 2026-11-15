package com.rfksgn.myprayer.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity

private val CardShape22 = RoundedCornerShape(22.dp)
private val CardShape20 = RoundedCornerShape(20.dp)
private val CardShape18 = RoundedCornerShape(18.dp)

private val ArabicAyahTextStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 2.0.em,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.End
)

private val LatinAyahTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 1.45.em,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Medium
)

private val TranslationAyahTextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 1.5.em
)

private val AyahNumberBadgeTextStyle = TextStyle(
    fontSize = 13.sp,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum"
)

private val SurahTitleSerifStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 19.sp,
    fontWeight = FontWeight.Bold
)

private val SurahArabicCalligraphyStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold
)

@Composable
fun QuranScreen(
    uiState: QuranUiState,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
    onCloseSurah: () -> Unit,
    onToggleBookmark: (AyahEntity) -> Unit,
    onMarkLastRead: (SurahEntity, Int) -> Unit,
    onToggleShowLatin: () -> Unit,
    onDownloadFullQuran: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surahMap = remember(uiState.surahs) {
        uiState.surahs.associateBy { it.number }
    }

    val isReaderActive = uiState.activeSurahNumber != null || uiState.activeSurah != null || uiState.isLoadingSurah || uiState.surahLoadError != null
    if (isReaderActive) {
        BackHandler(onBack = onCloseSurah)

        val currentNum = uiState.activeSurahNumber ?: uiState.activeSurah?.number ?: 1
        val prevSurah = remember(currentNum, surahMap) { surahMap[currentNum - 1] }
        val nextSurah = remember(currentNum, surahMap) { surahMap[currentNum + 1] }

        SurahReaderView(
            surah = uiState.activeSurah,
            targetSurahNumber = currentNum,
            prevSurah = prevSurah,
            nextSurah = nextSurah,
            ayahs = uiState.activeAyahs,
            initialScrollAyah = uiState.initialScrollAyah,
            lastReadAyahNumber = if (uiState.lastRead.surahNumber == uiState.activeSurah?.number) {
                uiState.lastRead.ayahNumber
            } else -1,
            showLatin = uiState.showLatin,
            isLoading = uiState.isLoadingSurah,
            errorMessage = uiState.surahLoadError,
            onBackClick = onCloseSurah,
            onRetry = { onOpenSurah(currentNum, uiState.initialScrollAyah) },
            onNavigateSurah = { targetSurahNumber -> onOpenSurah(targetSurahNumber, 1) },
            onToggleShowLatin = onToggleShowLatin,
            onToggleBookmark = onToggleBookmark,
            onMarkLastRead = { ayahNum ->
                val surah = uiState.activeSurah
                if (surah != null) onMarkLastRead(surah, ayahNum)
            },
            modifier = modifier
        )
        return
    }

    val filteredSurahs = remember(uiState.surahs, uiState.searchQuery) {
        val q = uiState.searchQuery.trim()
        if (q.isEmpty()) {
            uiState.surahs
        } else {
            uiState.surahs.filter {
                it.nameLatin.contains(q, ignoreCase = true) ||
                        it.nameTranslation.contains(q, ignoreCase = true) ||
                        it.number.toString() == q
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER TITLE & OFFLINE SYNC ACTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Al-Quran",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Surface(
                onClick = onDownloadFullQuran,
                enabled = !uiState.isDownloadingFullQuran,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.isDownloadingFullQuran) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Simpan 30 Juz Offline",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = uiState.downloadProgressText ?: "Simpan 30 Juz",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. SEARCH BAR PILL
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Cari Surah atau nomor...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus")
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 3. TERRACOTTA "TERAKHIR DIBACA" BANNER CARD
        Surface(
            onClick = {
                onOpenSurah(uiState.lastRead.surahNumber, uiState.lastRead.ayahNumber)
            },
            shape = CardShape22,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Terakhir Dibaca",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Text(
                        text = "${uiState.lastRead.surahName}: Ayat ${uiState.lastRead.ayahNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // 4. FILTER PILLS: DAFTAR SURAH vs BOOKMARK AYAT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterTabPill(
                title = "Daftar Surah (${uiState.surahs.size})",
                selected = uiState.selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            FilterTabPill(
                title = "Bookmark (${uiState.bookmarkedAyahs.size})",
                selected = uiState.selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // 5. LIST SURAH ATAU LIST BOOKMARK (Dengan contentType untuk optimasi recycling)
        if (uiState.selectedTabIndex == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = filteredSurahs,
                    key = { it.number },
                    contentType = { "surah_card" }
                ) { surah ->
                    val onClickRemembered = remember(surah.number) {
                        { onOpenSurah(surah.number, 1) }
                    }
                    SurahItemCard(
                        surah = surah,
                        onClick = onClickRemembered
                    )
                }
            }
        } else {
            if (uiState.bookmarkedAyahs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada ayat yang disimpan ke Bookmark.\nKetuk ikon pita pada ayat saat membaca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.bookmarkedAyahs,
                        key = { it.id },
                        contentType = { "bookmark_card" }
                    ) { ayah ->
                        val surahName = surahMap[ayah.surahNumber]?.nameLatin ?: "Surah ${ayah.surahNumber}"
                        BookmarkedAyahCard(
                            surahName = surahName,
                            ayah = ayah,
                            onOpenClick = { onOpenSurah(ayah.surahNumber, ayah.ayahNumber) },
                            onRemoveBookmark = { onToggleBookmark(ayah) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SurahItemCard(
    surah: SurahEntity,
    onClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val border = remember(outlineColor) { BorderStroke(1.dp, outlineColor.copy(alpha = 0.6f)) }

    Surface(
        onClick = onClick,
        shape = CardShape20,
        color = MaterialTheme.colorScheme.surface,
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${surah.number}. ${surah.nameLatin}",
                    style = SurahTitleSerifStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${surah.versesCount} Ayat • ${surah.revelationType} • ${surah.nameTranslation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = surah.nameArabic,
                style = SurahArabicCalligraphyStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BookmarkedAyahCard(
    surahName: String,
    ayah: AyahEntity,
    onOpenClick: () -> Unit,
    onRemoveBookmark: () -> Unit
) {
    Surface(
        onClick = onOpenClick,
        shape = CardShape20,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$surahName • Ayat ${ayah.ayahNumber}",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(
                    onClick = onRemoveBookmark,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Hapus Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = ayah.textArabic,
                style = ArabicAyahTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = ayah.translationId,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SurahReaderView(
    surah: SurahEntity?,
    targetSurahNumber: Int,
    prevSurah: SurahEntity?,
    nextSurah: SurahEntity?,
    ayahs: List<AyahEntity>,
    initialScrollAyah: Int,
    lastReadAyahNumber: Int,
    showLatin: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onToggleShowLatin: () -> Unit,
    onToggleBookmark: (AyahEntity) -> Unit,
    onMarkLastRead: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var hasScrolledToInitial by remember(surah?.number, initialScrollAyah) { mutableStateOf(false) }

    // Reset posisi scroll saat berpindah ke surah baru atau loncat ke ayat terakhir dibaca
    LaunchedEffect(surah?.number, initialScrollAyah, ayahs.isNotEmpty()) {
        if (ayahs.isNotEmpty() && !hasScrolledToInitial) {
            val hasBasmalah = surah != null && surah.number != 1 && surah.number != 9
            val headerOffset = if (hasBasmalah) 1 else 0
            if (initialScrollAyah > 1) {
                val targetIndex = (initialScrollAyah - 1 + headerOffset).coerceIn(0, ayahs.lastIndex + headerOffset)
                listState.scrollToItem(targetIndex)
            } else {
                listState.scrollToItem(0)
            }
            hasScrolledToInitial = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. TOP READER BAR: Back, Quick Prev/Next Surah, Title, dan Toggle Latin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Judul Surah + Tombol Quick Prev/Next di kiri-kanannya
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { if (prevSurah != null) onNavigateSurah(prevSurah.number) },
                    enabled = prevSurah != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Surah Sebelumnya",
                        tint = if (prevSurah != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = surah?.nameLatin ?: "Surah $targetSurahNumber",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    if (surah != null) {
                        Text(
                            text = "Surah ke-${surah.number} • ${surah.versesCount} Ayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { if (nextSurah != null) onNavigateSurah(nextSurah.number) },
                    enabled = nextSurah != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Surah Selanjutnya",
                        tint = if (nextSurah != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }
                    )
                }
            }

            // Tombol Pill Toggle Teks Latin On/Off
            Surface(
                onClick = onToggleShowLatin,
                shape = CircleShape,
                color = if (showLatin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Toggle Latin",
                        modifier = Modifier.size(14.dp),
                        tint = if (showLatin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Latin",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (showLatin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isLoading && ayahs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (errorMessage != null && ayahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry) {
                        Text("Coba Lagi")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // Header Basmalah
                if (surah != null && surah.number != 1 && surah.number != 9) {
                    item(key = "basmalah_header", contentType = "header") {
                        Surface(
                            shape = CardShape18,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Daftar Ayat dengan contentType agar Node Layout di-recycle secara optimal
                items(
                    items = ayahs,
                    key = { it.id },
                    contentType = { "ayah_card" }
                ) { ayah ->
                    val onBookmarkClick = remember(ayah.id, ayah.isBookmarked) {
                        { onToggleBookmark(ayah) }
                    }
                    val onLastReadClick = remember(ayah.ayahNumber) {
                        { onMarkLastRead(ayah.ayahNumber) }
                    }

                    AyahReaderCard(
                        ayah = ayah,
                        showLatin = showLatin,
                        isLastRead = ayah.ayahNumber == lastReadAyahNumber,
                        onToggleBookmark = onBookmarkClick,
                        onMarkLastRead = onLastReadClick
                    )
                }

                // Footer Navigasi Surah Sebelumnya & Selanjutnya di bagian paling bawah
                if (prevSurah != null || nextSurah != null) {
                    item(key = "surah_footer_nav", contentType = "footer") {
                        SurahFooterNavigation(
                            prevSurah = prevSurah,
                            nextSurah = nextSurah,
                            onNavigateSurah = onNavigateSurah
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahFooterNavigation(
    prevSurah: SurahEntity?,
    nextSurah: SurahEntity?,
    onNavigateSurah: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tombol Surah Sebelumnya (Kiri)
        if (prevSurah != null) {
            Surface(
                onClick = { onNavigateSurah(prevSurah.number) },
                shape = CardShape18,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sebelumnya",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${prevSurah.number}. ${prevSurah.nameLatin}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Tombol Surah Selanjutnya (Kanan)
        if (nextSurah != null) {
            Surface(
                onClick = { onNavigateSurah(nextSurah.number) },
                shape = CardShape18,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Selanjutnya",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "${nextSurah.number}. ${nextSurah.nameLatin}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AyahReaderCard(
    ayah: AyahEntity,
    showLatin: Boolean,
    isLastRead: Boolean,
    onToggleBookmark: () -> Unit,
    onMarkLastRead: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val cardBorder = remember(isLastRead, primaryColor, outlineColor) {
        BorderStroke(
            width = if (isLastRead) 1.5.dp else 1.dp,
            color = if (isLastRead) primaryColor else outlineColor.copy(alpha = 0.6f)
        )
    }

    Surface(
        shape = CardShape22,
        color = MaterialTheme.colorScheme.surface,
        border = cardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Baris Atas: Badge Nomor Ayat (Kiri) + Teks Arab Utsmani (Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.ayahNumber}",
                        style = AyahNumberBadgeTextStyle,
                        color = onSurfaceColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = ayah.textArabic,
                    style = ArabicAyahTextStyle,
                    color = onSurfaceColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Teks Transliterasi Latin (Jika diaktifkan)
            if (showLatin && ayah.textLatin.isNotBlank()) {
                Text(
                    text = ayah.textLatin,
                    style = LatinAyahTextStyle,
                    color = primaryColor
                )
            }

            // Teks Terjemahan Bahasa Indonesia Kemenag RI
            Text(
                text = ayah.translationId,
                style = TranslationAyahTextStyle,
                color = onSurfaceColor.copy(alpha = 0.9f)
            )

            // Baris Aksi Bawah: Tandai Terakhir Dibaca & Bookmark Ayat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onMarkLastRead,
                    shape = CircleShape,
                    color = if (isLastRead) {
                        primaryColor.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = "Tandai Terakhir Dibaca",
                            modifier = Modifier.size(15.dp),
                            tint = if (isLastRead) primaryColor else onSurfaceVariantColor
                        )
                        Text(
                            text = if (isLastRead) "Terakhir Dibaca ✓" else "Tandai Dibaca",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLastRead) primaryColor else onSurfaceVariantColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = onToggleBookmark,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (ayah.isBookmarked) {
                            primaryColor
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (ayah.isBookmarked) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            primaryColor
                        }
                    ),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Ayat",
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}