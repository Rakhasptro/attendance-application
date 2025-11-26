package com.rakha.hadirapp.data.network.dto

// Plain DTO for Retrofit+Gson
data class UserResponse(
    val id: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val isActive: Boolean = true
)
