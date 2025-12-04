package com.rakha.hadirapp.ui.attendance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {
    private val _state = MutableStateFlow<AttendanceState>(AttendanceState.Idle)
    val state: StateFlow<AttendanceState> = _state

    fun submitAttendance(sessionId: String, studentId: String, name: String, imageBase64: String) {
        viewModelScope.launch {
            _state.value = AttendanceState.Loading
            try {
                Log.d("AttendanceViewModel", "Submitting attendance with params:")
                Log.d("AttendanceViewModel", "  sessionId: $sessionId")
                Log.d("AttendanceViewModel", "  studentId: $studentId")
                Log.d("AttendanceViewModel", "  name: $name")
                Log.d("AttendanceViewModel", "  imageSize: ${imageBase64.length} chars")

                val request = AttendanceRequest(sessionId = sessionId, studentId = studentId, name = name, imageBase64 = imageBase64)
                val response = repository.submitAttendance(request)

                Log.d("AttendanceViewModel", "Attendance submitted successfully: ${response.id}")
                _state.value = AttendanceState.Success(response)
            } catch (e: Exception) {
                Log.e("AttendanceViewModel", "attendance error: ${e.message}", e)
                _state.value = AttendanceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _state.value = AttendanceState.Idle
    }
}

