package com.rakha.hadirapp.data.network.dto

// DTOs for profile endpoints

data class Profile(
    val id: String?,
    val userId: String?,
    val fullName: String?,
    val npm: String?,
    val createdAt: String?
)

data class User(
    val id: String?,
    val email: String?,
    val role: String?,
    val profile: Profile?
)

data class ProfileResponse(
    val message: String?,
    val user: User?
)

