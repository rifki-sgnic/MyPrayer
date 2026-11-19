package com.rfksgn.myprayer.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.ui.theme.QuranFontFamily
import com.rfksgn.myprayer.ui.theme.QuranNightLatin
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val CardShape22 = RoundedCornerShape(22.dp)
private val CardShape20 = RoundedCornerShape(20.dp)
private val CardShape16 = RoundedCornerShape(16.dp)

private val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

// Arabic is always laid out right to left, whatever the app's layout direction
private val ArabicAyahTextStyle = TextStyle(
    fontFamily = QuranFontFamily,
    fontSize = 30.sp,
    lineHeight = 2.15.em,
    textDirection = TextDirection.Rtl,
    textAlign = TextAlign.Start
)

private val BasmalahTextStyle = ArabicAyahTextStyle.copy(
    fontSize = 27.sp,
    lineHeight = 1.9.em,
    textAlign = TextAlign.Center
)

private val SurahOpeningArabicStyle = TextStyle(
    fontFamily = QuranFontFamily,
    fontSize = 40.sp,
    lineHeight = 1.5.em,
    textDirection = TextDirection.Rtl
)

private val SurahOpeningTitleStyle = TextStyle(
    fontSize = 24.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.Bold
)

private val ReaderMetaTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontFeatureSettings = "tnum"
)

private val LatinAyahTextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 23.sp,
    fontStyle = FontStyle.Italic
)

private val TranslationAyahTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 26.sp
)

private val SurahTitleSerifStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 19.sp,
    fontWeight = FontWeight.Bold
)

