package com.funkin.playtest.viewmodel

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.funkin.playtest.audio.AudioSynth
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.GameNote
import com.funkin.playtest.model.Judgement
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.model.PreferencesManager
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

data class JudgementPopup(
    val judgement: Judgement,
    val combo: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class PlayUiState(
    val song: SongData,
    val difficulty: Difficulty,
    val songPositionMs: Long = 0L,
    val songLengthMs: Long = 60_000L,
    val health: Float = 0.5f,
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val misses: Int = 0,
    val accuracy: Float = 1.0f,
    val notesHitCount: Int = 0,
    val totalPlayableNotes: Int = 0,
    val sicks: Int = 0,
    val goods: Int = 0,
    val bads: Int = 0,
    val shits: Int = 0,
    val latestJudgement: JudgementPopup? = null,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val currentBeat: Int = 0,
    val currentStep: Int = 0,
    val playerSingingDir: NoteDirection? = null,
    val opponentSingingDir: NoteDirection? = null,
    val playerMissedDir: NoteDirection? = null,
    val pressedDirections: Set<NoteDirection> = emptySet(),
    val opponentPressedDirections: Set<NoteDirection> = emptySet(),
    val botplay: Boolean = false,
    val godMode: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val downscroll: Boolean = false
)

class PlayViewModel(
    application: Application,
    private val song: SongData,
    private val difficulty: Difficulty,
    customChart: List<GameNote>? = null
) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val vibrator = application.getSystemService(Vibrator::class.java)

    private val allNotes: List<GameNote> = customChart ?: SongCatalog.generateChart(song, difficulty)
    private val playerNotes = allNotes.filter { it.isPlayer }.sortedBy { it.strumTime }
    private val opponentNotes = allNotes.filter { !it.isPlayer }.sortedBy { it.strumTime }

    private val _uiState = MutableStateFlow(
        PlayUiState(
            song = song,
            difficulty = difficulty,
            songLengthMs = max(30_000L, allNotes.maxOfOrNull { it.strumTime } ?: 60_000L + 3000L),
            totalPlayableNotes = playerNotes.size,
            botplay = prefs.botplay,
            godMode = prefs.godMode,
            downscroll = prefs.downscroll
        )
    )
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    private var loopJob: Job? = null
    private var lastBeatTriggered = -1
    private val stepMs = (60_000f / song.bpm) / 4f
    private val beatMs = (60_000f / song.bpm)

    init {
        startSong()
    }

    fun startSong() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            var startTime = System.currentTimeMillis() - _uiState.value.songPositionMs
            var lastTime = System.currentTimeMillis()

            while (isActive) {
                if (!_uiState.value.isPaused && !_uiState.value.isGameOver && !_uiState.value.isVictory) {
                    val now = System.currentTimeMillis()
                    val delta = (now - lastTime) * _uiState.value.playbackSpeed
                    lastTime = now

                    val newPos = _uiState.value.songPositionMs + delta.toLong()
                    val curBeat = (newPos / beatMs).toInt()
                    val curStep = (newPos / stepMs).toInt()

                    if (curBeat > lastBeatTriggered) {
                        lastBeatTriggered = curBeat
                        AudioSynth.playBeat(curBeat)
                    }

                    // Opponent autoplay
                    handleOpponentNotes(newPos)

                    // Player botplay handling if enabled
                    if (_uiState.value.botplay) {
                        handleBotplay(newPos)
                    }

                    // Miss detection for passed notes
                    handlePassedNotes(newPos)

                    // Check song end
                    val victory = newPos >= _uiState.value.songLengthMs

                    _uiState.value = _uiState.value.copy(
                        songPositionMs = newPos,
                        currentBeat = curBeat,
                        currentStep = curStep,
                        isVictory = victory
                    )

                    if (victory) {
                        saveFinalScores()
                        break
                    }
                } else {
                    lastTime = System.currentTimeMillis()
                }
                delay(12) // ~80fps tick
            }
        }
    }

    private fun handleOpponentNotes(currentTime: Long) {
        val oppActiveDirs = mutableSetOf<NoteDirection>()
        var singingDir: NoteDirection? = null

        for (note in opponentNotes) {
            if (!note.isHit && abs(currentTime - note.strumTime) <= 60) {
                note.isHit = true
                oppActiveDirs.add(note.direction)
                singingDir = note.direction
                AudioSynth.playNoteHit(note.direction, isPlayer = false)
            } else if (currentTime < note.strumTime + 120 && currentTime >= note.strumTime) {
                oppActiveDirs.add(note.direction)
            }
        }

        if (oppActiveDirs.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                opponentPressedDirections = oppActiveDirs,
                opponentSingingDir = singingDir ?: _uiState.value.opponentSingingDir
            )
        } else if (_uiState.value.opponentPressedDirections.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(opponentPressedDirections = emptySet())
        }
    }

    private fun handleBotplay(currentTime: Long) {
        for (note in playerNotes) {
            if (!note.isHit && !note.isMissed && abs(currentTime - note.strumTime) <= 25) {
                processHit(note, 0f)
            }
        }
    }

    private fun handlePassedNotes(currentTime: Long) {
        val missThreshold = prefs.shitWindowMs + 30f
        for (note in playerNotes) {
            if (!note.isHit && !note.isMissed && currentTime - note.strumTime > missThreshold) {
                note.isMissed = true
                processMiss(note.direction)
            }
        }
    }

    fun onNoteDown(direction: NoteDirection) {
        if (_uiState.value.isPaused || _uiState.value.isGameOver || _uiState.value.botplay) return

        val pressed = _uiState.value.pressedDirections + direction
        _uiState.value = _uiState.value.copy(
            pressedDirections = pressed,
            playerSingingDir = direction
        )

        // Haptic feedback
        if (prefs.hapticFeedback && vibrator?.hasVibrator() == true) {
            try {
                vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) {}
        }

        val currentTime = _uiState.value.songPositionMs
        val hitWindow = prefs.shitWindowMs

        // Find closest unhit note for this direction
        val candidate = playerNotes
            .filter { it.direction == direction && !it.isHit && !it.isMissed }
            .minByOrNull { abs(currentTime - it.strumTime) }

        if (candidate != null && abs(currentTime - candidate.strumTime) <= hitWindow) {
            val diff = (currentTime - candidate.strumTime).toFloat()
            processHit(candidate, diff)
        } else if (!prefs.ghostTapping) {
            // Ghost tapping disabled -> tapping empty air counts as a miss!
            processMiss(direction)
        }
    }

    fun onNoteUp(direction: NoteDirection) {
        val pressed = _uiState.value.pressedDirections - direction
        _uiState.value = _uiState.value.copy(
            pressedDirections = pressed,
            playerSingingDir = if (pressed.isEmpty()) null else pressed.first()
        )
    }

    private fun processHit(note: GameNote, diffMs: Float) {
        note.isHit = true
        val judgement = Judgement.judge(diffMs)
        AudioSynth.playNoteHit(note.direction, isPlayer = true)

        val newCombo = _uiState.value.combo + 1
        val newMaxCombo = max(_uiState.value.maxCombo, newCombo)
        val newScore = _uiState.value.score + judgement.score
        val newHealth = (_uiState.value.health + judgement.healthDelta).coerceIn(0f, 1f)

        val sicks = _uiState.value.sicks + if (judgement == Judgement.SICK) 1 else 0
        val goods = _uiState.value.goods + if (judgement == Judgement.GOOD) 1 else 0
        val bads = _uiState.value.bads + if (judgement == Judgement.BAD) 1 else 0
        val shits = _uiState.value.shits + if (judgement == Judgement.SHIT) 1 else 0
        val hits = _uiState.value.notesHitCount + 1

        val totalPoints = (sicks * 1.0f) + (goods * 0.75f) + (bads * 0.45f) + (shits * 0.2f)
        val accuracy = if (hits + _uiState.value.misses > 0) totalPoints / (hits + _uiState.value.misses) else 1.0f

        _uiState.value = _uiState.value.copy(
            score = newScore,
            combo = newCombo,
            maxCombo = newMaxCombo,
            health = newHealth,
            sicks = sicks,
            goods = goods,
            bads = bads,
            shits = shits,
            notesHitCount = hits,
            accuracy = accuracy,
            latestJudgement = JudgementPopup(judgement, newCombo),
            playerMissedDir = null
        )
    }

    private fun processMiss(direction: NoteDirection) {
        AudioSynth.playMiss()
        val newMisses = _uiState.value.misses + 1
        val newHealth = if (_uiState.value.godMode) {
            max(0.05f, _uiState.value.health - 0.04f)
        } else {
            (_uiState.value.health - 0.07f).coerceAtLeast(0f)
        }

        val isGameOver = newHealth <= 0f && !_uiState.value.godMode

        val hits = _uiState.value.notesHitCount
        val totalPoints = (_uiState.value.sicks * 1.0f) + (_uiState.value.goods * 0.75f) + (_uiState.value.bads * 0.45f) + (_uiState.value.shits * 0.2f)
        val accuracy = if (hits + newMisses > 0) (totalPoints / (hits + newMisses)).coerceIn(0f, 1f) else 0f

        _uiState.value = _uiState.value.copy(
            combo = 0,
            misses = newMisses,
            score = max(0, _uiState.value.score - 100),
            health = newHealth,
            accuracy = accuracy,
            isGameOver = isGameOver,
            playerMissedDir = direction,
            latestJudgement = JudgementPopup(Judgement.MISS, 0)
        )
    }

    fun togglePause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun restartSong() {
        for (n in allNotes) {
            n.isHit = false
            n.isMissed = false
        }
        lastBeatTriggered = -1
        _uiState.value = PlayUiState(
            song = song,
            difficulty = difficulty,
            songLengthMs = _uiState.value.songLengthMs,
            totalPlayableNotes = playerNotes.size,
            botplay = _uiState.value.botplay,
            godMode = _uiState.value.godMode,
            downscroll = _uiState.value.downscroll,
            playbackSpeed = _uiState.value.playbackSpeed
        )
        startSong()
    }

    fun scrubSongPosition(targetMs: Long) {
        val clamped = targetMs.coerceIn(0L, _uiState.value.songLengthMs)
        for (n in allNotes) {
            if (n.strumTime < clamped) {
                n.isHit = true
            } else {
                n.isHit = false
                n.isMissed = false
            }
        }
        _uiState.value = _uiState.value.copy(songPositionMs = clamped)
    }

    fun toggleBotplay() {
        val next = !_uiState.value.botplay
        _uiState.value = _uiState.value.copy(botplay = next)
        prefs.botplay = next
    }

    fun toggleGodMode() {
        val next = !_uiState.value.godMode
        _uiState.value = _uiState.value.copy(godMode = next)
        prefs.godMode = next
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleDownscroll() {
        val next = !_uiState.value.downscroll
        _uiState.value = _uiState.value.copy(downscroll = next)
        prefs.downscroll = next
    }

    private fun saveFinalScores() {
        prefs.saveHighScore(song.id, difficulty, _uiState.value.score)
        prefs.saveAccuracy(song.id, difficulty, _uiState.value.accuracy)
    }

    fun getVisibleNotes(windowMs: Long = 1200L): List<GameNote> {
        val current = _uiState.value.songPositionMs
        val minTime = current - 250L
        val maxTime = current + windowMs
        return allNotes.filter { it.strumTime in minTime..maxTime && (!it.isHit || current - it.strumTime < 180) }
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
    }
}
