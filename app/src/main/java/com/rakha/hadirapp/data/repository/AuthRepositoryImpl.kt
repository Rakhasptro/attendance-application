package com.rakha.hadirapp.data.repository

import android.util.Log
import com.rakha.hadirapp.data.network.AuthApi

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): String {
        val response = api.login(email, password)
        val token = response.token
        Log.d("AuthRepositoryImpl", "login response: status=${response.status}, message=${response.message}, token=$token")

        // Fallback: if server returned a token, accept it regardless of the status flag
        if (!token.isNullOrBlank()) {
            Log.d("AuthRepositoryImpl", "accepting token from response despite status=${response.status}")
            return token
        }

        if (response.status) {
            // status true but token missing -> return explicit error
            throw AuthException("Login succeeded but server did not return an authentication token")
        }
        val message = response.message?.ifBlank { "Invalid email or password" } ?: "Invalid email or password"
        throw AuthException(message)
    }

    override suspend fun register(email: String, password: String): String {
        val response = api.register(email, password)
        val token = response.token
        Log.d("AuthRepositoryImpl", "register response: status=${response.status}, message=${response.message}, token=$token")

        // Fallback: accept token if present
        if (!token.isNullOrBlank()) {
            Log.d("AuthRepositoryImpl", "accepting token from register response despite status=${response.status}")
            return token
        }

        if (response.status) {
            throw AuthException("Registration succeeded but server did not return an authentication token")
        }
        val message = response.message?.ifBlank { "Registration failed" } ?: "Registration failed"
        throw AuthException(message)
    }
}

class AuthException(message: String) : Exception(message)
