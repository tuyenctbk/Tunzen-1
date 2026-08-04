package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AudioFFTVisualizer
import com.example.ui.components.MicrophonePermissionCard
import com.example.ui.components.PitchNeedleArc
import com.example.ui.components.StringSelectorRow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.IntonationDialog
import com.example.ui.components.OnboardingTourOverlay
import com.example.ui.components.StrobeWheelVisualizer
import com.example.ui.components.VocalPitchGraphCanvas
import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun TunerScreen(
    viewModel: MainViewModel,
    hasMicPermission: Boolean,
    isPermissionDeniedByUser: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToPresets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pitchResult by viewModel.pitchState.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isSimulationMode by viewModel.isSimulationMode.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val selectedString by viewModel.selectedString.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val tunerDisplayMode by viewModel.tunerDisplayMode.collectAsState()
    val isTourActive by viewModel.isTourActive.collectAsState()
    val tourStep by viewModel.tourStep.collectAsState()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    val hasSeenTour by viewModel.hasSeenTour.collectAsState()

    var showIntonationDialog by remember { mutableStateOf(false) }

    // Auto-start interactive tour on first load after onboarding
    LaunchedEffect(hasCompletedOnboarding, hasSeenTour) {
        if (hasCompletedOnboarding && !hasSeenTour) {
            viewModel.startTour()
        }
    }

    val hapticFeedback = LocalHapticFeedback.current

    // Trigger subtle haptic pulse when pitch enters the 'perfect tuning' range
    LaunchedEffect(pitchResult.inTune) {
        if (pitchResult.inTune && hapticsEnabled) {
            try {
                // Perform Compose level subtle tactile feedback
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                // Perform hardware vibrator subtle click
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator?.hasVibrator() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(30, 80))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(30)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val referenceA4 by viewModel.referenceA4.collectAsState()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .background(OledBackground)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("tuner_screen_column"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Professional Polish Top Brand & Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRecording) stringResource(R.string.signal_active) else stringResource(R.string.dsp_idle),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) EmeraldInTune else TextMuted,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "TuneZen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tour Trigger Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.startTour() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("start_interactive_tour_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Interactive Tour",
                                tint = CyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Tour",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "REF. PITCH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCardVariant)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "A=${referenceA4.toInt()}Hz",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

        // Instrument Preset Header Selector & Intonation Diagnostic Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToPresets() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("instrument_preset_header_box")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Instrument Icon",
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TUNING INSTRUMENT",
                                fontSize = 10.sp,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedPreset.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Change Preset",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .clickable { showIntonationDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Intonation Check",
                    tint = CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Tuner View Mode Selector Pills (Needle vs Strobe vs Vocal Curve)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("NEEDLE" to "Needle Arc", "STROBE" to "Strobe Wheel", "VOCAL_GRAPH" to "Pitch Curve").forEach { (modeKey, label) ->
                val isSelected = tunerDisplayMode == modeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceCard)
                        .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.setTunerDisplayMode(modeKey) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextSecondary
                    )
                }
            }
        }

        // Temperament Selector Component (Standard, Chromatic, Just Intonation, Pythagorean, Meantone)
        val selectedTemperament by viewModel.selectedTemperament.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
                .testTag("temperament_selector_card"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TUNING TEMPERAMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                Text(
                    text = when (selectedTemperament) {
                        "JUST" -> "Just Intonation (Pure Thirds/Fifths)"
                        "PYTHAGOREAN" -> "Pythagorean Tuning"
                        "MEANTONE" -> "1/4-Comma Meantone"
                        "CHROMATIC" -> "Auto Chromatic Scale"
                        else -> "Standard 12-TET"
                    },
                    fontSize = 10.sp,
                    color = CyanAccent
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "STANDARD" to "Standard",
                    "CHROMATIC" to "Chromatic",
                    "JUST" to "Just",
                    "PYTHAGOREAN" to "Pythag.",
                    "MEANTONE" to "Meantone"
                ).forEach { (key, label) ->
                    val isSel = selectedTemperament == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else SurfaceCard)
                            .border(1.dp, if (isSel) CyanAccent else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setTemperament(key) }
                            .padding(vertical = 6.dp)
                            .testTag("temperament_pill_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) CyanAccent else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Microphone Status & Permission Card
        MicrophonePermissionCard(
            hasPermission = hasMicPermission,
            isPermissionDeniedByUser = isPermissionDeniedByUser,
            isRecording = isRecording,
            isSimulationMode = isSimulationMode,
            onRequestPermission = onRequestPermission,
            onOpenSettings = onOpenSettings,
            onToggleRecording = {
                if (isRecording) viewModel.stopListening() else viewModel.startListening()
            },
            onToggleSimulation = { viewModel.toggleSimulation() }
        )

        // Selected Visualizer Mode Rendering
        when (tunerDisplayMode) {
            "STROBE" -> StrobeWheelVisualizer(pitchResult = pitchResult)
            "VOCAL_GRAPH" -> VocalPitchGraphCanvas(pitchResult = pitchResult)
            else -> PitchNeedleArc(
                pitchResult = pitchResult,
                isVocalMode = selectedPreset.category == "Vocal Trainer"
            )
        }

        // Target String Selector
        StringSelectorRow(
            preset = selectedPreset,
            selectedString = selectedString,
            matchedString = pitchResult.matchedString,
            onSelectString = { viewModel.selectString(it) },
            onPlayTone = { viewModel.playReferenceTone(it) }
        )

        // Real-Time Audio FFT Spectrum Visualizer
        AudioFFTVisualizer(
            fftMagnitude = pitchResult.fftMagnitude,
            isPitchDetected = pitchResult.isPitchDetected
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showIntonationDialog) {
        IntonationDialog(
            pitchResult = pitchResult,
            onDismiss = { showIntonationDialog = false }
        )
    }

    // Interactive Onboarding Feature Tour Tooltip Overlay
    OnboardingTourOverlay(
        isTourActive = isTourActive,
        currentStep = tourStep,
        onNextStep = { viewModel.nextTourStep() },
        onPreviousStep = { viewModel.previousTourStep() },
        onCompleteTour = { viewModel.completeTour() }
    )
    }
}
