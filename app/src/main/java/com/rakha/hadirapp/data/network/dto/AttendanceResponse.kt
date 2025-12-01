package com.rakha.hadirapp.data.network.dto

import com.google.gson.annotations.SerializedName

// Response model based on API response provided
data class AttendanceResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("scheduleId") val scheduleId: String?,
    @SerializedName("studentName") val studentName: String?,
    @SerializedName("studentNpm") val studentNpm: String?,
    @SerializedName("studentEmail") val studentEmail: String?,
    @SerializedName("selfieImage") val selfieImage: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("scannedAt") val scannedAt: String?,
    @SerializedName("createdAt") val createdAt: String?
)

