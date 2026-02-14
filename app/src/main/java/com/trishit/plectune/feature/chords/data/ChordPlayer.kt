package com.trishit.plectune.feature.chords.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.trishit.plectune.feature.chords.domain.Chord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

class ChordPlayer {
    private val SAMPLE_RATE = 44100

    // Standard Tuning Base Frequencies (Open Strings: E2, A2, D3, G3, B3, E4)
    private val OPEN_STRINGS = listOf(82.41, 110.00, 146.83, 196.00, 246.94, 329.63)

    fun playChord(chord: Chord) {
        CoroutineScope(Dispatchers.Default).launch {
            val buffer = generateStrum(chord)
            playSound(buffer)
        }
    }

    private fun generateStrum(chord: Chord): ShortArray {
        val durationMs = 1500 // 1.5 seconds decay
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = FloatArray(numSamples)

        // 1. Calculate active frequencies for this chord
        val frequencies = mutableListOf<Double>()
        chord.frets.forEachIndexed { stringIndex, fret ->
            if (fret >= 0) { // If not muted (-1)
                // Formula: Freq = OpenFreq * 2^(fret/12)
                val freq = OPEN_STRINGS[stringIndex] * 2.0.pow(fret / 12.0)
                frequencies.add(freq)
            }
        }

        // 2. Mix the sine waves (Additive Synthesis)
        // We stagger the start times slightly to simulate a "Strum" (Arpeggio effect)
        val strumDelaySamples = 1500 // ~30ms delay between strings

        frequencies.forEachIndexed { index, freq ->
            val startOffset = index * strumDelaySamples

            for (i in startOffset until numSamples) {
                val t = (i - startOffset).toDouble() / SAMPLE_RATE

                // Karplus-Strong-ish decay (Exponential fade out)
                val decay = exp(-3.0 * t)

                // Simple Sine Wave (Guitar-like tone would need saw/triangle, but sine is clean)
                val wave = sin(2.0 * Math.PI * freq * t)

                // Add to mix (divide by 6 to prevent clipping)
                buffer[i] += (wave * decay * 0.15).toFloat()
            }
        }

        // 3. Convert Float to Short (PCM 16-bit)
        val shortBuffer = ShortArray(numSamples)
        for (i in buffer.indices) {
            // Clamp values to -1.0 to 1.0 to avoid distortion
            val clamped = buffer[i].coerceIn(-1f, 1f)
            shortBuffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
        }

        return shortBuffer
    }

    private fun playSound(data: ShortArray) {
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
            .setBufferSizeInBytes(data.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(data, 0, data.size)
        track.play()

        // Clean up after playing (simple way)
        // In a real app, you might recycle this track
        Thread.sleep(2000)
        track.release()
    }
}