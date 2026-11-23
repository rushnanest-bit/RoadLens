package com.roadlens.app.ui.navigation

/**
 * Navigation destinations for the RoadLens application bottom navigation bar.
 *
 * @property title Display label shown below the icon.
 * @property symbol Distinctive visual icon or symbol.
 */
enum class RoadLensTab(
    val title: String,
    val symbol: String
) {
    HOME(title = "Home", symbol = "⌂"),
    SCAN(title = "Scan", symbol = "⌕"),
    MAP(title = "Map", symbol = "◈"),
    REPORT(title = "Report", symbol = "▤")
}
