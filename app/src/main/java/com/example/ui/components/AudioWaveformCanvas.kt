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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun AudioWaveformCanvas(
    waveform: FloatArray,
    amplitudeRms: Double,
    isPitchDetected: Boolean,
    inTune: Boolean,
    appliedGainFactor: Float = 1.0f,
    agcEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeColor = when {
        inTune -> EmeraldInTune
        isPitchDetected -> CyanAccent
        else -> TextSecondary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("audio_waveform_canvas"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
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
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Waveform",
                    tint = activeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "REAL-TIME SIGNAL WAVEFORM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (agcEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("AGC %.1fx", appliedGainFactor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent
                        )
                    }
                }

                Text(
                    text = String.format("RMS: %.3f", amplitudeRms),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = activeColor
                )
            }
        }

        // Canvas Waveform Scope
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCardVariant)
                .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            val centerZeroColor = TextMuted.copy(alpha = 0.25f)
            val strokeColor = activeColor
            val fillColor = activeColor.copy(alpha = 0.15f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f

                // Zero-Crossing Baseline
                drawLine(
                    color = centerZeroColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                // Reference bounds (+/- 50% max amplitude)
                val boundOffset = height * 0.35f
                drawLine(
                    color = centerZeroColor.copy(alpha = 0.12f),
                    start = Offset(0f, centerY - boundOffset),
                    end = Offset(width, centerY - boundOffset),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = centerZeroColor.copy(alpha = 0.12f),
                    start = Offset(0f, centerY + boundOffset),
                    end = Offset(width, centerY + boundOffset),
                    strokeWidth = 1.dp.toPx()
                )

                if (waveform.isNotEmpty()) {
                    val count = waveform.size
                    val stepX = width / (count - 1).coerceAtLeast(1)

                    val path = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, centerY)

                    for (i in 0 until count) {
                        val sample = waveform[i]
                        val x = i * stepX
                        // Scale amplitude smoothly for canvas height
                        val y = centerY - (sample * (height * 0.42f))

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevSample = waveform[i - 1]
                            val prevY = centerY - (prevSample * (height * 0.42f))
                            val controlX = (prevX + x) / 2f

                            path.quadraticTo(controlX, prevY, x, y)
                            fillPath.quadraticTo(controlX, prevY, x, y)
                        }
                    }

                    fillPath.lineTo(width, centerY)
                    fillPath.close()

                    // Draw glowing fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(fillColor, fillColor.copy(alpha = 0.02f)),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw dynamic waveform path stroke
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
