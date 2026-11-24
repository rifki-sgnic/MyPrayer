package com.rfksgn.myprayer.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.compose.ui.semantics.contentDescription
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
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import com.rfksgn.myprayer.domain.model.Wirid
import kotlinx.coroutines.delay
import com.rfksgn.myprayer.ui.theme.QuranFontFamily
import com.rfksgn.myprayer.data.local.QuranArabicSizes
import com.rfksgn.myprayer.ui.theme.LocalQuranReadingColors
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val CardShape20 = RoundedCornerShape(20.dp)
private val CardShape16 = RoundedCornerShape(16.dp)

internal val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

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
    onSetArabicSize: (Int) -> Unit,
    onDownloadFullQuran: () -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onContinueKhatam: () -> Unit,
    onResumeLastRead: () -> Unit,
    onToggleWiridDone: (Wirid) -> Unit,
    onToggleWiridActive: (Wirid) -> Unit,
    onStartNewKhatam: () -> Unit,
    // Today's prayer times, for "Terakhir dibaca, ba'da Maghrib tadi"; null until the schedule is ready
    schedule: PrayerSchedule?,
    // The list pads for the bars; the reader pads only the top and the system nav bar, so the app bar
    // slides over it without the content jumping
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val surahMap = remember(uiState.surahs) {
        uiState.surahs.associateBy { it.number }
    }
    // Held above the reader, so back returns to the surah that was opened instead of Al-Fatihah
    val surahListState = rememberLazyListState()
    // The khatam's place when the reader opened: back on the list, each juz and surah it has since passed seals once
    var sealFrom by remember { mutableIntStateOf(-1) }
    LaunchedEffect(uiState.isReaderActive) {
        if (uiState.isReaderActive) {
            sealFrom = uiState.khatam.position
        } else if (sealFrom >= 0) {
            delay(2500)
            sealFrom = -1
        }
    }

    // Into a surah is a step in depth, not sideways like the tabs: the reader settles in from 96% while
    // the list fades away, and back reverses it a little quicker. "Remove animations" makes it a cut.
    AnimatedContent(
        targetState = uiState.isReaderActive,
        transitionSpec = {
            if (targetState) {
                (fadeIn(tween(210, delayMillis = 60)) +
                        scaleIn(tween(260, easing = EaseOutExpo), initialScale = 0.96f)) togetherWith
                        fadeOut(tween(90))
            } else {
                fadeIn(tween(150, delayMillis = 50)) togetherWith
                        (fadeOut(tween(150)) + scaleOut(tween(200, easing = EaseOutExpo), targetScale = 0.96f))
            }
        },
        modifier = modifier,
        label = "quranDepth"
    ) { reader ->
        if (reader) {
            // The exiting reader stays composed during the back transition; it must not take a second back press
            BackHandler(enabled = uiState.isReaderActive, onBack = onCloseSurah)

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
                arabicSize = uiState.arabicSize,
                isLoading = uiState.isLoadingSurah,
                errorMessage = uiState.surahLoadError,
                onBackClick = onCloseSurah,
                onRetry = { onOpenSurah(currentNum, uiState.initialScrollAyah) },
                onNavigateSurah = onNavigateSurah,
                onToggleShowLatin = onToggleShowLatin,
                onSetArabicSize = onSetArabicSize,
                onToggleBookmark = onToggleBookmark,
                onMarkLastRead = { ayahNum ->
                    val surah = uiState.activeSurah
                    if (surah != null) onMarkLastRead(surah, ayahNum)
                },
                modifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .navigationBarsPadding()
            )
        } else {
            QuranHome(
                uiState = uiState,
                schedule = schedule,
                sealFrom = sealFrom,
                listState = surahListState,
                onSearchQueryChange = onSearchQueryChange,
                onTabSelected = onTabSelected,
                onOpenSurah = onOpenSurah,
                onContinueKhatam = onContinueKhatam,
                onResumeLastRead = onResumeLastRead,
                onToggleWiridDone = onToggleWiridDone,
                onToggleWiridActive = onToggleWiridActive,
                onStartNewKhatam = onStartNewKhatam,
                onToggleBookmark = onToggleBookmark,
                onDownloadFullQuran = onDownloadFullQuran,
                contentPadding = contentPadding
            )
        }
    }
}

