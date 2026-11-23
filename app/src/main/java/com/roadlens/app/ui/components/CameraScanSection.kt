package com.roadlens.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.ui.viewmodel.CameraScanUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Live CameraX scan UI section with model readiness gating, route session dialogs, and pause controls.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun CameraScanSection(
    cameraState: CameraScanUiState,
    viewModel: ScanViewModel,
    detector: RoadDefectDetector
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
    }

    var showStartDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }

    val brandGreen = Color(0xFF1E3A2F)
    val amberPause = Color(0xFFD97706)
    val dangerRed = Color(0xFFDC2626)

    if (showStartDialog) {
        val currentVehicle = viewModel.uiState.collectAsState().value.vehicleRegistration
        StartScanDialog(
            initialVehicle = currentVehicle,
            onDismiss = { showStartDialog = false },
            onConfirm = { vehicle, startLoc ->
                viewModel.startCameraScan(vehicle, startLoc)
                showStartDialog = false
            }
        )
    }

    if (showStopDialog) {
        StopScanDialog(
            totalDetections = cameraState.totalDetectionsInSession,
            onDismiss = { showStopDialog = false },
            onConfirm = { stopLoc ->
                viewModel.stopCameraScan(stopLoc)
                showStopDialog = false
            }
        )
    }

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Camera Permission Required", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = brandGreen)
            Text("RoadLens requires camera access to scan road defects in real time.", fontSize = 13.sp, color = Color(0xFF4B5563))
            Button(
                onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)) },
                colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Grant Camera Permission", color = Color.White)
            }
        }
    } else {
        CameraXLiveScannerView(
            detector = detector,
            isScanningActive = cameraState.isScanning && !cameraState.isPaused,
            onFrameAnalyzed = { dets, time -> viewModel.processCameraFrame(dets, time) }
        )

        Spacer(Modifier.height(12.dp))
        StatusCard(status = cameraState.statusMessage, isLoading = cameraState.isScanning && !cameraState.isPaused)

        Spacer(Modifier.height(12.dp))
        if (!cameraState.isScanning) {
            Button(
                onClick = {
                    if (viewModel.checkModelAvailability()) {
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
}
