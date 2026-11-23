package com.roadlens.app.ui.screens

import org.maplibre.android.maps.Style

/**
 * Enterprise dark theme map style configuration for MapLibre Native.
 * Features Dark Matter vector cartography with high-visibility roads and labels.
 * Strictly under 200 lines per project engineering rules.
 */
object RoadMapStyle {

    /**
     * Standard Dark Matter vector tile style JSON URI.
     * High-contrast dark asphalt theme with crisp street networks and road labels.
     */
    const val DARK_MATTER_STYLE_URI: String = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"

    fun createStyleBuilder(customStyleUri: String? = null): Style.Builder {
        val uri = customStyleUri?.takeIf { it.isNotBlank() } ?: DARK_MATTER_STYLE_URI
        return Style.Builder().fromUri(uri)
    }
}
