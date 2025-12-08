package com.moneymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthViewModel - Manages authentication state and logic
 * 
 * Responsibilities:
 * - Handle user login and registration
 * - Manage authentication state
 * - Handle errors and loading states
 * - Persist user session tokens
 * 
 * Uses MVVM pattern with StateFlow for reactive UI updates
 * Uses Hilt for dependency injection
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // UI State data class
    data class AuthUiState(
        val isLoading: Boolean = false,
        val isLoginSuccess: Boolean = false,
        val isRegistrationSuccess: Boolean = false,
        val error: String? = null,
        val userToken: String? = null
    )
    
    // Mutable state flow for UI updates
    private val _uiState = MutableStateFlow(AuthUiState())
    
    // Public read-only state flow exposed to UI
    val uiState: StateFlow<AuthUiState> = _uiState
    
    /**
     * Perform user login
     * - Validate input
     * - Call API via repository
     * - Update UI state with result
     * - Save token to local storage
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Call repository to authenticate user
                val response = authRepository.login(email, password)
                
                if (response.isSuccessful) {
                    // Save token to secure storage
                    authRepository.saveToken(response.token)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        userToken = response.token
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Login failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Perform user registration
     */
    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Call repository to register user
                val response = authRepository.register(email, password, name)
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Registration failed: ${e.message}"
                )
            }
        }
    }
}
