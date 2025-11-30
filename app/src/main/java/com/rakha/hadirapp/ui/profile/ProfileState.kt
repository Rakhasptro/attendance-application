package com.rakha.hadirapp.ui.profile

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String?) : ProfileUiState()
    sealed class Error(val message: String) : ProfileUiState() {
        class NetworkError(message: String) : Error(message)
        class ServerError(message: String) : Error(message)
        class Unknown(message: String) : Error(message)
    }
}

