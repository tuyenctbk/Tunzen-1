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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ChordItem(
    val name: String,          // e.g. "C Major"
    val instrument: String,    // "Ukulele" or "Guitar"
    val frets: String,         // e.g. "0-0-0-3" for Uke or "x-3-2-0-1-0" for Guitar
    val rootFreq: Double       // Frequency to play audio preview
)

val DEFAULT_CHORDS = listOf(
    // Ukulele Chords
    ChordItem("C Major", "Ukulele", "0 • 0 • 0 • 3", 261.63),
    ChordItem("G Major", "Ukulele", "0 • 2 • 3 • 2", 196.00),
    ChordItem("A Minor", "Ukulele", "2 • 0 • 0 • 0", 220.00),
    ChordItem("F Major", "Ukulele", "2 • 0 • 1 • 0", 174.61),
    ChordItem("D Minor", "Ukulele", "2 • 2 • 1 • 0", 146.83),
    ChordItem("E Minor", "Ukulele", "0 • 4 • 3 • 2", 164.81),

    // Guitar Chords
    ChordItem("C Major", "Guitar", "x • 3 • 2 • 0 • 1 • 0", 261.63),
    ChordItem("G Major", "Guitar", "3 • 2 • 0 • 0 • 0 • 3", 196.00),
    ChordItem("D Major", "Guitar", "x • x • 0 • 2 • 3 • 2", 146.83),
    ChordItem("E Minor", "Guitar", "0 • 2 • 2 • 0 • 0 • 0", 164.81),
    ChordItem("A Minor", "Guitar", "x • 0 • 2 • 2 • 1 • 0", 220.00),
    ChordItem("E Major", "Guitar", "0 • 2 • 2 • 1 • 0 • 0", 164.81)
)

@Composable
fun ChordsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedInstrument by remember { mutableStateOf("Ukulele") }

    val filteredChords = remember(selectedInstrument) {
        DEFAULT_CHORDS.filter { it.instrument == selectedInstrument }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp)
            .testTag("chords_screen_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Chords",
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Chord Library & Tones",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Finger placement diagrams & audio reference",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Instrument Filter Pills (Ukulele vs Guitar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Ukulele", "Guitar").forEach { inst ->
                val isSelected = selectedInstrument == inst
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceCard)
                        .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedInstrument = inst }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inst,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextSecondary
                    )
                }
            }
        }

        // Chord Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredChords) { chord ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.toneGeneratorEngine.playTone(
                                frequencyHz = chord.rootFreq,
                                waveType = "SOFT",
                                durationMs = 1800
                            )
                        }
                        .testTag("chord_card_${chord.name.lowercase().replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chord.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Chord Tone",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(OledBackground)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chord.frets,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldInTune
                            )
                        }

                        Text(
                            text = "Tap to listen tone",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
