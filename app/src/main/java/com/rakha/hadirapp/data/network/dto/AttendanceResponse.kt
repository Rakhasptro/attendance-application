package com.rakha.hadirapp.data.network.dto

import com.google.gson.annotations.SerializedName

// Response model based on actual API response
data class AttendanceResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("scheduleId") val scheduleId: String?,
    @SerializedName("studentName") val studentName: String?,
    @SerializedName("studentNpm") val studentNpm: String?,
    @SerializedName("studentEmail") val studentEmail: String?,
    @SerializedName("selfieImage") val selfieImage: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("confirmedBy") val confirmedBy: String?,
    @SerializedName("confirmedAt") val confirmedAt: String?,
    @SerializedName("rejectionReason") val rejectionReason: String?,
    @SerializedName("scannedAt") val scannedAt: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

