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
                val request = AttendanceRequest(sessionId = sessionId, studentId = studentId, name = name, imageBase64 = imageBase64)
                val response = repository.submitAttendance(request)
                _state.value = AttendanceState.Success(response)
            } catch (e: Exception) {
                Log.d("AttendanceViewModel", "attendance error: ${e.message}")
                _state.value = AttendanceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _state.value = AttendanceState.Idle
    }
}

