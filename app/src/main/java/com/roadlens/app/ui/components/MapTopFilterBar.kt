package com.roadlens.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.domain.model.MapDefectFilter
import com.roadlens.app.domain.model.MapStatusFilter

/**
 * Top overlay styled with RoadLens navy/teal theme with location search,
 * and compact side-by-side dropdown menus for defect category and review status.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun MapTopFilterBar(
    hazardCount: Int,
    defectFilter: MapDefectFilter,
    onDefectFilterSelected: (MapDefectFilter) -> Unit,
    statusFilter: MapStatusFilter,
    onStatusFilterSelected: (MapStatusFilter) -> Unit,
    onSearchLocation: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var defectExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val brandNavy = Color(0xFF0B1F3A)
    val brandTeal = Color(0xFF0EA5A4)
    val softTeal = Color(0xFFCCFBF1)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val inputBackground = Color(0xFFF1F5F9)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
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
                    color = brandNavy
                )
                Surface(shape = RoundedCornerShape(10.dp), color = softTeal) {
                    Text(
                        text = "$hazardCount ${if (hazardCount == 1) "hazard" else "hazards"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city, road, or area...", fontSize = 13.sp, color = textSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearchLocation(searchQuery)
                }),
                leadingIcon = { Text("⌕", fontSize = 16.sp, color = textSecondary, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = brandNavy, strokeWidth = 2.dp)
                    } else if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) { Text("✕", fontSize = 13.sp, color = textSecondary) }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = brandTeal,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Compact Filter Row: Two Dropdown Selectors Side-by-Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Defect Category Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val isDefectActive = defectFilter != MapDefectFilter.ALL
                    Surface(
                        onClick = { defectExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDefectActive) brandNavy else inputBackground,
                        border = if (!isDefectActive) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = defectFilter.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDefectActive) Color.White else textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = if (defectExpanded) "▴" else "▾",
                                fontSize = 11.sp,
                                color = if (isDefectActive) Color.White else textSecondary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = defectExpanded,
                        onDismissRequest = { defectExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        MapDefectFilter.entries.forEach { f ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        f.title,
                                        fontWeight = if (defectFilter == f) FontWeight.Bold else FontWeight.Normal,
                                        color = if (defectFilter == f) brandNavy else textPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onDefectFilterSelected(f)
                                    defectExpanded = false
                                },
                                trailingIcon = {
                                    if (defectFilter == f) {
                                        Text("✓", color = brandNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            )
                        }
                    }
                }

                // 2. Review Status Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val isStatusActive = statusFilter != MapStatusFilter.ALL
                    val activeBg = when (statusFilter) {
                        MapStatusFilter.PENDING -> Color(0xFFF59E0B)
                        MapStatusFilter.CONFIRMED -> Color(0xFF0EA5A4)
                        MapStatusFilter.DISMISSED -> Color(0xFF64748B)
                        else -> brandNavy
                    }

                    Surface(
                        onClick = { statusExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isStatusActive) activeBg else inputBackground,
                        border = if (!isStatusActive) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusFilter.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isStatusActive) Color.White else textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = if (statusExpanded) "▴" else "▾",
                                fontSize = 11.sp,
                                color = if (isStatusActive) Color.White else textSecondary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        MapStatusFilter.entries.forEach { s ->
                            val dotColor = when (s) {
                                MapStatusFilter.ALL -> null
                                MapStatusFilter.PENDING -> Color(0xFFF59E0B)
                                MapStatusFilter.CONFIRMED -> Color(0xFF0EA5A4)
                                MapStatusFilter.DISMISSED -> Color(0xFF64748B)
                            }
                            DropdownMenuItem(
                                leadingIcon = dotColor?.let { color ->
                                    { Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {} }
                                },
                                text = {
                                    Text(
                                        s.title,
                                        fontWeight = if (statusFilter == s) FontWeight.Bold else FontWeight.Normal,
                                        color = if (statusFilter == s) brandNavy else textPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onStatusFilterSelected(s)
                                    statusExpanded = false
                                },
                                trailingIcon = {
                                    if (statusFilter == s) {
                                        Text("✓", color = brandNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
