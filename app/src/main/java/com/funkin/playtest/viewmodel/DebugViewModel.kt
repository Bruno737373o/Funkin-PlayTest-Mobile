package com.funkin.playtest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.funkin.playtest.audio.AudioSynth
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.GameNote
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.model.PreferencesManager
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class DebugTab {
    CHART_EDITOR,
    SONG_WARP,
    LATENCY_CALIBRATION,
    GAMEPLAY_MODS,
    ANIMATION_INSPECTOR
}

data class DebugUiState(
    val selectedTab: DebugTab = DebugTab.CHART_EDITOR,
    val selectedSong: SongData = SongCatalog.songs[0],
    val selectedDifficulty: Difficulty = Difficulty.NORMAL,
    val botplay: Boolean = false,
    val godMode: Boolean = false,
    val ghostTapping: Boolean = true,
    val downscroll: Boolean = false,
    val hitboxOpacity: Float = 0.65f,
    val audioOffsetMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val sickWindowMs: Float = 45f,
    val goodWindowMs: Float = 90f,
    val badWindowMs: Float = 135f,
    val shitWindowMs: Float = 166f,
    // Chart Editor state
    val editorBpm: Float = 120f,
    val customNotes: List<GameNote> = emptyList(),
    val editorCurrentMeasure: Int = 0,
    val editorIsPlayerMode: Boolean = true,
    // Animation inspector state
    val inspectorCharacter: String = "Boyfriend",
    val inspectorCurrentAnimation: String = "idle",
    // Latency calibration state
    val calibrationTaps: List<Long> = emptyList(),
    val estimatedLatencyMs: Int = 0
)

class DebugViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(
        DebugUiState(
            botplay = prefs.botplay,
            godMode = prefs.godMode,
            ghostTapping = prefs.ghostTapping,
            downscroll = prefs.downscroll,
            hitboxOpacity = prefs.hitboxOpacity,
            audioOffsetMs = prefs.audioOffsetMs,
            sickWindowMs = prefs.sickWindowMs,
            goodWindowMs = prefs.goodWindowMs,
            badWindowMs = prefs.badWindowMs,
            shitWindowMs = prefs.shitWindowMs
        )
    )
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    init {
        // Initialize with default template notes in Chart Editor
        loadInitialChart()
    }

    private fun loadInitialChart() {
        val initial = SongCatalog.generateChart(_uiState.value.selectedSong, _uiState.value.selectedDifficulty)
        _uiState.value = _uiState.value.copy(
            customNotes = initial.take(64),
            editorBpm = _uiState.value.selectedSong.bpm
        )
    }

    fun setTab(tab: DebugTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectSong(song: SongData) {
        _uiState.value = _uiState.value.copy(
            selectedSong = song,
            editorBpm = song.bpm
        )
        loadInitialChart()
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        loadInitialChart()
    }

    // Chart Editor operations
    fun toggleEditorMode() {
        _uiState.value = _uiState.value.copy(editorIsPlayerMode = !_uiState.value.editorIsPlayerMode)
    }

    fun setEditorMeasure(measure: Int) {
        _uiState.value = _uiState.value.copy(editorCurrentMeasure = measure.coerceAtLeast(0))
    }

    fun toggleGridNote(stepInMeasure: Int, direction: NoteDirection) {
        val stepMs = (60_000f / _uiState.value.editorBpm) / 4f
        val measureStartMs = _uiState.value.editorCurrentMeasure * 16 * stepMs
        val targetStrum = (measureStartMs + (stepInMeasure * stepMs)).toLong()
        val isPlayer = _uiState.value.editorIsPlayerMode

        val existing = _uiState.value.customNotes.find {
            it.isPlayer == isPlayer && it.direction == direction && abs(it.strumTime - targetStrum) < (stepMs * 0.4f)
        }

        val updatedNotes = if (existing != null) {
            _uiState.value.customNotes - existing
        } else {
            val newNote = GameNote(
                id = System.nanoTime(),
                strumTime = targetStrum,
                direction = direction,
                isPlayer = isPlayer
            )
            AudioSynth.playNoteHit(direction, isPlayer)
            _uiState.value.customNotes + newNote
        }

        _uiState.value = _uiState.value.copy(customNotes = updatedNotes.sortedBy { it.strumTime })
    }

    fun clearChart() {
        _uiState.value = _uiState.value.copy(customNotes = emptyList())
    }

    fun generateRandomPattern() {
        val stepMs = (60_000f / _uiState.value.editorBpm) / 4f
        val newNotes = mutableListOf<GameNote>()
        var id = 1L
        for (m in 0..3) {
            for (step in listOf(0, 2, 4, 6, 8, 10, 12, 14)) {
                val strum = ((m * 16 + step) * stepMs).toLong()
                val dir = NoteDirection.fromIndex((step / 2 + m) % 4)
                newNotes.add(GameNote(id++, strum, dir, isPlayer = true))
                newNotes.add(GameNote(id++, strum, NoteDirection.fromIndex((dir.index + 1) % 4), isPlayer = false))
            }
        }
        _uiState.value = _uiState.value.copy(customNotes = newNotes.sortedBy { it.strumTime })
    }

    // Latency calibration
    fun recordCalibrationTap() {
        val now = System.currentTimeMillis()
        AudioSynth.playBeat(0)
        val taps = (_uiState.value.calibrationTaps + now).takeLast(8)
        var estimated = 0
        if (taps.size >= 4) {
            val diffs = mutableListOf<Long>()
            for (i in 1 until taps.size) {
                diffs.add(taps[i] - taps[i - 1])
            }
            val avg = diffs.average()
            estimated = (avg % 500).toInt()
        }
        _uiState.value = _uiState.value.copy(
            calibrationTaps = taps,
            estimatedLatencyMs = estimated
        )
    }

    fun saveCalibrationOffset(offset: Int) {
        prefs.audioOffsetMs = offset
        _uiState.value = _uiState.value.copy(audioOffsetMs = offset)
    }

    // Animation inspector
    fun setInspectorCharacter(character: String) {
        _uiState.value = _uiState.value.copy(inspectorCharacter = character)
    }

    fun triggerAnimation(anim: String) {
        _uiState.value = _uiState.value.copy(inspectorCurrentAnimation = anim)
        when (anim) {
            "left" -> AudioSynth.playNoteHit(NoteDirection.LEFT, true)
            "down" -> AudioSynth.playNoteHit(NoteDirection.DOWN, true)
            "up" -> AudioSynth.playNoteHit(NoteDirection.UP, true)
            "right" -> AudioSynth.playNoteHit(NoteDirection.RIGHT, true)
            "miss" -> AudioSynth.playMiss()
            else -> AudioSynth.playBeat(0)
        }
    }

    // Modifiers & Flags
    fun toggleBotplay() {
        val next = !_uiState.value.botplay
        prefs.botplay = next
        _uiState.value = _uiState.value.copy(botplay = next)
    }

    fun toggleGodMode() {
        val next = !_uiState.value.godMode
        prefs.godMode = next
        _uiState.value = _uiState.value.copy(godMode = next)
    }

    fun toggleGhostTapping() {
        val next = !_uiState.value.ghostTapping
        prefs.ghostTapping = next
        _uiState.value = _uiState.value.copy(ghostTapping = next)
    }

    fun toggleDownscroll() {
        val next = !_uiState.value.downscroll
        prefs.downscroll = next
        _uiState.value = _uiState.value.copy(downscroll = next)
    }

    fun setHitboxOpacity(opacity: Float) {
        prefs.hitboxOpacity = opacity
        _uiState.value = _uiState.value.copy(hitboxOpacity = opacity)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setSickWindow(ms: Float) {
        prefs.sickWindowMs = ms
        _uiState.value = _uiState.value.copy(sickWindowMs = ms)
    }

    fun setGoodWindow(ms: Float) {
        prefs.goodWindowMs = ms
        _uiState.value = _uiState.value.copy(goodWindowMs = ms)
    }

    fun setBadWindow(ms: Float) {
        prefs.badWindowMs = ms
        _uiState.value = _uiState.value.copy(badWindowMs = ms)
    }

    fun setShitWindow(ms: Float) {
        prefs.shitWindowMs = ms
        _uiState.value = _uiState.value.copy(shitWindowMs = ms)
    }
}
