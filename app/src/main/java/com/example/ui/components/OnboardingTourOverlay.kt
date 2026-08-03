package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class TourStepInfo(
    val stepIndex: Int,
    val title: String,
    val badge: String,
    val icon: ImageVector,
    val description: String,
    val featurePoint1: String,
    val featurePoint2: String,
    val targetHighlightText: String
)

val TOUR_STEPS = listOf(
    TourStepInfo(
        stepIndex = 0,
        title = "Interactive Needle Gauge",
        badge = "VISUAL PRECISION ±1 CENT",
        icon = Icons.Default.Speed,
        description = "The central needle gauge detects pitch in real time using YIN & FFT DSP algorithms. It instantly shows your note, frequency, and exact cent deviation.",
        featurePoint1 = "Yellow needle = Flat pitch • Cyan = Sharp pitch",
        featurePoint2 = "Glowing Emerald Arc = Perfect In-Tune lock",
        targetHighlightText = "FEATURE 1: PITCH NEEDLE & CENT OFFSET"
    ),
    TourStepInfo(
        stepIndex = 1,
        title = "Reference Tone Generator",
        badge = "AUDIO HARMONIC SYNTHESIZER",
        icon = Icons.Default.MusicNote,
        description = "Tap any string pill (E2 to E4) to play clean reference tones. Switch to the Tone Generator tab to synthesize pure sine, triangle, or sawtooth waves.",
        featurePoint1 = "Listen to exact target pitches for manual tuning",
        featurePoint2 = "Develop relative pitch & ear training skills",
        targetHighlightText = "FEATURE 2: REFERENCE TONES & STRING SOUNDS"
    ),
    TourStepInfo(
        stepIndex = 2,
        title = "Pitch & Temperament Calibration",
        badge = "A4 BASELINE & SCALES",
        icon = Icons.Default.GraphicEq,
        description = "Need Baroque 415Hz or Verdi 432Hz tuning? Adjust the concert pitch A4 baseline via slider or quick presets. You can also select historical scale temperaments!",
        featurePoint1 = "Calibrate A4 from 415.0 Hz to 466.0 Hz",
        featurePoint2 = "Support Equal, Just, Pythagorean & Meantone temperaments",
        targetHighlightText = "FEATURE 3: A4 CALIBRATION & TEMPERAMENTS"
    )
)

@Composable
fun OnboardingTourOverlay(
    isTourActive: Boolean,
    currentStep: Int,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onCompleteTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isTourActive) return

    val stepData = TOUR_STEPS.getOrElse(currentStep.coerceIn(0, TOUR_STEPS.size - 1)) { TOUR_STEPS[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(enabled = false) {} // Prevent click-through
            .testTag("onboarding_tour_overlay_scrim"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Target Feature Banner Indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CyanAccent.copy(alpha = 0.25f))
                    .border(1.dp, CyanAccent, RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("tour_target_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stepData.targetHighlightText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Floating Tooltip Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, CyanAccent, RoundedCornerShape(20.dp))
                    .testTag("tour_tooltip_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Bar (Step count + Close)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = stepData.icon,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "INTERACTIVE TOUR • STEP ${stepData.stepIndex + 1} OF ${TOUR_STEPS.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = stepData.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = onCompleteTour,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("tour_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tour",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceCardVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stepData.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Main Explanation
                    Text(
                        text = stepData.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    // Feature Highlight Bullets
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCardVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldInTune,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stepData.featurePoint1,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldInTune,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stepData.featurePoint2,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Bottom Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Step Button
                        if (stepData.stepIndex > 0) {
                            OutlinedButton(
                                onClick = onPreviousStep,
                                modifier = Modifier.testTag("tour_previous_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", fontSize = 12.sp, color = TextSecondary)
                            }
                        } else {
                            TextButton(
                                onClick = onCompleteTour,
                                modifier = Modifier.testTag("tour_skip_btn")
                            ) {
                                Text("Skip Tour", fontSize = 12.sp, color = TextMuted)
                            }
                        }

                        // Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TOUR_STEPS.forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .size(if (step.stepIndex == stepData.stepIndex) 18.dp else 6.dp, 6.dp)
                                        .clip(CircleShape)
                                        .background(if (step.stepIndex == stepData.stepIndex) CyanAccent else SurfaceBorder)
                                )
                            }
                        }

                        // Next / Finish Button
                        Button(
                            onClick = {
                                if (stepData.stepIndex < TOUR_STEPS.size - 1) {
                                    onNextStep()
                                } else {
                                    onCompleteTour()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("tour_next_btn")
                        ) {
                            Text(
                                text = if (stepData.stepIndex < TOUR_STEPS.size - 1) "Next Step" else "Got It!",
                                color = OledBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (stepData.stepIndex < TOUR_STEPS.size - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                                contentDescription = null,
                                tint = OledBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
