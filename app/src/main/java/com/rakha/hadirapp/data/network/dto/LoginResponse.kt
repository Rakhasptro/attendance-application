package com.rakha.hadirapp.data.network.dto

// Plain DTO for Retrofit+Gson
data class LoginResponse(
    val status: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val user: UserResponse? = null
)
