package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PitchResult
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
fun SuggestedNoteCard(
    pitchResult: PitchResult,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .testTag("suggested_note_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Suggested Note",
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AUTOMATIC NOTE SUGGESTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (pitchResult.isPitchDetected) activeColor.copy(alpha = 0.15f) else SurfaceCardVariant,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (pitchResult.isPitchDetected) activeColor.copy(alpha = 0.4f) else SurfaceBorder,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (pitchResult.isPitchDetected) "12-TET MATCH" else "LISTENING...",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pitchResult.isPitchDetected) activeColor else TextMuted,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Large Typography Note Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                if (pitchResult.isPitchDetected && pitchResult.noteName != "-") {
                    Text(
                        text = pitchResult.noteName,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (pitchResult.inTune) EmeraldInTune else TextPrimary,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = "${pitchResult.octave}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (pitchResult.inTune) EmeraldInTune else CyanAccent,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = String.format("(%.2f Hz)", pitchResult.targetFrequency),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = "— —",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                        color = TextMuted,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Subtitle status line
            if (pitchResult.isPitchDetected && pitchResult.noteName != "-") {
                val freqDiff = pitchResult.frequency - pitchResult.targetFrequency
                val statusText = when {
                    pitchResult.inTune -> "Matched standard ${pitchResult.noteName}${pitchResult.octave} precisely (In-Tune)"
                    freqDiff < 0 -> String.format("Suggested ${pitchResult.noteName}${pitchResult.octave} • %.2f Hz below target (%.1f c flat)", abs(freqDiff), abs(pitchResult.centsOffset))
                    else -> String.format("Suggested ${pitchResult.noteName}${pitchResult.octave} • +%.2f Hz above target (+%.1f c sharp)", freqDiff, pitchResult.centsOffset)
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (pitchResult.inTune) EmeraldInTune else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Sing or play a note to auto-detect closest standard musical pitch",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
