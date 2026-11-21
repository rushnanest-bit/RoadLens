package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.roadlens.app.domain.model.VideoDefectRecord
import java.util.Locale
import kotlin.math.abs

/**
 * Chronological timeline list of unique road defects detected across video frames.
 * Allows tapping an item to seek the video player directly to that defect's occurrence timestamp.
 */
@Composable
fun VideoDetectionsListSection(
    defectTimeline: List<VideoDefectRecord>,
    currentTimeMs: Long,
    onSeekTo: (Long) -> Unit,
    title: String = "Defects Timeline"
) {
    if (defectTimeline.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$title (${defectTimeline.size})",
            color = Color(0xFF1F2937),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        defectTimeline.forEach { record ->
            val isCurrent = abs(currentTimeMs - record.timestampMs) < 600L
            val color = Color(CLASS_COLORS.getOrElse(record.classIndex) { android.graphics.Color.RED })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        color = if (isCurrent) Color(0xFFEFF6FF) else Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSeekTo(record.timestampMs) }
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
                                text = "#${record.id} ${record.className}",
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "At ${record.timeFormatted} (Tap to view)",
                                color = Color(0xFF6B7280),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = String.format(Locale.US, "%.2f", record.confidence),
                        color = color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
