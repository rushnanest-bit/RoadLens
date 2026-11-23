package com.roadlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.ui.navigation.RoadLensTab

/**
 * Bottom navigation bar with primary navigation tabs and a 3-dot overflow menu for fleet actions.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun RoadLensBottomBar(
    selectedTab: RoadLensTab,
    onTabSelected: (RoadLensTab) -> Unit,
    onAddNewVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val brandGreen = Color(0xFF1E3A2F)
    val brandLime = Color(0xFFC7F284)

    NavigationBar(
        containerColor = Color.White,
        contentColor = brandGreen,
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
                    selectedIconColor = brandGreen,
                    selectedTextColor = brandGreen,
                    indicatorColor = brandLime.copy(alpha = 0.35f),
                    unselectedIconColor = Color(0xFF9CA3AF),
                    unselectedTextColor = Color(0xFF9CA3AF)
                )
            )
        }

        // 3-dot overflow menu item
        NavigationBarItem(
            selected = false,
            onClick = { isMenuExpanded = true },
            icon = {
                Box {
                    Text("⋮", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add New Vehicle", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = brandGreen) },
                            onClick = {
                                isMenuExpanded = false
                                onAddNewVehicle()
                            }
                        )
                    }
                }
            },
            label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                indicatorColor = brandLime.copy(alpha = 0.35f),
                unselectedIconColor = Color(0xFF9CA3AF),
                unselectedTextColor = Color(0xFF9CA3AF)
            )
        )
    }
}
