package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.dakaniktochniwegsmijten.R
import edu.ap.dakaniktochniwegsmijten.data.auth.AuthRepository
import edu.ap.dakaniktochniwegsmijten.data.user.UserProfile
import edu.ap.dakaniktochniwegsmijten.data.user.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(
    val isLoading: Boolean = true,
    val userId: String? = null,
    val isProfileLoading: Boolean = false,
    val profile: UserProfile? = null,
    @StringRes val profileErrorRes: Int? = null
)

class SessionViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val retryRequests = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                authRepository.observeUserId(),
                retryRequests
            ) { userId, _ ->
                userId
            }.collectLatest { userId ->
                _uiState.value = SessionUiState(
                    isLoading = false,
                    userId = userId,
                    isProfileLoading = userId != null
                )

                if (userId == null) {
                    return@collectLatest
                }

                try {
                    val profile = userRepository.getProfile(userId)

                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            profile = profile
                        )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            profileErrorRes = R.string.session_error_profile
                        )
                    }
                }
            }
        }
    }

    fun retryProfile() {
        val state = _uiState.value

        if (
            state.isLoading ||
            state.userId == null ||
            state.isProfileLoading
        ) {
            return
        }

        retryRequests.update { it + 1 }
    }

    fun signOut() {
        authRepository.signOut()
    }
}