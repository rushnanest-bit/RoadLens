package com.roadlens.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.ScanInputMode
import com.roadlens.app.domain.util.TimeFormatter
import com.roadlens.app.ui.components.DetectionsListSection
import com.roadlens.app.ui.components.LiveVideoDetectorCanvas
import com.roadlens.app.ui.components.StatusCard
import com.roadlens.app.ui.components.VideoDetectionsListSection
import com.roadlens.app.ui.viewmodel.ScanScreenUiState
import com.roadlens.app.ui.viewmodel.ScanViewModel

/**
 * Main scanning screen supporting both static image inspection and real-time video detection.
 * Strictly kept below 200 lines per project engineering guidelines.
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
    val brandLime = Color(0xFFC7F284)

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
            ScanInputMode.IMAGE -> {
                Button(
                    onClick = onChooseImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select Road Image from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
                StatusCard(status = uiState.imageState.statusMessage, isLoading = uiState.imageState.isScanning)

                uiState.imageState.detectionResult?.let { result ->
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = result.annotatedBitmap.asImageBitmap(),
                            contentDescription = "Annotated Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    DetectionsListSection(detections = result.detections)
                }
            }

            ScanInputMode.VIDEO -> {
                Button(
                    onClick = onChooseVideo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select Road Video from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
                StatusCard(status = uiState.videoState.statusMessage, isLoading = false)

                uiState.videoState.selectedVideoUri?.let { uri ->
                    Spacer(Modifier.height(12.dp))
                    LiveVideoDetectorCanvas(
                        videoUri = uri,
                        detector = detector,
                        isPlaying = uiState.videoState.isPlaying,
                        seekToMs = uiState.videoState.seekToTargetMs,
                        onPlaybackStateChanged = { playing, cur, dur -> viewModel.updatePlaybackPosition(playing, cur, dur) },
                        onFrameAnalyzed = { dets, time -> viewModel.processAnalyzedFrame(dets, time) }
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(TimeFormatter.formatMmSs(uiState.videoState.currentPositionMs), fontSize = 12.sp, color = Color(0xFF6B7280))
                        Slider(
                            value = uiState.videoState.currentPositionMs.toFloat(),
                            onValueChange = { viewModel.seekVideo(it.toLong()) },
                            valueRange = 0f..uiState.videoState.durationMs.toFloat().coerceAtLeast(1f),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = brandGreen, activeTrackColor = brandLime)
                        )
                        Text(TimeFormatter.formatMmSs(uiState.videoState.durationMs), fontSize = 12.sp, color = Color(0xFF6B7280))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { viewModel.stepVideo(-3000L) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB))) {
                            Text("-3s", color = Color(0xFF1F2937))
                        }
                        Button(onClick = { viewModel.toggleVideoPlay() }, colors = ButtonDefaults.buttonColors(containerColor = brandGreen)) {
                            Text(if (uiState.videoState.isPlaying) "Pause" else "Play", color = Color.White)
                        }
                        Button(onClick = { viewModel.stepVideo(3000L) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB))) {
                            Text("+3s", color = Color(0xFF1F2937))
                        }
                        Button(onClick = { viewModel.resetVideo() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2))) {
                            Text("Reset", color = Color(0xFFDC2626))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    VideoDetectionsListSection(
                        defectTimeline = uiState.videoState.defectTimeline,
                        currentTimeMs = uiState.videoState.currentPositionMs,
                        onSeekTo = { viewModel.seekVideo(it) }
                    )
                }
            }
        }
    }
}
