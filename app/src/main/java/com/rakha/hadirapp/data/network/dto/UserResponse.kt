@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.rakha.hadirapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val isActive: Boolean = true
)
