package com.rakha.hadirapp.data.repository

import android.util.Log
import com.rakha.hadirapp.data.network.AuthApi
import com.rakha.hadirapp.data.network.dto.LoginRequest
import com.rakha.hadirapp.data.network.dto.RegisterRequest
import com.rakha.hadirapp.data.network.dto.ForgotPasswordRequest
import com.rakha.hadirapp.data.network.parseError
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): String {
        try {
            val request = LoginRequest(email = email, password = password)
            val response = api.login(request)
            val token = response.token
            Log.d("AuthRepositoryImpl", "login response: message=${response.message}, token=${if (token.isNullOrBlank()) "null" else "present"}")

            if (!token.isNullOrBlank()) {
                return token
            }

            // If token is null/blank, something went wrong
            val message = response.message?.ifBlank { "Server tidak mengembalikan token" } ?: "Server tidak mengembalikan token"
            throw AuthException(message)
        } catch (e: HttpException) {
            Log.d("AuthRepositoryImpl", "HttpException: ${e.message}")
            val msg = e.parseError()
            throw AuthException(msg)
        } catch (e: IOException) {
            Log.d("AuthRepositoryImpl", "IOException: ${e.message}")
            throw AuthException("Gagal terhubung ke server")
        } catch (e: Exception) {
            if (e is AuthException) throw e
            Log.d("AuthRepositoryImpl", "Exception: ${e.message}")
            throw AuthException(e.message ?: "Unknown error")
        }
    }

    override suspend fun register(email: String, password: String): String {
        try {
            val request = RegisterRequest(email = email, password = password)
            val response = api.register(request)
            val token = response.token
            Log.d("AuthRepositoryImpl", "register response: message=${response.message}, token=${if (token.isNullOrBlank()) "null" else "present"}")

            if (!token.isNullOrBlank()) {
                return token
            }

            // If token is null/blank, something went wrong
            val message = response.message?.ifBlank { "Registration failed" } ?: "Registration failed"
            throw AuthException(message)
        } catch (e: HttpException) {
            val msg = e.parseError()
            throw AuthException(msg)
        } catch (e: IOException) {
            throw AuthException("Gagal terhubung ke server")
        } catch (e: Exception) {
            if (e is AuthException) throw e
            throw AuthException(e.message ?: "Unknown error")
        }
    }

    override suspend fun forgotPassword(npm: String, newPassword: String): String {
        try {
            val request = ForgotPasswordRequest(npm = npm, newPassword = newPassword)
            val response = api.forgotPassword(request)
            Log.d("AuthRepositoryImpl", "forgotPassword response: message=${response.message}")

            return response.message
        } catch (e: HttpException) {
            Log.d("AuthRepositoryImpl", "HttpException: ${e.message}")
            val msg = e.parseError()
            throw AuthException(msg)
        } catch (e: IOException) {
            Log.d("AuthRepositoryImpl", "IOException: ${e.message}")
            throw AuthException("Gagal terhubung ke server")
        } catch (e: Exception) {
            if (e is AuthException) throw e
            Log.d("AuthRepositoryImpl", "Exception: ${e.message}")
            throw AuthException(e.message ?: "Unknown error")
        }
    }
}

class AuthException(message: String) : Exception(message)


