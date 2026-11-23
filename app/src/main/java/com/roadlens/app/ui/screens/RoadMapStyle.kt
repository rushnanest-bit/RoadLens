package com.roadlens.app.ui.screens

import org.maplibre.android.maps.Style

/**
 * Map style configuration for MapLibre Native.
 * Uses OpenStreetMap standard street tiles for crisp street, highway, and local detail.
 * Strictly under 200 lines per project engineering rules.
 */
object RoadMapStyle {

    /**
     * Standard OpenStreetMap raster style JSON with street-level resolution up to zoom level 19.
     * Completely free, public, and requires zero API keys.
     */
    val OSM_STREET_STYLE_JSON: String = """
    {
      "version": 8,
      "name": "OpenStreetMap Street",
      "sources": {
        "osm-tiles": {
          "type": "raster",
          "tiles": [
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          ],
          "tileSize": 256,
          "attribution": "© OpenStreetMap contributors",
          "maxzoom": 19
        }
      },
      "layers": [
        {
          "id": "osm-tiles-layer",
          "type": "raster",
          "source": "osm-tiles",
          "minzoom": 0,
          "maxzoom": 19
        }
      ]
    }
    """.trimIndent()

    fun createStyleBuilder(customStyleUri: String? = null): Style.Builder {
        return if (!customStyleUri.isNullOrBlank()) {
            Style.Builder().fromUri(customStyleUri)
        } else {
            Style.Builder().fromJson(OSM_STREET_STYLE_JSON)
        }
    }
}
