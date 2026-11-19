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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.rfksgn.myprayer.data.local.QuranReadingMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locationPrefs = LocationPreferences(applicationContext)
        val quranPrefs = QuranPreferences(applicationContext)
        val locationTracker = LocationTracker(applicationContext)
        val compassSensor = CompassSensorManager(applicationContext)
        val alarmScheduler = AdzanAlarmScheduler(applicationContext)
        val quranDb = QuranDatabase.getInstance(applicationContext)
        val quranRepository = QuranRepository(quranDb.quranDao(), NetworkClient.quranApiService)

        setContent {
            MyPrayerTheme {
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
                val savedLocation by locationPrefs.locationFlow.collectAsState(
                    initial = SavedLocation(-6.1754, 106.8272, "Jakarta Pusat")
                )

                // State tab aktif (0 = Jadwal, 1 = Kiblat, 2 = Al-Quran, 3 = Setelan)
                var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                // Quran night: the reader's own choice, or automatic from Isya to Subuh (the schedule's next prayer is
                // Subuh exactly in that window) and whenever the phone is in dark mode
                val systemDark = isSystemInDarkTheme()
                val quranNight = when (quranUiState.readingMode) {
                    QuranReadingMode.NIGHT -> true
                    QuranReadingMode.LIGHT -> false
                    QuranReadingMode.AUTO -> systemDark || uiState.schedule?.nextPrayerName == "Subuh"
                }
                // The Quran tab can be dark on a light phone (or the reverse), so the system bar icons follow the page
                val darkPage = if (selectedTab == 2) quranNight else systemDark
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // On the Quran tab the bar takes Quran's palette, so it matches the page; surface and hairline fade
                        val appScheme = MaterialTheme.colorScheme
                        MaterialTheme(colorScheme = if (selectedTab == 2) quranColorScheme(quranNight) else appScheme) {
                        val barSurface by animateColorAsState(MaterialTheme.colorScheme.surface, tween(200), label = "navSurface")
                        val barLine by animateColorAsState(MaterialTheme.colorScheme.outline, tween(200), label = "navLine")
                        Column {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = barLine
                            )
                            NavigationBar(
                                containerColor = barSurface,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = "Jadwal"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Jadwal",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.12f
                                        )
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = {
                                        Icon(
                                            Icons.Default.Explore,
                                            contentDescription = "Kiblat"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Kiblat",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.12f
                                        )
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = "Al-Quran"
                                        )
                                    },
                                    label = {
                                        Text(
                                            "Al-Quran",
                                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.12f
                                        )
                                    )
                                )
                                NavigationBarItem(
                                    onClick = { selectedTab = 3 },
                                    selected = selectedTab == 3,
                                    icon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Setelan"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Setelan",
                                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.12f
                                        )
                                    )
                                )
                            }
                        }
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> HomeScreen(
                            uiState = uiState,
                            onRefreshLocationClick = {
                                val permissions = buildList {
                                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    // First-run setup asks for notifications in the same explained step
                                    if (uiState.isDefaultLocation &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                    ) {
                                        add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                permissionLauncher.launch(permissions.toTypedArray())
                            },
                            onToggleAlarm = { prayerName -> viewModel.cycleReminderMode(prayerName) },
                            onOpenSettings = { selectedTab = 3 },
                            // Home draws its sky under the status bar and pads the top itself
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        )

                        1 -> {
                            val azimuth by compassSensor.getAzimuthFlow(
                                savedLocation.latitude,
                                savedLocation.longitude
                            ).collectAsState(initial = 0f)

                            QiblaScreen(
                                currentAzimuth = azimuth,
                                qiblaDirection = (uiState.schedule?.qiblaDirection
                                    ?: 295.1).toFloat(),
                                locationName = uiState.locationName,
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
                            onSetReadingMode = quranViewModel::setReadingMode,
                            onSetArabicSize = quranViewModel::setArabicSize,
                            onDownloadFullQuran = quranViewModel::downloadAll30JuzOffline,
                            modifier = Modifier.padding(innerPadding)
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
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}