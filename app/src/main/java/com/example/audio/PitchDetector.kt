package com.example.audio

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class PitchDetector(
    private val sampleRate: Int = 44100,
    private val bufferSize: Int = 2048,
    private val threshold: Double = 0.15
) {

    private var lastPitch = 0.0

    /**
     * Cooley-Tukey Decimation-in-Time Radix-2 FFT implementation in-place for power-of-two size N.
     */
    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        if (n <= 1) return

        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val tempR = real[i]
                real[i] = real[j]
                real[j] = tempR
                val tempI = imag[i]
                imag[i] = imag[j]
                imag[j] = tempI
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        // Cooley-Tukey decimation-in-time stages
        var size = 2
        while (size <= n) {
            val halfSize = size shr 1
            val angleStep = -2.0 * Math.PI / size
            for (i in 0 until n step size) {
                for (k in 0 until halfSize) {
                    val angle = k * angleStep
                    val wr = cos(angle)
                    val wi = sin(angle)
                    
                    val pr = real[i + k + halfSize]
                    val pi = imag[i + k + halfSize]
                    
                    val tr = pr * wr - pi * wi
                    val ti = pr * wi + pi * wr
                    
                    real[i + k + halfSize] = real[i + k] - tr
                    imag[i + k + halfSize] = imag[i + k] - ti
                    real[i + k] += tr
                    imag[i + k] += ti
                }
            }
            size = size shl 1
        }
    }

    /**
     * Calculates the RMS amplitude of a short PCM buffer.
     */
    fun calculateRms(buffer: ShortArray, readSize: Int): Double {
        if (readSize <= 0) return 0.0
        var sum = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i] / 32768.0
            sum += sample * sample
        }
        return sqrt(sum / readSize)
    }

    /**
     * Executes the FFT-based frequency estimation algorithm with Harmonic Product Spectrum (HPS)
     * and Parabolic Interpolation. Uses an Exponential Moving Average (EMA) to ensure rock-solid stability.
     */
    fun detectPitch(buffer: ShortArray, readSize: Int, minFreq: Double = 30.0, maxFreq: Double = 2200.0): Double {
        if (readSize < bufferSize) {
            lastPitch = 0.0
            return 0.0
        }

        val real = DoubleArray(bufferSize)
        val imag = DoubleArray(bufferSize)

        // Apply Hanning Window to input PCM
        for (i in 0 until bufferSize) {
            val window = 0.5 * (1.0 - cos(2.0 * Math.PI * i / (bufferSize - 1)))
            real[i] = (buffer[i] / 32768.0) * window
            imag[i] = 0.0
        }

        // Run Cooley-Tukey FFT
        fft(real, imag)

        // Calculate linear magnitude spectrum for positive frequencies
        val halfSize = bufferSize / 2
        val magnitude = DoubleArray(halfSize)
        for (i in 0 until halfSize) {
            magnitude[i] = sqrt(real[i] * real[i] + imag[i] * imag[i])
        }

        // Map search range to FFT bin indices
        val binResolution = sampleRate.toDouble() / bufferSize.toDouble()
        val minBin = (minFreq / binResolution).toInt().coerceAtLeast(2)
        val maxBin = (maxFreq / binResolution).toInt().coerceAtMost(halfSize / 4 - 1)

        val hps = DoubleArray(halfSize)
        var maxHpsVal = -1.0
        var peakBin = -1

        // Harmonic Product Spectrum (HPS) calculation
        for (i in minBin..maxBin) {
            var product = magnitude[i]
            // Multiply fundamental by its harmonics (2nd, 3rd, 4th) to suppress octave errors
            for (h in 2..4) {
                val idx = i * h
                if (idx < halfSize) {
                    product *= magnitude[idx]
                }
            }
            hps[i] = product

            if (product > maxHpsVal) {
                maxHpsVal = product
                peakBin = i
            }
        }

        // Return 0.0 if no dominant peak is found or below noise threshold
        if (peakBin == -1 || maxHpsVal <= 1e-12) {
            lastPitch = 0.0
            return 0.0
        }

        // Find the absolute local maximum in original magnitude spectrum around the HPS estimate
        var refinedBin = peakBin
        var maxMag = magnitude[peakBin]
        val searchRadius = 1
        for (offset in -searchRadius..searchRadius) {
            val idx = peakBin + offset
            if (idx in 0 until halfSize) {
                if (magnitude[idx] > maxMag) {
                    maxMag = magnitude[idx]
                    refinedBin = idx
                }
            }
        }

        // Parabolic Interpolation for ultra-precise sub-bin localization
        val betterBin: Double = if (refinedBin > 0 && refinedBin + 1 < halfSize) {
            val alpha = magnitude[refinedBin - 1]
            val beta = magnitude[refinedBin]
            val gamma = magnitude[refinedBin + 1]

            val denominator = 2.0 * beta - alpha - gamma
            if (abs(denominator) > 1e-6) {
                val offset = 0.5 * (gamma - alpha) / denominator
                refinedBin + offset
            } else {
                refinedBin.toDouble()
            }
        } else {
            refinedBin.toDouble()
        }

        val rawFreq = betterBin * binResolution

        // Sanity frequency range check
        if (rawFreq !in minFreq..maxFreq) {
            lastPitch = 0.0
            return 0.0
        }

        // High-stability EMA filter: smooth jitters on stable tones but adapt instantly on large pitch shifts
        val alphaEMA = 0.35
        val ratio = if (lastPitch > 0.0) rawFreq / lastPitch else 1.0
        val isSameNote = ratio in 0.94..1.06 // Roughly within 1 semitone

        val finalFreq = if (lastPitch == 0.0 || !isSameNote) {
            rawFreq
        } else {
            alphaEMA * rawFreq + (1.0 - alphaEMA) * lastPitch
        }

        lastPitch = finalFreq
        return finalFreq
    }

    /**
     * Computes a 64-bin FFT power spectrum array (0.0 to 1.0) using optimized Cooley-Tukey FFT.
     */
    fun computeFftSpectrum(buffer: ShortArray, readSize: Int, binCount: Int = 64): FloatArray {
        val result = FloatArray(binCount)
        if (readSize <= 0) return result

        val n = if (readSize >= 256) 256 else 128
        val real = DoubleArray(n)
        val imag = DoubleArray(n)

        // Hanning Window
        for (i in 0 until n) {
            val window = 0.5 * (1.0 - cos(2.0 * Math.PI * i / (n - 1)))
            real[i] = (buffer[i] / 32768.0f * window)
            imag[i] = 0.0
        }

        // Perform fast Radix-2 Cooley-Tukey FFT
        fft(real, imag)

        // Map positive frequency spectrum to the target visualizer bins
        val positiveBins = n / 2
        for (k in 0 until binCount) {
            val fromBin = (k * positiveBins / binCount).coerceIn(0, positiveBins - 1)
            val toBin = ((k + 1) * positiveBins / binCount).coerceIn(0, positiveBins - 1)
            
            var maxMag = 0.0
            for (b in fromBin..toBin) {
                val mag = sqrt(real[b] * real[b] + imag[b] * imag[b])
                if (mag > maxMag) maxMag = mag
            }
            
            val db = (20.0 * log10(maxMag + 1e-6) + 60.0) / 60.0
            result[k] = db.coerceIn(0.0, 1.0).toFloat()
        }

        return result
    }
}
