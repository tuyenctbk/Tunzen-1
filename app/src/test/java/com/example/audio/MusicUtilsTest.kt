package com.example.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class MusicUtilsTest {

    @Test
    fun frequencyToMidiNote_A4_returns69() {
        val midi = MusicUtils.frequencyToMidiNote(440.0, 440.0)
        assertEquals(69.0, midi, 0.001)
    }

    @Test
    fun midiNoteToFrequency_69_returns440() {
        val freq = MusicUtils.midiNoteToFrequency(69, 440.0)
        assertEquals(440.0, freq, 0.001)
    }

    @Test
    fun getClosestNoteInfo_A4_returnsCorrectInfo() {
        val (note, octave, cents) = MusicUtils.getClosestNoteInfo(440.0, 440.0)
        assertEquals("A", note)
        assertEquals(4, octave)
        assertEquals(0.0, cents, 0.1)
    }

    @Test
    fun getClosestNoteInfo_E4_returnsCorrectInfo() {
        val (note, octave, cents) = MusicUtils.getClosestNoteInfo(329.63, 440.0)
        assertEquals("E", note)
        assertEquals(4, octave)
        assertEquals(0.0, cents, 1.0)
    }

    @Test
    fun calculateCents_exactSameFreq_returnsZero() {
        val cents = MusicUtils.calculateCents(440.0, 440.0)
        assertEquals(0.0, cents, 0.001)
    }
}
