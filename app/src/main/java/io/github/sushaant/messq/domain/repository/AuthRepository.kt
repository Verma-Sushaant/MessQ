package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun signUp(email: String, password: String, fullName: String, rollNumber: String?, role: String): Resource<Unit>
    suspend fun logout(): Resource<Unit>
}