package edu.ap.dakaniktochniwegsmijten.ui.map

import androidx.lifecycle.SavedStateHandle
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.dakaniktochniwegsmijten.data.location.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val latitude: Double = 51.230167,
    val longitude: Double = 4.416129,
    val zoom: Double = 17.0,
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null,
    val isLocating: Boolean = false,
    val locationUnavailable: Boolean = false,
    val shouldCenterOnSelection: Boolean = false
)

class MapViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private var locationJob: Job? = null
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
        locationJob?.cancel()
        setSelectedLocation(latitude, longitude, centerMap = false)
    }

    fun useDeviceLocation() {
        if (_uiState.value.isLocating) return

        _uiState.value = _uiState.value.copy(
            isLocating = true,
            locationUnavailable = false,
            shouldCenterOnSelection = false
        )

        locationJob = viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()

            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLocating = false,
                    locationUnavailable = true
                )
                return@launch
            }

            setSelectedLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                centerMap = true
            )
        }
    }

    fun onSelectionCentered() {
        _uiState.value = _uiState.value.copy(shouldCenterOnSelection = false)
    }

    private fun setSelectedLocation(
        latitude: Double,
        longitude: Double,
        centerMap: Boolean
    ) {
        savedStateHandle["selectedLatitude"] = latitude
        savedStateHandle["selectedLongitude"] = longitude

        _uiState.value = _uiState.value.copy(
            selectedLatitude = latitude,
            selectedLongitude = longitude,
            isLocating = false,
            locationUnavailable = false,
            shouldCenterOnSelection = centerMap
        )
    }
}