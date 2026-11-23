package com.roadlens.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.domain.model.MapFilterType

/**
 * Top floating filter overlay containing the Road Monitoring title and category chips.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun MapTopFilterBar(
    hazardCount: Int,
    currentFilter: MapFilterType,
    onFilterSelected: (MapFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryGreen = Color(0xFF173D32)
    val secondaryGreen = Color(0xFFE8F5D9)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Road Monitoring",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202923)
                )
                Surface(shape = RoundedCornerShape(10.dp), color = secondaryGreen) {
                    Text(
                        text = "$hazardCount ${if (hazardCount == 1) "hazard" else "hazards"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                val chips = listOf(
                    MapFilterType.ALL,
                    MapFilterType.POTHOLES,
                    MapFilterType.CRACKS,
                    MapFilterType.OTHER
                )
                chips.forEach { f ->
                    FilterChip(
                        selected = currentFilter == f,
                        onClick = { onFilterSelected(f) },
                        label = { Text(f.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF7F8F6),
                            labelColor = Color(0xFF78827D)
                        )
                    )
                }
            }
        }
    }
}
