package com.roadlens.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ui.components.OverviewCard
import com.roadlens.app.ui.viewmodel.OverviewMetricsUiState

/**
 * Main dashboard screen displaying fleet inspection statistics and quick scan actions.
 * Maintained under 150 lines per project engineering guidelines.
 */
@Composable
fun HomeScreen(
    metrics: OverviewMetricsUiState,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandGreen = Color(0xFF1E3A2F)
    val brandLime = Color(0xFFC7F284)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ROADLENS", color = brandGreen, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("FLEET INTELLIGENCE", color = Color(0xFF6B7280), fontSize = 9.sp, letterSpacing = 2.sp)
            }
            Surface(shape = CircleShape, color = brandGreen, modifier = Modifier.size(45.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("R", color = brandLime, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Road Defect Overview", color = Color(0xFF1F2937), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("ALL TIME", color = Color(0xFF6B7280), fontSize = 10.sp, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(15.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewCard("Total scans", metrics.totalScans.toString(), "◉", brandGreen, Modifier.weight(1f))
            OverviewCard("Road hazards", metrics.roadHazards.toString(), "⌖", Color(0xFFB17A35), Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewCard("Potholes", metrics.potholes.toString(), "!", Color(0xFFC46B54), Modifier.weight(1f))
            OverviewCard("Pending review", metrics.pendingReview.toString(), "◷", Color(0xFF738A59), Modifier.weight(1f))
        }

        Spacer(Modifier.height(25.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(brandGreen, Color(0xFF132A22))),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text("Ready to scan\nthe roads?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text("Analyze images or run real-time hardware video detection.", color = Color(0xFFD1D5DB), fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onStartScan,
                    colors = ButtonDefaults.buttonColors(containerColor = brandLime, contentColor = brandGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Start scanning", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(25.dp))
    }
}
