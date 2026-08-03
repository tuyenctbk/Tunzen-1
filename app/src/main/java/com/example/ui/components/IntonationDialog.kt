package com.example.ui.components

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun IntonationDialog(
    pitchResult: PitchResult,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var openFreq by remember { mutableDoubleStateOf(0.0) }
    var fretted12Freq by remember { mutableDoubleStateOf(0.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Intonation Check",
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text("Intonation Diagnostic Tool", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (step) {
                    1 -> {
                        Text(
                            text = "Step 1: Pluck Open String",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "Pluck open string and hold steady until frequency stabilizes.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = OledBackground)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (pitchResult.isPitchDetected) String.format("%.2f Hz", pitchResult.frequency) else "Listening...",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (pitchResult.isPitchDetected) EmeraldInTune else TextMuted
                                )
                                if (pitchResult.isPitchDetected) {
                                    Text(
                                        text = "${pitchResult.noteName}${pitchResult.octave}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Step 2: Press & Pluck 12th Fret",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "Press down at the 12th fret (or touch 12th harmonic) and pluck string.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = OledBackground)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (pitchResult.isPitchDetected) String.format("%.2f Hz", pitchResult.frequency) else "Listening...",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (pitchResult.isPitchDetected) EmeraldInTune else TextMuted
                                )
                                if (pitchResult.isPitchDetected) {
                                    Text(
                                        text = "${pitchResult.noteName}${pitchResult.octave}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // Diagnostic Result Evaluation
                        val expected12thFreq = openFreq * 2.0
                        val deltaHz = fretted12Freq - expected12thFreq
                        val centsError = 1200.0 * Math.log(fretted12Freq / expected12thFreq) / Math.log(2.0)
                        val isGood = abs(centsError) <= 4.0

                        Text(
                            text = "Diagnostic Report",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGood) EmeraldInTune else AmberFlat
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = OledBackground)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Open String:", fontSize = 12.sp, color = TextMuted)
                                    Text(String.format("%.2f Hz", openFreq), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("12th Fret Note:", fontSize = 12.sp, color = TextMuted)
                                    Text(String.format("%.2f Hz", fretted12Freq), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Intonation Error:", fontSize = 12.sp, color = TextMuted)
                                    Text(String.format("%+.1f Cents", centsError), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (isGood) EmeraldInTune else RubySharp)
                                }
                            }
                        }

                        if (isGood) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldInTune, modifier = Modifier.size(16.dp))
                                Text("Intonation Excellent! Bridge saddle & neck setup optimal.", fontSize = 11.sp, color = EmeraldInTune)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberFlat, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (centsError > 0) "Intonation Sharp: Move bridge saddle backward or replace old string." else "Intonation Flat: Move bridge saddle forward.",
                                    fontSize = 11.sp,
                                    color = AmberFlat
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (step) {
                1 -> {
                    Button(
                        onClick = {
                            if (pitchResult.isPitchDetected) {
                                openFreq = pitchResult.frequency
                                step = 2
                            }
                        },
                        enabled = pitchResult.isPitchDetected,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                        modifier = Modifier.testTag("intonation_step1_confirm")
                    ) {
                        Text("Capture Open Pitch", fontWeight = FontWeight.Bold)
                    }
                }
                2 -> {
                    Button(
                        onClick = {
                            if (pitchResult.isPitchDetected) {
                                fretted12Freq = pitchResult.frequency
                                step = 3
                            }
                        },
                        enabled = pitchResult.isPitchDetected,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                        modifier = Modifier.testTag("intonation_step2_confirm")
                    ) {
                        Text("Capture 12th Fret Pitch", fontWeight = FontWeight.Bold)
                    }
                }
                3 -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
