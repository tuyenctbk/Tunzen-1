package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PitchResult
import com.example.ui.theme.AmberFlat
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.RubySharp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

data class PitchDeviationSample(
    val timeMs: Long,
    val freqDevHz: Float,
    val centsDev: Float,
    val isDetected: Boolean
)

@Composable
fun VocalPitchGraphCanvas(
    pitchResult: PitchResult,
    modifier: Modifier = Modifier
) {
    val windowDurationMs = 10000L // 10-second sliding window
    val sampleHistory = remember { mutableStateListOf<PitchDeviationSample>() }

    LaunchedEffect(pitchResult) {
        val now = System.currentTimeMillis()
        val freqDev = if (pitchResult.isPitchDetected) {
            (pitchResult.frequency - pitchResult.targetFrequency).toFloat()
        } else 0f

        val centsDev = if (pitchResult.isPitchDetected) pitchResult.centsOffset.toFloat() else 0f

        // Prune samples older than 10 seconds
        val cutoff = now - windowDurationMs
        sampleHistory.removeAll { it.timeMs < cutoff }

        sampleHistory.add(
            PitchDeviationSample(
                timeMs = now,
                freqDevHz = freqDev,
                centsDev = centsDev,
                isDetected = pitchResult.isPitchDetected
            )
        )
    }

    val activeSamples = sampleHistory.filter { it.isDetected }
    val avgDevHz = if (activeSamples.isNotEmpty()) {
        activeSamples.map { abs(it.freqDevHz) }.average().toFloat()
    } else 0f

    val stabilityScore = if (activeSamples.isNotEmpty()) {
        val inTuneCount = activeSamples.count { abs(it.centsDev) <= 5.0f }
        ((inTuneCount.toFloat() / activeSamples.size) * 100f).roundToInt()
    } else 100

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("pitch_stability_line_graph"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = "Pitch Stability",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "10s PITCH STABILITY (FREQ DEVIATION)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.1.sp
                    )
                }

                Text(
                    text = if (pitchResult.isPitchDetected) String.format("%+.1f Hz", pitchResult.frequency - pitchResult.targetFrequency) else "0.0 Hz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (pitchResult.inTune) EmeraldInTune else CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stability: $stabilityScore%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stabilityScore > 80) EmeraldInTune else AmberFlat
                )
                Text(
                    text = String.format("Avg Dev: %.2f Hz", avgDevHz),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Window: 10.0s",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val zeroLineColor = EmeraldInTune.copy(alpha = 0.5f)
            val gridLineColor = SurfaceBorder.copy(alpha = 0.7f)
            val lineColor = if (pitchResult.inTune) EmeraldInTune else CyanAccent
            val areaFillColor = lineColor.copy(alpha = 0.15f)

            // Line Graph Canvas (Frequency Deviation over last 10s)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OledBackground)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f
                    val nowMs = System.currentTimeMillis()

                    // 1. Draw horizontal grid lines (Frequency Deviation scale: +10Hz, 0Hz, -10Hz)
                    val scaleMaxHz = 10f // +/- 10 Hz scale bound
                    val upperQuarterY = midY - (height * 0.35f)
                    val lowerQuarterY = midY + (height * 0.35f)

                    // Target 0 Hz deviation center baseline
                    drawLine(
                        color = zeroLineColor,
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // +5 Hz / +25c guideline
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, upperQuarterY),
                        end = Offset(width, upperQuarterY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // -5 Hz / -25c guideline
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, lowerQuarterY),
                        end = Offset(width, lowerQuarterY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 2. Draw vertical time grid lines (-7.5s, -5s, -2.5s)
                    for (sec in listOf(2.5f, 5.0f, 7.5f)) {
                        val gridX = width - (sec / 10f) * width
                        drawLine(
                            color = gridLineColor.copy(alpha = 0.4f),
                            start = Offset(gridX, 0f),
                            end = Offset(gridX, height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 3. Plot Frequency Deviation over time path
                    if (sampleHistory.isNotEmpty()) {
                        val linePath = Path()
                        val areaPath = Path()
                        var hasStarted = false

                        areaPath.moveTo(0f, midY)

                        for (sample in sampleHistory) {
                            val timeDelta = (nowMs - sample.timeMs).coerceIn(0L, windowDurationMs)
                            val x = width - (timeDelta.toFloat() / windowDurationMs.toFloat()) * width

                            // Clamp deviation to [-10Hz, +10Hz] range for rendering
                            val clampedDev = sample.freqDevHz.coerceIn(-scaleMaxHz, scaleMaxHz)
                            val normY = (clampedDev / scaleMaxHz) // -1.0 to +1.0
                            val y = midY - (normY * (height * 0.38f))

                            if (sample.isDetected) {
                                if (!hasStarted) {
                                    linePath.moveTo(x, y)
                                    areaPath.lineTo(x, y)
                                    hasStarted = true
                                } else {
                                    linePath.lineTo(x, y)
                                    areaPath.lineTo(x, y)
                                }
                            } else {
                                if (hasStarted) {
                                    areaPath.lineTo(x, midY)
                                    hasStarted = false
                                }
                            }
                        }

                        if (hasStarted) {
                            areaPath.lineTo(width, midY)
                        }
                        areaPath.close()

                        // Draw area under curve
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(areaFillColor, Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw line curve
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Time X-Axis & Deviation Y-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-10.0s", fontSize = 10.sp, color = TextMuted)
                Text("-7.5s", fontSize = 10.sp, color = TextMuted)
                Text("-5.0s", fontSize = 10.sp, color = TextMuted)
                Text("-2.5s", fontSize = 10.sp, color = TextMuted)
                Text("Now (0s)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            }
        }
    }
}
