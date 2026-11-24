package edu.ap.dakaniktochniwegsmijten.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import edu.ap.dakaniktochniwegsmijten.R
import org.osmdroid.views.overlay.Marker

private enum class LocationAccess {
    NONE,
    APPROXIMATE,
    PRECISE
}

private fun readLocationAccess(context: Context): LocationAccess {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return when {
        fine == PackageManager.PERMISSION_GRANTED -> LocationAccess.PRECISE
        coarse == PackageManager.PERMISSION_GRANTED -> LocationAccess.APPROXIMATE
        else -> LocationAccess.NONE
    }
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    var locationAccess by remember(context) {
        mutableStateOf(readLocationAccess(context))
    }
    var hasRequestedLocation by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        locationAccess = readLocationAccess(context)
    }
    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.setZoom(uiState.zoom)
            controller.setCenter(
                GeoPoint(uiState.latitude, uiState.longitude)
            )

            overlays.add(CopyrightOverlay(context))
        }
    }

    DisposableEffect(mapView, mapViewModel) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                val center = mapView.mapCenter

                mapViewModel.updateViewport(
                    latitude = center.latitude,
                    longitude = center.longitude,
                    zoom = mapView.zoomLevelDouble
                )

                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                val center = mapView.mapCenter

                mapViewModel.updateViewport(
                    latitude = center.latitude,
                    longitude = center.longitude,
                    zoom = event.zoomLevel
                )

                return false
            }
        }

        mapView.addMapListener(listener)

        onDispose {
            mapView.removeMapListener(listener)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycle = lifecycleOwner.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    locationAccess = readLocationAccess(context)
                }
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    val selectedLatitude = uiState.selectedLatitude
    val selectedLongitude = uiState.selectedLongitude

    if (selectedLatitude != null && selectedLongitude != null) {
        DisposableEffect(mapView, selectedLatitude, selectedLongitude) {
            val marker = Marker(mapView).apply {
                position = GeoPoint(selectedLatitude, selectedLongitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ -> true }
            }

            mapView.overlays.add(marker)
            mapView.invalidate()

            onDispose {
                mapView.overlays.remove(marker)
                mapView.invalidate()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (locationAccess == LocationAccess.NONE) {
            Button(
                onClick = {
                    hasRequestedLocation = true
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.map_request_location))
            }
        }

        val statusRes = when (locationAccess) {
            LocationAccess.PRECISE -> R.string.map_location_precise
            LocationAccess.APPROXIMATE -> R.string.map_location_approximate
            LocationAccess.NONE -> if (hasRequestedLocation) {
                R.string.map_location_denied
            } else {
                null
            }
        }

        if (statusRes != null) {
            Text(
                text = stringResource(statusRes),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                val center = mapView.mapCenter
                mapViewModel.selectLocation(center.latitude, center.longitude)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(stringResource(R.string.map_place_pin))
        }

        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}