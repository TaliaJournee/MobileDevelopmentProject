package edu.ap.dakaniktochniwegsmijten.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val latitude: Double = 51.230167,
    val longitude: Double = 4.416129,
    val zoom: Double = 17.0,
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null
)

class MapViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MapUiState(
            latitude = savedStateHandle["latitude"] ?: 51.230167,
            longitude = savedStateHandle["longitude"] ?: 4.416129,
            zoom = savedStateHandle["zoom"] ?: 17.0,
            selectedLatitude = savedStateHandle["selectedLatitude"],
            selectedLongitude = savedStateHandle["selectedLongitude"]
        )
    )

    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateViewport(latitude: Double, longitude: Double, zoom: Double) {
        val newState = _uiState.value.copy(
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

    fun selectLocation(latitude: Double, longitude: Double) {
        savedStateHandle["selectedLatitude"] = latitude
        savedStateHandle["selectedLongitude"] = longitude

        _uiState.value = _uiState.value.copy(
            selectedLatitude = latitude,
            selectedLongitude = longitude
        )
    }
}