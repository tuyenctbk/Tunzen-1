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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextSecondary

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedToolTab by remember { mutableIntStateOf(0) } // 0: Metronome, 1: Chords

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .background(OledBackground)
                .padding(horizontal = 16.dp)
                .testTag("tools_screen_column")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Minimalist Tool Segment Selector (Metronome, Tone Generator, Chord Library)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("⏱️ Metronome", "🎵 Tone Gen", "🎼 Chords").forEachIndexed { index, label ->
                    val isSelected = selectedToolTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceCard)
                            .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedToolTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CyanAccent else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedToolTab) {
                    0 -> MetronomeScreen(viewModel = viewModel)
                    1 -> ToneGeneratorScreen(viewModel = viewModel)
                    else -> ChordsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