private val SurahArabicCalligraphyStyle = TextStyle(
    fontFamily = QuranFontFamily,
    fontSize = 26.sp,
    textDirection = TextDirection.Rtl
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
                        text = "Belum ada ayat yang disimpan ke Bookmark.\nKetuk ayat saat membaca, lalu pilih Simpan ayat.",
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
    // Locked when the surah opens, so the marker stays put while the reading position auto-saves
    val resumeAyah = remember(surah?.number) { lastReadAyahNumber }
    var openAyahId by remember(surah?.number) { mutableStateOf<Int?>(null) }
    // Bumped each time the reader lands on the resume ayah, which replays its ribbon and glow
    var arrival by remember(surah?.number) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    // A strip of space above the landed ayah, so its hairline and ribbon aren't pressed under the bar
    val landingGapPx = with(LocalDensity.current) { 16.dp.roundToPx() }

    // Item 0 is the surah opening, so ayah n sits at index n
    LaunchedEffect(surah?.number, initialScrollAyah, ayahs.isNotEmpty()) {
        if (ayahs.isNotEmpty() && !hasScrolledToInitial) {
            if (initialScrollAyah > 1) {
                listState.scrollToItem(initialScrollAyah.coerceAtMost(ayahs.size), -landingGapPx)
                if (initialScrollAyah == resumeAyah) arrival++
            } else {
                listState.scrollToItem(0)
            }
            hasScrolledToInitial = true
        }
    }

    // Auto-save the last read ayah whenever the reader stops scrolling
    val currentAyahs by rememberUpdatedState(ayahs)
    val currentOnMarkLastRead by rememberUpdatedState(onMarkLastRead)
    LaunchedEffect(listState, surah?.number, hasScrolledToInitial) {
        if (!hasScrolledToInitial) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }
            .drop(1)
            .filter { !it }
            .collect {
                val item = listState.readingItem()
                currentAyahs.find { it.id == item?.key }?.let { currentOnMarkLastRead(it.ayahNumber) }
            }
    }

    // The ayah the bar reports, which is also the one the auto-save stores
    val readingAyah by remember { derivedStateOf { listState.readingItem()?.index ?: 1 } }
    // The number ticks up on the layer only, so a fast fling costs no extra compositions
    val numberTick = remember { Animatable(1f) }
    LaunchedEffect(readingAyah) {
        numberTick.snapTo(0f)
        numberTick.animateTo(1f, tween(220, easing = EaseOutExpo))
    }

    // Jump-to-resume transition, applied to the list's layer
    val pageAlpha = remember { Animatable(1f) }
    val pageRise = remember { Animatable(0f) }
    val risePx = with(LocalDensity.current) { 28.dp.toPx() }
    var jumping by remember { mutableStateOf(false) }

    // Offer the way back only while the resume ayah is still ahead of (below) the reader
    val peekPx = with(LocalDensity.current) { 40.dp.toPx() }
    val resumeAhead by remember(resumeAyah, peekPx) {
        derivedStateOf {
            if (!hasScrolledToInitial || resumeAyah <= 1 || resumeAyah > currentAyahs.size) return@derivedStateOf false
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            val item = info.visibleItemsInfo.firstOrNull { it.index == resumeAyah }
            if (item != null) item.offset > info.viewportEndOffset - peekPx else last.index < resumeAyah
        }
    }

    val condenseAfterPx = with(LocalDensity.current) { 104.dp.toPx() }
    val isCondensed by remember(condenseAfterPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > condenseAfterPx
        }
    }
    val condensedFraction by animateFloatAsState(
        targetValue = if (isCondensed) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = EaseOutExpo),
        label = "readerBarCondense"
    )

    val hairline = MaterialTheme.colorScheme.outline
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact bar: the title and a reading-progress line appear once the opening scrolls away
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp)
                .drawBehind {
                    if (condensedFraction == 0f) return@drawBehind
                    val y = size.height - 0.5.dp.toPx()
                    drawLine(hairline.copy(alpha = condensedFraction), Offset(-4.dp.toPx(), y), Offset(size.width + 4.dp.toPx(), y), 1.dp.toPx())
                    val fullWidth = size.width + 8.dp.toPx()
                    drawLine(
                        progressColor.copy(alpha = condensedFraction),
                        Offset(-4.dp.toPx(), y),
                        Offset(-4.dp.toPx() + fullWidth * listState.readingProgress(), y),
                        2.dp.toPx()
                    )
                }
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            if (condensedFraction > 0f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp)
                        .graphicsLayer {
                            alpha = condensedFraction
                            translationY = (1f - condensedFraction) * 6.dp.toPx()
                        }
                ) {
                    Text(
                        text = surah?.nameLatin ?: "Surah $targetSurahNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (surah != null) {
                        val metaStyle = ReaderMetaTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp)
                        val metaColor = MaterialTheme.colorScheme.onSurfaceVariant
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Ayat ", style = metaStyle, color = metaColor)
                            Text(
                                text = "$readingAyah",
                                style = metaStyle,
                                color = metaColor,
                                modifier = Modifier.graphicsLayer {
                                    alpha = 0.3f + 0.7f * numberTick.value
                                    translationY = (1f - numberTick.value) * 5.dp.toPx()
                                }
                            )
                            Text(text = " dari ${surah.versesCount}", style = metaStyle, color = metaColor)
                        }
                    }
                }
            }

            IconToggleButton(
                checked = showLatin,
                onCheckedChange = { onToggleShowLatin() },
                colors = IconButtonDefaults.iconToggleButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(imageVector = Icons.Default.Translate, contentDescription = "Teks Latin")
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
        } else Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = pageAlpha.value
                        translationY = pageRise.value
                    }
            ) {
                item(key = "surah_opening", contentType = "opening") {
                    SurahOpening(
                        surah = surah,
                        targetSurahNumber = targetSurahNumber,
                        showBasmalah = surah != null && surah.number != 1 && surah.number != 9
                    )
                }

                items(
                    items = ayahs,
                    key = { it.id },
                    contentType = { "ayah" }
                ) { ayah ->
                    AyahBlock(
                        ayah = ayah,
                        showLatin = showLatin,
                        isResumePoint = ayah.ayahNumber == resumeAyah && resumeAyah > 1,
                        arrival = if (ayah.ayahNumber == resumeAyah) arrival else 0,
                        isOpen = openAyahId == ayah.id,
                        onClick = { openAyahId = if (openAyahId == ayah.id) null else ayah.id },
                        onToggleBookmark = { onToggleBookmark(ayah) }
                    )
                }

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

            androidx.compose.animation.AnimatedVisibility(
                // Steps aside while an ayah is open, so it never covers that ayah's save button
                visible = resumeAhead && openAyahId == null,
                enter = slideInVertically(tween(320, easing = EaseOutExpo)) { it / 2 } + fadeIn(tween(240)),
                exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                ResumeJumpPill(
                    ayahNumber = resumeAyah,
                    onClick = {
                        if (jumping) return@ResumeJumpPill
                        jumping = true
                        scope.launch {
                            try {
                                if (listState.layoutInfo.visibleItemsInfo.any { it.index == resumeAyah }) {
                                    // Already in view: a short glide composes nothing new
                                    listState.animateScrollToItem(resumeAyah, -landingGapPx)
                                    arrival++
                                } else {
                                    // Far: scrolling through would compose and shape every ayah in between.
                                    // Fade the page out, cut there in one layout pass, then let it rise in on the layer.
                                    pageAlpha.animateTo(0f, tween(100, easing = LinearEasing))
                                    listState.scrollToItem(resumeAyah, -landingGapPx)
                                    pageRise.snapTo(risePx)
                                    arrival++
                                    coroutineScope {
                                        launch { pageAlpha.animateTo(1f, tween(240, easing = EaseOutExpo)) }
                                        launch { pageRise.animateTo(0f, tween(420, easing = EaseOutExpo)) }
                                    }
                                }
                            } finally {
                                jumping = false
                            }
                        }
                    }
                )
            }
        }
    }
}

