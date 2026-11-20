package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource

data class UserSessionInfo(
    val userId: String,
    val accessToken: String,
    val role: String
)

interface AuthRepository {
    suspend fun login(email: String, pass: String): Resource<Unit>
    suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        rollNumber: String?,
        role: String
    ): Resource<Unit>
    suspend fun logout(): Resource<Unit>
    suspend fun getCurrentUserId(): String?
}