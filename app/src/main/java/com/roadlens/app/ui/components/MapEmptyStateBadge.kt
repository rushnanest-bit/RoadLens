package com.roadlens.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact, dark-themed badge displayed when no geo-tagged hazards exist in the view.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun MapEmptyStateBadge(
    modifier: Modifier = Modifier
) {
    val cardDark = Color(0xFF18181B)
    val accentGreen = Color(0xFF10B981)
    val textLight = Color(0xFFF8FAFC)
    val textMuted = Color(0xFF94A3B8)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardDark.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        modifier = modifier
            .padding(start = 16.dp, bottom = 20.dp, end = 84.dp)
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("i", fontSize = 13.sp, color = accentGreen, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "No hazards detected in this area",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textLight
                )
                Text(
                    text = "Detected hazards with valid GPS locations will appear here.",
                    fontSize = 10.sp,
                    color = textMuted
                )
            }
        }
    }
}
