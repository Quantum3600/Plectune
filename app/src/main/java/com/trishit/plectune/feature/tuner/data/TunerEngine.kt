package com.trishit.plectune.feature.tuner.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.trishit.plectune.feature.tuner.domain.Yin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Thread.currentThread

class TunerEngine {
    private val sampleRate = 44100
    private val yinBufferSize = 4096
    private val yin = Yin(sampleRate.toFloat(), yinBufferSize)
    @SuppressLint("MissingPermission")
    fun startListening(): Flow<Float> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val bufferSize = maxOf(minBufferSize, yinBufferSize)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        val buffer = ShortArray(bufferSize)
        audioRecord.startRecording()
        try {
            while (currentThread().isAlive) {
                val readCount = audioRecord.read(buffer, 0, bufferSize)
                if(readCount > 0) {
                    val pitch = yin.getPitch(buffer)
                    if(yin.probability > 0.1f) emit(pitch)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioRecord.stop()
                audioRecord.release()
            } catch (e: Exception) {}
        }
    }
}