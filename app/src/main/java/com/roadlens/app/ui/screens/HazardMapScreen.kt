package com.roadlens.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.roadlens.app.data.location.LocationTracker
import com.roadlens.app.ui.components.HazardDetailCard
import com.roadlens.app.ui.components.MapEmptyStateBadge
import com.roadlens.app.ui.components.MapTopFilterBar
import com.roadlens.app.ui.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/**
 * Modern dark-themed hazard map screen powered by MapLibre Native Android.
 * Features ArcGIS World Dark Gray cartography, search location geocoding, and live GPS centering.
 * Strictly under 200 lines per project engineering rules.
 */
@Suppress("DEPRECATION")
@Composable
fun HazardMapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val filter by viewModel.filter.collectAsState()
    val hazards by viewModel.hazards.collectAsState()
    val selectedHazard by viewModel.selectedHazard.collectAsState()

    val locationTracker = remember { LocationTracker(context) }
    val userLocation by locationTracker.currentLocation.collectAsState()
    val hasLocationPermission = remember {
        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    var maplibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapView = remember { MapView(context) }
    var hasCenteredInitialLocation by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, hasLocationPermission) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); if (hasLocationPermission) locationTracker.startTracking() }
                Lifecycle.Event.ON_PAUSE -> { mapView.onPause(); locationTracker.stopTracking() }
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (hasLocationPermission) locationTracker.startTracking()
        onDispose {
            locationTracker.stopTracking()
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(userLocation, hazards, maplibreMapInstance) {
        val map = maplibreMapInstance ?: return@LaunchedEffect
        if (!hasCenteredInitialLocation) {
            val target = userLocation?.let { LatLng(it.latitude, it.longitude) }
                ?: hazards.firstOrNull()?.let { LatLng(it.latitude ?: 12.9716, it.longitude ?: 77.5946) }
            if (target != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15.0))
                hasCenteredInitialLocation = true
            }
        }
    }

    LaunchedEffect(hazards, maplibreMapInstance) {
        val map = maplibreMapInstance ?: return@LaunchedEffect
        map.clear()
        hazards.forEach { hazard ->
            val lat = hazard.latitude ?: return@forEach
            val lon = hazard.longitude ?: return@forEach
            val icon = HazardMapIconHelper.getIconForClass(context, hazard.classId)
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lon))
                    .icon(icon)
                    .title("${hazard.className} (#${hazard.eventId})")
                    .snippet("${(hazard.confidence * 100).toInt()}% • ${hazard.reviewStatus}")
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        maplibreMapInstance = map
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(12.9716, 77.5946))
                            .zoom(14.5)
                            .build()
                        map.setStyle(RoadMapStyle.createStyleBuilder())
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

        MapTopFilterBar(
            hazardCount = hazards.size,
            currentFilter = filter,
            onFilterSelected = { viewModel.setFilter(it) },
            isSearching = isSearching,
            onSearchLocation = { query ->
                coroutineScope.launch {
                    isSearching = true
                    val res = LocationSearchHelper.searchLocation(context, query)
                    isSearching = false
                    if (res != null) {
                        maplibreMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(res.latitude, res.longitude), 15.0))
                        Toast.makeText(context, "Located: ${res.title}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Location not found: $query", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (hazards.isEmpty()) {
            MapEmptyStateBadge(modifier = Modifier.align(Alignment.BottomStart))
        }

        FloatingActionButton(
            onClick = {
                val target = userLocation?.let { LatLng(it.latitude, it.longitude) }
                    ?: hazards.firstOrNull()?.let { LatLng(it.latitude ?: 12.9716, it.longitude ?: 77.5946) }
                    ?: LatLng(12.9716, 77.5946)
                maplibreMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15.0))
            },
            containerColor = Color(0xFF18181B),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedHazard != null) 280.dp else 20.dp)
                .border(1.dp, Color(0xFF10B981), CircleShape)
        ) {
            Text("⌖", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
        }

        selectedHazard?.let { hazard ->
            HazardDetailCard(
                hazard = hazard,
                onDismiss = { viewModel.selectHazard(null) },
                onUpdateReview = { status -> viewModel.updateReview(hazard.eventId, status) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
