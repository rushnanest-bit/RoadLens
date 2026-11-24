package com.roadlens.app.ui.screens

import org.maplibre.android.maps.Style

/**
 * Clean road navigation map style configuration for MapLibre Native.
 * Features Carto Voyager vector cartography with clear streets, soft greenery,
 * and high-contrast typography matching the RoadLens app theme.
 * Strictly under 200 lines per project engineering rules.
 */
object RoadMapStyle {

    /**
     * Standard Voyager vector tile style JSON URI.
     * Clean, modern street cartography with natural colors matching the app palette.
     */
    const val VOYAGER_STYLE_URI: String = "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json"

    fun createStyleBuilder(customStyleUri: String? = null): Style.Builder {
        val uri = customStyleUri?.takeIf { it.isNotBlank() } ?: VOYAGER_STYLE_URI
        return Style.Builder().fromUri(uri)
    }
}
