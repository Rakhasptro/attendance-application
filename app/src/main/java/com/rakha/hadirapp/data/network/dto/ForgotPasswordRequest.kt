package com.rakha.hadirapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("npm") val npm: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ForgotPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean? = null
)

