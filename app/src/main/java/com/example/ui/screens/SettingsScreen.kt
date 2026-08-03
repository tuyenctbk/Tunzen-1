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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.PoliteAdBanner
import com.example.ui.MainViewModel
import com.example.ui.components.CalibrationPresetsCard
import com.example.ui.components.ReferencePitchBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val referenceA4 by viewModel.referenceA4.collectAsState()
    val selectedTemperament by viewModel.selectedTemperament.collectAsState()
    val calibrationPresets by viewModel.allCalibrationPresets.collectAsState()
    val noiseGateThreshold by viewModel.noiseGateThreshold.collectAsState()
    val dynamicSensitivityEnabled by viewModel.dynamicSensitivityEnabled.collectAsState()
    val autoGainControlEnabled by viewModel.autoGainControlEnabled.collectAsState()
    val pitchResult by viewModel.pitchState.collectAsState()
    val waveType by viewModel.waveType.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val isPaperLightMode by viewModel.isPaperLightMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Tuner Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configure audio DSP, reference frequencies, and feedback",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Display Theme Selector Card (OLED Dark vs Paper Light)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = "Theme",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DISPLAY THEME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = if (isPaperLightMode) "Paper Light" else "OLED Dark",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPaperLightMode) "Paper Light mode provides high-contrast outdoor visibility in direct sunlight." else "OLED Dark mode saves power and reduces eye strain during low-light performances.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // OLED Dark Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isPaperLightMode) CyanAccent.copy(alpha = 0.2f) else SurfaceCardVariant)
                            .border(1.dp, if (!isPaperLightMode) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setPaperLightMode(false) }
                            .padding(vertical = 10.dp)
                            .testTag("theme_oled_dark_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (!isPaperLightMode) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "OLED Dark",
                                fontSize = 13.sp,
                                fontWeight = if (!isPaperLightMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isPaperLightMode) CyanAccent else TextSecondary
                            )
                        }
                    }

                    // Paper Light Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPaperLightMode) CyanAccent.copy(alpha = 0.2f) else SurfaceCardVariant)
                            .border(1.dp, if (isPaperLightMode) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setPaperLightMode(true) }
                            .padding(vertical = 10.dp)
                            .testTag("theme_paper_light_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (isPaperLightMode) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Paper Light",
                                fontSize = 13.sp,
                                fontWeight = if (isPaperLightMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPaperLightMode) CyanAccent else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Concert Pitch Reference A4
        ReferencePitchBar(
            referenceA4 = referenceA4,
            onReferenceA4Changed = { viewModel.setReferenceA4(it) }
        )

        // Saved Calibration & Temperament Presets
        CalibrationPresetsCard(
            currentA4 = referenceA4,
            currentTemperament = selectedTemperament,
            presets = calibrationPresets,
            onSelectPreset = { viewModel.applyCalibrationPreset(it) },
            onSaveCurrentAsPreset = { viewModel.saveCustomCalibrationPreset(it) },
            onDeletePreset = { viewModel.deleteCustomCalibrationPreset(it) }
        )

        // Microphone Sensitivity / Noise Gate Slider
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            contentDescription = "Noise Filter",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NOISE FILTER GATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                    }

                    if (dynamicSensitivityEnabled) {
                        Text(
                            text = stringResource(R.string.settings_sensitivity_auto, pitchResult.activeThreshold),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.settings_sensitivity_manual, noiseGateThreshold),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_sensitivity_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.settings_dynamic_sensitivity_desc),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = dynamicSensitivityEnabled,
                        onCheckedChange = { viewModel.setDynamicSensitivityEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.testTag("dynamic_sensitivity_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceBorder))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (dynamicSensitivityEnabled) {
                        stringResource(R.string.settings_dynamic_sensitivity_active)
                    } else {
                        stringResource(R.string.settings_manual_sensitivity_desc)
                    },
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Slider(
                    value = if (dynamicSensitivityEnabled) {
                        pitchResult.activeThreshold.toFloat()
                    } else {
                        noiseGateThreshold.toFloat()
                    },
                    onValueChange = { viewModel.setNoiseGateThreshold(it.toDouble()) },
                    valueRange = 0.005f..0.08f,
                    enabled = !dynamicSensitivityEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = if (dynamicSensitivityEnabled) TextMuted else CyanAccent,
                        activeTrackColor = if (dynamicSensitivityEnabled) SurfaceBorder else CyanAccent,
                        inactiveTrackColor = SurfaceBorder,
                        disabledThumbColor = TextMuted,
                        disabledActiveTrackColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("noise_gate_slider")
                )
            }
        }

        // Automated Microphone Input Gain Controller Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automated Mic Gain Control (AGC)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Normalizes signal amplitude dynamically for consistent waveform display & pitch lock",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = autoGainControlEnabled,
                        onCheckedChange = { viewModel.setAutoGainControlEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.testTag("auto_gain_control_switch")
                    )
                }

                if (autoGainControlEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardVariant)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Gain Boost:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            val dbGain = if (pitchResult.appliedGainFactor > 0f) {
                                20.0 * kotlin.math.log10(pitchResult.appliedGainFactor.toDouble())
                            } else 0.0
                            Text(
                                text = String.format("%.1fx (+%.1f dB)", pitchResult.appliedGainFactor, dbGain),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }
        }

        // Reference Tone Generator Waveform Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Waveform",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "REFERENCE TONE WAVEFORM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SINE" to "Pure Sine", "TRIANGLE" to "Triangle", "SOFT" to "Warm Soft").forEach { (type, label) ->
                        val isSelected = waveType.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceBorder)
                                .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setWaveType(type) }
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
            }
        }

        // In-Tune Tactile Haptics Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptics",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Tactile In-Tune Vibration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Subtle haptic pulse when note hits exact pitch",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { viewModel.setHapticsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = CyanAccent.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceBorder
                    ),
                    modifier = Modifier.testTag("haptics_switch")
                )
            }
        }

        // About TuneZen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = EmeraldInTune,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ABOUT TUNEZEN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldInTune,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TuneZen v1.0 • 100% Free Minimalist Audio DSP Engine",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Zero subscriptions, local YIN/FFT pitch detection. Built for student musicians, teachers, and acoustic players.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.startTour() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_tour_settings_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Feature Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.resetOnboarding() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCardVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replay_onboarding_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Reset Guide", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        PoliteAdBanner()

        Spacer(modifier = Modifier.height(30.dp))
    }
}
