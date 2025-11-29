package com.rakha.hadirapp.data.network

import com.google.gson.Gson
import com.rakha.hadirapp.data.network.dto.ApiError
import retrofit2.HttpException

fun HttpException.parseError(): String {
    return try {
        val errorBody = this.response()?.errorBody()?.string()
        if (errorBody != null) {
            val apiError = Gson().fromJson(errorBody, ApiError::class.java)
            apiError.message ?: "Unknown error"
        } else {
            this.message ?: "HTTP error"
        }
    } catch (e: Exception) {
        this.message ?: "Parse error failed"
    }
}
