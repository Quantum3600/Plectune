package com.trishit.plectune.feature.tuner.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.trishit.plectune.feature.tuner.domain.Yin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.math.sqrt


class TunerEngine {
    private val sampleRate = 44100
    private val yinBufferSize = 4096 // 4096 gives better bass resolution (E2)
    private val yin = Yin(sampleRate.toFloat(), yinBufferSize)

    // SENSITIVITY SETTINGS
    // 0.01f allows detection of sustain. 0.05f was too high (cutoff too early).
    private val minRms = 0.01f

    // CONFIDENCE THRESHOLD
    // 0.35f filters out talking/tapping. Requires a clear tonal sound.
    private val minProbability = 0.35f

    @SuppressLint("MissingPermission")
    fun startListening(): Flow<Float> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val bufferSize = maxOf(minBufferSize, yinBufferSize * 2)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val buffer = ShortArray(yinBufferSize) // Read in chunks matching Yin size

        try {
            audioRecord.startRecording()

            // Use coroutine context to check for cancellation
            while (currentCoroutineContext().isActive) {
                val readCount = audioRecord.read(buffer, 0, yinBufferSize)

                if (readCount > 0) {
                    val rms = calculateRms(buffer, readCount)

                    // 1. If too quiet, emit -1 (Silence)
                    if (rms < minRms) {
                        emit(-1f)
                        continue
                    }

                    // 2. Get Pitch
                    val pitch = yin.getPitch(buffer)

                    // 3. Check Confidence (Probability)
                    // Only emit if Yin is confident it's a real note, not just loud noise.
                    if (pitch > 0f && yin.probability > minProbability) {
                        emit(pitch)
                    } else {
                        // Loud but undefined (e.g. percussion/talking) -> Treat as silence
                        emit(-1f)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioRecord.stop()
                audioRecord.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }

    private fun calculateRms(buffer: ShortArray, count: Int): Float {
        var sumSq = 0.0
        // Pre-calculate inverse for speed
        val inv = 1.0 / Short.MAX_VALUE.toDouble()

        for (i in 0 until count) {
            val v = buffer[i].toDouble() * inv
            sumSq += v * v
        }
        return sqrt(sumSq / count).toFloat()
    }
}