package edu.ap.dakaniktochniwegsmijten.ui.store

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.ap.dakaniktochniwegsmijten.data.geocoding.GeocodingRepository
import edu.ap.dakaniktochniwegsmijten.data.geocoding.StoreAddress
import edu.ap.dakaniktochniwegsmijten.data.local.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import edu.ap.dakaniktochniwegsmijten.R
import edu.ap.dakaniktochniwegsmijten.data.store.StoreAlreadyExistsException
import edu.ap.dakaniktochniwegsmijten.data.store.StoreRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class StoreUiState(
    val name: String = "",
    val category: String = "",
    val address: StoreAddress? = null,
    val isLookingUpAddress: Boolean = false,
    val addressLookupFailed: Boolean = false,
    val isSaving: Boolean = false,
    val isRegistered: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val storeRepository = StoreRepository()
    private val geocodingRepository = GeocodingRepository(
        AppDatabase.getInstance(application).cachedAddressDao()
    )
    private var selectedStorePin: Pair<Double, Double>? = null
    private var addressLookupJob: Job? = null
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateAddress(change: (StoreAddress) -> StoreAddress) {
        _uiState.update { state ->
            if (state.isLookingUpAddress || state.isSaving || state.isRegistered) {
                return@update state
            }

            val current = state.address ?: StoreAddress(
                streetAndNumber = "",
                postalCode = "",
                city = ""
            )
            state.copy(address = change(current))
        }
    }

    fun onStorePinSelected(latitude: Double, longitude: Double) {
        val pin = latitude to longitude
        if (_uiState.value.isRegistered || selectedStorePin == pin) return

        selectedStorePin = pin
        addressLookupJob?.cancel()
        _uiState.update {
            it.copy(
                address = null,
                isLookingUpAddress = true,
                addressLookupFailed = false
            )
        }

        addressLookupJob = viewModelScope.launch {
            val address = try {
                geocodingRepository.findStoreAddress(latitude, longitude)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                null
            }

            _uiState.update {
                it.copy(
                    address = address,
                    isLookingUpAddress = false,
                    addressLookupFailed = address == null
                )
            }
        }
    }

    fun registerStore(ownerId: String, latitude: Double?, longitude: Double?) {
        val form = _uiState.value
        if (form.isSaving || form.isRegistered || form.isLookingUpAddress) return

        val address = form.address
        if (form.name.isBlank() || form.category.isBlank() ||
            latitude == null || longitude == null ||
            address == null || address.streetAndNumber.isBlank() ||
            address.postalCode.isBlank() || address.city.isBlank()
        ) {
            _uiState.update { it.copy(errorRes = R.string.store_incomplete) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorRes = null) }

        viewModelScope.launch {
            try {
                storeRepository.createStore(
                    ownerId = ownerId,
                    name = form.name,
                    category = form.category,
                    latitude = latitude,
                    longitude = longitude,
                    address = address
                )
                _uiState.update { it.copy(isSaving = false, isRegistered = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val alreadyExists = generateSequence<Throwable>(error) { it.cause }
                    .any { it is StoreAlreadyExistsException }

                val message = if (alreadyExists) {
                    R.string.store_already_exists
                } else {
                    R.string.store_save_failed
                }

                _uiState.update { it.copy(isSaving = false, errorRes = message) }
            }
        }
    }
}