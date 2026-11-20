package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ai.YoloConstants.CLASS_COLORS
import com.roadlens.app.domain.model.RoadDetection
import java.util.Locale

/**
 * Renders the detected road defects breakdown for static image scans.
 */
@Composable
fun DetectionsListSection(
    detections: List<RoadDetection>,
    title: String = "Detected Defects"
) {
    if (detections.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$title (${detections.size})",
            color = Color(0xFF1F2937),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        detections.forEachIndexed { index, detection ->
            val color = Color(CLASS_COLORS.getOrElse(detection.classIndex) { android.graphics.Color.RED })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text(
                                text = "#${index + 1} ${detection.className}",
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Box: [${detection.left.toInt()}, ${detection.top.toInt()} - ${detection.right.toInt()}, ${detection.bottom.toInt()}]",
                                color = Color(0xFF6B7280),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", detection.confidence * 100)}%",
                        color = color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
