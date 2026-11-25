package com.roadlens.app.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ui.navigation.RoadLensTab

/**
 * Bottom navigation bar with primary navigation tabs.
 * Maintained well below 200 lines strictly per project engineering rules.
 */
@Composable
fun RoadLensBottomBar(
    selectedTab: RoadLensTab,
    onTabSelected: (RoadLensTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val brandNavy = Color(0xFF0B1F3A)
    val brandTeal = Color(0xFF0EA5A4)

    NavigationBar(
        containerColor = Color.White,
        contentColor = brandNavy,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        RoadLensTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Text(tab.symbol, fontSize = 21.sp, fontWeight = FontWeight.Bold) },
                label = { Text(tab.title, fontSize = 11.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = brandNavy,
                    selectedTextColor = brandNavy,
                    indicatorColor = brandTeal.copy(alpha = 0.20f),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                )
            )
        }
    }
}