@Composable
internal fun BookmarkedAyahCard(
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
                    // Full 48dp target; the card keeps its row height
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
    arabicSize: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onToggleShowLatin: () -> Unit,
    onSetArabicSize: (Int) -> Unit,
    onToggleBookmark: (AyahEntity) -> Unit,
    onMarkLastRead: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }
    val arabicSp = QuranArabicSizes[arabicSize]
    val arabicStyle = remember(arabicSp) { ArabicAyahTextStyle.copy(fontSize = arabicSp.sp) }
    val basmalahStyle = remember(arabicSp) { BasmalahTextStyle.copy(fontSize = (arabicSp - 3).sp) }
    var hasScrolledToInitial by remember(surah?.number, initialScrollAyah) { mutableStateOf(false) }
    // Locked when the surah opens, so the marker stays put while the reading position auto-saves
    val resumeAyah = remember(surah?.number) { lastReadAyahNumber }
    var openAyahId by remember(surah?.number) { mutableStateOf<Int?>(null) }
    // Bumped each time the reader lands on the resume ayah, which replays its ribbon and glow
    var arrival by remember(surah?.number) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    // A strip of space above the landed ayah, so its hairline and ribbon aren't pressed under the bar
    val landingGapPx = with(LocalDensity.current) { 16.dp.roundToPx() }

    // Landing at an ayah, on open or on a far jump: the page fades up and rises the last 28dp on its layer
    val pageAlpha = remember { Animatable(1f) }
    val pageRise = remember { Animatable(0f) }
    val risePx = with(LocalDensity.current) { 28.dp.toPx() }
    val landPage: suspend () -> Unit = {
        coroutineScope {
            launch { pageAlpha.animateTo(1f, tween(240, easing = EaseOutExpo)) }
            launch { pageRise.animateTo(0f, tween(280, easing = EaseOutExpo)) }
        }
    }

    // Item 0 is the surah opening, so ayah n sits at index n
    LaunchedEffect(surah?.number, initialScrollAyah, ayahs.isNotEmpty()) {
        if (ayahs.isNotEmpty() && !hasScrolledToInitial) {
            if (initialScrollAyah > 1) {
                listState.scrollToItem(initialScrollAyah.coerceAtMost(ayahs.size), -landingGapPx)
                if (initialScrollAyah == resumeAyah) arrival++
                // The list was held invisible until it sat at the ayah, so the surah's top never flashes first
                pageAlpha.snapTo(0f)
                pageRise.snapTo(risePx)
                hasScrolledToInitial = true
                // On the reader's scope, so a restart of this effect can't strand the page half faded
                scope.launch { landPage() }
            } else {
                listState.scrollToItem(0)
                hasScrolledToInitial = true
            }
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
    // Composition only asks whether the title shows; the fade itself is read on the layer
    val condensedShown by remember { derivedStateOf { condensedFraction > 0f } }

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

            if (condensedShown) {
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
                                color = metaColor
                            )
                            Text(text = " dari ${surah.versesCount}", style = metaStyle, color = metaColor)
                        }
                    }
                }
            }

            // Latin, Arabic size and night mode live together in one labelled sheet
            IconButton(onClick = { showSettings = true }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(
                    imageVector = Icons.Default.FormatSize,
                    contentDescription = "Tampilan bacaan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showSettings) {
            ReadingSettingsSheet(
                showLatin = showLatin,
                arabicSize = arabicSize,
                onToggleShowLatin = onToggleShowLatin,
                onSetArabicSize = onSetArabicSize,
                onDismiss = { showSettings = false }
            )
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
                        // Hidden while a resume open waits to sit at its ayah
                        alpha = if (!hasScrolledToInitial && initialScrollAyah > 1) 0f else pageAlpha.value
                        translationY = pageRise.value
                    }
            ) {
                item(key = "surah_opening", contentType = "opening") {
                    SurahOpening(
                        surah = surah,
                        targetSurahNumber = targetSurahNumber,
                        showBasmalah = surah != null && surah.number != 1 && surah.number != 9,
                        basmalahStyle = basmalahStyle
                    )
                }

                items(
                    items = ayahs,
                    key = { it.id },
                    contentType = { "ayah" }
                ) { ayah ->
                    AyahBlock(
                        ayah = ayah,
                        arabicStyle = arabicStyle,
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
                enter = slideInVertically(tween(250, easing = EaseOutExpo)) { it / 2 } + fadeIn(tween(200)),
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
                                    landPage()
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
internal fun DrawScope.drawRibbon(color: Color, left: Float, width: Float, height: Float, notch: Float) {
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
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LocalQuranReadingColors.current.pillEdge),
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

/**
 * Draws [text] with its visible ink centred in the box. The Quran font's line box is far taller than the
 * letter and puts it low, so centring the text box (what Text does) leaves glyphs sitting below the middle.
 */
@Composable
private fun InkCenteredGlyph(text: String, fontSize: TextUnit) {
    // Paint.getTextBounds measures the glyph's real outline; Compose's text path missed the ع tail
    val typeface = LocalFontFamilyResolver.current.resolve(QuranFontFamily).value as? android.graphics.Typeface
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(36.dp)) {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = fontSize.toPx()
            this.color = color.toArgb()
        }
        val ink = android.graphics.Rect().also { paint.getTextBounds(text, 0, text.length, it) }
        drawContext.canvas.nativeCanvas.drawText(
            text, size.width / 2 - ink.exactCenterX(), size.height / 2 - ink.exactCenterY(), paint
        )
    }
}

private val ArabicSizeLabels = listOf("Kecil", "Sedang", "Besar", "Sangat besar")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingSettingsSheet(
    showLatin: Boolean,
    arabicSize: Int,
    onToggleShowLatin: () -> Unit,
    onSetArabicSize: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        // The sheet is its own window and sets its bar icons only when it opens; the palette flipping
        // while it is open (Isya arriving, phone dark mode) must flip them too, or the system buttons vanish against the sheet
        val sheetView = LocalView.current
        val lightBars = MaterialTheme.colorScheme.surface.luminance() > 0.5f
        SideEffect {
            (sheetView.parent as? DialogWindowProvider)?.window?.let { window ->
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = lightBars
                    isAppearanceLightNavigationBars = lightBars
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                text = "Tampilan bacaan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(value = showLatin, role = Role.Switch, onValueChange = { onToggleShowLatin() })
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Teks Latin", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Cara baca di bawah teks Arab", style = MaterialTheme.typography.bodySmall, color = muted)
                }
                Switch(checked = showLatin, onCheckedChange = null)
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ukuran huruf Arab", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    QuranArabicSizes.indices.forEach { i ->
                        SegmentedButton(
                            selected = i == arabicSize,
                            onClick = { onSetArabicSize(i) },
                            shape = SegmentedButtonDefaults.itemShape(i, QuranArabicSizes.size),
                            icon = {},
                            modifier = Modifier.height(48.dp).semantics { contentDescription = ArabicSizeLabels[i] }
                        ) {
                            // The letter itself grows, so each step previews its size
                            InkCenteredGlyph(text = "ع", fontSize = (15 + i * 3).sp)
                        }
                    }
                }
            }
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

