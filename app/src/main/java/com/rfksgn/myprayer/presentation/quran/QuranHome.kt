package com.rfksgn.myprayer.presentation.quran

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import com.rfksgn.myprayer.domain.model.QuranIndex
import com.rfksgn.myprayer.domain.model.Wirid
import com.rfksgn.myprayer.ui.theme.QuranFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Indonesian = Locale.forLanguageTag("id-ID")
private val CardShape = RoundedCornerShape(18.dp)
private val ControlShape = RoundedCornerShape(10.dp)
private val SwingEasing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f)
private val Tnum = TextStyle(fontFeatureSettings = "tnum")

private val PageArabicStyle = TextStyle(
    fontFamily = QuranFontFamily,
    fontSize = 24.sp,
    lineHeight = 1.9.em,
    textDirection = TextDirection.Rtl,
    textAlign = TextAlign.Start
)
private val RowArabicStyle = TextStyle(fontFamily = QuranFontFamily, fontSize = 23.sp, textDirection = TextDirection.Rtl)

/**
 * The Quran tab's front page (Halaman): the page you left, held by a brass ribbon; today's wirid; then every surah
 * and juz behind one search, with the khatam's progress on their octagram numbers.
 *
 * [sealFrom] is the khatam's place when the reader last opened (-1 when nothing is pending): a juz or surah it has
 * since passed seals its octagram once.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun QuranHome(
    uiState: QuranUiState,
    schedule: PrayerSchedule?,
    sealFrom: Int,
    listState: LazyListState,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
    onContinueKhatam: () -> Unit,
    onResumeLastRead: () -> Unit,
    onToggleWiridDone: (Wirid) -> Unit,
    onToggleWiridActive: (Wirid) -> Unit,
    onStartNewKhatam: () -> Unit,
    onToggleBookmark: (AyahEntity) -> Unit,
    onDownloadFullQuran: () -> Unit,
    contentPadding: PaddingValues
) {
    val surahMap = remember(uiState.surahs) { uiState.surahs.associateBy { it.number } }
    val prayers = remember(schedule) { schedule?.let(::prayerMinutes) }
    val now = remember(uiState.lastRead, uiState.wirid) { LocalDateTime.now() }
    var showSaved by rememberSaveable { mutableStateOf(false) }
    val khatam = uiState.khatam
    val query = uiState.searchQuery.trim()
    val filtered = remember(uiState.surahs, query) { if (query.isEmpty()) uiState.surahs else uiState.surahs.filter { matches(it, query) } }
    val juzTab = uiState.selectedTabIndex == 1 && query.isEmpty()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item(key = "head", contentType = "head") {
            Column(Modifier.padding(start = 20.dp, end = 4.dp, top = 10.dp)) {
                Text(
                    text = contextLine(now, prayers),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Al-Quran",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() }
                    )
                    IconButton(onClick = { showSaved = true }) {
                        BadgedBox(badge = {
                            if (uiState.bookmarkedAyahs.isNotEmpty()) Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("${uiState.bookmarkedAyahs.size}", style = Tnum)
                            }
                        }) {
                            Icon(
                                Icons.Outlined.Bookmarks,
                                contentDescription = "Ayat tersimpan, ${uiState.bookmarkedAyahs.size}",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        item(key = "card", contentType = "card") {
            LastReadCard(uiState, surahMap, now, prayers, onClick = onResumeLastRead)
        }

        item(key = "wirid", contentType = "wirid") {
            WiridSection(
                uiState = uiState,
                surahMap = surahMap,
                fridayTime = isFridayTime(now, prayers),
                sealFrom = sealFrom,
                onOpen = { w ->
                    when {
                        w == Wirid.KHATAM && uiState.khatamDone -> onStartNewKhatam()
                        w == Wirid.KHATAM -> onContinueKhatam()
                        else -> {
                            val n = w.surah!!
                            val last = uiState.lastRead
                            val resume = last.surahNumber == n && !last.inKhatam && last.ayahNumber < QuranIndex.verseCount(n)
                            onOpenSurah(n, if (resume) last.ayahNumber else 1)
                        }
                    }
                },
                onTick = onToggleWiridDone,
                onToggleActive = onToggleWiridActive
            )
        }

        stickyHeader(key = "find", contentType = "find") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchField(uiState.searchQuery, onSearchQueryChange)
                val juzDone = remember(khatam.position) { (1..30).count { khatam.position >= QuranIndex.juzEndGlobal(it) } }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("Surah", "Juz  $juzDone/30").forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = uiState.selectedTabIndex == i,
                            onClick = { onTabSelected(i) },
                            shape = SegmentedButtonDefaults.itemShape(i, 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                inactiveContainerColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.height(48.dp)
                        ) { Text(label, style = Tnum) }
                    }
                }
            }
        }

        if (juzTab) {
            items(30, key = { "juz${it + 1}" }, contentType = { "juz" }) { i ->
                JuzRow(juz = i + 1, uiState = uiState, surahMap = surahMap, sealFrom = sealFrom, onContinueKhatam = onContinueKhatam, onOpenSurah = onOpenSurah)
            }
        } else if (filtered.isEmpty() && query.isNotEmpty()) {
            item(key = "empty", contentType = "empty") { EmptySearch(query) }
        } else {
            items(filtered, key = { it.number }, contentType = { "surah" }) { surah ->
                SurahRow(surah = surah, uiState = uiState, sealFrom = sealFrom, onClick = {
                    if (surahState(surah.number, uiState) == OctState.Now && khatam.position > 0) onContinueKhatam()
                    else onOpenSurah(surah.number, 1)
                })
            }
            if (query.isEmpty()) item(key = "download", contentType = "download") { DownloadRow(uiState, onDownloadFullQuran) }
        }
    }

    if (showSaved) SavedSheet(uiState, surahMap, onDismiss = { showSaved = false }, onOpen = { s, a -> showSaved = false; onOpenSurah(s, a) }, onToggleBookmark = onToggleBookmark)
}

/* ── Terakhir dibaca: the page you left ── */

