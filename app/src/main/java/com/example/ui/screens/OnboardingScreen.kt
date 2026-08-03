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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun OnboardingScreen(
    hasMicPermission: Boolean,
    isPermissionDeniedByUser: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Skip Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TuneZen",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            TextButton(
                onClick = onCompleteOnboarding,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    fontSize = 13.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Center Content Card based on currentPage
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            when (currentPage) {
                0 -> OnboardingPageContent(
                    icon = Icons.Default.GraphicEq,
                    title = stringResource(R.string.onboarding_title_1),
                    description = stringResource(R.string.onboarding_desc_1),
                    badge = "YIN & FFT AUDIO ENGINE"
                )
                1 -> OnboardingPageContent(
                    icon = Icons.Default.LibraryMusic,
                    title = stringResource(R.string.onboarding_title_2),
                    description = stringResource(R.string.onboarding_desc_2),
                    badge = "MULTI-INSTRUMENT"
                )
                else -> OnboardingPageContent(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.onboarding_title_3),
                    description = stringResource(R.string.onboarding_desc_3),
                    badge = "PRIVACY GUARANTEED"
                )
            }
        }

        // Bottom Controls Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (currentPage == index) CyanAccent else SurfaceBorder)
                            .clickable { currentPage = index }
                    )
                }
            }

            // Primary Action Buttons
            if (currentPage < 2) {
                Button(
                    onClick = { currentPage++ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = OledBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text("Next", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                if (hasMicPermission) {
                    // Success Permission Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EmeraldInTune.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = EmeraldInTune.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = EmeraldInTune,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "✅ Microphone Permission Granted! Ready to Tune.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldInTune
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onCompleteOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = OledBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_start_button")
                    ) {
                        Text(
                            text = "Start Tuning Now 🎵",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    if (isPermissionDeniedByUser) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.permission_status_denied),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = stringResource(R.string.permission_denied_help),
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = OledBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_grant_mic_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.onboarding_grant_mic), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isPermissionDeniedByUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOpenSettings,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceBorder,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("onboarding_settings_button")
                        ) {
                            Text(stringResource(R.string.permission_button_settings), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onCompleteOnboarding) {
                        Text("Continue to Tuner in Demo Mode", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badge: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.2.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(CyanAccent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
