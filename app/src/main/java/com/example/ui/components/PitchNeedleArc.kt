package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.example.R
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
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PitchNeedleArc(
    pitchResult: PitchResult,
    isVocalMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val targetCents = if (pitchResult.isPitchDetected) pitchResult.centsOffset.toFloat() else 0f

    // Animated Cents Angle with spring dampening
    val animatedCents by animateFloatAsState(
        targetValue = targetCents.coerceIn(-50f, 50f),
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "NeedleCents"
    )

    val activeColor = when {
        !pitchResult.isPitchDetected -> TextMuted
        pitchResult.inTune -> EmeraldInTune
        pitchResult.centsOffset < 0 -> AmberFlat
        else -> RubySharp
    }

    val animatedNeedleColor by animateColorAsState(
        targetValue = activeColor,
        animationSpec = tween(durationMillis = 150),
        label = "NeedleColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pitch_needle_arc_box"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Suggested Note Display in large typography directly above the gauge
            SuggestedNoteCard(
                pitchResult = pitchResult,
                activeColor = animatedNeedleColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isVocalMode) {
                VocalToneStabilityTrainer(
                    pitchResult = pitchResult,
                    animatedNeedleColor = animatedNeedleColor
                )
            } else {
                // Arc Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                val arcTrackColor = SurfaceCardVariant
                val inTuneColor = EmeraldInTune
                val textSecColor = TextSecondary
                val textMutedColor = TextMuted
                val pivotCenterColor = OledBackground

                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(top = 10.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height * 0.82f)
                    val radius = width * 0.40f

                    // Sweep Angles: Arc ranges from 210° to 330° (120° total span)
                    val startAngle = 210f
                    val sweepSpan = 120f

                    // 1. Background Arc Track
                    drawArc(
                        color = arcTrackColor,
                        startAngle = startAngle,
                        sweepAngle = sweepSpan,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 2. In-Tune Center Zone Arc Segment
                    val centerSpan = 12f // +/- 2.5 cents equivalent in arc degrees
                    drawArc(
                        color = inTuneColor.copy(alpha = 0.35f),
                        startAngle = 270f - (centerSpan / 2f),
                        sweepAngle = centerSpan,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Tick Marks (-50, -30, -20, -10, 0, +10, +20, +30, +50)
                    val ticks = listOf(-50, -30, -20, -10, 0, 10, 20, 30, 50)
                    ticks.forEach { centsVal ->
                        val ratio = (centsVal + 50f) / 100f
                        val angleDeg = startAngle + (ratio * sweepSpan)
                        val angleRad = angleDeg * (PI / 180f)

                        val isCenter = centsVal == 0
                        val isMajor = abs(centsVal) == 50 || isCenter || abs(centsVal) == 20

                        val innerR = radius - (if (isMajor) 22.dp.toPx() else 14.dp.toPx())
                        val outerR = radius - 8.dp.toPx()

                        val p1 = Offset(
                            center.x + innerR * cos(angleRad).toFloat(),
                            center.y + innerR * sin(angleRad).toFloat()
                        )
                        val p2 = Offset(
                            center.x + outerR * cos(angleRad).toFloat(),
                            center.y + outerR * sin(angleRad).toFloat()
                        )

                        val tickColor = when {
                            isCenter -> inTuneColor
                            abs(centsVal) == 50 -> textSecColor
                            else -> textMutedColor
                        }

                        drawLine(
                            color = tickColor,
                            start = p1,
                            end = p2,
                            strokeWidth = if (isCenter) 3.5.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 4. Rotating Needle
                    val needleRatio = (animatedCents + 50f) / 100f
                    val needleAngleDeg = startAngle + (needleRatio * sweepSpan)
                    val needleAngleRad = needleAngleDeg * (PI / 180f)

                    val needleLength = radius - 4.dp.toPx()
                    val needleEnd = Offset(
                        center.x + needleLength * cos(needleAngleRad).toFloat(),
                        center.y + needleLength * sin(needleAngleRad).toFloat()
                    )

                    // Glow line if pitch detected
                    if (pitchResult.isPitchDetected) {
                        drawLine(
                            color = animatedNeedleColor.copy(alpha = 0.4f),
                            start = center,
                            end = needleEnd,
                            strokeWidth = 9.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    drawLine(
                        color = animatedNeedleColor,
                        start = center,
                        end = needleEnd,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Needle Base Pivot Circle
                    drawCircle(
                        color = animatedNeedleColor,
                        radius = 8.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = pivotCenterColor,
                        radius = 3.5.dp.toPx(),
                        center = center
                    )
                }

                // In-Center Note & Octave Display
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pitchResult.isPitchDetected) pitchResult.noteName else "—",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            color = if (pitchResult.inTune) EmeraldInTune else TextPrimary,
                            letterSpacing = (-2).sp
                        )
                        if (pitchResult.isPitchDetected && pitchResult.octave > 0) {
                            Text(
                                text = "${pitchResult.octave}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (pitchResult.inTune) EmeraldInTune else TextSecondary,
                                modifier = Modifier.padding(top = 10.dp, start = 2.dp)
                            )
                        }
                    }

                    // Cents Status Badge
                    val centsText = when {
                        !pitchResult.isPitchDetected -> "READY"
                        pitchResult.inTune -> "0.0 CENTS IN-TUNE"
                        pitchResult.centsOffset < 0 -> String.format("%.1f CENTS FLAT", pitchResult.centsOffset)
                        else -> String.format("+%.1f CENTS SHARP", pitchResult.centsOffset)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = animatedNeedleColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = animatedNeedleColor.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = centsText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = animatedNeedleColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exact Hz Frequency Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DETECTED FREQ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (pitchResult.isPitchDetected) String.format("%.2f Hz", pitchResult.frequency) else "0.00 Hz",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TARGET FREQ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (pitchResult.targetFrequency > 0) String.format("%.2f Hz", pitchResult.targetFrequency) else "0.00 Hz",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Signal Waveform Canvas below gauge
            AudioWaveformCanvas(
                waveform = pitchResult.waveform,
                amplitudeRms = pitchResult.amplitudeRms,
                isPitchDetected = pitchResult.isPitchDetected,
                inTune = pitchResult.inTune,
                appliedGainFactor = pitchResult.appliedGainFactor,
                agcEnabled = pitchResult.agcEnabled
            )
        }
    }
}

@Composable
fun VocalToneStabilityTrainer(
    pitchResult: PitchResult,
    animatedNeedleColor: Color,
    modifier: Modifier = Modifier
) {
    var holdStreakMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(pitchResult.isPitchDetected, pitchResult.inTune) {
        if (pitchResult.isPitchDetected && pitchResult.inTune) {
            var lastTime = System.currentTimeMillis()
            while (true) {
                delay(50)
                val now = System.currentTimeMillis()
                holdStreakMs += (now - lastTime)
                lastTime = now
            }
        } else {
            holdStreakMs = 0L
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimalist circular focus container
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    color = animatedNeedleColor.copy(alpha = 0.05f),
                    shape = CircleShape
                )
                .border(
                    width = if (pitchResult.inTune) 2.dp else 1.dp,
                    color = animatedNeedleColor.copy(alpha = if (pitchResult.isPitchDetected) 0.8f else 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background concentric pulse circles when pitch is locked
            if (pitchResult.isPitchDetected && pitchResult.inTune) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .border(
                            width = 1.dp,
                            color = animatedNeedleColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Note Name & Octave
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (pitchResult.isPitchDetected) pitchResult.noteName else "—",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light,
                        color = if (pitchResult.inTune) EmeraldInTune else TextPrimary,
                        letterSpacing = (-2).sp
                    )
                    if (pitchResult.isPitchDetected && pitchResult.octave > 0) {
                        Text(
                            text = "${pitchResult.octave}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (pitchResult.inTune) EmeraldInTune else TextSecondary,
                            modifier = Modifier.padding(top = 8.dp, start = 1.dp)
                        )
                    }
                }

                // Microscopic visualizer for wave stability
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val mutedLineColor = TextMuted.copy(alpha = 0.2f)
                    Canvas(modifier = Modifier.matchParentSize()) {
                        if (pitchResult.isPitchDetected) {
                            val width = size.width
                            val centerY = size.height / 2f
                            // Perfect tuning -> almost straight flat line
                            val waveAmplitude = if (pitchResult.inTune) 1.5.dp.toPx() else (abs(pitchResult.centsOffset).toFloat() * 0.4f).coerceIn(4f, 15f)
                            val waveFrequency = if (pitchResult.inTune) 1.0f else (abs(pitchResult.centsOffset).toFloat() * 0.05f).coerceIn(1.5f, 4f)

                            val path = Path()
                            path.moveTo(0f, centerY)
                            for (x in 0..width.toInt() step 2) {
                                val angle = (x / width) * 2f * PI * waveFrequency + wavePhase
                                val y = centerY + sin(angle).toFloat() * waveAmplitude
                                path.lineTo(x.toFloat(), y)
                            }
                            drawPath(
                                path = path,
                                color = animatedNeedleColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else {
                            // Flat thin placeholder
                            drawLine(
                                color = mutedLineColor,
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // Stability badge & streak counter
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stability Indicator Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.tone_stability),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    val stabilityScore = if (pitchResult.isPitchDetected) {
                        (100 - (abs(pitchResult.centsOffset) * 2.0)).coerceIn(0.0, 100.0).toInt()
                    } else 0
                    Text(
                        text = if (pitchResult.isPitchDetected) "$stabilityScore%" else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (pitchResult.isPitchDetected) animatedNeedleColor else TextMuted
                    )
                }
            }

            // Streak/Hold Indicator Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.hold_streak),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    val streakSeconds = holdStreakMs / 1000f
                    val streakText = if (streakSeconds > 0.1f) String.format("%.1f s", streakSeconds) else "—"
                    Text(
                        text = streakText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (streakSeconds > 0.5f) EmeraldInTune else TextMuted
                    )
                }
            }
        }

        // Precise cents scale bar (extremely minimalist and beautiful)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.flat_label), fontSize = 9.sp, color = TextMuted)
                Text(
                    text = when {
                        !pitchResult.isPitchDetected -> stringResource(R.string.sing_a_pitch)
                        pitchResult.inTune -> stringResource(R.string.perfect_tone_support)
                        pitchResult.centsOffset < 0 -> String.format("%.1f¢ %s", abs(pitchResult.centsOffset), stringResource(R.string.flat_label).split(" ")[0])
                        else -> String.format("%.1f¢ %s", abs(pitchResult.centsOffset), stringResource(R.string.sharp_label).split(" ")[0])
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedNeedleColor
                )
                Text(stringResource(R.string.sharp_label), fontSize = 9.sp, color = TextMuted)
            }

            // Simple line-slider cents tracker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(SurfaceCardVariant, RoundedCornerShape(3.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (pitchResult.isPitchDetected) {
                    val progress = ((pitchResult.centsOffset + 50f) / 100f).coerceIn(0.0, 1.0).toFloat()
                    val inTuneRectColor = EmeraldInTune.copy(alpha = 0.3f)
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        val width = size.width
                        val centerY = size.height / 2f
                        val pointX = width * progress

                        // Central in-tune range marker
                        drawRect(
                            color = inTuneRectColor,
                            topLeft = Offset(width * 0.45f, 0f),
                            size = Size(width * 0.1f, size.height)
                        )

                        // Glowing target pointer
                        drawCircle(
                            color = animatedNeedleColor,
                            radius = 5.dp.toPx(),
                            center = Offset(pointX, centerY)
                        )
                    }
                }
            }
        }
    }
}
