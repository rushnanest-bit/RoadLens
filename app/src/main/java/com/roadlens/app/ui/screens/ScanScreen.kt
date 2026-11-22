package com.roadlens.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.ScanInputMode
import com.roadlens.app.ui.components.CameraScanSection
import com.roadlens.app.ui.components.ImageScanSection
import com.roadlens.app.ui.components.VideoScanSection
import com.roadlens.app.ui.viewmodel.ScanScreenUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Main scanning screen supporting Live CameraX scan, Image inspection, and Video replay.
 * Maintained under 100 lines strictly per project engineering rules.
 */
@Composable
fun ScanScreen(
    uiState: ScanScreenUiState,
    viewModel: ScanViewModel,
    detector: RoadDefectDetector,
    onChooseImage: () -> Unit,
    onChooseVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandGreen = Color(0xFF1E3A2F)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = uiState.inputMode.ordinal,
            containerColor = Color.White,
            contentColor = brandGreen,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            ScanInputMode.entries.forEach { mode ->
                Tab(
                    selected = uiState.inputMode == mode,
                    onClick = { viewModel.setInputMode(mode) },
                    text = { Text("${mode.icon} ${mode.title}", fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (uiState.inputMode) {
            ScanInputMode.CAMERA -> CameraScanSection(
                cameraState = uiState.cameraState,
                viewModel = viewModel,
                detector = detector
            )
            ScanInputMode.IMAGE -> ImageScanSection(
                imageState = uiState.imageState,
                onChooseImage = onChooseImage
            )
            ScanInputMode.VIDEO -> VideoScanSection(
                videoState = uiState.videoState,
                viewModel = viewModel,
                detector = detector,
                onChooseVideo = onChooseVideo
            )
        }
    }
}
