package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberFlat
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

data class PitchPresetStandard(
    val frequency: Double,
    val label: String,
    val description: String
)

val STANDARD_PITCH_PRESETS = listOf(
    PitchPresetStandard(415.0, "415 Hz", "Baroque"),
    PitchPresetStandard(432.0, "432 Hz", "Verdi"),
    PitchPresetStandard(440.0, "440 Hz", "Concert A4"),
    PitchPresetStandard(442.0, "442 Hz", "Orchestra"),
    PitchPresetStandard(444.0, "444 Hz", "Chamber")
)

@Composable
fun ReferencePitchBar(
    referenceA4: Double,
    onReferenceA4Changed: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val standardCategory = when {
        abs(referenceA4 - 440.0) < 0.05 -> "Standard Concert Pitch (440.0 Hz)"
        abs(referenceA4 - 432.0) < 0.05 -> "Verdi / Scientific Pitch (432.0 Hz)"
        abs(referenceA4 - 415.0) < 0.05 -> "Baroque Tuning (415.0 Hz)"
        abs(referenceA4 - 442.0) < 0.05 -> "European Symphonic (442.0 Hz)"
        abs(referenceA4 - 444.0) < 0.05 -> "High Chamber Pitch (444.0 Hz)"
        referenceA4 < 432.0 -> "Low Historical Tuning (${String.format("%.1f", referenceA4)} Hz)"
        referenceA4 > 444.0 -> "High Orchestra Pitch (${String.format("%.1f", referenceA4)} Hz)"
        else -> "Custom Calibrated Pitch (${String.format("%.1f", referenceA4)} Hz)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .testTag("reference_pitch_bar_box"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Pitch Calibration",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "REFERENCE PITCH CALIBRATION (A4)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = standardCategory,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = String.format("%.1f Hz", referenceA4),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanAccent
                )
            }

            // Quick Preset Standard Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                STANDARD_PITCH_PRESETS.forEach { preset ->
                    val isSelected = abs(referenceA4 - preset.frequency) < 0.05
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceCardVariant)
                            .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { onReferenceA4Changed(preset.frequency) }
                            .padding(vertical = 8.dp)
                            .testTag("pitch_chip_${preset.label.replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                            Text(
                                text = preset.description,
                                fontSize = 9.sp,
                                color = if (isSelected) CyanAccent.copy(alpha = 0.8f) else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Calibration Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("415.0 Hz (Baroque)", fontSize = 10.sp, color = TextMuted)
                    Text("440.0 Hz (Standard)", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    Text("466.0 Hz (High)", fontSize = 10.sp, color = TextMuted)
                }

                Slider(
                    value = referenceA4.toFloat(),
                    onValueChange = { onReferenceA4Changed((Math.round(it * 10f) / 10f).toDouble()) },
                    valueRange = 415f..466f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = SurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reference_pitch_slider")
                )
            }

            // Fine Nudge Calibration Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -1.0 Hz
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardVariant)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReferenceA4Changed(referenceA4 - 1.0) }
                        .padding(vertical = 8.dp)
                        .testTag("pitch_sub_1hz"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("-1.0 Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }

                // -0.1 Hz
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardVariant)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReferenceA4Changed(referenceA4 - 0.1) }
                        .padding(vertical = 8.dp)
                        .testTag("pitch_sub_01hz"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("-0.1 Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }

                // Reset (440 Hz)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (abs(referenceA4 - 440.0) < 0.05) SurfaceCardVariant else EmeraldInTune.copy(alpha = 0.18f))
                        .border(1.dp, if (abs(referenceA4 - 440.0) < 0.05) SurfaceBorder else EmeraldInTune, RoundedCornerShape(8.dp))
                        .clickable { onReferenceA4Changed(440.0) }
                        .padding(vertical = 8.dp)
                        .testTag("pitch_reset_440"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset 440Hz",
                            tint = if (abs(referenceA4 - 440.0) < 0.05) TextMuted else EmeraldInTune,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Reset 440",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (abs(referenceA4 - 440.0) < 0.05) TextMuted else EmeraldInTune
                        )
                    }
                }

                // +0.1 Hz
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardVariant)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReferenceA4Changed(referenceA4 + 0.1) }
                        .padding(vertical = 8.dp)
                        .testTag("pitch_add_01hz"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+0.1 Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }

                // +1.0 Hz
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardVariant)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReferenceA4Changed(referenceA4 + 1.0) }
                        .padding(vertical = 8.dp)
                        .testTag("pitch_add_1hz"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+1.0 Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }

            Text(
                text = "Adjusting concert pitch A4 recalibrates target frequencies, note mapping, and tone generator output across all instrument presets in real time.",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}
