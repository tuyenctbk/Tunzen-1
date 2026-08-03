package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRecordEngine
import com.example.audio.MetronomeEngine
import com.example.audio.MusicUtils
import com.example.audio.ToneGeneratorEngine
import com.example.data.AppDatabase
import com.example.data.TuningHistoryEntity
import com.example.data.TuningRepository
import com.example.model.CalibrationPreset
import com.example.model.InstrumentPreset
import com.example.model.PitchResult
import com.example.model.TuningString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TuningRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        TuningRepository(db.tuningDao())
    }

    private val prefs = application.getSharedPreferences("tunezen_prefs", Context.MODE_PRIVATE)
    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("completed_onboarding", false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _isTourActive = MutableStateFlow(false)
    val isTourActive: StateFlow<Boolean> = _isTourActive.asStateFlow()

    private val _tourStep = MutableStateFlow(0)
    val tourStep: StateFlow<Int> = _tourStep.asStateFlow()

    private val _hasSeenTour = MutableStateFlow(prefs.getBoolean("has_seen_interactive_tour", false))
    val hasSeenTour: StateFlow<Boolean> = _hasSeenTour.asStateFlow()

    val audioRecordEngine = AudioRecordEngine()
    val toneGeneratorEngine = ToneGeneratorEngine()
    val metronomeEngine = MetronomeEngine()

    val pitchState: StateFlow<PitchResult> = audioRecordEngine.pitchState
    val isRecording: StateFlow<Boolean> = audioRecordEngine.isRecording
    val isSimulationMode: StateFlow<Boolean> = audioRecordEngine.isSimulationMode

    private val _tunerDisplayMode = MutableStateFlow("NEEDLE") // "NEEDLE", "STROBE", "VOCAL_GRAPH"
    val tunerDisplayMode: StateFlow<String> = _tunerDisplayMode.asStateFlow()

    fun setTunerDisplayMode(mode: String) {
        _tunerDisplayMode.value = mode
    }

    private val _selectedPreset = MutableStateFlow<InstrumentPreset>(MusicUtils.UKULELE_STANDARD)
    val selectedPreset: StateFlow<InstrumentPreset> = _selectedPreset.asStateFlow()

    private val _selectedString = MutableStateFlow<TuningString?>(null)
    val selectedString: StateFlow<TuningString?> = _selectedString.asStateFlow()

    private val _referenceA4 = MutableStateFlow(prefs.getFloat("reference_a4", 440.0f).toDouble())
    val referenceA4: StateFlow<Double> = _referenceA4.asStateFlow()

    private val _selectedTemperament = MutableStateFlow(prefs.getString("selected_temperament", "STANDARD") ?: "STANDARD")
    val selectedTemperament: StateFlow<String> = _selectedTemperament.asStateFlow()

    private val _customCalibrationPresets = MutableStateFlow<List<CalibrationPreset>>(emptyList())
    val customCalibrationPresets: StateFlow<List<CalibrationPreset>> = _customCalibrationPresets.asStateFlow()

    val allCalibrationPresets: StateFlow<List<CalibrationPreset>> = combine(
        _customCalibrationPresets
    ) { customs ->
        DEFAULT_CALIBRATION_PRESETS + customs.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_CALIBRATION_PRESETS)

    private val _noiseGateThreshold = MutableStateFlow(prefs.getFloat("noise_gate_threshold", 0.015f).toDouble())
    val noiseGateThreshold: StateFlow<Double> = _noiseGateThreshold.asStateFlow()

    private val _dynamicSensitivityEnabled = MutableStateFlow(prefs.getBoolean("dynamic_sensitivity", true))
    val dynamicSensitivityEnabled: StateFlow<Boolean> = _dynamicSensitivityEnabled.asStateFlow()

    private val _autoGainControlEnabled = MutableStateFlow(prefs.getBoolean("auto_gain_control", true))
    val autoGainControlEnabled: StateFlow<Boolean> = _autoGainControlEnabled.asStateFlow()

    private val _waveType = MutableStateFlow("SINE")
    val waveType: StateFlow<String> = _waveType.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _isPaperLightMode = MutableStateFlow(prefs.getBoolean("paper_light_mode", false))
    val isPaperLightMode: StateFlow<Boolean> = _isPaperLightMode.asStateFlow()

    val customPresets: StateFlow<List<InstrumentPreset>> = repository.customPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tuningHistory: StateFlow<List<TuningHistoryEntity>> = repository.historyLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<InstrumentPreset>> = combine(
        customPresets,
        referenceA4
    ) { customList, a4 ->
        val defaults = MusicUtils.DEFAULT_PRESETS.map { MusicUtils.recalculatePresetFrequencies(it, a4) }
        val updatedCustoms = customList.map { MusicUtils.recalculatePresetFrequencies(it, a4) }
        defaults + updatedCustoms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicUtils.DEFAULT_PRESETS)

    private var lastLoggedNote: String = ""
    private var lastLogTimestamp: Long = 0

    init {
        // Initialize engine parameters
        audioRecordEngine.referenceA4 = _referenceA4.value
        audioRecordEngine.selectedTemperament = _selectedTemperament.value
        audioRecordEngine.noiseThresholdRms = _noiseGateThreshold.value
        audioRecordEngine.dynamicSensitivityEnabled = _dynamicSensitivityEnabled.value
        audioRecordEngine.autoGainControlEnabled = _autoGainControlEnabled.value

        loadCustomCalibrationPresets()

        // Observe pitch state and auto log history when locked in-tune
        viewModelScope.launch {
            pitchState.collect { res ->
                if (res.isPitchDetected && res.inTune) {
                    val now = System.currentTimeMillis()
                    if (res.noteName != lastLoggedNote || (now - lastLogTimestamp) > 4000) {
                        lastLoggedNote = res.noteName
                        lastLogTimestamp = now
                        repository.logTuningSession(
                            instrumentName = _selectedPreset.value.name,
                            targetNote = "${res.targetNote}${res.octave}",
                            detectedFrequency = res.frequency,
                            centsOffset = res.centsOffset,
                            inTune = true
                        )
                    }
                }
            }
        }
    }

    fun selectPreset(preset: InstrumentPreset) {
        val updated = MusicUtils.recalculatePresetFrequencies(preset, _referenceA4.value)
        _selectedPreset.value = updated
        audioRecordEngine.currentPreset = updated
        _selectedString.value = null
        audioRecordEngine.selectedString = null
    }

    fun selectString(string: TuningString?) {
        _selectedString.value = string
        audioRecordEngine.selectedString = string
    }

    fun setReferenceA4(freq: Double) {
        val clampedFreq = (kotlin.math.round(freq * 10.0) / 10.0).coerceIn(415.0, 466.0)
        _referenceA4.value = clampedFreq
        audioRecordEngine.referenceA4 = clampedFreq
        prefs.edit().putFloat("reference_a4", clampedFreq.toFloat()).apply()
        val updatedPreset = MusicUtils.recalculatePresetFrequencies(_selectedPreset.value, clampedFreq)
        _selectedPreset.value = updatedPreset
        audioRecordEngine.currentPreset = updatedPreset
    }

    fun setTemperament(temperament: String) {
        _selectedTemperament.value = temperament
        audioRecordEngine.selectedTemperament = temperament
        prefs.edit().putString("selected_temperament", temperament).apply()
    }

    fun applyCalibrationPreset(preset: CalibrationPreset) {
        setReferenceA4(preset.referenceA4)
        setTemperament(preset.temperament)
    }

    fun saveCustomCalibrationPreset(name: String) {
        val trimmedName = if (name.isBlank()) "Calib ${referenceA4.value.toInt()}Hz" else name.trim()
        val newPreset = CalibrationPreset(
            id = "calib_${System.currentTimeMillis()}",
            name = trimmedName,
            referenceA4 = _referenceA4.value,
            temperament = _selectedTemperament.value,
            isCustom = true
        )
        val updatedList = _customCalibrationPresets.value + newPreset
        _customCalibrationPresets.value = updatedList
        saveCustomCalibrationPresetsToPrefs(updatedList)
    }

    fun deleteCustomCalibrationPreset(id: String) {
        val updatedList = _customCalibrationPresets.value.filter { it.id != id }
        _customCalibrationPresets.value = updatedList
        saveCustomCalibrationPresetsToPrefs(updatedList)
    }

    private fun loadCustomCalibrationPresets() {
        val jsonStr = prefs.getString("custom_calibration_presets", "[]") ?: "[]"
        val list = mutableListOf<CalibrationPreset>()
        try {
            val array = org.json.JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalibrationPreset(
                        id = obj.optString("id", "preset_$i"),
                        name = obj.optString("name", "Custom Calibration"),
                        referenceA4 = obj.optDouble("referenceA4", 440.0),
                        temperament = obj.optString("temperament", "STANDARD"),
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _customCalibrationPresets.value = list
    }

    private fun saveCustomCalibrationPresetsToPrefs(list: List<CalibrationPreset>) {
        try {
            val array = org.json.JSONArray()
            for (p in list) {
                val obj = org.json.JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("referenceA4", p.referenceA4)
                obj.put("temperament", p.temperament)
                array.put(obj)
            }
            prefs.edit().putString("custom_calibration_presets", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val DEFAULT_CALIBRATION_PRESETS = listOf(
            CalibrationPreset("default_440_std", "Concert Standard", 440.0, "STANDARD", false),
            CalibrationPreset("verdi_432_just", "Verdi Tuning", 432.0, "JUST", false),
            CalibrationPreset("baroque_415_pyth", "Baroque Pitch", 415.0, "PYTHAGOREAN", false),
            CalibrationPreset("french_435_meantone", "French Meantone", 435.0, "MEANTONE", false),
            CalibrationPreset("chamber_444_chrom", "Chamber Chromatic", 444.0, "CHROMATIC", false)
        )
    }

    fun setNoiseGateThreshold(value: Double) {
        _noiseGateThreshold.value = value
        prefs.edit().putFloat("noise_gate_threshold", value.toFloat()).apply()
        audioRecordEngine.noiseThresholdRms = value
    }

    fun setDynamicSensitivityEnabled(enabled: Boolean) {
        _dynamicSensitivityEnabled.value = enabled
        prefs.edit().putBoolean("dynamic_sensitivity", enabled).apply()
        audioRecordEngine.dynamicSensitivityEnabled = enabled
    }

    fun setAutoGainControlEnabled(enabled: Boolean) {
        _autoGainControlEnabled.value = enabled
        prefs.edit().putBoolean("auto_gain_control", enabled).apply()
        audioRecordEngine.autoGainControlEnabled = enabled
    }

    fun setWaveType(type: String) {
        _waveType.value = type
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }

    fun setPaperLightMode(enabled: Boolean) {
        _isPaperLightMode.value = enabled
        prefs.edit().putBoolean("paper_light_mode", enabled).apply()
    }

    fun playReferenceTone(string: TuningString) {
        toneGeneratorEngine.playTone(
            frequencyHz = string.targetFrequency,
            waveType = _waveType.value,
            durationMs = 2500
        )
    }

    fun stopReferenceTone() {
        toneGeneratorEngine.stopTone()
    }

    fun startListening() {
        audioRecordEngine.startRecording()
    }

    fun stopListening() {
        audioRecordEngine.stopRecording()
    }

    fun toggleSimulation() {
        if (audioRecordEngine.isSimulationMode.value) {
            audioRecordEngine.stopSimulation()
        } else {
            audioRecordEngine.startSimulation()
        }
    }

    fun createCustomPreset(name: String, category: String, strings: List<TuningString>) {
        viewModelScope.launch {
            val preset = InstrumentPreset(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                category = if (category.isBlank()) "Custom" else category,
                strings = strings,
                isCustom = true
            )
            repository.saveCustomPreset(preset)
            selectPreset(preset)
        }
    }

    fun deleteCustomPreset(id: String) {
        viewModelScope.launch {
            repository.deleteCustomPreset(id)
            if (_selectedPreset.value.id == id) {
                selectPreset(MusicUtils.UKULELE_STANDARD)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun startTour() {
        _tourStep.value = 0
        _isTourActive.value = true
    }

    fun nextTourStep() {
        if (_tourStep.value < 2) {
            _tourStep.value += 1
        } else {
            completeTour()
        }
    }

    fun previousTourStep() {
        if (_tourStep.value > 0) {
            _tourStep.value -= 1
        }
    }

    fun completeTour() {
        _isTourActive.value = false
        prefs.edit().putBoolean("has_seen_interactive_tour", true).apply()
        _hasSeenTour.value = true
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("completed_onboarding", true).apply()
        _hasCompletedOnboarding.value = true
        if (!_hasSeenTour.value) {
            startTour()
        }
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("completed_onboarding", false).apply()
        prefs.edit().putBoolean("has_seen_interactive_tour", false).apply()
        _hasCompletedOnboarding.value = false
        _hasSeenTour.value = false
    }

    override fun onCleared() {
        super.onCleared()
        audioRecordEngine.stopRecording()
        audioRecordEngine.stopSimulation()
        toneGeneratorEngine.stopTone()
        metronomeEngine.stop()
    }
}