// The page the ribbon last swung for. Outlives the card, which is disposed on every tab switch, so coming back
// to Al-Quran without having read on doesn't replay it. ponytail: process-wide, so a restored process swings once.
private var swungFor: Pair<Int, Int>? = null

@Composable
private fun LastReadCard(
    uiState: QuranUiState,
    surahMap: Map<Int, SurahEntity>,
    now: LocalDateTime,
    prayers: List<Pair<String, Int>>?,
    onClick: () -> Unit
) {
    val last = uiState.lastRead
    val fresh = last.readAt == 0L && last.surahNumber == 1 && last.ayahNumber == 1
    val surah = surahMap[last.surahNumber]
    val latin = surah?.nameLatin ?: last.surahName
    val brass = MaterialTheme.colorScheme.secondary
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.985f else 1f, tween(160, easing = EaseOutExpo), label = "cardPress")

    // The ribbon drops onto the page and swings to rest: the first time it shows, and again whenever the page turns
    val page = last.surahNumber to last.ayahNumber
    val drop = remember { Animatable(if (page == swungFor) 1f else 0f) }
    val swing = remember { Animatable(0f) }
    LaunchedEffect(page) {
        if (page == swungFor) return@LaunchedEffect
        swungFor = page
        drop.snapTo(0f)
        swing.snapTo(0f)
        delay(150)
        launch { drop.animateTo(1f, tween(300, easing = EaseOutExpo)) }
        swing.animateTo(0f, keyframes {
            durationMillis = 700
            7f at 300 using SwingEasing
            -2.5f at 500
        })
    }

    val whenText = if (fresh) null else readWhen(last.readAt, now, prayers)
    val action = resumeLabel(uiState, surahMap)
    val context = when {
        fresh -> "Pembukaan · 7 ayat"
        last.inKhatam -> "Juz ${QuranIndex.juzOf(last.surahNumber, last.ayahNumber)} · khatam ke-${uiState.khatam.round}"
        else -> "Juz ${QuranIndex.juzOf(last.surahNumber, last.ayahNumber)}"
    }

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
            .fillMaxWidth()
            .graphicsLayer { scaleX = press; scaleY = press }
            .semantics(mergeDescendants = true) {
                contentDescription = if (fresh) "Mulai membaca dari Al-Fatihah"
                else "Terakhir dibaca: $latin ayat ${last.ayahNumber}${whenText?.let { ", $it" } ?: ""}. $action."
            }
    ) {
        Box {
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)) {
                Text(
                    text = when {
                        fresh -> "Belum ada bacaan. Mulai dari pembuka."
                        whenText != null -> "Terakhir dibaca, $whenText"
                        else -> "Terakhir dibaca"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 44.dp)
                )
                AnimatedContent(
                    targetState = LastPage(last.surahNumber, last.ayahNumber, uiState.lastReadAyah, surah),
                    contentKey = { it.surah to it.ayah },
                    transitionSpec = {
                        (fadeIn(tween(200, delayMillis = 50)) +
                                slideInVertically(tween(250, easing = EaseOutExpo)) { it / 6 }) togetherWith
                                (fadeOut(tween(90)) + slideOutVertically(tween(250, easing = EaseOutExpo)) { -it / 6 }) using
                                SizeTransform(clip = false)
                    },
                    label = "lastReadPage"
                ) { page -> LastReadPage(page) }
                HorizontalDivider(Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (fresh) "Al-Fatihah" else "$latin · ${last.ayahNumber}",
                            style = MaterialTheme.typography.titleMedium.merge(Tnum),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = context,
                            style = MaterialTheme.typography.bodySmall.merge(Tnum),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .background(MaterialTheme.colorScheme.primary, ControlShape)
                            .padding(start = 14.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(action, style = MaterialTheme.typography.labelLarge.merge(Tnum), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
            // Clipped by the card's shape, so it seems to come out from under the top edge
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 22.dp)
                    .size(width = 16.dp, height = 46.dp)
                    .graphicsLayer {
                        translationY = -(1f - drop.value) * size.height
                        rotationZ = swing.value
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
            ) {
                drawRibbon(brass, 0f, size.width, size.height, 0.837f)
                drawLine(Color.White.copy(alpha = 0.18f), Offset(3.dp.toPx(), 0f), Offset(3.dp.toPx(), size.height * 0.86f), 1.dp.toPx())
            }
        }
    }
}

