package com.roadlens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.domain.model.SessionReportItem
import com.roadlens.app.domain.util.ReportExportHelper
import com.roadlens.app.ui.components.SessionDetailDialog
import com.roadlens.app.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise road inspection reporting screen with history list and CSV export.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessions by viewModel.sessionReports.collectAsState()
    val rawSessions by viewModel.rawSessions.collectAsState()
    val rawEvents by viewModel.rawEvents.collectAsState()
    val selectedDetail by viewModel.selectedSessionDetail.collectAsState()

    val brandNavy = Color(0xFF0B1F3A)

    selectedDetail?.let { detail ->
        SessionDetailDialog(detail = detail, onDismiss = { viewModel.selectSession(null) })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Road Reports", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = brandNavy)
                Text("Fleet road inspection logs & summaries", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            Button(
                onClick = {
                    if (rawSessions.isNotEmpty() || rawEvents.isNotEmpty()) {
                        val csv = ReportExportHelper.generateCsv(rawSessions, rawEvents)
                        ReportExportHelper.shareCsvReport(context, csv)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
                shape = RoundedCornerShape(10.dp),
                enabled = rawEvents.isNotEmpty() || rawSessions.isNotEmpty()
            ) {
                Text("Export CSV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReportMetricItem("Total Drives", sessions.size.toString(), Color(0xFF0B1F3A))
                ReportMetricItem("Logged Hazards", rawEvents.size.toString(), Color(0xFF0EA5A4))
                val potholes = rawEvents.count { it.classId == 3 }
                ReportMetricItem("Potholes", potholes.toString(), Color(0xFFF59E0B))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("INSPECTION SESSIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
        Spacer(Modifier.height(8.dp))

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No scanning sessions recorded yet.\nStart a live road scan to log data.", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(sessions) { item ->
                    SessionCard(item = item, onClick = { viewModel.selectSession(item.session) })
                }
            }
        }
    }
}

@Composable
private fun ReportMetricItem(label: String, value: String, valueColor: Color = Color(0xFF0B1F3A)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = valueColor)
        Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun SessionCard(item: SessionReportItem, onClick: () -> Unit) {
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val start = item.session.startLocation ?: "Origin"
    val stop = item.session.stopLocation ?: "In progress"

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.session.vehicleLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0B1F3A))
                Surface(
                    color = if (item.session.status == "COMPLETED") Color(0xFFCCFBF1) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        item.session.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.session.status == "COMPLETED") Color(0xFF0F766E) else Color(0xFFB45309)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("$start → $stop", fontSize = 12.sp, color = Color(0xFF4B5563))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeFormat.format(Date(item.session.startedAt)), fontSize = 11.sp, color = Color(0xFF9CA3AF))
                Text("${item.totalHazards} defects (${item.potholes} potholes, ${item.cracks} cracks)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0B1F3A))
            }
        }
    }
}
