package com.example.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PitchDetectorTest {

    private val detector = PitchDetector(sampleRate = 44100, bufferSize = 2048)

    @Test
    fun calculateRms_emptyBuffer_returnsZero() {
        val buffer = ShortArray(2048)
        val rms = detector.calculateRms(buffer, 2048)
        assertEquals(0.0, rms, 0.0001)
    }

    @Test
    fun computeFftSpectrum_validBuffer_returnsTargetBinCount() {
        val buffer = ShortArray(2048) { (Math.sin(2.0 * Math.PI * 440.0 * it / 44100.0) * 10000).toInt().toShort() }
        val spectrum = detector.computeFftSpectrum(buffer, 2048, 64)
        assertNotNull(spectrum)
        assertEquals(64, spectrum.size)
    }
}
