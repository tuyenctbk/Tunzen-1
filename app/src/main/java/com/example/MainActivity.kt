package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.ads.AdMobManager
import com.example.ui.MainViewModel
import com.example.ui.screens.ChordsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.OledBackground
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TuneZenTheme

enum class TuneZenTab(val labelResId: Int, val icon: ImageVector) {
    TUNER(R.string.tab_tuner, Icons.Default.GraphicEq),
    TOOLS(R.string.tab_tools, Icons.Default.Speed),
    PRESETS(R.string.tab_presets, Icons.Default.LibraryMusic),
    SETTINGS(R.string.tab_settings, Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AdMobManager.initRemoteConfigDefaults(this)

        setContent {
            val isPaperLightMode by viewModel.isPaperLightMode.collectAsState()
            TuneZenTheme(isPaperLightMode = isPaperLightMode) {
                TuneZenApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TuneZenApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isPermissionDeniedByUser by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED &&
            (context as? ComponentActivity)?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        isPermissionDeniedByUser = !isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Auto-check permission when app returns to foreground from settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                hasMicPermission = granted
                if (granted) {
                    isPermissionDeniedByUser = false
                } else {
                    isPermissionDeniedByUser = (context as? ComponentActivity)
                        ?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val onOpenSettings = {
        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(hasMicPermission) {
        if (hasMicPermission) {
            viewModel.startListening()
        }
    }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            hasMicPermission = hasMicPermission,
            isPermissionDeniedByUser = isPermissionDeniedByUser,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onOpenSettings = onOpenSettings,
            onCompleteOnboarding = { viewModel.completeOnboarding() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = OledBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceCard,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("tunezen_bottom_navigation_bar")
                ) {
                    TuneZenTab.entries.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        val title = stringResource(tab.labelResId)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = title
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OledBackground,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (TuneZenTab.entries[selectedTab]) {
                    TuneZenTab.TUNER -> TunerScreen(
                        viewModel = viewModel,
                        hasMicPermission = hasMicPermission,
                        isPermissionDeniedByUser = isPermissionDeniedByUser,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onOpenSettings = onOpenSettings,
                        onNavigateToPresets = { selectedTab = 2 }
                    )
                    TuneZenTab.TOOLS -> ToolsScreen(
                        viewModel = viewModel
                    )
                    TuneZenTab.PRESETS -> PresetsScreen(
                        viewModel = viewModel,
                        onPresetSelected = { selectedTab = 0 }
                    )
                    TuneZenTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