private data class LastPage(val surah: Int, val ayah: Int, val text: AyahEntity?, val surahEntity: SurahEntity?)

@Composable
private fun LastReadPage(page: LastPage) {
    val medallion = MaterialTheme.colorScheme.tertiary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    // The ayah's text once that surah is cached (a match guards against a stale row); the surah's name until then
    val ayah = page.text?.takeIf { it.surahNumber == page.surah && it.ayahNumber == page.ayah }
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (ayah != null) {
            val arabic = remember(ayah.textArabic, medallion) {
                buildAnnotatedString {
                    append(ayah.textArabic)
                    withStyle(SpanStyle(color = medallion)) { append(" ۝${ayah.ayahNumber.toArabicIndicDigits()}") }
                }
            }
            Text(arabic, style = PageArabicStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
            Text(ayah.translationId, style = MaterialTheme.typography.bodyMedium, color = muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            Text(
                text = page.surahEntity?.nameArabic ?: "",
                style = PageArabicStyle.copy(fontSize = 36.sp, lineHeight = 1.6.em),
                color = medallion,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${page.surahEntity?.nameTranslation?.let { "$it · " } ?: ""}ayat ${page.ayah} dari ${QuranIndex.verseCount(page.surah)}",
                style = MaterialTheme.typography.bodyMedium.merge(Tnum),
                color = muted
            )
        }
    }
}

private fun resumeLabel(uiState: QuranUiState, surahMap: Map<Int, SurahEntity>): String {
    val last = uiState.lastRead
    return when {
        last.readAt == 0L && last.surahNumber == 1 && last.ayahNumber == 1 -> "Mulai"
        last.inKhatam && !uiState.khatamDone -> {
            val (n, a) = QuranIndex.fromGlobal(uiState.khatam.position + 1)
            if (n == last.surahNumber) "Lanjut ayat $a" else "Lanjut ${surahMap[n]?.nameLatin ?: "surah $n"}"
        }
        last.ayahNumber >= QuranIndex.verseCount(last.surahNumber) -> "Baca lagi"
        else -> "Lanjut ayat ${last.ayahNumber}"
    }
}

/* ── Wirid hari ini ── */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WiridSection(
    uiState: QuranUiState,
    surahMap: Map<Int, SurahEntity>,
    fridayTime: Boolean,
    sealFrom: Int,
    onOpen: (Wirid) -> Unit,
    onTick: (Wirid) -> Unit,
    onToggleActive: (Wirid) -> Unit
) {
    val active = uiState.wirid.active
    val today = active.filter { !it.friday || fridayTime }
    val later = active - today.toSet()
    val doneCount = today.count { uiState.isWiridDone(it) }
    val allDone = today.isNotEmpty() && doneCount == today.size
    var editing by rememberSaveable { mutableStateOf(false) }
    val brass = MaterialTheme.colorScheme.secondary

    Column(Modifier.padding(top = 28.dp)) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // All of today's ticked: a brass ribbon drops beside the heading
            val ribbon by animateFloatAsState(if (allDone) 1f else 0f, tween(600, easing = EaseOutExpo), label = "wiridRibbon")
            if (ribbon > 0f) {
                Canvas(
                    Modifier
                        .padding(end = 10.dp)
                        .offset(y = (-8).dp)
                        .size(width = 10.dp, height = 18.dp)
                // Unrolls from the top: a clip reveals it, so the notched tail never squashes
                ) { clipRect(bottom = size.height * ribbon) { drawRibbon(brass, 0f, size.width, size.height, 0.74f) } }
            }
            Text(
                text = if (allDone) "Wirid hari ini selesai" else "Wirid hari ini",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            if (today.isNotEmpty()) {
                Text("$doneCount dari ${today.size}", style = MaterialTheme.typography.labelLarge.merge(Tnum), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (active.isEmpty()) {
            Text(
                text = "Pilih bacaan yang ingin Anda jaga. Setiap hari, centangnya muncul di sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 4.dp)
            )
        }

        if (today.isNotEmpty() || later.isNotEmpty()) Spacer(Modifier.height(6.dp))
        today.forEach { w -> WiridRow(w, uiState, surahMap, sealFrom, onOpen, onTick) }
        later.forEach { w -> WiridRow(w, uiState, surahMap, sealFrom, onOpen, onTick = null) }
        if (today.isNotEmpty() || later.isNotEmpty()) HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        val showChips = editing || active.isEmpty()
        if (active.isNotEmpty()) {
            TextButton(
                onClick = { editing = !editing },
                modifier = Modifier
                    .padding(start = 8.dp, top = 4.dp)
                    .heightIn(min = 48.dp)
            ) {
                Icon(if (editing) Icons.Default.ExpandLess else Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (editing) "Selesai mengatur" else "Atur wirid", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        AnimatedVisibility(visible = showChips, enter = expandVertically(tween(250, easing = EaseOutExpo)) + fadeIn(), exit = shrinkVertically(tween(200)) + fadeOut(tween(120))) {
            FlowRow(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Wirid.entries.forEach { w ->
                    val on = w in active
                    FilterChip(
                        selected = on,
                        onClick = { editing = true; onToggleActive(w) },
                        label = { Text(if (w == Wirid.KHATAM) "Khatam" else w.label) },
                        leadingIcon = { Icon(if (on) Icons.Default.Check else Icons.Default.Add, null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }
        }
    }
}

/** One wirid. [onTick] is null for a Friday reading shown ahead of its day. */
@Composable
private fun WiridRow(
    w: Wirid,
    uiState: QuranUiState,
    surahMap: Map<Int, SurahEntity>,
    sealFrom: Int,
    onOpen: (Wirid) -> Unit,
    onTick: ((Wirid) -> Unit)?
) {
    val done = onTick != null && uiState.isWiridDone(w)
    val khatam = uiState.khatam
    val tj = uiState.todayJuz
    val name: String
    val meta: String
    val lead: @Composable () -> Unit
    if (w == Wirid.KHATAM) {
        name = "Khatam ke-${khatam.round}, 1 juz sehari"
        val end = QuranIndex.juzEndGlobal(tj)
        meta = when {
            uiState.khatamDone -> "Khatam ke-${khatam.round} selesai. Ketuk untuk khatam ke-${khatam.round + 1}."
            done -> "Juz $tj selesai${if (tj < 30) " · besok Juz ${tj + 1}" else ""}"
            else -> "Juz $tj · ${minOf(end - QuranIndex.juzStartGlobal(tj) + 1, end - khatam.position)} ayat lagi"
        }
        lead = { Octagram("$tj", if (done) OctState.Done else OctState.Now, seal = sealFrom in 0 until end && khatam.position >= end) }
    } else {
        val n = w.surah!!
        name = surahMap[n]?.nameLatin ?: w.label
        meta = when {
            onTick == null -> "Jumat · ${w.schedule}"
            done -> "Selesai hari ini"
            else -> "${w.schedule} · ${QuranIndex.verseCount(n)} ayat"
        }
        val last = uiState.lastRead
        val reading = last.surahNumber == n && !last.inKhatam && last.readAt > 0 && last.ayahNumber < QuranIndex.verseCount(n)
        lead = { Octagram("$n", if (done) OctState.Done else if (reading) OctState.Part else OctState.None, seal = false, animateDone = true) }
    }

    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = if (onTick == null) 60.dp else 72.dp)
                    .clickable(onClickLabel = "Buka") { onOpen(w) }
                    .padding(start = 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                lead()
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = if (onTick == null) FontWeight.Medium else FontWeight.SemiBold)
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodyMedium.merge(Tnum),
                        color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (done) FontWeight.Medium else null
                    )
                }
            }
            if (onTick != null) {
                val fill by animateColorAsState(if (done) MaterialTheme.colorScheme.primary else Color.Transparent, tween(200), label = "tickFill")
                val edge by animateColorAsState(
                    if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    tween(200),
                    label = "tickEdge"
                )
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 72.dp)
                        .toggleable(value = done, role = Role.Checkbox, onValueChange = { onTick(w) })
                        .semantics { contentDescription = "$name: tandai sudah dibaca" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(fill, CircleShape)
                            .border(1.5.dp, edge, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/* ── Surah and juz rows ── */

internal enum class OctState { None, Part, Now, Done }

private fun surahState(n: Int, uiState: QuranUiState): OctState {
    val k = uiState.khatam.position
    val start = QuranIndex.globalIndex(n, 1)
    val end = QuranIndex.surahEnd(n)
    val last = uiState.lastRead
    val khatamOn = k > 0 || Wirid.KHATAM in uiState.wirid.active
    return when {
        k >= end -> OctState.Done
        khatamOn && k + 1 in start..end -> OctState.Now
        last.surahNumber == n && !last.inKhatam && last.readAt > 0 && last.ayahNumber < QuranIndex.verseCount(n) -> OctState.Part
        else -> OctState.None
    }
}

@Composable
private fun SurahRow(surah: SurahEntity, uiState: QuranUiState, sealFrom: Int, onClick: () -> Unit) {
    val state = surahState(surah.number, uiState)
    val end = QuranIndex.surahEnd(surah.number)
    val primary = MaterialTheme.colorScheme.primary
    val brass = MaterialTheme.colorScheme.secondary
    val k = uiState.khatam.position
    val meta = remember(surah, state, k, uiState.lastRead, primary, brass) {
        buildAnnotatedString {
            when (state) {
                OctState.Done -> {
                    withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) { append("Selesai") }
                    append(" · ${surah.versesCount} ayat · ${surah.nameTranslation}")
                }
                OctState.Now -> if (k > 0) {
                    withStyle(SpanStyle(color = brass, fontWeight = FontWeight.SemiBold)) { append("Khatam · lanjut ayat ${k + 1 - QuranIndex.globalIndex(surah.number, 0)}") }
                    append(" · ${surah.versesCount} ayat")
                } else append("${surah.versesCount} ayat · ${surah.revelationType} · ${surah.nameTranslation}")
                OctState.Part -> {
                    withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) { append("Sampai ayat ${uiState.lastRead.ayahNumber}") }
                    append(" · ${surah.versesCount} ayat")
                }
                OctState.None -> append("${surah.versesCount} ayat · ${surah.revelationType} · ${surah.nameTranslation}")
            }
        }
    }
    ListRow(
        lead = { Octagram("${surah.number}", state, seal = state == OctState.Done && sealFrom in 0 until end) },
        title = surah.nameLatin,
        meta = meta,
        trailing = { Text(surah.nameArabic, style = RowArabicStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1) },
        onClick = onClick
    )
}

@Composable
private fun JuzRow(
    juz: Int,
    uiState: QuranUiState,
    surahMap: Map<Int, SurahEntity>,
    sealFrom: Int,
    onContinueKhatam: () -> Unit,
    onOpenSurah: (Int, Int) -> Unit
) {
    val k = uiState.khatam.position
    val start = QuranIndex.juzStartGlobal(juz)
    val end = QuranIndex.juzEndGlobal(juz)
    val state = when {
        k >= end -> OctState.Done
        k + 1 in start..end -> OctState.Now
        else -> OctState.None
    }
    val primary = MaterialTheme.colorScheme.primary
    val brass = MaterialTheme.colorScheme.secondary
    val name = { n: Int -> surahMap[n]?.nameLatin ?: "Surah $n" }
    val meta = remember(juz, state, k, surahMap, primary, brass) {
        val (n1, a1) = QuranIndex.juzStart(juz)
        val (n2, a2) = QuranIndex.fromGlobal(end)
        val range = "${name(n1)} $a1 – ${if (n2 == n1) "" else name(n2) + " "}$a2"
        buildAnnotatedString {
            when (state) {
                OctState.Done -> {
                    withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) { append("Selesai") }
                    append(" · $range")
                }
                OctState.Now -> withStyle(SpanStyle(color = brass, fontWeight = FontWeight.SemiBold)) {
                    if (k > 0) {
                        val (n, a) = QuranIndex.fromGlobal(k)
                        append("Sedang dikhatamkan · ${name(n)} ayat $a")
                    } else append("Mulai khatam di sini")
                }
                else -> append(range)
            }
        }
    }
    ListRow(
        lead = { Octagram("$juz", state, seal = state == OctState.Done && sealFrom in 0 until end) },
        title = "Juz $juz",
        meta = meta,
        trailing = null,
        onClick = {
            if (state == OctState.Now) onContinueKhatam()
            else QuranIndex.juzStart(juz).let { (n, a) -> onOpenSurah(n, a) }
        }
    )
}

@Composable
private fun ListRow(
    lead: @Composable () -> Unit,
    title: String,
    meta: AnnotatedString,
    trailing: (@Composable () -> Unit)?,
    onClick: () -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            lead()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium.merge(Tnum), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(meta, style = MaterialTheme.typography.bodySmall.merge(Tnum), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            trailing?.invoke()
        }
    }
}

/* ── The octagram number (an eight-pointed khatim), with its seal ── */

private val StarPoints = listOf(
    16f to 2.6f, 19.9f to 6.5f, 25.5f to 6.5f, 25.5f to 12.1f, 29.4f to 16f, 25.5f to 19.9f, 25.5f to 25.5f, 19.9f to 25.5f,
    16f to 29.4f, 12.1f to 25.5f, 6.5f to 25.5f, 6.5f to 19.9f, 2.6f to 16f, 6.5f to 12.1f, 6.5f to 6.5f, 12.1f to 6.5f
)

/**
 * A number in the Daftar Isi frame. [OctState.Now] (where the khatam stands) draws it in brass, [OctState.Part] in
 * jade, [OctState.Done] filled with a check. [seal] plays the completion once: the frame draws itself, fills, the
 * check pops and a brass glint spreads. [animateDone] plays it whenever the state turns Done while on screen.
 */
@Composable
internal fun Octagram(label: String, state: OctState, seal: Boolean, animateDone: Boolean = false) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    val brass = MaterialTheme.colorScheme.secondary
    val primary = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.secondaryContainer
    val bg = MaterialTheme.colorScheme.background
    val done = state == OctState.Done
    val draw = remember { Animatable(1f) }
    val glint = remember { Animatable(1f) }
    var wasDone by remember { mutableStateOf(done) }
    LaunchedEffect(seal, done) {
        val play = (seal && done) || (animateDone && done && !wasDone)
        wasDone = done
        if (!play) return@LaunchedEffect
        draw.snapTo(0f)
        glint.snapTo(0f)
        delay(200)
        launch { draw.animateTo(1f, tween(1000, easing = EaseOutExpo)) }
        delay(450)
        glint.animateTo(1f, tween(900, easing = EaseOutExpo))
    }
    // Read in draw and on the layer, so the draw-in recomposes only once, when the check first shows
    val fillAlpha = { if (done) ((draw.value - 0.55f) / 0.3f).coerceIn(0f, 1f) else 0f }
    val badge = { if (done) ((draw.value - 0.7f) / 0.3f).coerceIn(0f, 1f) else 0f }
    val showBadge by remember(done) { derivedStateOf { badge() > 0f } }

    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(36.dp)) {
            val u = size.width / 32f
            val star = Path().apply {
                StarPoints.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
                close()
            }
            val frame = Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(6.5f * u, 6.5f * u, 25.5f * u, 25.5f * u))
                moveTo(16f * u, 2.6f * u); lineTo(29.4f * u, 16f * u); lineTo(16f * u, 29.4f * u); lineTo(2.6f * u, 16f * u); close()
            }
            val fill = fillAlpha()
            if (fill > 0f) drawPath(star, fillColor, alpha = fill)
            val (lineColor, lineAlpha, width) = when (state) {
                OctState.Now -> Triple(brass, 1f, 1.4f)
                OctState.Part -> Triple(primary, 0.85f, 1.1f)
                OctState.Done -> Triple(primary, 0.75f, 1.1f)
                OctState.None -> Triple(tertiary, 0.55f, 1.1f)
            }
            val visible = if (draw.value < 1f) {
                // Draws itself, square and diamond together
                Path().also { out ->
                    val m = PathMeasure()
                    m.setPath(frame, false)
                    m.getSegment(0f, m.length * draw.value, out, true)
                }
            } else frame
            drawPath(visible, lineColor, alpha = lineAlpha, style = Stroke(width * u))
            if (glint.value in 0.001f..0.999f) {
                val s = 0.8f + glint.value
                rotate(45f) {
                    scale(s) {
                        val side = 22f * u
                        drawRect(brass, topLeft = Offset(center.x - side / 2, center.y - side / 2), size = Size(side, side), alpha = 0.9f * (1f - glint.value), style = Stroke(1.5.dp.toPx() / s))
                    }
                }
            }
        }
        Text(
            text = label,
            style = TextStyle(fontSize = 11.sp, fontWeight = if (state == OctState.Now) FontWeight.Bold else FontWeight.SemiBold, fontFeatureSettings = "tnum"),
            color = when (state) {
                OctState.Now -> brass
                OctState.Done -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.clearAndSetSemantics { }
        )
        if (showBadge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-3).dp)
                    .graphicsLayer { val b = badge(); scaleX = b; scaleY = b; alpha = b }
                    .size(18.dp)
                    .background(bg, CircleShape)
                    .padding(1.dp)
                    .background(primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

/* ── Search, empty state, download, saved ── */

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Cari surah" },
        decorationBox = { field ->
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, ControlShape),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Search, null, tint = muted) }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text("Cari nama, arti, atau nomor", style = MaterialTheme.typography.bodyLarge, color = muted)
                    field()
                }
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onChange("") }) { Icon(Icons.Default.Close, "Hapus pencarian", tint = muted) }
                }
            }
        }
    )
}

