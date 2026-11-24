package com.roadlens.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.data.location.LocationTracker
import com.roadlens.app.domain.util.PermissionHelper
import com.roadlens.app.ui.viewmodel.CameraScanUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Live CameraX scan UI section with fused GPS tracking, model readiness, and telemetry.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun CameraScanSection(
    cameraState: CameraScanUiState,
    viewModel: ScanViewModel,
    detector: RoadDefectDetector
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locationTracker = remember { LocationTracker(context) }
    val location by locationTracker.currentLocation.collectAsState()

    var hasPermissions by remember { mutableStateOf(PermissionHelper.hasAllScanPermissions(context)) }
    var isSettingsMode by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermissions = PermissionHelper.hasAllScanPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermissions = PermissionHelper.hasAllScanPermissions(context)
        if (!hasPermissions) {
            isSettingsMode = true
            showPermissionDialog = true
        }
    }

    DisposableEffect(cameraState.isScanning, cameraState.isPaused) {
        if (cameraState.isScanning && !cameraState.isPaused) locationTracker.startTracking() else locationTracker.stopTracking()
        onDispose { locationTracker.stopTracking() }
    }

    var showStartDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        PermissionRequiredDialog(
            isSettingsMode = isSettingsMode,
            onDismiss = { showPermissionDialog = false },
            onRequestPermissions = { permissionLauncher.launch(PermissionHelper.REQUIRED_SCAN_PERMISSIONS) },
            onOpenSettings = { PermissionHelper.openAppSettings(context) }
        )
    }

    val (brandGreen, amberPause, dangerRed) = Triple(Color(0xFF1E3A2F), Color(0xFFD97706), Color(0xFFDC2626))

    if (showStartDialog) {
        val currentVehicle = viewModel.uiState.collectAsState().value.vehicleRegistration
        StartScanDialog(
            initialVehicle = currentVehicle,
            onDismiss = { showStartDialog = false },
            onConfirm = { v, l -> viewModel.startCameraScan(v, l); showStartDialog = false }
        )
    }

    if (showStopDialog) {
        StopScanDialog(
            totalDetections = cameraState.totalDetectionsInSession,
            onDismiss = { showStopDialog = false },
            onConfirm = { l -> viewModel.stopCameraScan(l); showStopDialog = false }
        )
    }

    if (hasPermissions) {
        CameraXLiveScannerView(
            detector = detector,
            isScanningActive = cameraState.isScanning && !cameraState.isPaused,
            onFrameAnalyzed = { dets, time ->
                val loc = locationTracker.currentLocation.value
                viewModel.processCameraFrame(dets, time, loc?.latitude, loc?.longitude, loc?.accuracy)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF132A22))
                .clickable { isSettingsMode = false; showPermissionDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("◉", fontSize = 28.sp, color = Color(0xFF9CA3AF))
                Text("Camera & GPS inactive • Tap to grant access", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
    }

    Spacer(Modifier.height(10.dp))
    val (gpsLabel, gpsColor) = when {
        location == null -> "GPS: Acquiring satellite fix..." to Color(0xFF6B7280)
        (location?.accuracy ?: 99f) <= 25f -> "GPS: Locked (${location?.accuracy?.toInt()}m accuracy)" to Color(0xFF16A34A)
        else -> "GPS: Weak fix (${location?.accuracy?.toInt()}m)" to Color(0xFFD97706)
    }
    Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = gpsColor, shape = RoundedCornerShape(4.dp), modifier = Modifier.size(8.dp)) {}
            Spacer(Modifier.width(8.dp))
            Text(gpsLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gpsColor)
        }
    }

    Spacer(Modifier.height(10.dp))
    StatusCard(status = cameraState.statusMessage, isLoading = cameraState.isScanning && !cameraState.isPaused)

    Spacer(Modifier.height(12.dp))
    if (!cameraState.isScanning) {
        Button(
            onClick = {
                if (!PermissionHelper.hasAllScanPermissions(context)) {
                    isSettingsMode = false
                    showPermissionDialog = true
                } else if (viewModel.checkModelAvailability()) {
                    showStartDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start Live Scan (1 FPS)", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { if (cameraState.isPaused) viewModel.resumeCameraScan() else viewModel.pauseCameraScan() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (cameraState.isPaused) brandGreen else amberPause),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (cameraState.isPaused) "Resume Scan" else "Pause Scan", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { showStopDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = dangerRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Stop Scan", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OverviewCard("Potholes", cameraState.potholesInSession.toString(), "!", Color(0xFFC46B54), Modifier.weight(1f))
        OverviewCard("Cracks", cameraState.cracksInSession.toString(), "⌖", Color(0xFFB17A35), Modifier.weight(1f))
    }

    if (cameraState.liveDetections.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        DetectionsListSection(detections = cameraState.liveDetections)
    }
}