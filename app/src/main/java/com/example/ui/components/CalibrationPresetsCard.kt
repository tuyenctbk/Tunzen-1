package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.CalibrationPreset
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.RubySharp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalibrationPresetsCard(
    currentA4: Double,
    currentTemperament: String,
    presets: List<CalibrationPreset>,
    onSelectPreset: (CalibrationPreset) -> Unit,
    onSaveCurrentAsPreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .testTag("calibration_presets_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Presets",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "CALIBRATION & TEMPERAMENT PRESETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "Quickly restore saved A4 pitch & scale temperaments",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAccent.copy(alpha = 0.15f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", currentA4)}Hz • $currentTemperament",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Preset Badges Flow / Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = abs(currentA4 - preset.referenceA4) < 0.05 && currentTemperament == preset.temperament

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.18f) else SurfaceCardVariant)
                            .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("calibration_preset_item_${preset.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (isSelected) CyanAccent else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) CyanAccent else TextPrimary
                                    )
                                    if (preset.isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldInTune.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "CUSTOM",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldInTune
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${String.format("%.1f", preset.referenceA4)} Hz • Temperament: ${preset.temperament}",
                                    fontSize = 10.sp,
                                    color = if (isSelected) CyanAccent.copy(alpha = 0.8f) else TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (preset.isCustom) {
                            IconButton(
                                onClick = { onDeletePreset(preset.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_preset_${preset.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Preset",
                                    tint = RubySharp,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Button to Save Current Settings as Preset
            Button(
                onClick = {
                    newPresetName = "Calib ${currentA4.toInt()}Hz $currentTemperament"
                    showSaveDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("save_calibration_preset_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceCardVariant,
                    contentColor = CyanAccent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Save Current (${String.format("%.1f", currentA4)}Hz • $currentTemperament) as Preset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = CyanAccent
                    )
                    Text(
                        text = "Save Calibration Preset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saves reference A4 pitch (${String.format("%.1f", currentA4)} Hz) and temperament ($currentTemperament) together for 1-tap switching.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCurrentAsPreset(newPresetName)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("confirm_save_preset_btn")
                ) {
                    Text("Save Preset", color = SurfaceCard)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
