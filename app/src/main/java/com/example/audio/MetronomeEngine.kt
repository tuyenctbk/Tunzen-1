package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class MetronomeEngine {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var metronomeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _currentBeat = MutableStateFlow(1)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    var timeSignatureBeats: Int = 4
    var timeSignatureNoteValue: Int = 4

    private val tapTimestamps = mutableListOf<Long>()

    private val highClickBuffer: ShortArray by lazy { generateClickBuffer(1200.0) }
    private val lowClickBuffer: ShortArray by lazy { generateClickBuffer(800.0) }

    private fun generateClickBuffer(frequencyHz: Double): ShortArray {
        val durationMs = 15
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val angleStep = 2.0 * Math.PI * frequencyHz / sampleRate

        for (i in 0 until totalSamples) {
            val angle = i * angleStep
            val envelope = (1.0 - i.toDouble() / totalSamples) // Linear decay
            val sample = (sin(angle) * envelope * 28000.0).toInt().coerceIn(-32768, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(30, 300)
    }

    fun registerTapTempo(): Int {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)

        // Remove taps older than 3 seconds
        tapTimestamps.removeAll { now - it > 3000 }

        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgIntervalMs = intervals.average()
            if (avgIntervalMs > 0) {
                val calculatedBpm = (60000.0 / avgIntervalMs).toInt().coerceIn(30, 300)
                setBpm(calculatedBpm)
                return calculatedBpm
            }
        }
        return _bpm.value
    }

    fun start() {
        stop()
        _isPlaying.value = true

        try {
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
                .setBufferSizeInBytes(highClickBuffer.size * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            metronomeJob = scope.launch {
                var beatCount = 1
                while (isActive && _isPlaying.value) {
                    _currentBeat.value = beatCount

                    val clickBuffer = if (beatCount == 1) highClickBuffer else lowClickBuffer
                    audioTrack?.write(clickBuffer, 0, clickBuffer.size)

                    val intervalMs = (60000.0 / _bpm.value).toLong()
                    delay(intervalMs)

                    beatCount = if (beatCount >= timeSignatureBeats) 1 else beatCount + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun stop() {
        _isPlaying.value = false
        metronomeJob?.cancel()
        metronomeJob = null

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
