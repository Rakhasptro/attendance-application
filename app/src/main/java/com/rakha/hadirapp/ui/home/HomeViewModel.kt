package com.rakha.hadirapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakha.hadirapp.data.network.dto.AttendanceHistoryItem
import com.rakha.hadirapp.data.repository.AttendanceException
import com.rakha.hadirapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val history: List<AttendanceHistoryItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allHistory = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())

    private val _filteredHistory = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())
    val filteredHistory: StateFlow<List<AttendanceHistoryItem>> = _filteredHistory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val history = attendanceRepository.getAttendanceHistory()
                _allHistory.value = history
                _filteredHistory.value = history
                _uiState.value = HomeUiState.Success(history)
                Log.d("HomeViewModel", "History loaded: ${history.size} items")
            } catch (e: AttendanceException) {
                Log.e("HomeViewModel", "Error loading history: ${e.message}")
                _uiState.value = HomeUiState.Error(e.message ?: "Error loading history")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Unknown error: ${e.message}")
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterHistory(query)
    }

    private fun filterHistory(query: String) {
        if (query.isBlank()) {
            _filteredHistory.value = _allHistory.value
        } else {
            _filteredHistory.value = _allHistory.value.filter { item ->
                // Filter by course name, student name, or NPM
                item.schedule?.courseName?.contains(query, ignoreCase = true) == true ||
                item.studentName?.contains(query, ignoreCase = true) == true ||
                item.studentNpm?.contains(query, ignoreCase = true) == true
            }
        }
    }
}

