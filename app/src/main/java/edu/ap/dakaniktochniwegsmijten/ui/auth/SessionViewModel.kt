package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.dakaniktochniwegsmijten.data.auth.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SessionUiState(
    val isLoading: Boolean = true,
    val userId: String? = null
)

class SessionViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    val uiState: StateFlow<SessionUiState> =
        authRepository.observeUserId()
            .map { userId ->
                SessionUiState(
                    isLoading = false,
                    userId = userId
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SessionUiState()
            )

    fun signOut() {
        authRepository.signOut()
    }
}