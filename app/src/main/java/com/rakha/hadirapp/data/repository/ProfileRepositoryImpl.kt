package com.rakha.hadirapp.data.repository

import android.util.Log
import com.rakha.hadirapp.data.network.ProfileApi
import com.rakha.hadirapp.data.network.parseError
import com.rakha.hadirapp.data.network.dto.ProfileResponse
import com.rakha.hadirapp.data.network.dto.UpdateProfileRequest
import retrofit2.HttpException
import java.io.IOException

class ProfileRepositoryImpl(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getProfile(): ProfileResponse {
        try {
            val response = api.getProfile()
            Log.d("ProfileRepositoryImpl", "getProfile response: ${response.message}")
            return response
        } catch (e: HttpException) {
            val msg = e.parseError()
            throw ProfileException(msg)
        } catch (e: IOException) {
            throw ProfileException("Gagal terhubung ke server")
        } catch (e: Exception) {
            throw ProfileException(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse {
        try {
            val response = api.updateProfile(request)
            Log.d("ProfileRepositoryImpl", "updateProfile response: ${response.message}")
            return response
        } catch (e: HttpException) {
            val msg = e.parseError()
            throw ProfileException(msg)
        } catch (e: IOException) {
            throw ProfileException("Gagal terhubung ke server")
        } catch (e: Exception) {
            throw ProfileException(e.message ?: "Unknown error")
        }
    }
}

class ProfileException(message: String) : Exception(message)

