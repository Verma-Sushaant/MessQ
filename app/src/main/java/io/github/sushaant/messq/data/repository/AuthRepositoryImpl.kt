package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.local.datastore.UserPreferencesManager
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.AuthRepository
import io.github.sushaant.messq.exception.MessQException
import kotlinx.serialization.Serializable

class AuthRepositoryImpl(
    private val preferencesManager: UserPreferencesManager
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Resource<Unit> {
        return safeApiCall {
            SupabaseClientInstance.client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }

            // Retrieve session token after successful login
            val session = SupabaseClientInstance.client.auth.currentSessionOrNull()
                ?: throw MessQException.AuthException("Session could not be established.")
            val userId = session.user?.id ?: throw MessQException.AuthException("User ID not found.")

            // Fetch the actual role from Supabase 'profiles' table
            @Serializable
            data class UserProfileDto(val role: String)

            val profile = SupabaseClientInstance.client
                .from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfileDto>()

            // Save real session token & role to DataStore
            preferencesManager.saveUserSession(session.accessToken, profile.role)

            Unit
        }
    }

    override suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        rollNumber: String?,
        role: String
    ): Resource<Unit> {
        return safeApiCall {
            // 1. Register user with Supabase Auth
            SupabaseClientInstance.client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }

            // 2. Get the newly created user's session & ID
            val session = SupabaseClientInstance.client.auth.currentSessionOrNull()
                ?: throw MessQException.AuthException("Sign up successful, but session could not be established.")
            val userId = session.user?.id ?: throw MessQException.AuthException("User ID not found after sign up.")

            // 3. Insert profile record with selected role into 'profiles' table
            @Serializable
            data class ProfileInsertDto(
                val id: String,
                val full_name: String,
                val roll_number: String?,
                val role: String
            )

            SupabaseClientInstance.client
                .from("profiles")
                .insert(
                    ProfileInsertDto(
                        id = userId,
                        full_name = fullName,
                        roll_number = if (role == "student") rollNumber else null,
                        role = role
                    )
                )

            // 4. Save real session token & role locally so user is logged in immediately
            preferencesManager.saveUserSession(session.accessToken, role)

            Unit
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return safeApiCall {
            SupabaseClientInstance.client.auth.signOut()
            preferencesManager.clearSession()
            Unit
        }
    }
}