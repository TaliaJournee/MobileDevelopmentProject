package edu.ap.dakaniktochniwegsmijten.ui.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import edu.ap.dakaniktochniwegsmijten.R
import edu.ap.dakaniktochniwegsmijten.data.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(
        LoginUiState(
            email = savedStateHandle["email"] ?: ""
        )
    )

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        if (_uiState.value.isLoading || _uiState.value.isSignedIn) {
            return
        }

        savedStateHandle["email"] = email
        _uiState.update {
            it.copy(email = email, errorRes = null)
        }
    }

    fun updatePassword(password: String) {
        if (_uiState.value.isLoading || _uiState.value.isSignedIn) {
            return
        }

        _uiState.update {
            it.copy(password = password, errorRes = null)
        }
    }

    fun signIn() {
        val state = _uiState.value

        if (state.isLoading || state.isSignedIn) {
            return
        }

        val email = state.email.trim()

        val validationError = when {
            email.isEmpty() -> R.string.login_error_email_required
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                R.string.login_error_email_invalid
            state.password.isEmpty() -> R.string.login_error_password_required
            else -> null
        }

        if (validationError != null) {
            _uiState.update {
                it.copy(errorRes = validationError)
            }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, errorRes = null)
        }

        viewModelScope.launch {
            try {
                authRepository.signIn(
                    email = email,
                    password = state.password
                )

                _uiState.update {
                    it.copy(
                        password = "",
                        isSignedIn = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val errorRes = when (exception) {
                    is FirebaseNetworkException ->
                        R.string.login_error_network

                    is FirebaseTooManyRequestsException ->
                        R.string.login_error_too_many_requests

                    is FirebaseAuthInvalidCredentialsException ->
                        R.string.login_error_credentials

                    is FirebaseAuthInvalidUserException -> {
                        if (exception.errorCode == "ERROR_USER_DISABLED") {
                            R.string.login_error_account_disabled
                        } else {
                            R.string.login_error_credentials
                        }
                    }

                    else -> R.string.login_error_unknown
                }

                _uiState.update {
                    it.copy(errorRes = errorRes)
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}