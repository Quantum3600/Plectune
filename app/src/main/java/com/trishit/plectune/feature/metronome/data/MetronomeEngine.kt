package com.trishit.plectune.feature.metronome.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToLong
import kotlin.math.sin

class MetronomeEngine {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val startStopMutex = Mutex()
    @Volatile private var isPlaying = false

    private var normalTrack: AudioTrack? = null
    private var accentTrack: AudioTrack? = null
    private var job: Job? = null

    private fun generateClick(freqHz: Double, amplitude: Double): ShortArray {
        val sampleRate = 44100
        val durationMs = 30
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / numSamples)
            val sine = sin(2.0 * Math.PI * freqHz * time)
            val v = sine * decay * amplitude
            buffer[i] = (v * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private val normalClick: ShortArray by lazy { generateClick(freqHz = 1200.0, amplitude = 0.65) }
    private val accentClick: ShortArray by lazy { generateClick(freqHz = 1700.0, amplitude = 0.95) }


    suspend fun start(bpm: Int, beatsPerBar: Int, onTick: (beatInBar: Int, isAccent: Boolean) -> Unit) {
        require(bpm > 0) { "bpm must be > 0" }
        require(beatsPerBar > 0) { "beatsPerBar must be > 0" }

        startStopMutex.withLock {
            stopLocked()

            isPlaying = true

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            fun buildStaticTrack(click: ShortArray): AudioTrack {
                val bufferSizeBytes = click.size * 2
                return AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .build()
                    .also { it.write(click, 0, click.size) }
            }

            normalTrack = buildStaticTrack(normalClick)
            accentTrack = buildStaticTrack(accentClick)

            val intervalNanos = (60_000_000_000.0 / bpm.toDouble()).roundToLong()

            job = engineScope.launch {
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                } catch (_: Throwable) {
                    // ignore
                }

                var nextTickTime = System.nanoTime()
                var beat = 0

                while (isActive && isPlaying) {
                    val now = System.nanoTime()
                    val remaining = nextTickTime - now

                    if (remaining > 0L) {
                        val ms = remaining / 1_000_000L
                        val ns = (remaining % 1_000_000L).toInt() // must be 0..999_999
                        try {
                            Thread.sleep(ms, ns)
                        } catch (_: InterruptedException) {
                            // allow cancellation
                        } catch (_: IllegalArgumentException) {
                            // shouldn't happen now; don't crash
                        }
                        continue
                    }

                    val beatInBar = beat % beatsPerBar
                    val isAccent = beatInBar == 0

                    val track = if (isAccent) accentTrack else normalTrack
                    if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
                        isPlaying = false
                        break
                    }

                    try {
                        // replay without reloading static data
                        track.pause()
                        try {
                            track.flush()
                        } catch (_: Throwable) {
                            // some devices are picky; ignore if unsupported in this state
                        }
                        track.playbackHeadPosition = 0
                        track.play()

                        engineScope.launch(Dispatchers.Main.immediate) {
                            onTick(beatInBar, isAccent)
                        }
                    } catch (_: IllegalStateException) {
                        isPlaying = false
                        break
                    }

                    beat++

                    nextTickTime += intervalNanos
                    while (nextTickTime <= System.nanoTime()) {
                        nextTickTime += intervalNanos
                    }
                }
            }
        }
    }

    fun stop() {
        // Non-suspending API for callers; serialize internally
        engineScope.launch {
            startStopMutex.withLock {
                stopLocked()
            }
        }
    }

    private suspend fun stopLocked() {
        isPlaying = false

        val j = job
        job = null
        if (j != null) {
            j.cancel()
            j.join()
        }

        val n = normalTrack
        val a = accentTrack
        normalTrack = null
        accentTrack = null

        fun releaseTrack(t: AudioTrack?) {
            if (t == null) return
            try {
                t.pause()
                try {
                    t.flush()
                } catch (_: Throwable) {
                    // ignore
                }
                t.stop()
            } catch (_: Exception) {
                // ignore
            }
            try {
                t.release()
            } catch (_: Exception) {
                // ignore
            }
        }

        releaseTrack(n)
        releaseTrack(a)
    }
}