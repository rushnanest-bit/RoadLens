package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import com.roadlens.app.domain.model.SessionDetailReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Inspection session defect drilldown dialog.
 * Maintained strictly under 200 lines per project engineering rules.
 */
@Composable
fun SessionDetailDialog(
    detail: SessionDetailReport,
    onDismiss: () -> Unit
) {
    val brandNavy = Color(0xFF0B1F3A)
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(detail.session.vehicleLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = brandNavy)
                val start = detail.session.startLocation ?: "Origin"
                val stop = detail.session.stopLocation ?: "In progress"
                Text("$start → $stop", fontSize = 13.sp, color = Color(0xFF6B7280))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Logged", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${detail.events.size} hazards", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = brandNavy)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Started", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text(timeFormat.format(Date(detail.session.startedAt)), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF374151))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (detail.events.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No road defects recorded in this session.", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(detail.events) { ev ->
                            val chipColor = if (ev.classId == 3) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                            val textColor = if (ev.classId == 3) Color(0xFF991B1B) else Color(0xFF92400E)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(color = chipColor, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            ev.className,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )
                                    }
                                    val lat = ev.latitude?.let { String.format(Locale.US, "%.5f", it) } ?: "N/A"
                                    val lon = ev.longitude?.let { String.format(Locale.US, "%.5f", it) } ?: "N/A"
                                    Text("GPS: $lat, $lon", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                }
                                Text(
                                    "${(ev.confidence * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = brandNavy
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
