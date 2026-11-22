package com.rfksgn.myprayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import android.app.UiModeManager
import android.os.SystemClock
import android.view.animation.PathInterpolator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rfksgn.myprayer.ui.theme.LaunchCascadeMs
import com.rfksgn.myprayer.ui.theme.LocalLaunchProgress
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rfksgn.myprayer.core.permission.rememberAdzanReadiness
import com.rfksgn.myprayer.ui.theme.carpetColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rfksgn.myprayer.core.alarm.AdzanAlarmScheduler
import com.rfksgn.myprayer.core.database.QuranDatabase
import com.rfksgn.myprayer.core.location.LocationTracker
import com.rfksgn.myprayer.core.sensor.CompassSensorManager
import com.rfksgn.myprayer.data.local.LocationPreferences
import com.rfksgn.myprayer.data.local.QuranPreferences
import com.rfksgn.myprayer.data.local.SavedLocation
import com.rfksgn.myprayer.data.remote.NetworkClient
import com.rfksgn.myprayer.data.repository.QuranRepository
import com.rfksgn.myprayer.presentation.home.HomeScreen
import com.rfksgn.myprayer.presentation.home.HomeViewModel
import com.rfksgn.myprayer.presentation.qibla.QiblaScreen
import com.rfksgn.myprayer.presentation.quran.QuranScreen
import com.rfksgn.myprayer.presentation.quran.QuranViewModel
import com.rfksgn.myprayer.presentation.settings.SettingsScreen
import com.rfksgn.myprayer.ui.theme.MyPrayerTheme
import com.rfksgn.myprayer.ui.theme.QuranTheme
import com.rfksgn.myprayer.ui.theme.quranColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import com.rfksgn.myprayer.data.local.ThemeMode
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    // Set once Jadwal has its theme and schedule; the splash stays up until then
    @Volatile private var contentReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the splash over DataStore's first answer and the first schedule, so neither a blank frame nor
        // the loading spinner shows. ponytail: 1.5s cap, so a stuck load still opens the app.
        val splashStart = SystemClock.uptimeMillis()
        splash.setKeepOnScreenCondition { !contentReady && SystemClock.uptimeMillis() - splashStart < 1500 }
        // The mark shrinks as the splash fades in 160ms; Jadwal's cascade plays underneath. Strong ease-out, so the
        // schedule shows from the first frames instead of waiting behind a slow start.
        splash.setOnExitAnimationListener { provider ->
            val easeOut = PathInterpolator(0.23f, 1f, 0.32f, 1f)
            // The splash's fade carries the mark too, so the mark only shrinks
            provider.iconView.animate().scaleX(0.8f).scaleY(0.8f)
                .setDuration(160).setInterpolator(easeOut).start()
            provider.view.animate().alpha(0f).setDuration(160).setInterpolator(easeOut)
                .withEndAction { provider.remove() }.start()
        }
        // A config change or restored process doesn't replay the launch
        val playLaunch = savedInstanceState == null

        val locationPrefs = LocationPreferences(applicationContext)
        val quranPrefs = QuranPreferences(applicationContext)
        val locationTracker = LocationTracker(applicationContext)
        val compassSensor = CompassSensorManager(applicationContext)
        val alarmScheduler = AdzanAlarmScheduler(applicationContext)
        val quranDb = QuranDatabase.getInstance(applicationContext)
        val quranRepository = QuranRepository(quranDb.quranDao(), NetworkClient.quranApiService)

        setContent {
            // null until DataStore answers, so a saved Gelap never flashes a light frame first
            val themeMode by locationPrefs.themeModeFlow.collectAsState(initial = null)
            val mode = themeMode ?: return@setContent
            val systemDark = isSystemInDarkTheme()
            val appDark = when (mode) {
                ThemeMode.AUTO -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val themeScope = rememberCoroutineScope()
            // Android 12+: the system splash reads "night" from the app's own choice, so a Gelap app on a light
            // phone opens on obsidian, not limestone. The manifest handles uiMode, so this never recreates.
            LaunchedEffect(mode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(UiModeManager::class.java).setApplicationNightMode(
                        when (mode) {
                            ThemeMode.AUTO -> UiModeManager.MODE_NIGHT_AUTO
                            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                        }
                    )
                }
            }
            MyPrayerTheme(darkTheme = appDark) {
                val viewModel: HomeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HomeViewModel(
                                applicationContext,
                                locationPrefs,
                                locationTracker,
                                alarmScheduler
                            ) as T
                        }
                    }
                )

                val quranViewModel: QuranViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return QuranViewModel(quranRepository, quranPrefs) as T
                        }
                    }
                )
                val quranUiState by quranViewModel.uiState.collectAsState()

                val uiState by viewModel.uiState.collectAsState()
                val scheduleReady = uiState.schedule != null
                SideEffect { if (scheduleReady) contentReady = true }
                // The launch cascade starts as the splash begins to leave (the frame the schedule is ready)
                val launchProgress = remember { Animatable(if (playLaunch) 0f else 1f) }
                LaunchedEffect(scheduleReady) {
                    if (scheduleReady) launchProgress.animateTo(1f, tween(LaunchCascadeMs, easing = LinearEasing))
                }
                val savedLocation by locationPrefs.locationFlow.collectAsState(
                    initial = SavedLocation(-6.1754, 106.8272, "Jakarta Pusat")
                )

                // State tab aktif (0 = Jadwal, 1 = Kiblat, 2 = Al-Quran, 3 = Setelan)
                var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                // Quran night follows a forced Terang/Gelap; on Otomatis it is night from Isya to Subuh (the
                // schedule's next prayer is Subuh exactly in that window) and whenever the phone is in dark mode
                val quranNight = when (mode) {
                    ThemeMode.AUTO -> systemDark || uiState.schedule?.nextPrayerName == "Subuh"
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
                // The Quran tab can be dark on a light phone (or the reverse), so the system bar icons follow the page
                val darkPage = if (selectedTab == 2) quranNight else appDark
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkPage
                        isAppearanceLightNavigationBars = !darkPage
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        viewModel.refreshGpsLocation()
                    } else {
                        viewModel.onLocationPermissionDenied()
                    }
                }

                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(viewModel) {
                    // collectLatest: a newer message replaces the one on screen
                    viewModel.messages.collectLatest { message ->
                        val result = snackbarHostState.showSnackbar(
                            message = message.text,
                            actionLabel = message.actionLabel,
                            withDismissAction = message.actionLabel == null,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) message.action?.invoke()
                    }
                }

                // Back from Kiblat, Al-Quran or Setelan returns to Jadwal. The Quran reader's own handler is
                // registered later, so it still closes an open surah first.
                BackHandler(enabled = selectedTab != 0) { selectedTab = 0 }

                val readiness = rememberAdzanReadiness()
                // On first run the location card is the only call to action, as in Home's status row
                val missingPermissions = if (uiState.isDefaultLocation) 0 else readiness.missing
                val settingsScroll = rememberScrollState()
                val navScope = rememberCoroutineScope()
                val appScheme = MaterialTheme.colorScheme

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // The surah reader is full screen; its own back button returns to the list
                        AnimatedVisibility(
                            visible = !(selectedTab == 2 && quranUiState.isReaderActive),
                            enter = slideInVertically(tween(250, easing = EmphasizedEasing)) { it },
                            exit = slideOutVertically(tween(200, easing = EmphasizedEasing)) { it }
                        ) {
                        AppNavigationBar(
                            selectedTab = selectedTab,
                            // The bar takes the page's palette: Quran's on Al-Quran, the carpet on Setelan
                            scheme = when (selectedTab) {
                                2 -> quranColorScheme(quranNight)
                                3 -> carpetColorScheme(appDark)
                                else -> appScheme
                            },
                            missingPermissions = missingPermissions,
                            onSelect = { tab ->
                                if (tab != selectedTab) {
                                    selectedTab = tab
                                } else if (tab == 3) {
                                    navScope.launch { settingsScroll.animateScrollTo(0) }
                                }
                            }
                        )
                        }
                    }
                ) { innerPadding ->
                    // Jadwal and Kiblat share one location fix; first-run setup also asks for notifications
                    // in the same explained step
                    val refreshLocation = {
                        val permissions = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (uiState.isDefaultLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                    // Shared axis along the bar: a tab to the right enters from the right and pushes the old page
                    // left (the reverse going back), sliding a fifth of the width over 300ms. The old page fades
                    // out in 90ms and the new one fades in over the next 210ms, in step with the bar's colours.
                    // "Remove animations" makes it a cut.
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            val slide = tween<IntOffset>(300, easing = EmphasizedEasing)
                            (slideInHorizontally(slide) { w -> dir * w / 5 } +
                                    fadeIn(tween(210, delayMillis = 90))) togetherWith
                                    (slideOutHorizontally(slide) { w -> -dir * w / 5 } + fadeOut(tween(90)))
                        },
                        label = "tabContent"
                    ) { tab ->
                    when (tab) {
                        0 -> CompositionLocalProvider(LocalLaunchProgress provides { launchProgress.value }) { HomeScreen(
                            uiState = uiState,
                            onRefreshLocationClick = refreshLocation,
                            onToggleAlarm = { prayerName -> viewModel.cycleReminderMode(prayerName) },
                            onOpenSettings = { selectedTab = 3 },
                            themeMode = mode,
                            onCycleThemeMode = {
                                val next = ThemeMode.entries[(mode.ordinal + 1) % ThemeMode.entries.size]
                                themeScope.launch { locationPrefs.setThemeMode(next) }
                            },
                            // Home draws its sky under the status bar and pads the top itself
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        ) }

                        1 -> {
                            // Remembered per location, so a sensor tick never re-registers the listeners
                            val readings = remember(savedLocation.latitude, savedLocation.longitude) {
                                compassSensor.readings(savedLocation.latitude, savedLocation.longitude)
                            }
                            val reading by readings.collectAsState(initial = null)

                            QiblaScreen(
                                reading = reading,
                                hasCompass = compassSensor.isAvailable,
                                qiblaDirection = (uiState.schedule?.qiblaDirection
                                    ?: 295.1).toFloat(),
                                latitude = savedLocation.latitude,
                                longitude = savedLocation.longitude,
                                locationName = uiState.locationName,
                                isDefaultLocation = uiState.isDefaultLocation,
                                isUpdatingLocation = uiState.isUpdatingLocation,
                                onRefreshLocation = refreshLocation,
                                nextPrayerName = uiState.schedule?.nextPrayerName.orEmpty(),
                                // Like Home, the sky runs under the status bar and the screen pads the top itself
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        // Quran keeps its own night palette; the box paints it under the status bar too
                        2 -> QuranTheme(night = quranNight) { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { QuranScreen(
                            uiState = quranUiState,
                            onSearchQueryChange = quranViewModel::onSearchQueryChange,
                            onTabSelected = quranViewModel::onTabSelected,
                            onOpenSurah = { surahNum, ayahNum ->
                                quranViewModel.openSurah(
                                    surahNum,
                                    ayahNum
                                )
                            },
                            onCloseSurah = quranViewModel::closeActiveSurah,
                            onToggleBookmark = quranViewModel::toggleBookmark,
                            onMarkLastRead = { surah, ayahNum ->
                                quranViewModel.markAsLastRead(
                                    surah,
                                    ayahNum
                                )
                            },
                            onToggleShowLatin = quranViewModel::toggleShowLatin,
                            onSetArabicSize = quranViewModel::setArabicSize,
                            onDownloadFullQuran = quranViewModel::downloadAll30JuzOffline,
                            // Each side of the list/reader transition picks its own padding from this
                            contentPadding = innerPadding
                        ) } }

                        3 -> SettingsScreen(
                            reminderModes = uiState.reminderModes,
                            customAudio = uiState.customAudio,
                            onVolumeChange = { newVol -> viewModel.setAdzanVolume(newVol) },
                            onModeChange = { name, mode -> viewModel.setReminderMode(name, mode) },
                            onRegularAudioSelected = { uri, fileName ->
                                viewModel.updateCustomRegularAudio(uri, fileName)
                            },
                            onFajrAudioSelected = { uri, fileName ->
                                viewModel.updateCustomFajrAudio(uri, fileName)
                            },
                            onTestAdzan = { mode, isFajr ->
                                viewModel.testPlayAdzan(mode, isFajr)
                            },
                            onStopAdzan = { viewModel.stopAdzan() },
                            modifier = Modifier.padding(innerPadding),
                            scrollState = settingsScroll
                        )
                    }
                    }
                }
            }
        }
    }
}

