package com.example.audio

import com.example.model.InstrumentPreset
import com.example.model.TuningString
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

object MusicUtils {

    val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun frequencyToMidiNote(freq: Double, referenceA4: Double = 440.0): Double {
        if (freq <= 0) return 0.0
        return 12.0 * (ln(freq / referenceA4) / ln(2.0)) + 69.0
    }

    fun midiNoteToFrequency(midiNote: Int, referenceA4: Double = 440.0): Double {
        return referenceA4 * 2.0.pow((midiNote - 69) / 12.0)
    }

    fun calculateCents(freq: Double, targetFreq: Double): Double {
        if (freq <= 0 || targetFreq <= 0) return 0.0
        return 1200.0 * (ln(freq / targetFreq) / ln(2.0))
    }

    val TEMPERAMENT_OFFSETS = mapOf(
        "STANDARD" to DoubleArray(12) { 0.0 },
        "CHROMATIC" to DoubleArray(12) { 0.0 },
        "JUST" to doubleArrayOf(0.0, -11.7, 3.9, 15.6, -13.7, -2.0, 14.2, 2.0, 13.7, -15.6, 17.6, -11.7),
        "PYTHAGOREAN" to doubleArrayOf(0.0, 9.8, 3.9, 13.7, 7.8, -2.0, 11.7, 2.0, 11.7, 5.9, 15.6, 9.8),
        "MEANTONE" to doubleArrayOf(0.0, -24.1, -6.8, -30.9, -13.7, -3.4, -27.5, -10.3, -34.4, -17.1, -20.6, -24.1)
    )

    fun getClosestNoteInfo(freq: Double, referenceA4: Double = 440.0, temperament: String = "STANDARD"): Triple<String, Int, Double> {
        if (freq < 15.0 || freq > 8000.0) return Triple("-", 0, 0.0)

        val midiDouble = frequencyToMidiNote(freq, referenceA4)
        val midiRounded = midiDouble.roundToInt()

        val noteIndex = ((midiRounded % 12) + 12) % 12
        val octave = (midiRounded / 12) - 1
        val baseTargetFreq = midiNoteToFrequency(midiRounded, referenceA4)
        
        val offsets = TEMPERAMENT_OFFSETS[temperament] ?: TEMPERAMENT_OFFSETS["STANDARD"]!!
        val temperamentOffsetCents = offsets[noteIndex]
        val targetFreq = baseTargetFreq * 2.0.pow(temperamentOffsetCents / 1200.0)
        
        val cents = calculateCents(freq, targetFreq)

        return Triple(NOTE_NAMES[noteIndex], octave, cents)
    }

    // Standard Preset Library
    val CHROMATIC_PRESET = InstrumentPreset(
        id = "chromatic",
        name = "Auto Chromatic",
        category = "General",
        strings = emptyList()
    )

    val UKULELE_STANDARD = InstrumentPreset(
        id = "ukulele_std",
        name = "Ukulele Standard (G4 C4 E4 A4)",
        category = "Ukulele",
        strings = listOf(
            createString("4th", "G", 4, 440.0, 4),
            createString("3rd", "C", 4, 440.0, 3),
            createString("2nd", "E", 4, 440.0, 2),
            createString("1st", "A", 4, 440.0, 1)
        )
    )

    val UKULELE_LOW_G = InstrumentPreset(
        id = "ukulele_low_g",
        name = "Ukulele Low-G (G3 C4 E4 A4)",
        category = "Ukulele",
        strings = listOf(
            createString("4th", "G", 3, 440.0, 4),
            createString("3rd", "C", 4, 440.0, 3),
            createString("2nd", "E", 4, 440.0, 2),
            createString("1st", "A", 4, 440.0, 1)
        )
    )

