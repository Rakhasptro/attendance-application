package com.rakha.hadirapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.network.dto.UpdateProfileRequest
import com.rakha.hadirapp.data.repository.ProfileRepository
import com.rakha.hadirapp.data.repository.ProfileException
import com.rakha.hadirapp.data.network.dto.Profile
import com.rakha.hadirapp.data.store.TokenDataStore
import com.rakha.hadirapp.data.store.TokenHolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _profileData = MutableStateFlow<Profile?>(null)
    val profileData: StateFlow<Profile?> = _profileData.asStateFlow()

    // expose email separately (comes from user.email)
    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            // Wait for token to be available - check both TokenHolder and DataStore
            val tokenFromHolder = TokenHolder.token
            if (!tokenFromHolder.isNullOrBlank()) {
                Log.d("ProfileViewModel", "Token available from holder immediately")
            } else {
                Log.d("ProfileViewModel", "Token not in holder, waiting for DataStore...")
                val tokenAvailable = withTimeoutOrNull(5000) {
                    tokenDataStore.getTokenFlow().first { !it.isNullOrBlank() }
                }

                if (tokenAvailable.isNullOrBlank()) {
                    Log.e("ProfileViewModel", "No token available within timeout")
                    _uiState.value = ProfileUiState.Error.NetworkError("Tidak terautentikasi. Silakan login kembali.")
                    return@launch
                }
                // Update holder if it was loaded from DataStore
                TokenHolder.setToken(tokenAvailable)
            }

            try {
                val resp = repository.getProfile()
                val profile = resp.user?.profile
                _profileData.value = profile
                _email.value = resp.user?.email
                _uiState.value = ProfileUiState.Idle
                Log.d("ProfileViewModel", "Profile loaded: ${profile?.fullName}")
            } catch (e: ProfileException) {
                Log.d("ProfileViewModel", "profile error: ${e.message}")
                _uiState.value = ProfileUiState.Error.NetworkError(e.message ?: "Error")
            } catch (e: Exception) {
                Log.d("ProfileViewModel", "exception: ${e.message}")
                _uiState.value = ProfileUiState.Error.Unknown(e.message ?: "Unknown error")
            }
        }
    }

    fun saveProfile(fullName: String, npm: String, email: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val request = UpdateProfileRequest(fullName = fullName, npm = npm, email = email)
                val resp = repository.updateProfile(request)
                _uiState.value = ProfileUiState.Success(resp.message)
                // update local profile data
                val updated = resp.user?.profile
                _profileData.value = updated
                _email.value = resp.user?.email
                _eventFlow.emit(resp.message ?: "Berhasil disimpan")
            } catch (e: ProfileException) {
                Log.d("ProfileViewModel", "profile update error: ${e.message}")
                _uiState.value = ProfileUiState.Error.ServerError(e.message ?: "Error")
            } catch (e: Exception) {
                Log.d("ProfileViewModel", "exception: ${e.message}")
                _uiState.value = ProfileUiState.Error.Unknown(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                tokenDataStore.clearToken()
                TokenHolder.setToken(null)
                _profileData.value = null
                _email.value = null
                _uiState.value = ProfileUiState.Idle
                Log.d("ProfileViewModel", "User logged out successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error during logout: ${e.message}")
            }
        }
    }
}
