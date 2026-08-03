package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.RubySharp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MetronomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isPlayingState = viewModel.metronomeEngine.isPlaying.collectAsState()
    val isPlaying = isPlayingState.value
    val bpmState = viewModel.metronomeEngine.bpm.collectAsState()
    val bpm = bpmState.value
    val currentBeatState = viewModel.metronomeEngine.currentBeat.collectAsState()
    val currentBeat = currentBeatState.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("metronome_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Metronome",
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Precision Metronome",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Acoustic rhythm & tempo trainer",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Circular Pulse Ring & Large BPM Counter Display
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(
                    width = if (isPlaying && currentBeat == 1) 4.dp else 2.dp,
                    color = if (isPlaying && currentBeat == 1) EmeraldInTune else if (isPlaying) CyanAccent else SurfaceBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$bpm",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isPlaying) CyanAccent else TextPrimary
                )
                Text(
                    text = "BPM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextMuted
                )

                if (isPlaying) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (b in 1..4) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (b == currentBeat) {
                                            if (b == 1) EmeraldInTune else CyanAccent
                                        } else SurfaceBorder
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Quick BPM Adjustment Row (-5, -1, +1, +5)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.metronomeEngine.setBpm(bpm - 5) },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            ) {
                Text("-5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
            IconButton(
                onClick = { viewModel.metronomeEngine.setBpm(bpm - 1) },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Sub 1 BPM", tint = TextPrimary)
            }
            IconButton(
                onClick = { viewModel.metronomeEngine.setBpm(bpm + 1) },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add 1 BPM", tint = TextPrimary)
            }
            IconButton(
                onClick = { viewModel.metronomeEngine.setBpm(bpm + 5) },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            ) {
                Text("+5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }

        // BPM Slider
        Slider(
            value = bpm.toFloat(),
            onValueChange = { viewModel.metronomeEngine.setBpm(it.toInt()) },
            valueRange = 30f..300f,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = CyanAccent,
                inactiveTrackColor = SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("metronome_bpm_slider")
        )

        // Action Buttons Row (Start/Stop & Tap Tempo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isPlaying) viewModel.metronomeEngine.stop() else viewModel.metronomeEngine.start()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) RubySharp else CyanAccent,
                    contentColor = OledBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("metronome_start_stop_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        text = if (isPlaying) "STOP METRONOME" else "START METRONOME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = { viewModel.metronomeEngine.registerTapTempo() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceCard,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(50.dp)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .testTag("tap_tempo_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Text("TAP TEMPO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
