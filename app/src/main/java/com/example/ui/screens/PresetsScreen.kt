package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.audio.MusicUtils
import com.example.model.InstrumentPreset
import com.example.model.TuningString
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PresetsScreen(
    viewModel: MainViewModel,
    onPresetSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allPresets by viewModel.allPresets.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    val groupedPresets = remember(allPresets) {
        allPresets.groupBy { it.category }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OledBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CyanAccent,
                contentColor = OledBackground,
                modifier = Modifier.testTag("add_custom_preset_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Tuning")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = "Presets",
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Instrument Presets",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select an instrument tuning or create custom temperaments",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            groupedPresets.forEach { (category, presets) ->
                item {
                    Text(
                        text = category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(presets) { preset ->
                    val isSelected = preset.id == selectedPreset.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanAccent else SurfaceBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                viewModel.selectPreset(preset)
                                onPresetSelected()
                            }
                            .testTag("preset_card_${preset.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyanAccent.copy(alpha = 0.12f) else SurfaceCard
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanAccent else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (preset.strings.isNotEmpty()) {
                                    Text(
                                        text = preset.strings.joinToString(" • ") { "${it.noteName}${it.octave}" },
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                } else {
                                    Text(
                                        text = "Full Octave Auto Pitch Chromatic",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected Preset",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                if (preset.isCustom) {
                                    IconButton(
                                        onClick = { viewModel.deleteCustomPreset(preset.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Custom Preset",
                                            tint = TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

        if (showCreateDialog) {
            CreateCustomPresetDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, cat, strings ->
                    viewModel.createCustomPreset(name, cat, strings)
                    showCreateDialog = false
                    onPresetSelected()
                }
            )
        }
    }
}

@Composable
fun CreateCustomPresetDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, category: String, strings: List<TuningString>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("E2, A2, D3, G3, B3, E4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text("Create Custom Tuning", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter preset name and string notes (e.g. E2, A2, D3, G3, B3, E4 or G4, C4, E4, A4):",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_preset_name_input")
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("String Notes (comma separated)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_preset_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val stringTokens = notesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val strings = stringTokens.mapIndexed { index, token ->
                            // Parse token like "E4" or "C#4"
                            val note = token.filter { it.isLetter() || it == '#' }.uppercase()
                            val octave = token.filter { it.isDigit() }.toIntOrNull() ?: 4
                            MusicUtils.createString("${index + 1}st", note, octave, 440.0, index + 1)
                        }
                        onCreate(name, "Custom", strings)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                modifier = Modifier.testTag("save_custom_preset_button")
            ) {
                Text("Save Preset", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