    val GUITAR_STANDARD = InstrumentPreset(
        id = "guitar_std",
        name = "Guitar Standard (E2 A2 D3 G3 B3 E4)",
        category = "Guitar",
        strings = listOf(
            createString("6th", "E", 2, 440.0, 6),
            createString("5th", "A", 2, 440.0, 5),
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "B", 3, 440.0, 2),
            createString("1st", "E", 4, 440.0, 1)
        )
    )

    val GUITAR_DROP_D = InstrumentPreset(
        id = "guitar_drop_d",
        name = "Guitar Drop D (D2 A2 D3 G3 B3 E4)",
        category = "Guitar",
        strings = listOf(
            createString("6th", "D", 2, 440.0, 6),
            createString("5th", "A", 2, 440.0, 5),
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "B", 3, 440.0, 2),
            createString("1st", "E", 4, 440.0, 1)
        )
    )

    val GUITAR_DADGAD = InstrumentPreset(
        id = "guitar_dadgad",
        name = "Guitar DADGAD",
        category = "Guitar",
        strings = listOf(
            createString("6th", "D", 2, 440.0, 6),
            createString("5th", "A", 2, 440.0, 5),
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "A", 3, 440.0, 2),
            createString("1st", "D", 4, 440.0, 1)
        )
    )

    val VIOLIN_STANDARD = InstrumentPreset(
        id = "violin_std",
        name = "Violin Standard (G3 D4 A4 E5)",
        category = "Violin",
        strings = listOf(
            createString("4th", "G", 3, 440.0, 4),
            createString("3rd", "D", 4, 440.0, 3),
            createString("2nd", "A", 4, 440.0, 2),
            createString("1st", "E", 5, 440.0, 1)
        )
    )

    val KALIMBA_17KEY = InstrumentPreset(
        id = "kalimba_17",
        name = "Kalimba 17-Key C Major",
        category = "Kalimba",
        strings = listOf(
            createString("D6", "D", 6, 440.0, 1),
            createString("B5", "B", 5, 440.0, 2),
            createString("G5", "G", 5, 440.0, 3),
            createString("E5", "E", 5, 440.0, 4),
            createString("C5", "C", 5, 440.0, 5),
            createString("A4", "A", 4, 440.0, 6),
            createString("F4", "F", 4, 440.0, 7),
            createString("D4", "D", 4, 440.0, 8),
            createString("C4", "C", 4, 440.0, 9),
            createString("E4", "E", 4, 440.0, 10),
            createString("G4", "G", 4, 440.0, 11),
            createString("B4", "B", 4, 440.0, 12),
            createString("D5", "D", 5, 440.0, 13),
            createString("F5", "F", 5, 440.0, 14),
            createString("A5", "A", 5, 440.0, 15),
            createString("C6", "C", 6, 440.0, 16),
            createString("E6", "E", 6, 440.0, 17)
        )
    )

    val OUD_ARABIC = InstrumentPreset(
        id = "oud_arabic",
        name = "Oud Arabic Standard (C2 F2 A2 D3 G3 C4)",
        category = "Oud & World",
        strings = listOf(
            createString("6th", "C", 2, 440.0, 6),
            createString("5th", "F", 2, 440.0, 5),
            createString("4th", "A", 2, 440.0, 4),
            createString("3rd", "D", 3, 440.0, 3),
            createString("2nd", "G", 3, 440.0, 2),
            createString("1st", "C", 4, 440.0, 1)
        )
    )

    val VOCAL_SOPRANO = InstrumentPreset(
        id = "vocal_soprano",
        name = "Soprano Vocal Range (C4 - A5)",
        category = "Vocal Trainer",
        strings = listOf(
            createString("Low C4", "C", 4, 440.0, 1),
            createString("Mid E4", "E", 4, 440.0, 2),
            createString("Mid G4", "G", 4, 440.0, 3),
            createString("High C5", "C", 5, 440.0, 4),
            createString("High E5", "E", 5, 440.0, 5),
            createString("High A5", "A", 5, 440.0, 6)
        )
    )

    val VOCAL_ALTO = InstrumentPreset(
        id = "vocal_alto",
        name = "Alto Vocal Range (G3 - F5)",
        category = "Vocal Trainer",
        strings = listOf(
            createString("Low G3", "G", 3, 440.0, 1),
            createString("Low C4", "C", 4, 440.0, 2),
            createString("Mid E4", "E", 4, 440.0, 3),
            createString("Mid G4", "G", 4, 440.0, 4),
            createString("High C5", "C", 5, 440.0, 5),
            createString("High F5", "F", 5, 440.0, 6)
        )
    )

    val VOCAL_TENOR = InstrumentPreset(
        id = "vocal_tenor",
        name = "Tenor Vocal Range (C3 - A4)",
        category = "Vocal Trainer",
        strings = listOf(
            createString("Low C3", "C", 3, 440.0, 1),
            createString("Low E3", "E", 3, 440.0, 2),
            createString("Mid G3", "G", 3, 440.0, 3),
            createString("Mid C4", "C", 4, 440.0, 4),
            createString("Mid E4", "E", 4, 440.0, 5),
            createString("High A4", "A", 4, 440.0, 6)
        )
    )

    val VOCAL_BASS = InstrumentPreset(
        id = "vocal_bass",
        name = "Bass Vocal Range (E2 - E4)",
        category = "Vocal Trainer",
        strings = listOf(
            createString("Low E2", "E", 2, 440.0, 1),
            createString("Low A2", "A", 2, 440.0, 2),
            createString("Mid D3", "D", 3, 440.0, 3),
            createString("Mid G3", "G", 3, 440.0, 4),
            createString("Mid C4", "C", 4, 440.0, 5),
            createString("High E4", "E", 4, 440.0, 6)
        )
    )

    val GUITAR_HALF_STEP_DOWN = InstrumentPreset(
        id = "guitar_half_step_down",
        name = "Guitar Eb Half Step Down",
        category = "Guitar",
        strings = listOf(
            createString("6th", "D#", 2, 440.0, 6),
            createString("5th", "G#", 2, 440.0, 5),
            createString("4th", "C#", 3, 440.0, 4),
            createString("3rd", "F#", 3, 440.0, 3),
            createString("2nd", "A#", 3, 440.0, 2),
            createString("1st", "D#", 4, 440.0, 1)
        )
    )

    val GUITAR_FULL_STEP_DOWN = InstrumentPreset(
        id = "guitar_full_step_down",
        name = "Guitar D Full Step Down",
        category = "Guitar",
        strings = listOf(
            createString("6th", "D", 2, 440.0, 6),
            createString("5th", "G", 2, 440.0, 5),
            createString("4th", "C", 3, 440.0, 4),
            createString("3rd", "F", 3, 440.0, 3),
            createString("2nd", "A", 3, 440.0, 2),
            createString("1st", "D", 4, 440.0, 1)
        )
    )

    val GUITAR_DROP_C = InstrumentPreset(
        id = "guitar_drop_c",
        name = "Guitar Drop C",
        category = "Guitar",
        strings = listOf(
            createString("6th", "C", 2, 440.0, 6),
            createString("5th", "G", 2, 440.0, 5),
            createString("4th", "C", 3, 440.0, 4),
            createString("3rd", "F", 3, 440.0, 3),
            createString("2nd", "A", 3, 440.0, 2),
            createString("1st", "D", 4, 440.0, 1)
        )
    )

    val GUITAR_OPEN_D = InstrumentPreset(
        id = "guitar_open_d",
        name = "Guitar Open D",
        category = "Guitar",
        strings = listOf(
            createString("6th", "D", 2, 440.0, 6),
            createString("5th", "A", 2, 440.0, 5),
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "F#", 3, 440.0, 3),
            createString("2nd", "A", 3, 440.0, 2),
            createString("1st", "D", 4, 440.0, 1)
        )
    )

    val BASS_4STRING = InstrumentPreset(
        id = "bass_4string",
        name = "Bass 4-String Standard (E1 A1 D2 G2)",
        category = "Bass",
        strings = listOf(
            createString("4th", "E", 1, 440.0, 4),
            createString("3rd", "A", 1, 440.0, 3),
            createString("2nd", "D", 2, 440.0, 2),
            createString("1st", "G", 2, 440.0, 1)
        )
    )

    val BASS_5STRING = InstrumentPreset(
        id = "bass_5string",
        name = "Bass 5-String Standard (B0 E1 A1 D2 G2)",
        category = "Bass",
        strings = listOf(
            createString("5th", "B", 0, 440.0, 5),
            createString("4th", "E", 1, 440.0, 4),
            createString("3rd", "A", 1, 440.0, 3),
            createString("2nd", "D", 2, 440.0, 2),
            createString("1st", "G", 2, 440.0, 1)
        )
    )

    val UKULELE_BARITONE = InstrumentPreset(
        id = "ukulele_baritone",
        name = "Ukulele Baritone (D3 G3 B3 E4)",
        category = "Ukulele",
        strings = listOf(
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "B", 3, 440.0, 2),
            createString("1st", "E", 4, 440.0, 1)
        )
    )

    val VIOLA_STANDARD = InstrumentPreset(
        id = "viola_std",
        name = "Viola Standard (C3 G3 D4 A4)",
        category = "Orchestral Strings",
        strings = listOf(
            createString("4th", "C", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "D", 4, 440.0, 2),
            createString("1st", "A", 4, 440.0, 1)
        )
    )

    val CELLO_STANDARD = InstrumentPreset(
        id = "cello_std",
        name = "Cello Standard (C2 G2 D3 A3)",
        category = "Orchestral Strings",
        strings = listOf(
            createString("4th", "C", 2, 440.0, 4),
            createString("3rd", "G", 2, 440.0, 3),
            createString("2nd", "D", 3, 440.0, 2),
            createString("1st", "A", 3, 440.0, 1)
        )
    )

    val MANDOLIN_STANDARD = InstrumentPreset(
        id = "mandolin_std",
        name = "Mandolin Standard (G3 D4 A4 E5)",
        category = "Folk & World",
        strings = listOf(
            createString("4th", "G", 3, 440.0, 4),
            createString("3rd", "D", 4, 440.0, 3),
            createString("2nd", "A", 4, 440.0, 2),
            createString("1st", "E", 5, 440.0, 1)
        )
    )

    val BANJO_5STRING = InstrumentPreset(
        id = "banjo_5string",
        name = "Banjo 5-String Open G (g4 D3 G3 B3 D4)",
        category = "Folk & World",
        strings = listOf(
            createString("5th", "G", 4, 440.0, 5),
            createString("4th", "D", 3, 440.0, 4),
            createString("3rd", "G", 3, 440.0, 3),
            createString("2nd", "B", 3, 440.0, 2),
            createString("1st", "D", 4, 440.0, 1)
        )
    )

    val DEFAULT_PRESETS = listOf(
        CHROMATIC_PRESET,
        UKULELE_STANDARD,
        UKULELE_LOW_G,
        UKULELE_BARITONE,
        GUITAR_STANDARD,
        GUITAR_DROP_D,
        GUITAR_HALF_STEP_DOWN,
        GUITAR_FULL_STEP_DOWN,
        GUITAR_DROP_C,
        GUITAR_OPEN_D,
        GUITAR_DADGAD,
        BASS_4STRING,
        BASS_5STRING,
        VIOLIN_STANDARD,
        VIOLA_STANDARD,
        CELLO_STANDARD,
        MANDOLIN_STANDARD,
        BANJO_5STRING,
        KALIMBA_17KEY,
        OUD_ARABIC,
        VOCAL_SOPRANO,
        VOCAL_ALTO,
        VOCAL_TENOR,
        VOCAL_BASS
    )

    fun createString(label: String, note: String, octave: Int, referenceA4: Double = 440.0, index: Int): TuningString {
        val noteIdx = NOTE_NAMES.indexOf(note)
        val midi = (octave + 1) * 12 + if (noteIdx >= 0) noteIdx else 0
        val freq = midiNoteToFrequency(midi, referenceA4)
        return TuningString(
            name = "$label - $note$octave",
            noteName = note,
            octave = octave,
            targetFrequency = freq,
            stringIndex = index
        )
    }

    // Recalculates frequencies for a preset based on reference A4 (e.g. 432Hz)
    fun recalculatePresetFrequencies(preset: InstrumentPreset, referenceA4: Double): InstrumentPreset {
        if (preset.id == "chromatic") return preset
        val updatedStrings = preset.strings.map { str ->
            val noteIdx = NOTE_NAMES.indexOf(str.noteName)
            val midi = (str.octave + 1) * 12 + if (noteIdx >= 0) noteIdx else 0
            val newFreq = midiNoteToFrequency(midi, referenceA4)
            str.copy(targetFrequency = newFreq)
        }
        return preset.copy(strings = updatedStrings)
    }
}
