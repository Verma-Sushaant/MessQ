package io.github.sushaant.messq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.local.datastore.UserPreferencesManager
import io.github.sushaant.messq.domain.repository.AuthRepository
import io.github.sushaant.messq.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(email, pass)) {
                is Resource.Success -> {
                    val savedRole = preferencesManager.userRole.first()
                    if (!savedRole.isNullOrBlank()) {
                        _uiState.value = AuthUiState.Success(savedRole)
                    } else {
                        _uiState.value = AuthUiState.Error("User role not found. Please contact support.")
                    }
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.exception.localizedMessage ?: "Login failed")
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun signUp(email: String, pass: String, fullName: String, rollNumber: String?, role: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signUp(email, pass, fullName, rollNumber, role)) {
                is Resource.Success -> {
                    // Repository already persisted the real accessToken and role to DataStore
                    _uiState.value = AuthUiState.Success(role)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.exception.localizedMessage ?: "Sign up failed")
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState.Idle
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}