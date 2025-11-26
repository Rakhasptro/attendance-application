package com.rakha.hadirapp.data.repository

import android.util.Log
import com.rakha.hadirapp.data.network.AuthApi
import com.rakha.hadirapp.data.network.dto.LoginRequest
import com.rakha.hadirapp.data.network.dto.RegisterRequest
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): String {
        try {
            val request = LoginRequest(email = email, password = password)
            val response = api.login(request)
            val token = response.token
            Log.d("AuthRepositoryImpl", "login response: status=${response.status}, message=${response.message}, token=$token")

            if (!token.isNullOrBlank()) {
                return token
            }

            if (response.status) {
                throw AuthException("Login succeeded but server did not return an authentication token")
            }

            val message = response.message?.ifBlank { "Invalid email or password" } ?: "Invalid email or password"
            throw AuthException(message)
        } catch (e: HttpException) {
            Log.d("AuthRepositoryImpl", "HttpException: ${e.message}")
            val errBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                null
            }
            val msg = errBody ?: e.message ?: "HTTP error"
            throw AuthException(msg)
        } catch (e: IOException) {
            Log.d("AuthRepositoryImpl", "IOException: ${e.message}")
            throw AuthException("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.d("AuthRepositoryImpl", "Exception: ${e.message}")
            throw AuthException(e.message ?: "Unknown error")
        }
    }

    override suspend fun register(email: String, password: String): String {
        try {
            val request = RegisterRequest(email = email, password = password)
            val response = api.register(request)
            val token = response.token
            Log.d("AuthRepositoryImpl", "register response: status=${response.status}, message=${response.message}, token=$token")

            if (!token.isNullOrBlank()) {
                return token
            }

            if (response.status) {
                throw AuthException("Registration succeeded but server did not return an authentication token")
            }

            val message = response.message?.ifBlank { "Registration failed" } ?: "Registration failed"
            throw AuthException(message)
        } catch (e: HttpException) {
            val errBody = try { e.response()?.errorBody()?.string() } catch (ex: Exception) { null }
            throw AuthException(errBody ?: e.message ?: "HTTP error")
        } catch (e: IOException) {
            throw AuthException("Network error: ${e.message}")
        } catch (e: Exception) {
            throw AuthException(e.message ?: "Unknown error")
        }
    }
}

class AuthException(message: String) : Exception(message)
