package com.rakha.hadirapp.data.network

import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.network.dto.AttendanceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AttendanceApi {
    @POST("attendance/submit/mobile")
    suspend fun submitAttendance(@Body request: AttendanceRequest): Response<AttendanceResponse>
}