/**
 * A destination's outlined and filled icon, and how far it turns as it settles when tapped:
 * Kiblat's needle swings onto its line, Setelan's gear gives a quarter turn; the others only grow in.
 */
// Material 3 emphasized easing: quick start, long gentle settle
private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private class NavTab(val label: String, val outlined: ImageVector, val filled: ImageVector, val turn: Float)

private val navTabs = listOf(
    NavTab("Jadwal", Icons.Outlined.Schedule, Icons.Filled.Schedule, 0f),
    NavTab("Kiblat", Icons.Outlined.Explore, Icons.Filled.Explore, -45f),
    NavTab("Al-Quran", Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook, 0f),
    NavTab("Setelan", Icons.Outlined.Settings, Icons.Filled.Settings, 90f)
)

@Composable
private fun AppNavigationBar(
    selectedTab: Int,
    scheme: ColorScheme,
    missingPermissions: Int,
    onSelect: (Int) -> Unit
) {
    // Colours move with the incoming page (90ms delay, 210ms), so bar and page change together
    val spec = tween<Color>(210, delayMillis = 90)
    val surface by animateColorAsState(scheme.surface, spec, label = "navSurface")
    val line by animateColorAsState(scheme.outline, spec, label = "navLine")
    val accent by animateColorAsState(scheme.primary, spec, label = "navAccent")
    val indicator by animateColorAsState(scheme.secondaryContainer, spec, label = "navIndicator")
    val muted by animateColorAsState(scheme.onSurfaceVariant, spec, label = "navMuted")
    val badge by animateColorAsState(scheme.secondary, spec, label = "navBadge")
    // One pulse per tap, reselects included, so every tap is answered
    val pulses = remember { mutableStateListOf(0, 0, 0, 0) }
    // The niche glides to the selected tab; read only in the draw phase
    // 250ms, so the niche (the tap's answer) lands just before the 300ms page slide
    val nichePos by animateFloatAsState(selectedTab.toFloat(), tween(250, easing = EmphasizedEasing), label = "navNiche")

    Column {
        HorizontalDivider(thickness = 1.dp, color = line)
        Row(
            Modifier
                .fillMaxWidth()
                // Read in draw, so the palette change repaints the bar rather than recomposing it
                .drawBehind { drawRect(surface) }
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp)
                .drawWithCache {
                    val u = 1.dp.toPx()
                    val niche = mihrabPath(u, close = true)
                    val edge = mihrabPath(u, close = false)
                    val inner = Path().apply {
                        moveTo(7 * u, 58 * u); lineTo(7 * u, 29 * u)
                        cubicTo(7 * u, 19 * u, 18 * u, 12 * u, 29 * u, 7.5f * u)
                        cubicTo(40 * u, 12 * u, 51 * u, 19 * u, 51 * u, 29 * u)
                        lineTo(51 * u, 58 * u)
                    }
                    val hairline = Stroke(width = u)
                    // The niche fades into the bar from 62% of its height down
                    fun fade(c: Color) = Brush.verticalGradient(0.62f to c, 0.96f to c.copy(alpha = 0f), endY = 58 * u)
                    onDrawBehind {
                        // Rises 13dp above the bar's hairline, centred on the selected item
                        translate(left = (nichePos + 0.5f) * size.width / navTabs.size - 29 * u, top = -13 * u) {
                            drawPath(niche, fade(indicator))
                            drawPath(edge, fade(accent.copy(alpha = 0.4f)), style = hairline)
                            drawPath(inner, fade(accent.copy(alpha = 0.18f)), style = hairline)
                        }
                    }
                }
                .selectableGroup()
        ) {
            navTabs.forEachIndexed { index, tab ->
                val selected = selectedTab == index
                val flagged = index == 3 && missingPermissions > 0
                val color by animateColorAsState(if (selected) accent else muted, tween(200), label = "navItem")
                // The selected icon stands up into the niche
                val lift by animateDpAsState(if (selected) (-8).dp else 0.dp, tween(250, easing = EmphasizedEasing), label = "navLift")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // The icon's settle answers each tap, so no ripple fights the niche
                        .selectable(
                            selected = selected,
                            onClick = {
                                pulses[index]++
                                onSelect(index)
                            },
                            role = Role.Tab,
                            interactionSource = null,
                            indication = null
                        )
                        .semantics {
                            contentDescription = if (flagged) "${tab.label}, $missingPermissions izin belum aktif" else tab.label
                        }
                        .padding(bottom = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Bottom),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompositionLocalProvider(LocalContentColor provides color) {
                        BadgedBox(
                            badge = {
                                // qualified: the item's ColumnScope would otherwise pick ColumnScope.AnimatedVisibility
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = flagged,
                                    enter = scaleIn(initialScale = 0.9f) + fadeIn(tween(150)),
                                    exit = scaleOut(targetScale = 0.9f) + fadeOut(tween(150))
                                ) {
                                    Badge(containerColor = badge)
                                }
                            },
                            modifier = Modifier.offset { IntOffset(0, lift.roundToPx()) }
                        ) {
                            NavIcon(tab, selected, pulses[index])
                        }
                        Text(tab.label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clearAndSetSemantics { })
                    }
                }
            }
        }
    }
}

