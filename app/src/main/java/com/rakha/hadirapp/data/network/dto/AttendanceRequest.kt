package com.rakha.hadirapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class AttendanceRequest(
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("studentId") val studentId: String,
    @SerializedName("name") val name: String,
    @SerializedName("imageBase64") val imageBase64: String
)

