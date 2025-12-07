package com.rakha.hadirapp.data.repository

import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.network.dto.AttendanceResponse
import com.rakha.hadirapp.data.network.dto.AttendanceHistoryItem

interface AttendanceRepository {
    suspend fun submitAttendance(request: AttendanceRequest): AttendanceResponse
    suspend fun getAttendanceHistory(): List<AttendanceHistoryItem>
}

