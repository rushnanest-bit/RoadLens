package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.roadlens.app.data.local.entity.ScanSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pre-scan readiness dashboard displaying YOLO model readiness,
 * a prominent Start New Scan button, and details of the last scan only.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun PreScanDashboard(
    isModelLoaded: Boolean,
    lastSession: ScanSessionEntity?,
    lastSessionHazards: Int = 0,
    lastSessionPotholes: Int = 0,
    lastSessionCracks: Int = 0,
    onStartScan: () -> Unit,
    onViewReport: () -> Unit = {}
) {
    val navy = Color(0xFF0B1F3A)
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FLEET ROAD SCAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
            val (badgeText, badgeCol) = if (isModelLoaded) "YOLO11n Ready" to Color(0xFF10B981) else "Model Offline" to Color(0xFFEF4444)
            Surface(color = badgeCol.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(badgeCol, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeCol)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = navy),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("▶", fontSize = 15.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Start New Scan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("LAST SCAN DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))

        if (lastSession != null) {
            LastScanDetailsCard(
                session = lastSession, hazards = lastSessionHazards, potholes = lastSessionPotholes,
                cracks = lastSessionCracks, timeFormat = timeFormat, onViewReport = onViewReport
            )
        } else {
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No previous scan recorded yet.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Spacer(Modifier.height(4.dp))
                    Text("Details of your latest inspection run will appear here.", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}

@Composable
private fun LastScanDetailsCard(
    session: ScanSessionEntity, hazards: Int, potholes: Int, cracks: Int,
    timeFormat: SimpleDateFormat, onViewReport: () -> Unit
) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(session.vehicleLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0B1F3A))
                    Spacer(Modifier.width(8.dp))
                    val (stColor, stText) = when (session.status) {
                        "COMPLETED" -> Color(0xFF10B981) to "COMPLETED"
                        "PAUSED" -> Color(0xFFF59E0B) to "PAUSED"
                        else -> Color(0xFF3B82F6) to "ACTIVE"
                    }
                    Surface(color = stColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                        Text(stText, color = stColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(timeFormat.format(Date(session.startedAt)), fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
            Spacer(Modifier.height(8.dp))
            val route = "${session.startLocation ?: "Origin"} → ${session.stopLocation ?: "In progress"}"
            Text(route, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ScanStatChip("Total Defects", hazards.toString(), Color(0xFF0B1F3A), Modifier.weight(1f))
                ScanStatChip("Potholes", potholes.toString(), Color(0xFFC46B54), Modifier.weight(1f))
                ScanStatChip("Cracks", cracks.toString(), Color(0xFFB17A35), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "View in Reports ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B1F3A),
                    modifier = Modifier.clickable { onViewReport() }
                )
            }
        }
    }
}

@Composable
private fun ScanStatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
