package com.rakha.hadirapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.repository.AuthRepository
import com.rakha.hadirapp.data.store.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val token: String) : RegisterUiState()
    sealed class Error(val message: String) : RegisterUiState() {
        class EmptyEmail(message: String = "Email tidak boleh kosong") : Error(message)
        class EmptyPassword(message: String = "Password tidak boleh kosong") : Error(message)
        class PasswordMismatch(message: String = "Konfirmasi password tidak cocok") : Error(message)
        class InvalidEmail(message: String = "Email tidak valid") : Error(message)
        class EmailAlreadyUsed(message: String = "Email sudah digunakan") : Error(message)
        class NetworkError(message: String = "Network error") : Error(message)
        class ServerError(message: String = "Server error") : Error(message)
    }
}

class RegisterViewModel(
    private val repository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank()) {
            _uiState.value = RegisterUiState.Error.EmptyEmail()
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = RegisterUiState.Error.InvalidEmail()
            return
        }
        if (password.isBlank()) {
            _uiState.value = RegisterUiState.Error.EmptyPassword()
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error.PasswordMismatch()
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val token = repository.register(email, password)
                tokenDataStore.saveToken(token)
                _uiState.value = RegisterUiState.Success(token)
            } catch (e: Exception) {
                val msg = e.message ?: "Registration failed"
                val errorState = when {
                    msg.contains("already", ignoreCase = true) || msg.contains("used", ignoreCase = true) -> RegisterUiState.Error.EmailAlreadyUsed(msg)
                    msg.contains("terhubung", ignoreCase = true) -> RegisterUiState.Error.NetworkError(msg)
                    else -> RegisterUiState.Error.ServerError(msg)
                }
                _uiState.value = errorState
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
