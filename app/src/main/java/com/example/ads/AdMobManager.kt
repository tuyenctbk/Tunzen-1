package com.example.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobManager {

    private val _bannerAdsEnabled = MutableStateFlow(false)
    val bannerAdsEnabled: StateFlow<Boolean> = _bannerAdsEnabled.asStateFlow()

    private val _interstitialAdsEnabled = MutableStateFlow(false)
    val interstitialAdsEnabled: StateFlow<Boolean> = _interstitialAdsEnabled.asStateFlow()

    private val _adFrequencyMinutes = MutableStateFlow(15L)
    val adFrequencyMinutes: StateFlow<Long> = _adFrequencyMinutes.asStateFlow()

    fun initRemoteConfigDefaults(context: Context) {
        // Defaults: Ads are disabled by default for optimal user experience.
        // Remote Config can enable banner or interstitial ads when ready.
        _bannerAdsEnabled.value = false
        _interstitialAdsEnabled.value = false
        _adFrequencyMinutes.value = 15L
    }

    fun updateRemoteConfigValues(bannerEnabled: Boolean, interstitialEnabled: Boolean, freqMinutes: Long) {
        _bannerAdsEnabled.value = bannerEnabled
        _interstitialAdsEnabled.value = interstitialEnabled
        _adFrequencyMinutes.value = freqMinutes
    }
}

@Composable
fun PoliteAdBanner(modifier: Modifier = Modifier) {
    val isEnabled by AdMobManager.bannerAdsEnabled.collectAsState()

    if (isEnabled) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .testTag("polite_ad_banner_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SPONSORED ANNOUNCEMENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Support TuneZen free pitch DSP engine",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
