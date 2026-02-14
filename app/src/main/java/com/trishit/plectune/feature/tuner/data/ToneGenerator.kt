package com.trishit.plectune.feature.tuner.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

class ToneGenerator {
    private val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null

    /**
     * Generates and plays a synthesized guitar-like tone.
     * Uses additive synthesis to make bass notes audible on phone speakers.
     */
    suspend fun playTone(frequency: Float) {
        // Stop any currently playing tone
        stop()

        withContext(Dispatchers.Default) {
            val buffer = generateComplexTone(frequency)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(
                    AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            audioTrack = track
        }
    }

    private fun generateComplexTone(fundamentalFreq: Float): ShortArray {
        val durationMs = 2000 // 2 seconds sustain
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = FloatArray(numSamples)

        // Additive Synthesis: Stack harmonics to simulate a plucked string
        // 1. Fundamental (The actual note) - Weak on phones
        addSineWave(buffer, fundamentalFreq, 0.6f)

        // 2. 2nd Harmonic (Octave up) - Adds clarity
        addSineWave(buffer, fundamentalFreq * 2, 0.3f)

        // 3. 3rd Harmonic (Fifth) - Adds "body"
        addSineWave(buffer, fundamentalFreq * 3, 0.1f)

        // Convert to PCM 16-bit
        val shortBuffer = ShortArray(numSamples)
        for (i in buffer.indices) {
            // Apply a simple envelope (Attack + Decay) to avoid clicking
            val envelope = calculateEnvelope(i, numSamples)
            val value = (buffer[i] * envelope).coerceIn(-1f, 1f)
            shortBuffer[i] = (value * Short.MAX_VALUE).toInt().toShort()
        }
        return shortBuffer
    }

    private fun addSineWave(buffer: FloatArray, frequency: Float, amplitude: Float) {
        for (i in buffer.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            buffer[i] += (sin(2.0 * Math.PI * frequency * t).toFloat() * amplitude)
        }
    }

    private fun calculateEnvelope(index: Int, totalSamples: Int): Float {
        // Simple ADSR-like envelope
        val attackSamples = SAMPLE_RATE / 10 // 0.1s attack

        return when {
            index < attackSamples -> index.toFloat() / attackSamples // Fade In
            else -> 1.0f - ((index - attackSamples).toFloat() / (totalSamples - attackSamples)) // Linear Fade Out
        }
    }

    fun stop() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore errors if track is already released
        }
        audioTrack = null
    }
}