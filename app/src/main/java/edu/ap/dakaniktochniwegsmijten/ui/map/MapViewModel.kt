package edu.ap.dakaniktochniwegsmijten.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val latitude: Double = 51.230167,
    val longitude: Double = 4.416129,
    val zoom: Double = 17.0
)

class MapViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MapUiState(
            latitude = savedStateHandle["latitude"] ?: 51.230167,
            longitude = savedStateHandle["longitude"] ?: 4.416129,
            zoom = savedStateHandle["zoom"] ?: 17.0
        )
    )

    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateViewport(latitude: Double, longitude: Double, zoom: Double) {
        val newState = MapUiState(
            latitude = latitude,
            longitude = longitude,
            zoom = zoom
        )

        if (newState == _uiState.value) {
            return
        }

        savedStateHandle["latitude"] = latitude
        savedStateHandle["longitude"] = longitude
        savedStateHandle["zoom"] = zoom

        _uiState.value = newState
    }
}