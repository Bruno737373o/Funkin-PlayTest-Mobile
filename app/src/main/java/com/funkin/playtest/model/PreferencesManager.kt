package com.funkin.playtest.model

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("funkin_prefs", Context.MODE_PRIVATE)

    var downscroll: Boolean
        get() = prefs.getBoolean("downscroll", false)
        set(value) = prefs.edit().putBoolean("downscroll", value).apply()

    var ghostTapping: Boolean
        get() = prefs.getBoolean("ghost_tapping", true)
        set(value) = prefs.edit().putBoolean("ghost_tapping", value).apply()

    var hitboxOpacity: Float
        get() = prefs.getFloat("hitbox_opacity", 0.65f)
        set(value) = prefs.edit().putFloat("hitbox_opacity", value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) = prefs.edit().putBoolean("haptics", value).apply()

    var botplay: Boolean
        get() = prefs.getBoolean("botplay", false)
        set(value) = prefs.edit().putBoolean("botplay", value).apply()

    var godMode: Boolean
        get() = prefs.getBoolean("god_mode", false)
        set(value) = prefs.edit().putBoolean("god_mode", value).apply()

    var audioOffsetMs: Int
        get() = prefs.getInt("audio_offset_ms", 0)
        set(value) = prefs.edit().putInt("audio_offset_ms", value).apply()

    var noteSplashes: Boolean
        get() = prefs.getBoolean("note_splashes", true)
        set(value) = prefs.edit().putBoolean("note_splashes", value).apply()

    var showFps: Boolean
        get() = prefs.getBoolean("show_fps", true)
        set(value) = prefs.edit().putBoolean("show_fps", value).apply()

    // Debug timing window thresholds
    var sickWindowMs: Float
        get() = prefs.getFloat("sick_window", 45f)
        set(value) = prefs.edit().putFloat("sick_window", value).apply()

    var goodWindowMs: Float
        get() = prefs.getFloat("good_window", 90f)
        set(value) = prefs.edit().putFloat("good_window", value).apply()

    var badWindowMs: Float
        get() = prefs.getFloat("bad_window", 135f)
        set(value) = prefs.edit().putFloat("bad_window", value).apply()

    var shitWindowMs: Float
        get() = prefs.getFloat("shit_window", 166f)
        set(value) = prefs.edit().putFloat("shit_window", value).apply()

    // High scores by songId + difficulty
    fun getHighScore(songId: String, diff: Difficulty): Int =
        prefs.getInt("score_${songId}_${diff.name}", 0)

    fun saveHighScore(songId: String, diff: Difficulty, score: Int) {
        val current = getHighScore(songId, diff)
        if (score > current) {
            prefs.edit().putInt("score_${songId}_${diff.name}", score).apply()
        }
    }

    fun getAccuracy(songId: String, diff: Difficulty): Float =
        prefs.getFloat("acc_${songId}_${diff.name}", 0f)

    fun saveAccuracy(songId: String, diff: Difficulty, acc: Float) {
        val current = getAccuracy(songId, diff)
        if (acc > current) {
            prefs.edit().putFloat("acc_${songId}_${diff.name}", acc).apply()
        }
    }
}
