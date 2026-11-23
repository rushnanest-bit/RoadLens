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
import com.roadlens.app.domain.model.ScanOperationalMode
import com.roadlens.app.ui.components.CameraScanSection
import com.roadlens.app.ui.components.DatasetLabSection
import com.roadlens.app.ui.viewmodel.ScanScreenUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Main scanning screen supporting Live Fleet CameraX scan and the isolated Dataset Lab test bench.
 * Strictly under 100 lines per project engineering rules.
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
        // Operational Mode Selector: Live Fleet Scan vs. Dataset Lab
        TabRow(
            selectedTabIndex = uiState.operationalMode.ordinal,
            containerColor = Color.White,
            contentColor = brandGreen,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            ScanOperationalMode.entries.forEach { mode ->
                Tab(
                    selected = uiState.operationalMode == mode,
                    onClick = { viewModel.setOperationalMode(mode) },
                    text = { Text(mode.title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (uiState.operationalMode) {
            ScanOperationalMode.LIVE_SCAN -> CameraScanSection(
                cameraState = uiState.cameraState,
                viewModel = viewModel,
                detector = detector
            )
            ScanOperationalMode.DATASET_LAB -> DatasetLabSection(
                uiState = uiState,
                viewModel = viewModel,
                detector = detector,
                onChooseImage = onChooseImage,
                onChooseVideo = onChooseVideo
            )
        }
    }
}
