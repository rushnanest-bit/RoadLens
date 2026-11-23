package com.roadlens.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadlens.app.domain.model.MapFilterType

/**
 * Professional dark-themed top overlay with location search and hazard category filter chips.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun MapTopFilterBar(
    hazardCount: Int,
    currentFilter: MapFilterType,
    onFilterSelected: (MapFilterType) -> Unit,
    onSearchLocation: (String) -> Unit,
    isSearching: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val cardDark = Color(0xFF18181B)
    val inputDark = Color(0xFF27272A)
    val accentGreen = Color(0xFF10B981)
    val textLight = Color(0xFFF8FAFC)
    val textMuted = Color(0xFF94A3B8)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardDark.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            // Header Row: Title & Hazard Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Road Monitoring",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textLight
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$hazardCount ${if (hazardCount == 1) "hazard" else "hazards"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Location Search Input Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city, road, or area...", fontSize = 13.sp, color = textMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearchLocation(searchQuery)
                }),
                leadingIcon = {
                    Text("⌕", fontSize = 16.sp, color = textMuted, fontWeight = FontWeight.Bold)
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = accentGreen,
                            strokeWidth = 2.dp
                        )
                    } else if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text("✕", fontSize = 13.sp, color = textMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textLight,
                    unfocusedTextColor = textLight,
                    focusedContainerColor = inputDark,
                    unfocusedContainerColor = inputDark,
                    focusedBorderColor = accentGreen,
                    unfocusedBorderColor = Color(0xFF3F3F46)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Filter Chips
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
                            selectedContainerColor = accentGreen,
                            selectedLabelColor = Color.White,
                            containerColor = inputDark,
                            labelColor = textMuted
                        )
                    )
                }
            }
        }
    }
}
