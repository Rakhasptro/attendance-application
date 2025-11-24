package com.rakha.hadirapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val status: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val user: UserResponse? = null
)
