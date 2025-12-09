AuthViewModel.kt - ViewModel Responsibilities & Implementation Guide

Purpose:
- Provide UI state and actions for authentication flows (login, register, token refresh) for the Compose UI.
- Coordinate with `AuthRepository` (or remote service) to execute network calls and persist tokens securely.

Recommended state model (descriptive):
- `data class AuthUiState(
  val email: String,
  val password: String,
  val isLoading: Boolean,
  val emailError: String?,
  val passwordError: String?,
  val loginError: String?,
  val isAuthenticated: Boolean
)

Responsibilities / functions to implement (descriptive):
- `fun onEmailChanged(value: String)` — update `email` and clear related errors.
- `fun onPasswordChanged(value: String)` — update `password` and clear related errors.
- `suspend fun login()` — validate inputs, call `AuthRepository.login(email, password)`, handle success/failure, and store tokens securely.
- `suspend fun logout()` — clear tokens and update `isAuthenticated`.
- `fun observeAuthState()` — expose whether user is currently authenticated (e.g., check encrypted storage for token validity or use a repository flow).

Implementation and design notes:
- Use `ViewModel` + `viewModelScope` coroutines for async operations.
- Expose UI state as `StateFlow` (read-only for UI) so Compose can `collectAsState()`.
- Do not perform heavy IO on the main thread; use `Dispatchers.IO` for repository calls.
- Delegate actual token storage to a secure storage wrapper (EncryptedSharedPreferences or Jetpack Security).

Error handling and retry:
- Return structured errors from repository (e.g., `AuthResult.Success` / `AuthResult.Failure`) and map to user-friendly messages.
- Provide exponential retry/backoff for transient network errors in repository or via WorkManager for background refresh.

Security:
- Never expose tokens as plain text in the ViewModel logs or UI state.
- Use refresh token rotation and short-lived access tokens.

Testing guidance:
- Unit test ViewModel logic by providing a fake `AuthRepository` that simulates success, failure, and network errors.
- Verify state transitions (loading -> success -> authenticated) and that errors are surfaced correctly.

Integration notes:
- The `AuthViewModel` should be provided via DI so the UI can receive test doubles in UI tests.

This file provides descriptive guidance only. Implement the Kotlin `ViewModel` with actual code following these responsibilities and patterns.
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
     * - Call repository to authenticate user locally
     * - Update UI state with result
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Validate input
                if (email.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email cannot be empty"
                    )
                    return@launch
                }
                
                if (password.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Password cannot be empty"
                    )
                    return@launch
                }
                
                // Call repository to authenticate user
                val response = authRepository.login(email, password)
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        userToken = response.token
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.errorMessage ?: "Login failed"
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
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Validate input
                if (email.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email cannot be empty"
                    )
                    return@launch
                }
                
                if (password.isBlank() || password.length < 4) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Password must be at least 4 characters"
                    )
                    return@launch
                }
                
                if (name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Name cannot be empty"
                    )
                    return@launch
                }
                
                // Call repository to register user
                val response = authRepository.register(email, password, name)
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccess = true,
                        userToken = response.token
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.errorMessage ?: "Registration failed"
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
    
    /**
     * Logout current user
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                val token = authRepository.getToken()
                _uiState.value = _uiState.value.copy(
                    isLoginSuccess = true,
                    userToken = token
                )
            }
        }
    }
}
