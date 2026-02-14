package com.trishit.plectune.feature.tuner.domain

import kotlin.math.abs

class Yin(
    private val sampleRate: Float = 44100.0f,
    bufferSize: Int
) {
    private val halfBufferSize = bufferSize / 2
    private val yinBuffer = FloatArray(halfBufferSize)
    private val threshold = 0.10f

    var probability: Float = 0.0f
        private set

    fun getPitch(buffer: ShortArray): Float {
        // Step 2: Difference Function
        yinBuffer.fill(0f)
        for (tau in 0 until halfBufferSize) {
            for (i in 0 until halfBufferSize) {
                val delta = buffer[i].toFloat() - buffer[i + tau].toFloat()
                yinBuffer[tau] += delta * delta
            }
        }

        // Step 3: Cumulative Mean Normalized Difference Function
        yinBuffer[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfBufferSize) {
            runningSum += yinBuffer[tau]
            // Use 1f if runningSum is 0 to avoid division by zero
            yinBuffer[tau] *= if (runningSum > 0) tau / runningSum else 1f
        }

        // Step 4: Absolute Thresholding
        var tauEstimate = -1
        for (tau in 2 until halfBufferSize) {
            if (yinBuffer[tau] < threshold) {
                var currentTau = tau
                // Find local minimum
                while (currentTau + 1 < halfBufferSize && yinBuffer[currentTau + 1] < yinBuffer[currentTau]) {
                    currentTau++
                }
                probability = 1f - yinBuffer[currentTau]
                tauEstimate = currentTau
                break
            }
        }

        if (tauEstimate != -1) {
            // Step 5: Parabolic Interpolation
            val betterTau = parabolicInterpolation(tauEstimate)
            return sampleRate / betterTau
        }

        probability = 0f
        return -1.0f
    }

    private fun parabolicInterpolation(tauEstimate: Int): Float {
        val s0: Float
        val s2: Float

        val x0 = tauEstimate - 1
        val x2 = tauEstimate + 1

        if (x0 < 0) return if (x2 < halfBufferSize && yinBuffer[x2] < yinBuffer[tauEstimate]) x2.toFloat() else tauEstimate.toFloat()
        if (x2 >= halfBufferSize) return if (yinBuffer[x0] < yinBuffer[tauEstimate]) x0.toFloat() else tauEstimate.toFloat()

        s0 = yinBuffer[x0]
        val s1: Float = yinBuffer[tauEstimate]
        s2 = yinBuffer[x2]

        val denominator = 2 * (2 * s1 - s2 - s0)
        // Check for near-zero denominator to avoid NaN
        return if (abs(denominator) < 1e-6) {
            tauEstimate.toFloat()
        } else {
            tauEstimate + (s2 - s0) / denominator
        }
    }
}