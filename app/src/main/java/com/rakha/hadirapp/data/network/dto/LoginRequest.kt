package com.rakha.hadirapp.data.network.dto

// Plain Kotlin data class for Retrofit/Gson serialization
data class LoginRequest(
    val email: String,
    val password: String
)
