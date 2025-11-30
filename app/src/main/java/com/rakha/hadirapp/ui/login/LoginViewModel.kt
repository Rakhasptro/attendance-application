package com.rakha.hadirapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.repository.AuthException
import com.rakha.hadirapp.data.repository.AuthRepository
import com.rakha.hadirapp.data.store.TokenDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateToHome = MutableSharedFlow<Unit>(replay = 0)
    val navigateToHome: SharedFlow<Unit> = _navigateToHome.asSharedFlow()

    fun login(email: String, password: String) {
        // validation
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error.EmptyEmail()
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = LoginUiState.Error.InvalidEmail()
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error.EmptyPassword()
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val token = repository.login(email, password)
                Log.d("LoginViewModel", "login success, token=$token")
                tokenDataStore.saveToken(token)
                Log.d("LoginViewModel", "token saved to DataStore")
                _uiState.value = LoginUiState.Success(token)
                // emit navigation event
                _navigateToHome.emit(Unit)
            } catch (e: AuthException) {
                Log.d("LoginViewModel", "auth exception: ${e.message}")
                val msg = e.message ?: "Login failed"
                // Do NOT treat ambiguous success-like messages as successful login when no token is returned.
                val errorState = when {
                    msg.contains("invalid", ignoreCase = true) || msg.contains("salah", ignoreCase = true) -> LoginUiState.Error.WrongPassword(msg)
                    msg.contains("not found", ignoreCase = true) || msg.contains("not registered", ignoreCase = true) -> LoginUiState.Error.AccountNotFound(msg)
                    msg.contains("terhubung", ignoreCase = true) -> LoginUiState.Error.NetworkError(msg)
                    else -> LoginUiState.Error.ServerError(msg)
                }
                _uiState.value = errorState
            } catch (e: Exception) {
                Log.d("LoginViewModel", "exception: ${e.message}")
                _uiState.value = LoginUiState.Error.NetworkError("Gagal terhubung ke server")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
