package com.roadlens.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.util.TimeFormatter
import com.roadlens.app.ui.viewmodel.ScanViewModel
import com.roadlens.app.ui.viewmodel.VideoScanUiState

/**
 * Pre-recorded video playback and defect timeline section.
 * Maintained well below 200 lines per project engineering rules.
 */
@Composable
fun VideoScanSection(
    videoState: VideoScanUiState,
    viewModel: ScanViewModel,
    detector: RoadDefectDetector,
    onChooseVideo: () -> Unit
) {
    val brandNavy = Color(0xFF0B1F3A)
    val brandTeal = Color(0xFF0EA5A4)

    Button(
        onClick = onChooseVideo,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Select Road Video from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
    }

    Spacer(Modifier.height(12.dp))
    StatusCard(status = videoState.statusMessage, isLoading = false)

    videoState.selectedVideoUri?.let { uri ->
        Spacer(Modifier.height(12.dp))
        LiveVideoDetectorCanvas(
            videoUri = uri,
            detector = detector,
            isPlaying = videoState.isPlaying,
            seekToMs = videoState.seekToTargetMs,
            onPlaybackStateChanged = { playing, cur, dur -> viewModel.updatePlaybackPosition(playing, cur, dur) },
            onFrameAnalyzed = { dets, time -> viewModel.processAnalyzedFrame(dets, time) }
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(TimeFormatter.formatMmSs(videoState.currentPositionMs), fontSize = 12.sp, color = Color(0xFF6B7280))
            Slider(
                value = videoState.currentPositionMs.toFloat(),
                onValueChange = { viewModel.seekVideo(it.toLong()) },
                valueRange = 0f..videoState.durationMs.toFloat().coerceAtLeast(1f),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(thumbColor = brandNavy, activeTrackColor = brandTeal)
            )
            Text(TimeFormatter.formatMmSs(videoState.durationMs), fontSize = 12.sp, color = Color(0xFF6B7280))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.stepVideo(-3000L) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB))) {
                Text("-3s", color = Color(0xFF1F2937))
            }
            Button(onClick = { viewModel.toggleVideoPlay() }, colors = ButtonDefaults.buttonColors(containerColor = brandNavy)) {
                Text(if (videoState.isPlaying) "Pause" else "Play", color = Color.White)
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
            defectTimeline = videoState.defectTimeline,
            currentTimeMs = videoState.currentPositionMs,
            onSeekTo = { viewModel.seekVideo(it) }
        )
    }
}
