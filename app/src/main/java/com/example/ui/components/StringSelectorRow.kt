package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.model.InstrumentPreset
import com.example.model.TuningString
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StringSelectorRow(
    preset: InstrumentPreset,
    selectedString: TuningString?,
    matchedString: TuningString?,
    onSelectString: (TuningString?) -> Unit,
    onPlayTone: (TuningString) -> Unit,
    modifier: Modifier = Modifier
) {
    if (preset.strings.isEmpty()) {
        // Auto Chromatic mode indicator
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AUTO CHROMATIC MODE — Detects any note automatically",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("string_selector_row_column")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TARGET STRINGS (${preset.name})",
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            if (selectedString != null) {
                Text(
                    text = "Clear Lock",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelectString(null) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(preset.strings) { str ->
                val isSelected = selectedString?.stringIndex == str.stringIndex
                val isMatched = matchedString?.stringIndex == str.stringIndex && selectedString == null

                val bgColor = when {
                    isSelected -> CyanAccent.copy(alpha = 0.2f)
                    isMatched -> EmeraldInTune.copy(alpha = 0.18f)
                    else -> SurfaceCard
                }

                val borderColor = when {
                    isSelected -> CyanAccent
                    isMatched -> EmeraldInTune
                    else -> SurfaceBorder
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onSelectString(if (isSelected) null else str) }
                        .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
                        .testTag("string_button_${str.stringIndex}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            Text(
                                text = "${str.noteName}${str.octave}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) CyanAccent else if (isMatched) EmeraldInTune else TextPrimary
                            )
                            Text(
                                text = "String ${str.stringIndex}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(
                            onClick = { onPlayTone(str) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Tone for ${str.noteName}",
                                tint = if (isSelected) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