// The ayah being read: the first whose middle is still on screen, or the last one at the end of the surah
private fun LazyListState.readingItem(): LazyListItemInfo? {
    val info = layoutInfo
    val ayahItems = info.visibleItemsInfo.filter { it.key is Int }
    return if (!canScrollForward) {
        ayahItems.lastOrNull()
    } else {
        ayahItems.firstOrNull { it.offset + it.size / 2 > info.viewportStartOffset }
    }
}

// A mushaf ribbon: a strip with a notched tail
private fun DrawScope.drawRibbon(color: Color, left: Float, width: Float, height: Float, notch: Float) {
    val path = Path().apply {
        moveTo(left, 0f)
        lineTo(left + width, 0f)
        lineTo(left + width, height)
        lineTo(left + width / 2, height * notch)
        lineTo(left, height)
        close()
    }
    drawPath(path, color)
}

@Composable
private fun ResumeJumpPill(ayahNumber: Int, onClick: () -> Unit) {
    val brass = MaterialTheme.colorScheme.secondary
    // In dark the shadow doesn't show on near-black, so the pill's edge carries its separation
    val night = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (night) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(start = 14.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(width = 8.dp, height = 14.dp)) {
                drawRibbon(brass, 0f, size.width, size.height, 0.74f)
            }
            Text(
                text = "Lanjut ke ayat $ayahNumber",
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ponytail: estimates progress from item indices, so it moves unevenly when ayah lengths differ a lot
private fun LazyListState.readingProgress(): Float {
    val info = layoutInfo
    if (info.totalItemsCount == 0) return 0f
    if (!canScrollForward) return 1f
    val first = info.visibleItemsInfo.firstOrNull() ?: return 0f
    val within = if (first.size > 0) firstVisibleItemScrollOffset.toFloat() / first.size else 0f
    val scrollableItems = (info.totalItemsCount - info.visibleItemsInfo.size + 1).coerceAtLeast(1)
    return ((firstVisibleItemIndex + within) / scrollableItems).coerceIn(0f, 1f)
}

private fun Int.toArabicIndicDigits(): String = toString().map { '٠' + (it - '0') }.joinToString("")

@Composable
private fun SurahOpening(
    surah: SurahEntity?,
    targetSurahNumber: Int,
    showBasmalah: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp)
    ) {
        if (surah != null) {
            Text(
                text = surah.nameArabic,
                style = SurahOpeningArabicStyle,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Text(
            text = surah?.nameLatin ?: "Surah $targetSurahNumber",
            style = SurahOpeningTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        if (surah != null) {
            Text(
                text = "${surah.nameTranslation} · ${surah.versesCount} ayat · ${surah.revelationType}",
                style = ReaderMetaTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (showBasmalah) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )
            Text(
                text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                style = BasmalahTextStyle,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AyahBlock(
    ayah: AyahEntity,
    showLatin: Boolean,
    isResumePoint: Boolean,
    arrival: Int,
    isOpen: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val brass = MaterialTheme.colorScheme.secondary

    // Arrival at the resume ayah: the ribbon unrolls from the hairline, the label settles, the ayah glows once
    val ribbon = remember { Animatable(1f) }
    val label = remember { Animatable(1f) }
    val wash = remember { Animatable(0f) }
    LaunchedEffect(arrival) {
        if (!isResumePoint || arrival == 0) return@LaunchedEffect
        ribbon.snapTo(0f)
        label.snapTo(0f)
        launch {
            wash.animateTo(1f, tween(300, easing = EaseOutExpo))
            wash.animateTo(0f, tween(1900, easing = EaseOutExpo))
        }
        launch { ribbon.animateTo(1f, tween(700, delayMillis = 120, easing = EaseOutExpo)) }
        launch { label.animateTo(1f, tween(500, delayMillis = 300, easing = EaseOutExpo)) }
    }

    // A small pop on the bookmark icon when the ayah is saved here, not when it was already saved
    val pop = remember { Animatable(1f) }
    var wasBookmarked by remember { mutableStateOf(ayah.isBookmarked) }
    LaunchedEffect(ayah.isBookmarked) {
        if (ayah.isBookmarked && !wasBookmarked) {
            pop.snapTo(0.55f)
            pop.animateTo(1f, keyframes {
                durationMillis = 340
                1.18f at 187 using EaseOutExpo
            })
        }
        wasBookmarked = ayah.isBookmarked
    }
    // Warm night: jade stays on controls; the Latin line and the open fill take warm ivory tones
    val night = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val openFill = if (night) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f) else primary.copy(alpha = 0.05f)
    val selectedTint by animateColorAsState(
        targetValue = if (isOpen) openFill else Color.Transparent,
        label = "ayahSelected"
    )

    val medallion = MaterialTheme.colorScheme.tertiary
    val arabic = remember(ayah.textArabic, ayah.ayahNumber, medallion) {
        buildAnnotatedString {
            append(ayah.textArabic)
            // No-break space keeps the end-of-ayah medallion on the last word's line
            withStyle(SpanStyle(color = medallion)) {
                append(" ۝")
                append(ayah.ayahNumber.toArabicIndicDigits())
            }
        }
    }
    val translation = remember(ayah.translationId, ayah.ayahNumber, ayah.isBookmarked, muted) {
        buildAnnotatedString {
            if (ayah.isBookmarked) {
                appendInlineContent(BookmarkMarkId, "[tersimpan]")
                append(" ")
            }
            withStyle(SpanStyle(color = muted, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")) {
                append("${ayah.ayahNumber}")
            }
            append("  ")
            append(ayah.translationId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                if (!isResumePoint) return@drawWithContent
                // Hangs from the top hairline, 20dp in from the start edge
                val w = 10.dp.toPx()
                val left = if (layoutDirection == LayoutDirection.Rtl) size.width - 20.dp.toPx() - w else 20.dp.toPx()
                scale(scaleX = 1f, scaleY = ribbon.value, pivot = Offset(left, 0f)) {
                    drawRibbon(brass, left, w, 30.dp.toPx(), 0.76f)
                }
            }
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(selectedTint)
                    if (wash.value > 0f) drawRect(brass.copy(alpha = 0.16f * wash.value))
                }
                .clickable(
                    onClickLabel = if (isOpen) "Tutup pilihan ayat" else "Tampilkan pilihan ayat",
                    onClick = onClick
                )
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp)
        ) {
            if (isResumePoint) {
                Text(
                    text = "Lanjut dari sini",
                    style = MaterialTheme.typography.labelMedium,
                    color = muted,
                    modifier = Modifier
                        .padding(start = 18.dp, bottom = 10.dp)
                        .graphicsLayer {
                            alpha = label.value
                            translationX = (1f - label.value) * -6.dp.toPx()
                        }
                )
            }

            Text(
                text = arabic,
                style = ArabicAyahTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            if (showLatin && ayah.textLatin.isNotBlank()) {
                Text(
                    text = ayah.textLatin,
                    style = LatinAyahTextStyle,
                    color = if (night) QuranNightLatin else primary,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Text(
                text = translation,
                style = TranslationAyahTextStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                inlineContent = BookmarkMarkContent,
                modifier = Modifier.padding(top = 12.dp)
            )

            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(tween(320, easing = EaseOutExpo)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(240, easing = EaseOutExpo)) + fadeOut(tween(160))
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (ayah.isBookmarked) {
                        Button(onClick = onToggleBookmark, shape = RoundedCornerShape(10.dp)) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = pop.value
                                        scaleY = pop.value
                                    }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Tersimpan")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onToggleBookmark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Simpan ayat")
                        }
                    }
                }
            }
        }
    }
}

private const val BookmarkMarkId = "bookmark_mark"

private val BookmarkMarkContent = mapOf(
    BookmarkMarkId to InlineTextContent(
        Placeholder(16.sp, 16.sp, PlaceholderVerticalAlign.TextCenter)
    ) {
        Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "Tersimpan",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxSize()
        )
    }
)

@Composable
private fun SurahFooterNavigation(
    prevSurah: SurahEntity?,
    nextSurah: SurahEntity?,
    onNavigateSurah: (Int) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (prevSurah != null) {
                Surface(
                    onClick = { onNavigateSurah(prevSurah.number) },
                    shape = CardShape16,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
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
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${prevSurah.number}. ${prevSurah.nameLatin}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
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

            if (nextSurah != null) {
                Surface(
                    onClick = { onNavigateSurah(nextSurah.number) },
                    shape = CardShape16,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Selanjutnya",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${nextSurah.number}. ${nextSurah.nameLatin}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
