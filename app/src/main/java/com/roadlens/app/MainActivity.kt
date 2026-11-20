package com.roadlens.app

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roadlens.app.ai.OnnxYoloDetector
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.ui.navigation.RoadLensTab
import com.roadlens.app.ui.screens.HomeScreen
import com.roadlens.app.ui.screens.PlaceholderScreen
import com.roadlens.app.ui.screens.ScanScreen
import com.roadlens.app.ui.theme.RoadLensTheme
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Main application entry point for RoadLens.
 * Maintained well below 200 lines strictly per project engineering rules.
 */
class MainActivity : ComponentActivity() {

    private lateinit var yoloDetector: RoadDefectDetector

    private val scanViewModel: ScanViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScanViewModel(yoloDetector) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        yoloDetector = OnnxYoloDetector(applicationContext)

        setContent {
            RoadLensTheme {
                RoadLensApp(viewModel = scanViewModel, detector = yoloDetector)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::yoloDetector.isInitialized) {
            yoloDetector.close()
        }
    }
}

@Composable
fun RoadLensApp(
    viewModel: ScanViewModel,
    detector: RoadDefectDetector
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(RoadLensTab.HOME) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = context.contentResolver.openInputStream(it)?.use { s -> BitmapFactory.decodeStream(s) }
            viewModel.analyzeImage(bitmap)
            selectedTab = RoadLensTab.SCAN
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.selectVideo(it)
            selectedTab = RoadLensTab.SCAN
        }
    }

    val brandGreen = Color(0xFF1E3A2F)
    val brandLime = Color(0xFFC7F284)

    Scaffold(
        containerColor = Color(0xFFF3F5EE),
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = brandGreen, tonalElevation = 8.dp) {
                RoadLensTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.symbol, fontSize = 21.sp, fontWeight = FontWeight.Bold) },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = brandGreen, selectedTextColor = brandGreen,
                            indicatorColor = brandLime.copy(alpha = 0.35f), unselectedIconColor = Color(0xFF9CA3AF), unselectedTextColor = Color(0xFF9CA3AF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            RoadLensTab.HOME -> HomeScreen(
                metrics = uiState.metricsState,
                onStartScan = { selectedTab = RoadLensTab.SCAN },
                modifier = Modifier.padding(innerPadding)
            )
            RoadLensTab.SCAN -> ScanScreen(
                uiState = uiState,
                viewModel = viewModel,
                detector = detector,
                onChooseImage = { imageLauncher.launch("image/*") },
                onChooseVideo = { videoLauncher.launch("video/*") },
                modifier = Modifier.padding(innerPadding)
            )
            RoadLensTab.MAP -> PlaceholderScreen(
                title = "Hazard Map",
                description = "Detected road hazards will be displayed on the map here.",
                modifier = Modifier.padding(innerPadding)
            )
            RoadLensTab.REPORT -> PlaceholderScreen(
                title = "Road Reports",
                description = "Your road inspection reports and export options will appear here.",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