@Composable
private fun EmptySearch(query: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.SearchOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        Text("Tidak ada surah “$query”", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            "Coba ketik nomornya, misalnya 18 untuk Al-Kahf.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DownloadRow(uiState: QuranUiState, onDownload: () -> Unit) {
    val text = uiState.downloadProgressText
    val saved = text == "Semua surah tersimpan di HP"
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(if (saved) Icons.Default.OfflinePin else Icons.Default.DownloadForOffline, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = when {
                        uiState.isDownloadingFullQuran || text != null -> text.orEmpty()
                        else -> "Baca tanpa internet"
                    },
                    style = MaterialTheme.typography.titleSmall.merge(Tnum),
                    fontWeight = FontWeight.SemiBold
                )
                if (!uiState.isDownloadingFullQuran && text == null) {
                    Text("Simpan ke-114 surah di HP ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!uiState.isDownloadingFullQuran && !saved) {
                OutlinedButton(
                    onClick = onDownload,
                    shape = ControlShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text(if (text == null) "Simpan" else "Coba lagi", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedSheet(
    uiState: QuranUiState,
    surahMap: Map<Int, SurahEntity>,
    onDismiss: () -> Unit,
    onOpen: (Int, Int) -> Unit,
    onToggleBookmark: (AyahEntity) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            "Tersimpan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 20.dp, bottom = 8.dp)
                .semantics { heading() }
        )
        if (uiState.bookmarkedAyahs.isEmpty()) {
            Text(
                "Belum ada ayat tersimpan. Saat membaca, ketuk sebuah ayat lalu pilih Simpan ayat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.bookmarkedAyahs, key = { it.id }) { ayah ->
                    BookmarkedAyahCard(
                        surahName = surahMap[ayah.surahNumber]?.nameLatin ?: "Surah ${ayah.surahNumber}",
                        ayah = ayah,
                        onOpenClick = { onOpen(ayah.surahNumber, ayah.ayahNumber) },
                        onRemoveBookmark = { onToggleBookmark(ayah) }
                    )
                }
            }
        }
    }
}

/* ── Time, in prayer terms ── */

/** Today's prayers as (name, minute of day), from the schedule's "HH:mm" times. */
private fun prayerMinutes(s: PrayerSchedule): List<Pair<String, Int>>? = runCatching {
    listOf("Subuh" to s.fajr, "Dzuhur" to s.dhuhr, "Ashar" to s.asr, "Maghrib" to s.maghrib, "Isya" to s.isha).map { (n, t) ->
        n to t.substringBefore(':').toInt() * 60 + t.substringAfter(':').toInt()
    }
}.getOrNull()

private fun phaseAt(minute: Int, prayers: List<Pair<String, Int>>): String =
    prayers.lastOrNull { minute >= it.second }?.let { "ba'da ${it.first}" } ?: "sebelum Subuh"

private fun contextLine(now: LocalDateTime, prayers: List<Pair<String, Int>>?): String {
    val day = now.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Indonesian).replaceFirstChar { it.titlecase(Indonesian) }
    return if (prayers == null) day else "$day, ${phaseAt(now.hour * 60 + now.minute, prayers)}"
}

/** "ba'da Maghrib tadi", "kemarin, ba'da Isya", "3 hari lalu". Today's prayer times stand in for yesterday's. */
private fun readWhen(readAt: Long, now: LocalDateTime, prayers: List<Pair<String, Int>>?): String? {
    if (readAt <= 0L) return null
    val then = LocalDateTime.ofInstant(Instant.ofEpochMilli(readAt), ZoneId.systemDefault())
    if (ChronoUnit.MINUTES.between(then, now) < 10) return "baru saja"
    val days = ChronoUnit.DAYS.between(then.toLocalDate(), now.toLocalDate())
    val phase = prayers?.let { phaseAt(then.hour * 60 + then.minute, it) } ?: "pukul %02d.%02d".format(then.hour, then.minute)
    return when (days) {
        0L -> "$phase tadi"
        1L -> "kemarin, $phase"
        else -> "$days hari lalu"
    }
}

/** Malam Jumat runs from Thursday's Maghrib (18:00 without a schedule) through Friday. */
private fun isFridayTime(now: LocalDateTime, prayers: List<Pair<String, Int>>?): Boolean {
    val maghrib = prayers?.firstOrNull { it.first == "Maghrib" }?.second ?: (18 * 60)
    return now.dayOfWeek == DayOfWeek.FRIDAY || (now.dayOfWeek == DayOfWeek.THURSDAY && now.hour * 60 + now.minute >= maghrib)
}

/* ── Search that forgives how Indonesians spell surah names: "al kahfi", "yaasiin", "waqiah" ── */

private fun foldName(s: String): String = s.lowercase()
    .filter { it.isLetterOrDigit() }
    .replace("kh", "k").replace(Regex("dz|dh"), "z").replace(Regex("sy|sh"), "s").replace(Regex("ts|th"), "s").replace('q', 'k')
    .replace('o', 'a').replace('e', 'i').replace(Regex("(.)\\1+"), "$1").removeSuffix("h")

private fun dropArticle(s: String): String = s.lowercase().filter { it.isLetterOrDigit() }
    .replace(Regex("^(asy|adz|al|an|ar|as|at|az|ad)(?=[a-z]{3})"), "")

private fun matches(surah: SurahEntity, query: String): Boolean {
    query.toIntOrNull()?.let { return surah.number == it }
    val q = foldName(query)
    val qNoArt = foldName(dropArticle(query))
    if (q.isEmpty()) return true
    val keys = listOf(foldName(surah.nameLatin), foldName(dropArticle(surah.nameLatin)), foldName(surah.nameTranslation))
    return keys.any { k -> k.contains(q) || k.contains(qNoArt) || (k.length >= 3 && q.startsWith(k) && q.length - k.length <= 2) }
}
