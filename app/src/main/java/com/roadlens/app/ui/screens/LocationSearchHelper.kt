package com.roadlens.app.ui.screens

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Result model for geocoded location queries.
 */
data class SearchLocationResult(
    val title: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Geocoding search helper that resolves location queries to GPS coordinates.
 * Strictly under 200 lines per project engineering rules.
 */
object LocationSearchHelper {

    suspend fun searchLocation(context: Context, query: String): SearchLocationResult? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext null

        // 1. Attempt Android platform Geocoder
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(trimmed, 1)
            val match = results?.firstOrNull()
            if (match != null && match.hasLatitude() && match.hasLongitude()) {
                val label = match.featureName ?: match.locality ?: match.subAdminArea ?: trimmed
                return@withContext SearchLocationResult(label, match.latitude, match.longitude)
            }
        }

        // 2. Fallback to open Nominatim geocoding service
        runCatching {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val endpoint = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", "RoadLens/1.0 (contact: support@roadlens.app)")
                connectTimeout = 4000
                readTimeout = 4000
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(body)
                if (array.length() > 0) {
                    val first = array.getJSONObject(0)
                    val lat = first.getDouble("lat")
                    val lon = first.getDouble("lon")
                    val name = first.optString("name").ifBlank { trimmed }
                    return@withContext SearchLocationResult(name, lat, lon)
                }
            }
        }
        null
    }
}
