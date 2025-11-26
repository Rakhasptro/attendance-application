package com.rakha.hadirapp.data.network.dto

// Plain data class for Retrofit/Gson
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "STUDENT"
)
