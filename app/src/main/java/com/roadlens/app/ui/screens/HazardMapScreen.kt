package com.roadlens.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
 * Interactive hazard map screen powered by MapLibre Native Android.
 * Automatically focuses on user's GPS location on open and drops markers on searched places.
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
    var hasCenteredUserLocation by remember { mutableStateOf(false) }
    var searchedLocation by remember { mutableStateOf<SearchLocationResult?>(null) }
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
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        if (hasLocationPermission) locationTracker.startTracking()
        onDispose { locationTracker.stopTracking(); lifecycleOwner.lifecycle.removeObserver(observer); mapView.onDestroy() }
    }

    // Automatically focus on user's location when opened
    LaunchedEffect(userLocation, maplibreMapInstance) {
        val map = maplibreMapInstance ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        if (!hasCenteredUserLocation) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15.0))
            hasCenteredUserLocation = true
        }
    }

    // Plot hazard markers, user location dot, and searched location pin
    LaunchedEffect(hazards, searchedLocation, userLocation, maplibreMapInstance) {
        val map = maplibreMapInstance ?: return@LaunchedEffect
        map.clear()
        hazards.forEach { h ->
            val lat = h.latitude ?: return@forEach
            val lon = h.longitude ?: return@forEach
            map.addMarker(
                MarkerOptions().position(LatLng(lat, lon)).icon(HazardMapIconHelper.getIconForClass(context, h.classId))
                    .title("${h.className} (#${h.eventId})").snippet("${(h.confidence * 100).toInt()}% • ${h.reviewStatus}")
            )
        }
        userLocation?.let { loc ->
            map.addMarker(
                MarkerOptions().position(LatLng(loc.latitude, loc.longitude)).icon(HazardMapIconHelper.getUserLocationIcon(context))
                    .title("Your Current Location").snippet("GPS active")
            )
        }
        searchedLocation?.let { s ->
            val marker = map.addMarker(
                MarkerOptions().position(LatLng(s.latitude, s.longitude)).icon(HazardMapIconHelper.getSearchPinIcon(context))
                    .title(s.title).snippet("Searched Location")
            )
            map.selectMarker(marker)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    onCreate(null)
                    onStart()
                    onResume()
                    getMapAsync { map ->
                        val initialTarget = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(12.9716, 77.5946)
                        map.cameraPosition = CameraPosition.Builder().target(initialTarget).zoom(15.0).build()
                        map.setStyle(RoadMapStyle.createStyleBuilder()) { maplibreMapInstance = map }
                        map.setOnMarkerClickListener { marker ->
                            val clicked = hazards.firstOrNull { marker.title?.contains("#${it.eventId}") == true }
                            if (clicked != null) { viewModel.selectHazard(clicked); true } else false
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
                        searchedLocation = res
                        maplibreMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(res.latitude, res.longitude), 15.0))
                        Toast.makeText(context, "Located: ${res.title}", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Location not found: $query", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        FloatingActionButton(
            onClick = {
                userLocation?.let {
                    maplibreMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0))
                } ?: Toast.makeText(context, "Acquiring GPS fix...", Toast.LENGTH_SHORT).show()
            },
            containerColor = Color(0xFF1E3A2F),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedHazard != null) 280.dp else 20.dp)
        ) {
            Text("⌖", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC7F284))
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
