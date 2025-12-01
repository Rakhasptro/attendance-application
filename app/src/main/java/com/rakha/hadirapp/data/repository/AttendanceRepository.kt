package com.rakha.hadirapp.data.repository

import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.network.dto.AttendanceResponse

interface AttendanceRepository {
    suspend fun submitAttendance(request: AttendanceRequest): AttendanceResponse
}

