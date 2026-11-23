package com.roadlens.app.ui.screens

/**
 * Modern street map style definition for MapLibre Native.
 * Delivers clean street-level cartography with clearly visible roads, streets,
 * and local geographical features without requiring proprietary API keys.
 * Strictly under 200 lines per project engineering rules.
 */
object RoadMapStyle {

    /**
     * Standard Carto Voyager / OpenStreetMap raster tile style JSON.
     * High-contrast, clean navigation palette optimized for road inspection overlays.
     */
    val STREET_STYLE_JSON: String = """
    {
      "version": 8,
      "name": "RoadLens Street Map",
      "sources": {
        "street-tiles": {
          "type": "raster",
          "tiles": [
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
            "https://d.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"
          ],
          "tileSize": 256,
          "attribution": "© CartoDB, © OpenStreetMap contributors"
        }
      },
      "layers": [
        {
          "id": "street-tiles-layer",
          "type": "raster",
          "source": "street-tiles",
          "minzoom": 0,
          "maxzoom": 20
        }
      ]
    }
    """.trimIndent()
}
