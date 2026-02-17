package com.trishit.plectune.feature.metronome.data

import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
@Keep
class MetronomeEngine {

    // --- OBOE/JNI Implementation Placeholder ---
    companion object {
        init {
            try {
                // NOTE: This assumes 'metronome-native' library is built and placed correctly by CMake.
                System.loadLibrary("metronome-native")
                Log.i("MetronomeEngine", "Native metronome library loaded.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("MetronomeEngine", "Failed to load native library: metronome-native. Oboe will not work.", e)
            }
        }
    }

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val startStopMutex = Mutex()
    @Volatile private var isPlaying = false
    
    // New state flow for ticks, replacing the callback lambda
    private val _tickFlow = MutableSharedFlow<Pair<Int, Boolean>>(extraBufferCapacity = 10)
    val tickFlow: SharedFlow<Pair<Int, Boolean>> = _tickFlow.asSharedFlow()
    
    // JNI Declarations (must match native C++ function signatures)
    private external fun nativeStart(bpm: Int, beatsPerBar: Int): Boolean
    private external fun nativeStop(): Boolean
    private external fun nativeSetBpm(bpm: Int): Boolean
    private external fun nativeSetBeatsPerBar(beatsPerBar: Int): Boolean


    // This method MUST be implemented in C++ (metronome_native.cpp)
    // and called by the native Oboe scheduling loop when a beat occurs.
    @Suppress("unused")
    private fun onTickFromNative(beatInBar: Int, isAccent: Boolean) {
        engineScope.launch {
            // This runs on Default dispatcher because engineScope is Default.
            // ViewModel will collect this and update UI state on Main dispatcher.
            _tickFlow.emit(beatInBar to isAccent)
        }
    }
    
    // The public API must change to reflect that it no longer takes a lambda.
    suspend fun start(bpm: Int, beatsPerBar: Int): Boolean {
        require(bpm > 0) { "bpm must be > 0" }
        require(beatsPerBar > 0) { "beatsPerBar must be > 0" }

        return startStopMutex.withLock {
            if (isPlaying) stopLocked()
            isPlaying = true

            val ok = nativeStart(bpm, beatsPerBar)
            if (!ok) {
                Log.e("MetronomeEngine", "Native start failed.")
                isPlaying = false
            }
            ok
        }
    }

    fun stop() {
        engineScope.launch {
            startStopMutex.withLock {
                stopLocked()
            }
        }
    }

    suspend fun setBpm(bpm: Int): Boolean {
        require(bpm > 0) { "bpm must be > 0" }
        return startStopMutex.withLock {
            if (!isPlaying) return@withLock false
            nativeSetBpm(bpm)
        }
    }
    suspend fun setBeatsPerBar(beatsPerBar: Int): Boolean {
        require(beatsPerBar > 0) { "beatsPerBar must be > 0" }
        return startStopMutex.withLock {
            if (!isPlaying) return@withLock false
            nativeSetBeatsPerBar(beatsPerBar)
        }
    }
    private fun stopLocked() {
        if (!isPlaying) return
        isPlaying = false
        nativeStop()
    }
}