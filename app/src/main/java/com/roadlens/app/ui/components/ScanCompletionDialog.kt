package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ui.viewmodel.SessionSummaryData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog displaying inspection completion statistics and a View Report option
 * when a live camera scanning run concludes.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun ScanCompletionDialog(
    summary: SessionSummaryData,
    onViewReport: () -> Unit,
    onDismiss: () -> Unit
) {
    val navy = Color(0xFF0B1F3A)
    val timeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Scan Session Completed", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = navy)
                    Text(summary.vehicleLabel, fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("INSPECTION ROUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${summary.startLocation} → ${summary.stopLocation}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = navy)
                        Spacer(Modifier.height(2.dp))
                        Text(timeFormat.format(Date(summary.completedAt)), fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("DETECTION STATISTICS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 0.5.sp)
                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryMetricBadge("Total Defects", summary.totalDetections.toString(), navy, Modifier.weight(1f))
                    SummaryMetricBadge("Potholes", summary.potholes.toString(), Color(0xFFC46B54), Modifier.weight(1f))
                    SummaryMetricBadge("Cracks", summary.cracks.toString(), Color(0xFFB17A35), Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Session telemetry and verified defect events saved to local database.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewReport,
                colors = ButtonDefaults.buttonColors(containerColor = navy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View Report", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", color = Color(0xFF4B5563))
            }
        }
    )
}

@Composable
private fun SummaryMetricBadge(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
