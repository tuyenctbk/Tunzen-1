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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldInTune
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MicrophonePermissionCard(
    hasPermission: Boolean,
    isPermissionDeniedByUser: Boolean,
    isRecording: Boolean,
    isSimulationMode: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleRecording: () -> Unit,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("microphone_permission_card_box")
    ) {
        if (!hasPermission) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isPermissionDeniedByUser) {
                    // Beautiful Detailed Rationale & Privacy Explanation when denied
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Access Denied",
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.permission_rationale_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.permission_rationale_desc),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 100% Privacy box with border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyanAccent.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🔒 " + stringResource(R.string.permission_privacy_header),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Text(
                                text = stringResource(R.string.permission_privacy_desc),
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onOpenSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).testTag("open_settings_button")
                        ) {
                            Text(stringResource(R.string.permission_button_settings), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onRequestPermission,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.weight(1f).testTag("retry_permission_button")
                        ) {
                            Text("Retry", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onToggleSimulation,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.weight(1.1f).testTag("demo_mode_button")
                        ) {
                            Text(if (isSimulationMode) stringResource(R.string.stop_demo) else stringResource(R.string.try_demo_mode), fontSize = 12.sp)
                        }
                    }
                } else {
                    // Normal layout before any denials, with a brief explanation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Permission Needed",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.mic_access_required),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.onboarding_desc_3),
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OledBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("enable_mic_permission_button")
                        ) {
                            Text(stringResource(R.string.enable_microphone), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onToggleSimulation,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.weight(1f).testTag("demo_mode_button")
                        ) {
                            Text(if (isSimulationMode) stringResource(R.string.stop_demo) else stringResource(R.string.try_demo_mode), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isSimulationMode) CyanAccent else if (isRecording) EmeraldInTune else TextMuted,
                                shape = CircleShape
                            )
                    )
                    Column {
                        Text(
                            text = when {
                                isSimulationMode -> "DEMO SIMULATION ACTIVE"
                                isRecording -> stringResource(R.string.live_mic_listening)
                                else -> stringResource(R.string.tuner_paused)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isRecording) "Real-time FFT audio engine running" else "Tap button to start listening",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Button(
                    onClick = onToggleRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) SurfaceBorder else EmeraldInTune,
                        contentColor = if (isRecording) TextPrimary else OledBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("toggle_recording_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isRecording) "Pause" else "Start",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
