package com.rakha.hadirapp.data.network

import com.rakha.hadirapp.data.network.dto.LoginRequest
import com.rakha.hadirapp.data.network.dto.LoginResponse
import com.rakha.hadirapp.data.network.dto.RegisterRequest
import com.rakha.hadirapp.data.network.dto.ForgotPasswordRequest
import com.rakha.hadirapp.data.network.dto.ForgotPasswordResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @Headers("Content-Type: application/json")
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @Headers("Content-Type: application/json")
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse
}
