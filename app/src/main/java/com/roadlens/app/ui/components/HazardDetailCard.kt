package com.roadlens.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.data.local.entity.DetectionEventEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern inspection bottom sheet displaying geo-tagged road hazard details and review actions.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun HazardDetailCard(
    hazard: DetectionEventEntity,
    onDismiss: () -> Unit,
    onUpdateReview: (status: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val brandGreen = Color(0xFF173D32)
    val textPrimary = Color(0xFF202923)
    val textSecondary = Color(0xFF78827D)

    val categoryColor = when (hazard.classId) {
        3 -> Color(0xFFF59E0B) // Pothole - Amber
        in 0..2 -> Color(0xFFEF4444) // Cracks - Red
        else -> Color(0xFF8B5CF6) // Other - Purple
    }

    val statusColor = when (hazard.reviewStatus) {
        "CONFIRMED" -> Color(0xFF16A34A)
        "DISMISSED" -> Color(0xFF9CA3AF)
        else -> Color(0xFFD97706)
    }

    val formattedDate = remember(hazard.capturedAt) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(hazard.capturedAt))
    }

    val imageBitmap = remember(hazard.evidencePath) {
        hazard.evidencePath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.height(14.dp))

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = hazard.className,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(categoryColor))
                    Spacer(Modifier.width(8.dp))
                    Text(hazard.className, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }
                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = hazard.reviewStatus,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF7F8F6), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("Confidence", fontSize = 10.sp, color = textSecondary)
                        Text(String.format(Locale.US, "%.1f%%", hazard.confidence * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = brandGreen)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF7F8F6), modifier = Modifier.weight(2f)) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("Coordinates (${hazard.locationSource})", fontSize = 10.sp, color = textSecondary)
                        Text(String.format(Locale.US, "%.5f, %.5f", hazard.latitude ?: 0.0, hazard.longitude ?: 0.0), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Logged: $formattedDate", fontSize = 11.sp, color = textSecondary)

            Spacer(Modifier.height(16.dp))
            if (hazard.reviewStatus == "PENDING") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onUpdateReview("CONFIRMED") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Defect", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onUpdateReview("DISMISSED") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dismiss", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 16.sp, color = textSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Details", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
