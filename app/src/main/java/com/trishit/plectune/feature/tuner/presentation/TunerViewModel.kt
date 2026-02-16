package com.trishit.plectune.feature.tuner.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.plectune.feature.tuner.data.ToneGenerator
import com.trishit.plectune.feature.tuner.data.TunerEngine
import com.trishit.plectune.feature.tuner.domain.GuitarString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ln

class TunerViewModel : ViewModel() {

    private val tunerEngine = TunerEngine()
    private val toneGenerator = ToneGenerator()
    private var listeningJob: Job? = null

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState = _uiState.asStateFlow()

    // --- TUNING CONFIGURATION ---

    // Visual clamping: Limit the gauge needle to +/- 50 cents
    private val maxCentsForGauge = 50f

    // "In Tune" threshold (green zone)
    private val hitCents = 5f

    // Smoothing Variables (GuitarTuna Feel)
    // 0.2 = Very smooth/slow, 0.8 = Fast/jittery. 0.35 is a good balance.
    private val smoothingFactor = 0.35f
    private var smoothedFreq = 0f

    // Median Filter Queue: Stores last 5 raw pitches to remove random "pops" or glitches.
    private val pitchQueue = ArrayDeque<Float>()
    private val maxQueueSize = 5

    // Auto-Advance Logic
    private val stableFramesToAdvance = 8 // Require slightly more stability before auto-advancing
    private val autoAdvanceCooldownMs = 1200L
    private var autoStableFrames = 0
    private var nextAllowedAdvanceAtMs = 0L

    private val historyDecay = 0.86f
    private val historyDropBelow = 0.35f

    fun onEvent(event: TunerEvent) {
        when (event) {
            is TunerEvent.StartListening -> startTuner()
            is TunerEvent.StopListening -> stopTuner()
            is TunerEvent.SelectString -> {
                val currentMode = _uiState.value.mode
                if (currentMode is TunerMode.Manual && currentMode.string == event.string) {
                    playTone(event.string)
                } else {
                    _uiState.update {
                        it.copy(mode = TunerMode.Manual(event.string), activeString = event.string)
                    }
                    playTone(event.string)
                }
            }
            is TunerEvent.SetAutoMode -> {
                _uiState.update { state ->
                    if (event.enabled) {
                        resetSmoothing()
                        state.copy(
                            mode = TunerMode.Auto,
                            activeString = GuitarString.E2,
                            autoTarget = GuitarString.E2
                        )
                    } else {
                        val fallback = state.activeString ?: GuitarString.E2
                        state.copy(mode = TunerMode.Manual(fallback), activeString = fallback)
                    }
                }
            }
        }
    }

    private fun startTuner() {
        if (listeningJob?.isActive == true) return
        resetSmoothing()

        listeningJob = viewModelScope.launch(Dispatchers.IO) {
            tunerEngine.startListening().collect { frequency ->
                processFrequency(frequency)
            }
        }
    }

    private fun stopTuner() {
        listeningJob?.cancel()
        toneGenerator.stop()
    }

    private fun resetSmoothing() {
        smoothedFreq = 0f
        pitchQueue.clear()
        autoStableFrames = 0
    }

