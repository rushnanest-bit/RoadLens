package com.roadlens.app.ui.screens

import org.maplibre.android.maps.Style

/**
 * Map style configuration for MapLibre Native.
 * Uses MapLibre's verified public demo style by default to avoid watermark restrictions.
 * Supports configurable custom style URIs and API keys for production street tile providers (e.g. MapTiler).
 * Strictly under 200 lines per project engineering rules.
 */
object RoadMapStyle {

    /**
     * Officially hosted, verified MapLibre demo style requiring zero API keys.
     */
    const val DEMO_STYLE_URI: String = "https://demotiles.maplibre.org/style.json"

    /**
     * Builds a Style.Builder for MapLibre.
     * If [customStyleUri] is provided (e.g. MapTiler Streets or self-hosted vector tiles), it is loaded;
     * otherwise, it falls back to [DEMO_STYLE_URI].
     */
    fun createStyleBuilder(customStyleUri: String? = null): Style.Builder {
        val uri = customStyleUri?.takeIf { it.isNotBlank() } ?: DEMO_STYLE_URI
        return Style.Builder().fromUri(uri)
    }
}
