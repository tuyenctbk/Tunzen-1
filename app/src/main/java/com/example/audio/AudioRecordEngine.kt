package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.model.InstrumentPreset
import com.example.model.PitchResult
import com.example.model.TuningString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class AudioRecordEngine {

    private val sampleRate = 44100
    private val bufferSize = 2048

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val pitchDetector = PitchDetector(sampleRate, bufferSize)

    private val _pitchState = MutableStateFlow(PitchResult())
    val pitchState: StateFlow<PitchResult> = _pitchState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    var referenceA4: Double = 440.0
    var selectedTemperament: String = "STANDARD"
    var noiseThresholdRms: Double = 0.015 // Adjustable sensitivity threshold
    var dynamicSensitivityEnabled: Boolean = true
    var autoGainControlEnabled: Boolean = true // Automated Microphone Input Gain Controller

    private var currentAgcGain: Float = 1.0f
    private val targetAgcRms: Double = 0.18 // Ideal normalized amplitude for clear waveform & robust pitch detection
    private var runningNoiseFloorRms: Double = 0.008 // Adaptive background noise floor tracker
    var currentPreset: InstrumentPreset = MusicUtils.CHROMATIC_PRESET
    var selectedString: TuningString? = null

    @SuppressLint("MissingPermission")
    fun startRecording() {
        stopRecording()
        stopSimulation()

        try {
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val actualBufSize = minBufSize.coerceAtLeast(bufferSize * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                actualBufSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                _isRecording.value = true
                _isSimulationMode.value = false

                recordJob = scope.launch {
                    val pcmBuffer = ShortArray(bufferSize)
                    while (isActive && _isRecording.value) {
                        val readSize = audioRecord?.read(pcmBuffer, 0, bufferSize) ?: 0
                        if (readSize > 0) {
                            processPcmData(pcmBuffer, readSize)
                        }
                        delay(25) // ~40 updates per second for ultra smooth 60fps UI
                    }
                }
            } else {
                // Fallback to simulation mode if hardware mic unavailable or restricted
                startSimulation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            startSimulation()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }

    private fun processPcmData(buffer: ShortArray, readSize: Int) {
        val rawRms = pitchDetector.calculateRms(buffer, readSize)

        // Calculate dynamic AGC gain factor
        if (autoGainControlEnabled) {
            if (rawRms > 0.002) {
                val desiredGain = (targetAgcRms / rawRms).toFloat().coerceIn(0.5f, 15.0f)
                // Smoothly adapt gain: fast attack on loud signals to prevent clipping, smooth decay on soft notes
                if (desiredGain < currentAgcGain) {
                    currentAgcGain = currentAgcGain * 0.65f + desiredGain * 0.35f
                } else {
                    currentAgcGain = currentAgcGain * 0.90f + desiredGain * 0.10f
                }
            } else {
                // Near silence: gradually relax gain back towards 1.0f baseline
                currentAgcGain = currentAgcGain * 0.95f + 1.0f * 0.05f
            }
        } else {
            currentAgcGain = 1.0f
        }

        // Apply dynamic gain normalization to PCM buffer for waveform and pitch processing
        val normalizedBuffer = ShortArray(readSize)
        if (readSize > 0) {
            val gainMultiplier = currentAgcGain
            for (i in 0 until readSize) {
                normalizedBuffer[i] = (buffer[i] * gainMultiplier).toInt().coerceIn(-32768, 32767).toShort()
            }
        }

        val effectiveRms = pitchDetector.calculateRms(normalizedBuffer, readSize)
        val fft = pitchDetector.computeFftSpectrum(normalizedBuffer, readSize, 64)

        // Extract normalized PCM waveform samples (-1.0 to 1.0)
        val waveformSamples = FloatArray(64)
        if (readSize > 0) {
            val sampleStep = readSize.toFloat() / 64f
            for (i in 0 until 64) {
                val idx = (i * sampleStep).toInt().coerceIn(0, readSize - 1)
                waveformSamples[i] = (normalizedBuffer[idx] / 32768.0f).coerceIn(-1.0f, 1.0f)
            }
        }

        // Dynamically compute noise gate threshold or use user-specified static one
        val activeThreshold = if (dynamicSensitivityEnabled) {
            if (rawRms < 0.08) {
                // Smoothly update running background noise floor using EMA
                runningNoiseFloorRms = runningNoiseFloorRms * 0.95 + rawRms * 0.05
            }
            // Dynamic threshold is scaled above floor, bounded for sanity
            (runningNoiseFloorRms * 1.8).coerceIn(0.005, 0.06)
        } else {
            noiseThresholdRms
        }

        // Noise gate filter (checked against raw input RMS to ensure noise is rejected)
        if (rawRms < activeThreshold) {
            _pitchState.value = PitchResult(
                amplitudeRms = effectiveRms,
                fftMagnitude = fft,
                isPitchDetected = false,
                activeThreshold = activeThreshold,
                waveform = waveformSamples,
                appliedGainFactor = currentAgcGain,
                agcEnabled = autoGainControlEnabled,
                rawInputRms = rawRms
            )
            return
        }

        val freq = pitchDetector.detectPitch(normalizedBuffer, readSize)

        if (freq > 20.0 && freq < 4200.0) {
            val (noteName, octave, cents) = MusicUtils.getClosestNoteInfo(freq, referenceA4, selectedTemperament)

            // Match against target string if instrument preset is selected
            val targetString = matchTargetString(freq, noteName, octave)
            val inTune = abs(cents) <= 2.5 // In-tune within +/- 2.5 cents

            _pitchState.value = PitchResult(
                frequency = freq,
                noteName = noteName,
                octave = octave,
                centsOffset = cents.coerceIn(-50.0, 50.0),
                targetNote = targetString?.noteName ?: noteName,
                targetFrequency = targetString?.targetFrequency ?: run {
                    val noteIndex = MusicUtils.NOTE_NAMES.indexOf(noteName)
                    val midiNum = if (noteIndex != -1) ((octave + 1) * 12 + noteIndex) else MusicUtils.frequencyToMidiNote(freq, referenceA4).toInt()
                    MusicUtils.midiNoteToFrequency(midiNum.coerceIn(0, 127), referenceA4)
                },
                matchedString = targetString,
                inTune = inTune,
                isPitchDetected = true,
                amplitudeRms = effectiveRms,
                fftMagnitude = fft,
                activeThreshold = activeThreshold,
                waveform = waveformSamples,
                appliedGainFactor = currentAgcGain,
                agcEnabled = autoGainControlEnabled,
                rawInputRms = rawRms
            )
        } else {
            _pitchState.value = PitchResult(
                amplitudeRms = effectiveRms,
                fftMagnitude = fft,
                isPitchDetected = false,
                activeThreshold = activeThreshold,
                waveform = waveformSamples,
                appliedGainFactor = currentAgcGain,
                agcEnabled = autoGainControlEnabled,
                rawInputRms = rawRms
            )
        }
    }

    private fun matchTargetString(detectedFreq: Double, detectedNote: String, octave: Int): TuningString? {
        if (selectedString != null) return selectedString

        if (currentPreset.strings.isEmpty()) return null

        // Find string with closest frequency
        var bestString: TuningString? = null
        var minDiff = Double.MAX_VALUE

        for (str in currentPreset.strings) {
            val diff = abs(detectedFreq - str.targetFrequency)
            if (diff < minDiff) {
                minDiff = diff
                bestString = str
            }
        }

        return if (minDiff < 40.0) bestString else null
    }

    // Demo Interactive Simulation Mode (allows full testing of UI/Sliders/Needle in background preview)
    fun startSimulation() {
        stopSimulation()
        _isSimulationMode.value = true
        _isRecording.value = true

        simulationJob = scope.launch {
            var simFreq = 329.63 // E4
            var step = 0.4
            var frame = 0

            while (isActive && _isSimulationMode.value) {
                frame++
                // Oscillate gently around E4 target to simulate live tuning
                simFreq += step
                if (simFreq > 334.0 || simFreq < 325.0) step = -step

                val (noteName, octave, cents) = MusicUtils.getClosestNoteInfo(simFreq, referenceA4, selectedTemperament)
                val targetString = matchTargetString(simFreq, noteName, octave)

                // Simulated FFT array
                val simFft = FloatArray(64) { i ->
                    val centerBin = 20
                    val dist = abs(i - centerBin)
                    (1.0f / (1.0f + dist * 0.4f) + (Math.random() * 0.1).toFloat()).coerceIn(0f, 1f)
                }

                // Simulated PCM waveform array (moving fundamental sine wave + 2nd harmonic)
                val simWaveform = FloatArray(64) { i ->
                    val phase = (frame * 0.15) + (i * 0.25)
                    (kotlin.math.sin(phase) * 0.6 + kotlin.math.sin(phase * 2.1) * 0.25 + (Math.random() - 0.5) * 0.05).toFloat().coerceIn(-1f, 1f)
                }

                _pitchState.value = PitchResult(
                    frequency = simFreq,
                    noteName = noteName,
                    octave = octave,
                    centsOffset = cents.coerceIn(-50.0, 50.0),
                    targetNote = targetString?.noteName ?: noteName,
                    targetFrequency = targetString?.targetFrequency ?: 329.63,
                    matchedString = targetString,
                    inTune = abs(cents) <= 2.5,
                    isPitchDetected = true,
                    amplitudeRms = 0.18,
                    fftMagnitude = simFft,
                    waveform = simWaveform,
                    appliedGainFactor = if (autoGainControlEnabled) 2.25f else 1.0f,
                    agcEnabled = autoGainControlEnabled,
                    rawInputRms = 0.08
                )

                delay(30)
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        if (_isSimulationMode.value) {
            _isSimulationMode.value = false
            _isRecording.value = false
        }
    }
}
