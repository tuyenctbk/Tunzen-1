package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class ToneGeneratorEngine {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playTone(
        frequencyHz: Double,
        waveType: String = "SINE", // "SINE", "TRIANGLE", "SOFT"
        durationMs: Long = 3000
    ) {
        stopTone()

        if (frequencyHz <= 10.0 || frequencyHz > 10000.0) return

        playbackJob = scope.launch {
            try {
                val sampleRate = 44100
                val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(4410)
                val buffer = ShortArray(totalSamples)

                val anglePerSample = 2.0 * Math.PI * frequencyHz / sampleRate

                for (i in 0 until totalSamples) {
                    val angle = i * anglePerSample
                    val rawSample = when (waveType.uppercase()) {
                        "TRIANGLE" -> {
                            val cycle = (i * frequencyHz / sampleRate) % 1.0
                            if (cycle < 0.5) 4.0 * cycle - 1.0 else 3.0 - 4.0 * cycle
                        }
                        "SOFT" -> {
                            // Sine with subtle second harmonic
                            0.8 * sin(angle) + 0.2 * sin(2.0 * angle)
                        }
                        else -> sin(angle)
                    }

                    // Apply Attack & Release Fade envelope to prevent audio clicking
                    val fadeSamples = (sampleRate * 0.05).toInt()
                    val envelope = when {
                        i < fadeSamples -> i.toDouble() / fadeSamples
                        i > totalSamples - fadeSamples -> (totalSamples - i).toDouble() / fadeSamples
                        else -> 1.0
                    }

                    val sampleValue = (rawSample * envelope * 24000.0).toInt().coerceIn(-32768, 32767)
                    buffer[i] = sampleValue.toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack?.write(buffer, 0, buffer.size)
                audioTrack?.play()

                delay(durationMs)
                stopTone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopTone() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }
}
