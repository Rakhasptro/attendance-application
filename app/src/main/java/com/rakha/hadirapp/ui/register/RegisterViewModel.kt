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
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val repository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank()) {
            _uiState.value = RegisterUiState.Error("Email tidak boleh kosong")
            return
        }
        if (password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Password tidak boleh kosong")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("Konfirmasi password tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val token = repository.register(email, password)
                tokenDataStore.saveToken(token)
                _uiState.value = RegisterUiState.Success(token)
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Registration failed")
            }
        }
    }
}

