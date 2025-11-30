package com.rakha.hadirapp.data.network

import com.rakha.hadirapp.data.network.dto.ProfileResponse
import com.rakha.hadirapp.data.network.dto.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse
}

