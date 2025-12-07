package com.rakha.hadirapp.ui.forgotpassword

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.repository.AuthException
import com.rakha.hadirapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val message: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _navigateToLogin = MutableSharedFlow<Unit>()
    val navigateToLogin: SharedFlow<Unit> = _navigateToLogin.asSharedFlow()

    fun resetPassword(npm: String, newPassword: String, confirmPassword: String) {
        // Validation
        if (npm.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("NPM tidak boleh kosong")
            return
        }

        if (newPassword.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("Password baru tidak boleh kosong")
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = ForgotPasswordUiState.Error("Password minimal 6 karakter")
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.value = ForgotPasswordUiState.Error("Password tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            try {
                val message = repository.forgotPassword(npm.trim(), newPassword)
                Log.d("ForgotPasswordViewModel", "Password reset successful: $message")
                _uiState.value = ForgotPasswordUiState.Success(message)
                // Navigate to login after success
                _navigateToLogin.emit(Unit)
            } catch (e: AuthException) {
                Log.d("ForgotPasswordViewModel", "auth exception: ${e.message}")
                _uiState.value = ForgotPasswordUiState.Error(e.message ?: "Error")
            } catch (e: Exception) {
                Log.d("ForgotPasswordViewModel", "exception: ${e.message}")
                _uiState.value = ForgotPasswordUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

