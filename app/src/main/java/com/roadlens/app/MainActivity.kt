package com.roadlens.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
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
import com.roadlens.app.ui.screens.PlaceholderScreen
import com.roadlens.app.ui.screens.ScanScreen
import com.roadlens.app.ui.theme.RoadLensTheme
import com.roadlens.app.ui.viewmodel.MapViewModel
import com.roadlens.app.ui.viewmodel.ScanViewModel
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
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScanViewModel(yoloDetector, repository) as T
            }
        }
    }

    private val mapViewModel: MapViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(repository) as T
            }
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
    detector: RoadDefectDetector
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(RoadLensTab.HOME) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions updated */ }

    LaunchedEffect(Unit) {
        if (!PermissionHelper.hasAllScanPermissions(context)) {
            permissionLauncher.launch(PermissionHelper.REQUIRED_SCAN_PERMISSIONS)
        }
    }

    if (showPermissionDialog) {
        com.roadlens.app.ui.components.PermissionRequiredDialog(
            context = context,
            onDismiss = { showPermissionDialog = false },
            onRequestPermissions = { permissionLauncher.launch(PermissionHelper.REQUIRED_SCAN_PERMISSIONS) }
        )
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

    Scaffold(
        containerColor = Color(0xFFF3F5EE),
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
                onStartScan = {
                    if (PermissionHelper.hasAllScanPermissions(context)) {
                        selectedTab = RoadLensTab.SCAN
                    } else {
                        showPermissionDialog = true
                    }
                },
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
            RoadLensTab.REPORT -> PlaceholderScreen(
                title = "Road Reports",
                description = "Your road inspection reports and export options will appear here.",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
