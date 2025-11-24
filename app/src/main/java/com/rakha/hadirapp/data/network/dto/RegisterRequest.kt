@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.rakha.hadirapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "STUDENT"
)