internal fun Int.toArabicIndicDigits(): String = toString().map { '٠' + (it - '0') }.joinToString("")

@Composable
private fun SurahOpening(
    surah: SurahEntity?,
    targetSurahNumber: Int,
    showBasmalah: Boolean,
    basmalahStyle: TextStyle
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
                style = basmalahStyle,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AyahBlock(
    ayah: AyahEntity,
    arabicStyle: TextStyle,
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
        launch { ribbon.animateTo(1f, tween(400, delayMillis = 80, easing = EaseOutExpo)) }
        launch { label.animateTo(1f, tween(300, delayMillis = 160, easing = EaseOutExpo)) }
    }

    // A small pop on the bookmark icon when the ayah is saved here, not when it was already saved
    val pop = remember { Animatable(1f) }
    var wasBookmarked by remember { mutableStateOf(ayah.isBookmarked) }
    LaunchedEffect(ayah.isBookmarked) {
        if (ayah.isBookmarked && !wasBookmarked) {
            pop.snapTo(0.9f)
            pop.animateTo(1f, keyframes {
                durationMillis = 260
                1.1f at 120 using EaseOutExpo
            })
        }
        wasBookmarked = ayah.isBookmarked
    }
    val reading = LocalQuranReadingColors.current
    val selectedTint by animateColorAsState(
        targetValue = if (isOpen) reading.openFill else Color.Transparent,
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
                // Unrolls from the hairline: a clip reveals it, so the notched tail never squashes
                clipRect(bottom = 30.dp.toPx() * ribbon.value) {
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
                style = arabicStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            if (showLatin && ayah.textLatin.isNotBlank()) {
                Text(
                    text = ayah.textLatin,
                    style = LatinAyahTextStyle,
                    color = reading.latin,
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
                enter = expandVertically(tween(250, easing = EaseOutExpo)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200, easing = EaseOutExpo)) + fadeOut(tween(120))
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
