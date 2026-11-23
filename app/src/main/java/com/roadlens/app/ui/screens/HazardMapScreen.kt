package com.roadlens.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.domain.model.MapFilterType
import com.roadlens.app.ui.components.HazardDetailCard
import com.roadlens.app.ui.viewmodel.MapViewModel
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

/**
 * Interactive Hazard Map screen powered by MapLibre Native Android.
 * Plots offline-cached vector tiles and road hazard defect pins per FR-10.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun HazardMapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val filter by viewModel.filter.collectAsState()
    val hazards by viewModel.hazards.collectAsState()
    val selectedHazard by viewModel.selectedHazard.collectAsState()

    var maplibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(hazards, maplibreMapInstance) {
        val map = maplibreMapInstance ?: return@LaunchedEffect
        map.clear()
        hazards.forEach { hazard ->
            val lat = hazard.latitude ?: return@forEach
            val lon = hazard.longitude ?: return@forEach
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lon))
                    .title("${hazard.className} (#${hazard.eventId})")
                    .snippet("${(hazard.confidence * 100).toInt()}% • ${hazard.reviewStatus}")
            )
        }
        hazards.firstOrNull()?.let {
            val lat = it.latitude ?: 12.9716
            val lon = it.longitude ?: 77.5946
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 13.0))
        }
    }

    val brandGreen = Color(0xFF1E3A2F)

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        maplibreMapInstance = map
                        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"))
                        map.setOnMarkerClickListener { marker ->
                            val clicked = hazards.firstOrNull { marker.title?.contains("#${it.eventId}") == true }
                            viewModel.selectHazard(clicked)
                            true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Filter Bar & Offline Notice
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MAPLIBRE NATIVE • VECTOR CACHE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = brandGreen)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MapFilterType.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = brandGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Empty state badge if no hazards are localized
        if (hazards.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Geo-Tagged Hazards", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = brandGreen)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Detections with GPS accuracy <= 25m will plot hazard pins here automatically.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Bottom Hazard Inspection Detail Card
        selectedHazard?.let { hazard ->
            HazardDetailCard(
                hazard = hazard,
                onDismiss = { viewModel.selectHazard(null) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
