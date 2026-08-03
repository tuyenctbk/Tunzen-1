package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted

@Composable
fun AudioFFTVisualizer(
    fftMagnitude: FloatArray,
    isPitchDetected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("audio_fft_visualizer_box")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME SPECTRUM ANALYZER (FFT)",
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            val barTopColor = if (isPitchDetected) EmeraldInTune else CyanAccent
            val barBottomColor = SurfaceCardVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            ) {
                val width = size.width
                val height = size.height
                val binCount = fftMagnitude.size.coerceAtLeast(1)
                val barGap = 2.dp.toPx()
                val totalGap = barGap * (binCount - 1)
                val barWidth = ((width - totalGap) / binCount).coerceAtLeast(1f)

                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        barTopColor,
                        barBottomColor
                    )
                )

                for (i in 0 until binCount) {
                    val mag = fftMagnitude[i].coerceIn(0f, 1f)
                    val barHeight = (mag * height).coerceAtLeast(2.dp.toPx())

                    val x = i * (barWidth + barGap)
                    val y = height - barHeight

                    drawRoundRect(
                        brush = gradientBrush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }
    }
}
