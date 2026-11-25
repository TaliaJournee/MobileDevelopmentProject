package edu.ap.dakaniktochniwegsmijten.ui.store

import androidx.lifecycle.ViewModel
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
    val isSaving: Boolean = false,
    val isRegistered: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class StoreViewModel : ViewModel() {
    private val storeRepository = StoreRepository()
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun registerStore(ownerId: String, latitude: Double?, longitude: Double?) {
        val form = _uiState.value
        if (form.isSaving || form.isRegistered) return

        if (form.name.isBlank() || form.category.isBlank() ||
            latitude == null || longitude == null
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
                    longitude = longitude
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