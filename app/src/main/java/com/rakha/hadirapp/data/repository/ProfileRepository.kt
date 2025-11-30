package com.rakha.hadirapp.data.repository

import com.rakha.hadirapp.data.network.dto.ProfileResponse
import com.rakha.hadirapp.data.network.dto.UpdateProfileRequest

interface ProfileRepository {
    suspend fun getProfile(): ProfileResponse
    suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse
}

