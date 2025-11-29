package com.rakha.hadirapp.ui.login

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String) : LoginUiState()
    sealed class Error(val message: String) : LoginUiState() {
        class EmptyEmail(message: String = "Email tidak boleh kosong") : Error(message)
        class EmptyPassword(message: String = "Password tidak boleh kosong") : Error(message)
        class InvalidEmail(message: String = "Email tidak valid") : Error(message)
        class WrongPassword(message: String = "Password salah") : Error(message)
        class AccountNotFound(message: String = "Akun tidak terdaftar") : Error(message)
        class NetworkError(message: String = "Network error") : Error(message)
        class ServerError(message: String = "Server error") : Error(message)
    }
}
