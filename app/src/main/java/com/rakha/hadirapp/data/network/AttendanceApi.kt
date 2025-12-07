package com.rakha.hadirapp.data.network

import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.network.dto.AttendanceResponse
import com.rakha.hadirapp.data.network.dto.AttendanceHistoryItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AttendanceApi {
    @POST("attendance/submit/mobile")
    suspend fun submitAttendance(@Body request: AttendanceRequest): Response<AttendanceResponse>

    @GET("attendance/history")
    suspend fun getAttendanceHistory(): Response<List<AttendanceHistoryItem>>
}

