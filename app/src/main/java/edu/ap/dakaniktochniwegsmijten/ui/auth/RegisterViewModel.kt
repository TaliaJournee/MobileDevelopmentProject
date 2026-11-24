package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.ap.dakaniktochniwegsmijten.data.user.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.util.Patterns

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole? = null
) {
    val isNameValid: Boolean
        get() = name.trim().length in 1..100

    val isEmailValid: Boolean
        get() {
            val trimmedEmail = email.trim()
            return trimmedEmail.length in 1..254 &&
                    Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
        }

    val isPasswordValid: Boolean
        get() = password.length >= 6
}

class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegisterUiState(
            name = savedStateHandle["name"] ?: "",
            email = savedStateHandle["email"] ?: "",
            role = UserRole.fromFirestore(savedStateHandle.get<String>("role"))
        )
    )

    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        savedStateHandle["name"] = name
        _uiState.update { it.copy(name = name) }
    }

    fun updateEmail(email: String) {
        savedStateHandle["email"] = email
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun selectRole(role: UserRole) {
        savedStateHandle["role"] = role.firestoreValue
        _uiState.update { it.copy(role = role) }
    }
}