/** The mihrab niche: a pointed arch 54dp wide in a 58dp box, open at the bottom when only its edge is drawn. */
private fun mihrabPath(u: Float, close: Boolean) = Path().apply {
    moveTo(2 * u, 58 * u); lineTo(2 * u, 27 * u)
    cubicTo(2 * u, 15 * u, 15 * u, 6.5f * u, 29 * u, 1 * u)
    cubicTo(43 * u, 6.5f * u, 56 * u, 15 * u, 56 * u, 27 * u)
    lineTo(56 * u, 58 * u)
    if (close) close()
}

@Composable
private fun NavIcon(tab: NavTab, selected: Boolean, pulse: Int) {
    // Rests at 1; each tap restarts it from 0 and lets it settle with a slight overshoot
    val settle = remember { Animatable(1f) }
    LaunchedEffect(pulse) {
        if (pulse > 0) {
            settle.snapTo(0f)
            settle.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 380f))
        }
    }
    Crossfade(targetState = selected, animationSpec = tween(150), label = "navIconFill") { filled ->
        Icon(
            imageVector = if (filled) tab.filled else tab.outlined,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                val p = settle.value
                rotationZ = tab.turn * (1f - p)
                val s = 0.9f + 0.1f * p
                scaleX = s
                scaleY = s
            }
        )
    }
}