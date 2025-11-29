package com.rakha.hadirapp.data.network.dto

data class ApiError(
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)
