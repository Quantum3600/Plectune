package com.trishit.plectune.feature.metronome.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sin

class MetronomeEngine {
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null
    private val tickSound: ShortArray by lazy {
        val sampleRate = 44100
        val durationMs = 30
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ShortArray(numSamples)
        val freq = 1200.0
        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / numSamples)
            val sine = sin(2.0 * Math.PI * freq * time)
            buffer[i] = (sine * decay * Short.MAX_VALUE).toInt().toShort()
        }
        buffer
    }
    suspend fun start(
        bpm: Int,
        onTick: () -> Unit
    ) {
        stop()
        isPlaying = true
        val bufferSize = tickSound.size * 2
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(bufferSize)
            .build()
        audioTrack?.write(tickSound, 0, tickSound.size)
        job = CoroutineScope(Dispatchers.Default).launch {
            val intervalMs = (60_000 / bpm).toLong()
            var nextTickTime = System.nanoTime()
            val intevalNanos = intervalMs * 1_000_000
            while (isActive && isPlaying) {
                val now = System.nanoTime()
                if(now >= nextTickTime) {
                    audioTrack?.stop()
                    audioTrack?.reloadStaticData()
                    audioTrack?.play()
                    withContext(Dispatchers.Main) { onTick() }
                    nextTickTime += intevalNanos
                }
                delay(1)
            }
        }
    }
    fun stop() {
        isPlaying = false
        job?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
    }
}