    private fun playTone(string: GuitarString) {
        viewModelScope.launch {
            toneGenerator.playTone(string.frequency)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTuner()
    }

    private fun processFrequency(rawFrequency: Float) {
        _uiState.update { state ->
            val decayedHistory = decayHistory(state.pitchHistory)

            // 1. SILENCE / INVALID INPUT
            // If the engine sends -1 (silence) or 0, we decay the needle.
            if (rawFrequency <= 0f) {
                // If we've been silent for a while, reset smoothing so the next note starts fresh
                if (pitchQueue.isEmpty()) smoothedFreq = 0f

                // Slowly clear queue
                if (pitchQueue.isNotEmpty()) pitchQueue.removeFirst() else smoothedFreq = 0f

                return@update state.copy(
                    currentFreq = 0f, // Or keep last freq to show "last heard note"
                    // noteName = "--", // Optional: Don't clear note name immediately for better UX
                    centsOff = 0f,
                    isStable = false,
                    pitchHistory = decayedHistory,
                    activeString = if (state.mode is TunerMode.Auto) state.activeString else state.activeString
                )
            }

            // 2. MEDIAN FILTER (Remove outliers)
            if (pitchQueue.size >= maxQueueSize) pitchQueue.removeFirst()
            pitchQueue.addLast(rawFrequency)

            // Need at least 3 samples to calculate a valid median
            val medianFreq = if (pitchQueue.size < 3) rawFrequency else pitchQueue.sorted()[pitchQueue.size / 2]

            // 3. EXPONENTIAL SMOOTHING (Low Pass Filter)
            // If jump is huge (string switch), snap instantly. Otherwise, smooth it.
            val frequency = if (smoothedFreq == 0f || abs(medianFreq - smoothedFreq) > 40f) {
                medianFreq
            } else {
                (medianFreq * smoothingFactor) + (smoothedFreq * (1 - smoothingFactor))
            }
            smoothedFreq = frequency

            // 4. DETERMINE TARGET STRING
            val targetString = if (state.mode is TunerMode.Manual) {
                state.mode.string
            } else {
                // Auto Mode: Find closest string dynamically
                GuitarString.entries.minByOrNull { abs(it.frequency - frequency) } ?: GuitarString.E2
            }

            // 5. CALCULATE CENTS & UI
            val cents = centsBetween(frequency, targetString.frequency)

            // Visual Clamp: Keep the needle on screen (-50 to +50)
            val clampedCents = cents.coerceIn(-maxCentsForGauge, maxCentsForGauge)
            val isStable = abs(clampedCents) < hitCents
            val newHistory = (listOf(clampedCents) + decayedHistory).take(50)

            // 6. AUTO MODE ADVANCE LOGIC
            if (state.mode is TunerMode.Auto) {
                // Check if we are tuning the INTENDED string (or if user skipped ahead)
                // If 'targetString' matches current 'autoTarget', track stability.
                // If user is tuning a different string, reset stability for the *target*.

                if (targetString == state.autoTarget && isStable) {
                    autoStableFrames++
                } else {
                    autoStableFrames = 0
                }

                val now = SystemClock.elapsedRealtime()
                if (autoStableFrames >= stableFramesToAdvance && now >= nextAllowedAdvanceAtMs) {
                    autoStableFrames = 0
                    nextAllowedAdvanceAtMs = now + autoAdvanceCooldownMs
                    viewModelScope.launch { toneGenerator.playConfirmationTone() }

                    // Optional: Automatically switch "Active Goal" to next string
                    val next = nextString(targetString)

                    return@update state.copy(
                        currentFreq = frequency,
                        noteName = targetString.noteName,
                        centsOff = clampedCents,
                        isStable = true,
                        pitchHistory = newHistory,
                        activeString = next, // Move UI highlight to next string
                        autoTarget = next
                    )
                }
            }

            state.copy(
                currentFreq = frequency,
                noteName = targetString.noteName,
                centsOff = clampedCents,
                isStable = isStable,
                pitchHistory = newHistory,
                activeString = targetString, // Highlight the string we actually hear
                autoTarget = if (state.mode is TunerMode.Auto) targetString else state.autoTarget
            )
        }
    }

    private fun nextString(current: GuitarString): GuitarString {
        val order = GuitarString.entries
        val idx = order.indexOf(current)
        return if (idx >= 0) order[(idx + 1) % order.size] else GuitarString.E2
    }

    private fun decayHistory(history: List<Float>): List<Float> {
        if (history.isEmpty()) return history
        return history
            .asSequence()
            .map { it * historyDecay }
            .filter { abs(it) >= historyDropBelow }
            .take(50)
            .toList()
    }

    private fun centsBetween(freq: Float, target: Float): Float {
        return (1200f * (ln((freq / target).toDouble()) / ln(2.0))).toFloat()
    }
}