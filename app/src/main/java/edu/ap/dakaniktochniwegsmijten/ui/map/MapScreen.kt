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

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()

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
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
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

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}