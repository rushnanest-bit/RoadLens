package com.roadlens.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.roadlens.app.ui.viewmodel.ImageScanUiState

/**
 * Image inspection UI section for single photo and dataset lab analysis.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun ImageScanSection(
    imageState: ImageScanUiState,
    onChooseImage: () -> Unit
) {
    val brandNavy = Color(0xFF0B1F3A)

    Button(
        onClick = onChooseImage,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Select Road Image from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
    }

    Spacer(Modifier.height(12.dp))
    StatusCard(status = imageState.statusMessage, isLoading = imageState.isScanning)

    imageState.detectionResult?.let { result ->
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
