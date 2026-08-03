package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StrobeWheelVisualizer(
    pitchResult: PitchResult,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val activeColor = when {
        !pitchResult.isPitchDetected -> TextMuted
        pitchResult.inTune -> EmeraldInTune
        pitchResult.centsOffset < 0 -> AmberFlat
        else -> RubySharp
    }

    val animatedColor by animateColorAsState(
        targetValue = activeColor,
        animationSpec = tween(durationMillis = 150),
        label = "StrobeColor"
    )

    // Continuously animate rotation speed proportional to cents error
    LaunchedEffect(pitchResult.isPitchDetected, pitchResult.centsOffset) {
        while (true) {
            if (pitchResult.isPitchDetected && !pitchResult.inTune) {
                // Rotation velocity based on cents drift
                val velocity = (pitchResult.centsOffset / 10.0).toFloat()
                rotationAngle = (rotationAngle + velocity) % 360f
            }
            delay(16) // ~60fps smooth rotation frame rate
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("strobe_visualizer_box"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIRTUAL STROBE TUNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = if (pitchResult.inTune) "LOCKED 0.0c" else String.format("%+.1fc", pitchResult.centsOffset),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = animatedColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(OledBackground)
                    .border(2.dp, animatedColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                val cardBgColor = SurfaceCard
                Canvas(modifier = Modifier.size(190.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width / 2f - 4.dp.toPx()

                    rotate(rotationAngle, pivot = center) {
                        // Draw concentric strobe pattern bands
                        val bandCounts = listOf(12, 18, 24)
                        val radii = listOf(outerRadius * 0.85f, outerRadius * 0.65f, outerRadius * 0.45f)

                        bandCounts.forEachIndexed { levelIndex, count ->
                            val r = radii[levelIndex]
                            val arcAngle = 360f / count
                            for (i in 0 until count) {
                                if (i % 2 == 0) {
                                    val startAngle = i * arcAngle
                                    drawArc(
                                        color = animatedColor,
                                        startAngle = startAngle,
                                        sweepAngle = arcAngle,
                                        useCenter = false,
                                        topLeft = Offset(center.x - r, center.y - r),
                                        size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                                    )
                                }
                            }
                        }
                    }

                    // Static center target crosshair
                    drawCircle(
                        color = cardBgColor,
                        radius = 28.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = animatedColor,
                        radius = 26.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Center Note Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (pitchResult.isPitchDetected) pitchResult.noteName else "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    if (pitchResult.isPitchDetected) {
                        Text(
                            text = "${pitchResult.octave}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    !pitchResult.isPitchDetected -> "Pluck string to observe strobe wheel"
                    pitchResult.inTune -> "Strobe Wheel Frozen • In Perfect Tune"
                    pitchResult.centsOffset < 0 -> "Spinning Left • Pluck String Higher (Flat)"
                    else -> "Spinning Right • Pluck String Lower (Sharp)"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}
