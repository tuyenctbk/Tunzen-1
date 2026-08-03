package com.example.model

data class TuningString(
    val name: String,         // e.g. "1st - E4"
    val noteName: String,     // e.g. "E"
    val octave: Int,          // e.g. 4
    val targetFrequency: Double, // e.g. 329.63
    val stringIndex: Int      // 1-based index (e.g. 1, 2, 3...)
)

data class InstrumentPreset(
    val id: String,
    val name: String,
    val category: String,     // "Ukulele", "Guitar", "Violin", "Kalimba", "Oud", "Custom"
    val strings: List<TuningString>,
    val isCustom: Boolean = false
)

data class PitchResult(
    val frequency: Double = 0.0,
    val noteName: String = "-",
    val octave: Int = 0,
    val centsOffset: Double = 0.0, // -50.0 to +50.0
    val targetNote: String = "-",
    val targetFrequency: Double = 0.0,
    val matchedString: TuningString? = null,
    val inTune: Boolean = false,
    val isPitchDetected: Boolean = false,
    val amplitudeRms: Double = 0.0,
    val fftMagnitude: FloatArray = FloatArray(64),
    val activeThreshold: Double = 0.015,
    val waveform: FloatArray = FloatArray(64),
    val appliedGainFactor: Float = 1.0f,
    val agcEnabled: Boolean = true,
    val rawInputRms: Double = 0.0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PitchResult

        if (frequency != other.frequency) return false
        if (noteName != other.noteName) return false
        if (octave != other.octave) return false
        if (centsOffset != other.centsOffset) return false
        if (inTune != other.inTune) return false
        if (isPitchDetected != other.isPitchDetected) return false
        if (activeThreshold != other.activeThreshold) return false
        if (appliedGainFactor != other.appliedGainFactor) return false
        if (agcEnabled != other.agcEnabled) return false
        if (rawInputRms != other.rawInputRms) return false
        if (!fftMagnitude.contentEquals(other.fftMagnitude)) return false
        if (!waveform.contentEquals(other.waveform)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = frequency.hashCode()
        result = 31 * result + noteName.hashCode()
        result = 31 * result + octave
        result = 31 * result + centsOffset.hashCode()
        result = 31 * result + inTune.hashCode()
        result = 31 * result + isPitchDetected.hashCode()
        result = 31 * result + activeThreshold.hashCode()
        result = 31 * result + appliedGainFactor.hashCode()
        result = 31 * result + agcEnabled.hashCode()
        result = 31 * result + rawInputRms.hashCode()
        result = 31 * result + fftMagnitude.contentHashCode()
        result = 31 * result + waveform.contentHashCode()
        return result
    }
}
