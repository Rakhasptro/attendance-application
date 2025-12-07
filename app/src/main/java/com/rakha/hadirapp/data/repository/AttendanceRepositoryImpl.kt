package com.rakha.hadirapp.data.repository

import android.util.Log
import com.rakha.hadirapp.data.network.AttendanceApi
import com.rakha.hadirapp.data.network.parseError
import com.rakha.hadirapp.data.network.dto.AttendanceRequest
import com.rakha.hadirapp.data.network.dto.AttendanceResponse
import com.rakha.hadirapp.data.network.dto.AttendanceHistoryItem
import retrofit2.HttpException
import java.io.IOException

class AttendanceRepositoryImpl(private val api: AttendanceApi) : AttendanceRepository {
    override suspend fun submitAttendance(request: AttendanceRequest): AttendanceResponse {
        try {
            val response = api.submitAttendance(request)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AttendanceRepository", "submit success: $body")
                return body ?: throw AttendanceException("Empty response")
            } else {
                // parse error if http exception
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            val msg = e.parseError()
            throw AttendanceException(msg)
        } catch (e: IOException) {
            throw AttendanceException("Gagal terhubung ke server")
        } catch (e: Exception) {
            throw AttendanceException(e.message ?: "Unknown error")
        }
    }

    override suspend fun getAttendanceHistory(): List<AttendanceHistoryItem> {
        try {
            val response = api.getAttendanceHistory()
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AttendanceRepository", "history success: ${body?.size} items")
                return body ?: emptyList()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            val msg = e.parseError()
            throw AttendanceException(msg)
        } catch (e: IOException) {
            throw AttendanceException("Gagal terhubung ke server")
        } catch (e: Exception) {
            throw AttendanceException(e.message ?: "Unknown error")
        }
    }
}

class AttendanceException(message: String) : Exception(message)

