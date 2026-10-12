package com.funkin.playtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.GameNote
import com.funkin.playtest.model.PreferencesManager
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import com.funkin.playtest.ui.screens.CreditsScreen
import com.funkin.playtest.ui.screens.DebugMenuScreen
import com.funkin.playtest.ui.screens.FreeplayScreen
import com.funkin.playtest.ui.screens.MainMenuScreen
import com.funkin.playtest.ui.screens.OptionsScreen
import com.funkin.playtest.ui.screens.PlayScreen
import com.funkin.playtest.ui.screens.StoryModeScreen
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FunkinTheme
import com.funkin.playtest.viewmodel.DebugViewModel
import com.funkin.playtest.viewmodel.PlayViewModel

enum class Screen {
    MAIN_MENU,
    STORY_MODE,
    FREEPLAY,
    DEBUG_MENU,
    OPTIONS,
    CREDITS,
    PLAY
}

class MainActivity : ComponentActivity() {
    private val debugViewModel: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FunkinTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    containerColor = FnfDarkBg
                ) { innerPadding ->
                    MainAppNavigation(
                        debugViewModel = debugViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    debugViewModel: DebugViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
    var activeSong by remember { mutableStateOf(SongCatalog.songs[0]) }
    var activeDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    var activeCustomChart by remember { mutableStateOf<List<GameNote>?>(null) }
    var playViewModelInstance by remember { mutableStateOf<PlayViewModel?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as android.app.Application
    val prefs = remember { PreferencesManager(context) }

    when (currentScreen) {
        Screen.MAIN_MENU -> {
            MainMenuScreen(
                onNavigateStory = { currentScreen = Screen.STORY_MODE },
                onNavigateFreeplay = { currentScreen = Screen.FREEPLAY },
                onNavigateDebug = { currentScreen = Screen.DEBUG_MENU },
                onNavigateOptions = { currentScreen = Screen.OPTIONS },
                onNavigateCredits = { currentScreen = Screen.CREDITS },
                modifier = modifier
            )
        }

        Screen.STORY_MODE -> {
            StoryModeScreen(
                onStartSong = { song, diff ->
                    activeSong = song
                    activeDifficulty = diff
                    activeCustomChart = null
                    playViewModelInstance = PlayViewModel(app, song, diff, null)
                    currentScreen = Screen.PLAY
                },
                onBack = { currentScreen = Screen.MAIN_MENU },
                modifier = modifier
            )
        }

        Screen.FREEPLAY -> {
            FreeplayScreen(
                onPlaySong = { song, diff ->
                    activeSong = song
                    activeDifficulty = diff
                    activeCustomChart = null
                    playViewModelInstance = PlayViewModel(app, song, diff, null)
                    currentScreen = Screen.PLAY
                },
                onBack = { currentScreen = Screen.MAIN_MENU },
                modifier = modifier
            )
        }

        Screen.DEBUG_MENU -> {
            DebugMenuScreen(
                viewModel = debugViewModel,
                onBack = { currentScreen = Screen.MAIN_MENU },
                onLaunchPlaytest = { song, diff, customNotes ->
                    activeSong = song
                    activeDifficulty = diff
                    activeCustomChart = customNotes
                    playViewModelInstance = PlayViewModel(app, song, diff, customNotes)
                    currentScreen = Screen.PLAY
                },
                modifier = modifier
            )
        }

        Screen.OPTIONS -> {
            OptionsScreen(
                onOpenDebugMenu = { currentScreen = Screen.DEBUG_MENU },
                onBack = { currentScreen = Screen.MAIN_MENU },
                modifier = modifier
            )
        }

        Screen.CREDITS -> {
            CreditsScreen(
                onBack = { currentScreen = Screen.MAIN_MENU },
                modifier = modifier
            )
        }

        Screen.PLAY -> {
            val playVm = playViewModelInstance ?: remember(activeSong, activeDifficulty) {
                PlayViewModel(app, activeSong, activeDifficulty, activeCustomChart).also {
                    playViewModelInstance = it
                }
            }

            PlayScreen(
                viewModel = playVm,
                hitboxOpacity = prefs.hitboxOpacity,
                onOpenDebugMenu = { currentScreen = Screen.DEBUG_MENU },
                onExit = {
                    playViewModelInstance = null
                    currentScreen = Screen.MAIN_MENU
                },
                modifier = modifier
            )
        }
    }
}
