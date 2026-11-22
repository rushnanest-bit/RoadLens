package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.ui.viewmodel.ScanScreenUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Dataset Lab evaluation interface for testing images and replaying pre-recorded videos indoors.
 * Clearly isolates test data from live fleet metrics per Project Blueprint 7.3.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun DatasetLabSection(
    uiState: ScanScreenUiState,
    viewModel: ScanViewModel,
    detector: RoadDefectDetector,
    onChooseImage: () -> Unit,
    onChooseVideo: () -> Unit
) {
    val brandGreen = Color(0xFF1E3A2F)
    val testAmber = Color(0xFFB45309)
    val testAmberBg = Color(0xFFFEF3C7)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Test Mode Notification Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(testAmberBg, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🧪 Dataset Lab & Indoor Benchmarks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = testAmber
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Indoor model test mode. Media analysed here verifies detection accuracy without contaminating live fleet route records.",
                    fontSize = 12.sp,
                    color = Color(0xFF78350F),
                    lineHeight = 16.sp
                )
            }
        }

        // Sub-Mode Tabs (Static Image vs Video Replay)
        TabRow(
            selectedTabIndex = uiState.labSubMode.ordinal,
            containerColor = Color.White,
            contentColor = brandGreen,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            DatasetLabSubMode.entries.forEach { subMode ->
                Tab(
                    selected = uiState.labSubMode == subMode,
                    onClick = { viewModel.setLabSubMode(subMode) },
                    text = { Text("${subMode.icon} ${subMode.title}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        when (uiState.labSubMode) {
            DatasetLabSubMode.IMAGE -> ImageScanSection(
                imageState = uiState.imageState,
                onChooseImage = onChooseImage
            )
            DatasetLabSubMode.VIDEO -> VideoScanSection(
                videoState = uiState.videoState,
                viewModel = viewModel,
                detector = detector,
                onChooseVideo = onChooseVideo
            )
        }
    }
}
