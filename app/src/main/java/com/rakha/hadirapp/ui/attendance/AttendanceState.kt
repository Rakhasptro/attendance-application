package com.rakha.hadirapp.ui.attendance

import com.rakha.hadirapp.data.network.dto.AttendanceResponse

sealed class AttendanceState {
    object Idle : AttendanceState()
    object Loading : AttendanceState()
    data class Success(val data: AttendanceResponse) : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

