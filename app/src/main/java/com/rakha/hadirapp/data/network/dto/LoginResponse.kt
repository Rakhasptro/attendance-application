package com.rakha.hadirapp.data.network.dto

import com.google.gson.annotations.SerializedName

// Plain DTO for Retrofit+Gson
data class LoginResponse(
    val status: Boolean = false,
    val message: String? = null,
    @SerializedName("access_token") val token: String? = null,
    val user: UserResponse? = null
)
