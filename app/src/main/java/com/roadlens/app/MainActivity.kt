package com.roadlens.app

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roadlens.app.ai.OnnxYoloDetector
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.data.local.RoadLensDatabase
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.domain.model.ScanOperationalMode
import com.roadlens.app.domain.util.PermissionHelper
import com.roadlens.app.ui.components.RoadLensBottomBar
import com.roadlens.app.ui.navigation.RoadLensTab
import com.roadlens.app.ui.screens.HazardMapScreen
import com.roadlens.app.ui.screens.HomeScreen
import com.roadlens.app.ui.screens.ReportScreen
import com.roadlens.app.ui.screens.ScanScreen
import com.roadlens.app.ui.screens.SplashScreen
import com.roadlens.app.ui.theme.RoadLensTheme
import com.roadlens.app.ui.viewmodel.MapViewModel
import com.roadlens.app.ui.viewmodel.ReportViewModel
import com.roadlens.app.ui.viewmodel.ScanViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre

/**
 * Main application entry point for RoadLens.
 * Maintained well below 200 lines strictly per project engineering rules.
 */
class MainActivity : ComponentActivity() {

    private lateinit var yoloDetector: RoadDefectDetector
    private lateinit var repository: RoadInspectionRepository

    private val scanViewModel: ScanViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ScanViewModel(yoloDetector, repository) as T
        }
    }

    private val mapViewModel: MapViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MapViewModel(repository) as T
        }
    }

    private val reportViewModel: ReportViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportViewModel(repository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        val database = RoadLensDatabase.getInstance(applicationContext)
        repository = RoadInspectionRepository(database)
        yoloDetector = OnnxYoloDetector(applicationContext)

        setContent {
            RoadLensTheme {
                RoadLensApp(
                    viewModel = scanViewModel,
                    mapViewModel = mapViewModel,
                    reportViewModel = reportViewModel,
                    detector = yoloDetector
                )
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
    mapViewModel: MapViewModel,
    reportViewModel: ReportViewModel,
    detector: RoadDefectDetector
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(RoadLensTab.HOME) }
    var isSplashVisible by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions updated */ }

    LaunchedEffect(Unit) {
        delay(1800)
        isSplashVisible = false
    }

    LaunchedEffect(isSplashVisible) {
        if (!isSplashVisible && !PermissionHelper.hasAllScanPermissions(context)) {
            permissionLauncher.launch(PermissionHelper.REQUIRED_SCAN_PERMISSIONS)
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = context.contentResolver.openInputStream(it)?.use { s -> BitmapFactory.decodeStream(s) }
            viewModel.setOperationalMode(ScanOperationalMode.DATASET_LAB)
            viewModel.setLabSubMode(DatasetLabSubMode.IMAGE)
            viewModel.analyzeImage(bitmap)
            selectedTab = RoadLensTab.SCAN
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.setOperationalMode(ScanOperationalMode.DATASET_LAB)
            viewModel.setLabSubMode(DatasetLabSubMode.VIDEO)
            viewModel.selectVideo(it)
            selectedTab = RoadLensTab.SCAN
        }
    }

    if (isSplashVisible) {
        SplashScreen()
    } else {
        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            bottomBar = {
                RoadLensBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
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
                RoadLensTab.MAP -> HazardMapScreen(
                    viewModel = mapViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                RoadLensTab.REPORT -> ReportScreen(
                    viewModel = reportViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
