package com.roadlens.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.domain.util.TimeFormatter
import java.util.Locale

/**
 * Inspection card displayed when tapping a road hazard map marker.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun HazardDetailCard(
    hazard: DetectionEventEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandGreen = Color(0xFF1E3A2F)
    val statusColor = if (hazard.reviewStatus == "CONFIRMED") Color(0xFF16A34A) else Color(0xFFD97706)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hazard.className, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandGreen)
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = hazard.reviewStatus,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            val confText = String.format(Locale.US, "%.1f%% Confidence", hazard.confidence * 100)
            val coordText = String.format(Locale.US, "%.5f, %.5f (%s)", hazard.latitude ?: 0.0, hazard.longitude ?: 0.0, hazard.locationSource)
            Text(confText, fontSize = 13.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("Coordinates: $coordText", fontSize = 12.sp, color = Color(0xFF6B7280))
            Spacer(Modifier.height(4.dp))
            Text("Logged: ${TimeFormatter.formatMmSs(hazard.capturedAt)}", fontSize = 12.sp, color = Color(0xFF6B7280))

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close Details", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
