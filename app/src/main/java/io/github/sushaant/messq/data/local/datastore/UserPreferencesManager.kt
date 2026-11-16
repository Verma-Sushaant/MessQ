package io.github.sushaant.messq.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "messq_preferences")

class UserPreferencesManager(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role") // "student" or "admin"
    }

    val userToken: Flow<String?> = appContext.dataStore.data
        .map { preferences -> preferences[USER_TOKEN_KEY] }

    val userRole: Flow<String?> = appContext.dataStore.data
        .map { preferences -> preferences[USER_ROLE_KEY] }

    suspend fun saveUserSession(token: String, role: String) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun clearSession() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}