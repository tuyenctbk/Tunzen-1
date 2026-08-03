package com.example.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class MetronomeEngineTest {

    private val metronomeEngine = MetronomeEngine()

    @Test
    fun setBpm_clampedToValidRange() {
        metronomeEngine.setBpm(20)
        assertEquals(30, metronomeEngine.bpm.value)

        metronomeEngine.setBpm(350)
        assertEquals(300, metronomeEngine.bpm.value)

        metronomeEngine.setBpm(140)
        assertEquals(140, metronomeEngine.bpm.value)
    }
}
