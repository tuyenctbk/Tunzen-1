package com.example.ui.screens

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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MusicUtils
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
import kotlin.math.roundToInt

@Composable
fun ToneGeneratorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val referenceA4 by viewModel.referenceA4.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    var frequencyHz by remember { mutableDoubleStateOf(440.0) }
    var waveType by remember { mutableStateOf("SINE") } // SINE, TRIANGLE, SOFT
    var isPlaying by remember { mutableStateOf(false) }
    var durationMode by remember { mutableStateOf("INFINITE") } // INFINITE, 3S, 5S
    var selectedOctave by remember { mutableIntStateOf(4) }
    var selectedNoteIndex by remember { mutableIntStateOf(9) } // A is index 9 in C, C#, D...

    // Ear Training Quiz state
    var earTrainingMode by remember { mutableStateOf(false) }
    var targetQuizNote by remember { mutableStateOf("") }
    var quizMessage by remember { mutableStateOf("Tap 'Start Ear Quiz' to test your pitch identification!") }
    var quizScore by remember { mutableIntStateOf(0) }

    val noteInfo = MusicUtils.getClosestNoteInfo(frequencyHz, referenceA4)

    val playCurrentTone = {
        val durationMs = when (durationMode) {
            "3S" -> 3000L
            "5S" -> 5000L
            else -> 600000L // 10 minutes continuous
        }
        viewModel.toneGeneratorEngine.playTone(
            frequencyHz = frequencyHz,
            waveType = waveType,
            durationMs = durationMs
        )
        isPlaying = true
    }

    val stopTone = {
        viewModel.toneGeneratorEngine.stopTone()
        isPlaying = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("tone_generator_screen_column"),
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
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Tone Generator",
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Reference Tone & Ear Training",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configurable sine wave generator for manual tuning & ear training",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Central Frequency Display Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, if (isPlaying) CyanAccent else SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("tone_display_card"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${noteInfo.first}${noteInfo.second}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("%.2f Hz", frequencyHz),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Concert A4 = ${referenceA4.toInt()}Hz • Wave: $waveType",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Waveform Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SINE", "TRIANGLE", "SOFT").forEach { type ->
                val isSelected = waveType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceCard)
                        .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            waveType = type
                            if (isPlaying) playCurrentTone()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextSecondary
                    )
                }
            }
        }

        // Frequency Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("FREQUENCY (Hz)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("${frequencyHz.roundToInt()} Hz", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            }
            Slider(
                value = frequencyHz.toFloat(),
                onValueChange = {
                    frequencyHz = it.toDouble()
                    if (isPlaying) playCurrentTone()
                },
                valueRange = 65.0f..1046.5f, // C2 to C6
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = SurfaceBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tone_frequency_slider")
            )

            // Fine adjustment buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(-10.0, -1.0, -0.1, 0.1, 1.0, 10.0).forEach { delta ->
                    val label = if (delta > 0) "+$delta" else "$delta"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceBorder)
                            .clickable {
                                frequencyHz = (frequencyHz + delta).coerceIn(40.0, 4000.0)
                                if (isPlaying) playCurrentTone()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        // Chromatic Note Grid & Octave Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QUICK NOTE SELECTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(2, 3, 4, 5).forEach { oct ->
                        val isSel = selectedOctave == oct
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyanAccent else SurfaceBorder)
                                .clickable {
                                    selectedOctave = oct
                                    val midi = (oct + 1) * 12 + selectedNoteIndex
                                    frequencyHz = MusicUtils.midiNoteToFrequency(midi, referenceA4)
                                    if (isPlaying) playCurrentTone()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("O$oct", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) OledBackground else TextPrimary)
                        }
                    }
                }
            }

            // Note buttons grid (C, C#, D...)
            val notes = MusicUtils.NOTE_NAMES
            // Row 1: C, C#, D, D#, E, F
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                notes.take(6).forEachIndexed { index, note ->
                    val isSel = selectedNoteIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else SurfaceBorder)
                            .border(1.dp, if (isSel) CyanAccent else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedNoteIndex = index
                                val midi = (selectedOctave + 1) * 12 + index
                                frequencyHz = MusicUtils.midiNoteToFrequency(midi, referenceA4)
                                if (isPlaying) playCurrentTone()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(note, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) CyanAccent else TextSecondary)
                    }
                }
            }
            // Row 2: F#, G, G#, A, A#, B
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                notes.drop(6).forEachIndexed { index, note ->
                    val actualIndex = index + 6
                    val isSel = selectedNoteIndex == actualIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else SurfaceBorder)
                            .border(1.dp, if (isSel) CyanAccent else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedNoteIndex = actualIndex
                                val midi = (selectedOctave + 1) * 12 + actualIndex
                                frequencyHz = MusicUtils.midiNoteToFrequency(midi, referenceA4)
                                if (isPlaying) playCurrentTone()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(note, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) CyanAccent else TextSecondary)
                    }
                }
            }
        }

        // Instrument Strings Quick Reference for Manual Tuning
        if (selectedPreset.strings.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MANUAL TUNING: ${selectedPreset.name.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedPreset.strings.forEach { string ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceBorder)
                                .clickable {
                                    frequencyHz = string.targetFrequency
                                    playCurrentTone()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${string.noteName}${string.octave}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = string.name.substringBefore(" -"),
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ear Training Quiz Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EAR TRAINING PITCH QUIZ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("Score: $quizScore", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldInTune)
            }
            Text(
                text = quizMessage,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        earTrainingMode = true
                        val randomMidi = 48 + (Math.random() * 25).toInt() // C3 to C5
                        val randFreq = MusicUtils.midiNoteToFrequency(randomMidi, referenceA4)
                        val info = MusicUtils.getClosestNoteInfo(randFreq, referenceA4)
                        targetQuizNote = "${info.first}${info.second}"
                        frequencyHz = randFreq
                        playCurrentTone()
                        quizMessage = "Listen to the tone and select the correct note below!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PLAY RANDOM QUIZ TONE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            if (earTrainingMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("C", "E", "G", "A").forEach { noteGuess ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceBorder)
                                .clickable {
                                    if (targetQuizNote.startsWith(noteGuess)) {
                                        quizScore++
                                        quizMessage = "Correct! It was $targetQuizNote 🎉"
                                    } else {
                                        quizMessage = "Incorrect. It was $targetQuizNote. Try again!"
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(noteGuess, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Main Play / Stop Button
        Button(
            onClick = {
                if (isPlaying) stopTone() else playCurrentTone()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) RubySharp else CyanAccent,
                contentColor = OledBackground
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("tone_play_stop_button")
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
                    text = if (isPlaying) "STOP REFERENCE TONE" else "PLAY REFERENCE TONE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
