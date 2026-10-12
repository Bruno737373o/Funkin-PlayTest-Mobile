package com.funkin.playtest.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.funkin.playtest.model.NoteDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Low-latency procedural synth engine for FNF rhythms, drum beats, and vocal tones.
 */
object AudioSynth {
    private const val SAMPLE_RATE = 44100
    private var isMuted = false

    // Pre-rendered PCM buffers for instant playback
    private val kickBuffer: ByteArray by lazy { generateKick(SAMPLE_RATE) }
    private val snareBuffer: ByteArray by lazy { generateSnare(SAMPLE_RATE) }
    private val hihatBuffer: ByteArray by lazy { generateHihat(SAMPLE_RATE) }
    private val missBuffer: ByteArray by lazy { generateMissBuzz(SAMPLE_RATE) }

    // BF voice tones (high pitched beeps/bops: Left=C5, Down=D5, Up=F5, Right=G5)
    private val bfToneBuffers = mapOf(
        NoteDirection.LEFT to generateBfTone(523.25f, SAMPLE_RATE),  // C5
        NoteDirection.DOWN to generateBfTone(587.33f, SAMPLE_RATE),  // D5
        NoteDirection.UP to generateBfTone(698.46f, SAMPLE_RATE),    // F5
        NoteDirection.RIGHT to generateBfTone(783.99f, SAMPLE_RATE)  // G5
    )

    // Opponent voice tones (deeper tones: Left=C3, Down=D3, Up=F3, Right=G3)
    private val oppToneBuffers = mapOf(
        NoteDirection.LEFT to generateOppTone(130.81f, SAMPLE_RATE),
        NoteDirection.DOWN to generateOppTone(146.83f, SAMPLE_RATE),
        NoteDirection.UP to generateOppTone(174.61f, SAMPLE_RATE),
        NoteDirection.RIGHT to generateOppTone(196.00f, SAMPLE_RATE)
    )

    fun playBeat(beat: Int) {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            if (beat % 4 == 0) {
                playPcm(kickBuffer)
            } else if (beat % 4 == 2) {
                playPcm(snareBuffer)
            } else {
                playPcm(hihatBuffer)
            }
        }
    }

    fun playNoteHit(direction: NoteDirection, isPlayer: Boolean) {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            val buffer = if (isPlayer) {
                bfToneBuffers[direction] ?: kickBuffer
            } else {
                oppToneBuffers[direction] ?: kickBuffer
            }
            playPcm(buffer)
        }
    }

    fun playMiss() {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            playPcm(missBuffer)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    private fun playPcm(buffer: ByteArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback
            CoroutineScope(Dispatchers.IO).launch {
                kotlinx.coroutines.delay(200)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    private fun generateKick(sampleRate: Int): ByteArray {
        val durationMs = 120
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            // Pitch drop from 160Hz to 40Hz
            val freq = 160.0 * (1.0 - t * 0.75)
            val envelope = (1.0 - t) * (1.0 - t)
            phase += 2.0 * Math.PI * freq / sampleRate
            val sample = (sin(phase) * envelope * 0.85 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateSnare(sampleRate: Int): ByteArray {
        val durationMs = 100
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = (1.0 - t) * (1.0 - t)
            val noise = (Math.random() * 2.0 - 1.0)
            val tone = sin(2.0 * Math.PI * 220.0 * i / sampleRate) * 0.3
            val sample = ((noise * 0.7 + tone) * envelope * 0.7 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateHihat(sampleRate: Int): ByteArray {
        val durationMs = 50
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = (1.0 - t) * (1.0 - t) * (1.0 - t)
            val noise = (Math.random() * 2.0 - 1.0)
            val sample = (noise * envelope * 0.45 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateBfTone(freq: Float, sampleRate: Int): ByteArray {
        val durationMs = 140
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = when {
                t < 0.1 -> t / 0.1
                else -> 1.0 - (t - 0.1) / 0.9
            }
            phase += 2.0 * Math.PI * freq / sampleRate
            // Square-ish / synth wave
            val sinVal = sin(phase)
            val harmonic = sin(phase * 2.0) * 0.35 + sin(phase * 3.0) * 0.2
            val wave = (sinVal + harmonic).coerceIn(-1.0, 1.0)
            val sample = (wave * envelope * 0.75 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateOppTone(freq: Float, sampleRate: Int): ByteArray {
        val durationMs = 160
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = (1.0 - t)
            phase += 2.0 * Math.PI * freq / sampleRate
            // Sawtooth-ish rich tone
            val wave = (sin(phase) + sin(phase * 2.0) * 0.5 + sin(phase * 4.0) * 0.25).coerceIn(-1.0, 1.0)
            val sample = (wave * envelope * 0.8 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateMissBuzz(sampleRate: Int): ByteArray {
        val durationMs = 120
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = (1.0 - t)
            val buzz = if (i % 60 < 30) 1.0 else -1.0
            val sample = (buzz * envelope * 0.5 * Short.MAX_VALUE).toInt()
            val clamped = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i * 2] = (clamped and 0xFF).toByte()
            buffer[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        return buffer
    }
}
