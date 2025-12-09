package com.moneymanagement.data.repository

/**
 * AuthRepository - Interface for authentication operations
 * 
 * Provides methods for user registration, login, and session management.
 * For standalone app, all operations are local-only (no backend calls).
 */
interface AuthRepository {
    /**
     * Register a new user
     * @return AuthResult with success status and token or error message
     */
    suspend fun register(email: String, password: String, name: String): AuthResult
    
    /**
     * Login with email and password
     * @return AuthResult with success status and token or error message
     */
    suspend fun login(email: String, password: String): AuthResult
    
    /**
     * Save authentication token
     */
    suspend fun saveToken(token: String)
    
    /**
     * Get current authentication token
     */
    suspend fun getToken(): String?
    
    /**
     * Check if user is currently logged in
     */
    suspend fun isLoggedIn(): Boolean
    
    /**
     * Logout current user
     */
    suspend fun logout()
    
    /**
     * Get current user email
     */
    suspend fun getCurrentUserEmail(): String?
}

/**
 * AuthResult - Result of authentication operations
 */
data class AuthResult(
    val isSuccessful: Boolean,
    val token: String? = null,
    val errorMessage: String? = null
)

