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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

data class SessionUiState(
    val isLoading: Boolean = true,
    val userId: String? = null,
    val isProfileLoading: Boolean = false,
    val profile: UserProfile? = null,
    @StringRes val profileErrorRes: Int? = null
)

data class RegistrationUiState(
    val isSubmitting: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class SessionViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val retryRequests = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _registrationState = MutableStateFlow(RegistrationUiState())
    val registrationState: StateFlow<RegistrationUiState> =
        _registrationState.asStateFlow()

    private var pendingProfile: UserProfile? = null

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
                    val existingProfile = userRepository.getProfile(userId)

                    val profile = if (existingProfile != null) {
                        existingProfile
                    } else {
                        _uiState.update { it.copy(isProfileLoading = false) }

                        userRepository.awaitProfileCreation(userId)
                        userRepository.getProfile(userId)
                    }
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

    fun register(form: RegisterUiState) {
        val role = form.role ?: return

        if (_registrationState.value.isSubmitting ||
            _uiState.value.isLoading ||
            _uiState.value.userId != null ||
            !form.isNameValid ||
            !form.isEmailValid ||
            !form.isPasswordValid
        ) {
            return
        }

        _registrationState.value = RegistrationUiState(isSubmitting = true)

        viewModelScope.launch {
            val profile = try {
                authRepository.createAccount(
                    name = form.name,
                    email = form.email,
                    password = form.password,
                    role = role
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val errorRes = when (exception) {
                    is FirebaseAuthUserCollisionException ->
                        R.string.register_error_email_in_use
                    is FirebaseAuthWeakPasswordException ->
                        R.string.register_error_weak_password
                    is FirebaseAuthInvalidCredentialsException ->
                        R.string.login_error_email_invalid
                    is FirebaseNetworkException ->
                        R.string.login_error_network
                    is FirebaseTooManyRequestsException ->
                        R.string.login_error_too_many_requests
                    else ->
                        R.string.register_error_unknown
                }
                _registrationState.value = RegistrationUiState(errorRes = errorRes)
                return@launch
            }

            pendingProfile = profile
            savePendingProfile(profile)
        }
    }

    private suspend fun savePendingProfile(profile: UserProfile) {
        try {
            userRepository.createProfile(profile)
            pendingProfile = null
            _registrationState.value = RegistrationUiState()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _registrationState.value = RegistrationUiState(
                errorRes = R.string.register_error_profile_save
            )
        }
    }

    fun retryProfile() {
        val state = _uiState.value
        val userId = state.userId ?: return

        if (state.isLoading || state.isProfileLoading ||
            _registrationState.value.isSubmitting
        ) {
            return
        }

        val profile = pendingProfile
        if (profile != null && profile.uid == userId && state.profile == null) {
            _registrationState.value = RegistrationUiState(isSubmitting = true)

            viewModelScope.launch {
                savePendingProfile(profile)
                retryRequests.update { it + 1 }
            }
            return
        }

        retryRequests.update { it + 1 }
    }

    fun signOut() {
        if (_registrationState.value.isSubmitting) {
            return
        }

        pendingProfile = null
        _registrationState.value = RegistrationUiState()
        authRepository.signOut()
    }
}