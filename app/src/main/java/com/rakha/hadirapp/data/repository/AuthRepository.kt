package com.rakha.hadirapp.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): String // returns token
    suspend fun register(email: String, password: String): String // returns token
    suspend fun forgotPassword(npm: String, newPassword: String): String // returns message
}
