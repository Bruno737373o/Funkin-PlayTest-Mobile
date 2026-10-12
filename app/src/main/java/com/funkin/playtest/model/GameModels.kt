package com.funkin.playtest.model

import androidx.compose.ui.graphics.Color
import com.funkin.playtest.ui.theme.BadColor
import com.funkin.playtest.ui.theme.GoodColor
import com.funkin.playtest.ui.theme.MissColor
import com.funkin.playtest.ui.theme.NoteCyan
import com.funkin.playtest.ui.theme.NoteGreen
import com.funkin.playtest.ui.theme.NotePurple
import com.funkin.playtest.ui.theme.NoteRed
import com.funkin.playtest.ui.theme.ShitColor
import com.funkin.playtest.ui.theme.SickColor

enum class NoteDirection(val index: Int, val label: String, val color: Color) {
    LEFT(0, "Left", NotePurple),
    DOWN(1, "Down", NoteCyan),
    UP(2, "Up", NoteGreen),
    RIGHT(3, "Right", NoteRed);

    companion object {
        fun fromIndex(idx: Int): NoteDirection = entries.firstOrNull { it.index == idx } ?: LEFT
    }
}

enum class Judgement(
    val maxMs: Float,
    val score: Int,
    val healthDelta: Float,
    val displayName: String,
    val color: Color
) {
    SICK(45f, 350, 0.045f, "SICK!!", SickColor),
    GOOD(90f, 200, 0.025f, "GOOD", GoodColor),
    BAD(135f, 100, 0.005f, "BAD", BadColor),
    SHIT(166f, 50, -0.015f, "SHIT", ShitColor),
    MISS(200f, -100, -0.06f, "MISS", MissColor);

    companion object {
        fun judge(diffMs: Float): Judgement {
            val abs = kotlin.math.abs(diffMs)
            return when {
                abs <= SICK.maxMs -> SICK
                abs <= GOOD.maxMs -> GOOD
                abs <= BAD.maxMs -> BAD
                abs <= SHIT.maxMs -> SHIT
                else -> MISS
            }
        }
    }
}

data class GameNote(
    val id: Long,
    val strumTime: Long, // in milliseconds
    val direction: NoteDirection,
    val length: Long = 0L, // sustain length in ms
    val isPlayer: Boolean = true,
    var isHit: Boolean = false,
    var isMissed: Boolean = false
)

enum class Difficulty(val title: String, val noteMultiplier: Float, val baseSpeed: Float) {
    EASY("Easy", 0.75f, 1.3f),
    NORMAL("Normal", 1.0f, 1.8f),
    HARD("Hard", 1.4f, 2.3f),
    ERECT("Erect", 1.8f, 2.7f),
    NIGHTMARE("Nightmare", 2.2f, 3.2f)
}

data class SongData(
    val id: String,
    val name: String,
    val week: String,
    val bpm: Float,
    val opponentName: String,
    val playerName: String = "Boyfriend",
    val stage: String = "Stage",
    val description: String = ""
)

data class WeekData(
    val id: String,
    val title: String,
    val weekName: String,
    val opponentName: String,
    val songs: List<SongData>
)
