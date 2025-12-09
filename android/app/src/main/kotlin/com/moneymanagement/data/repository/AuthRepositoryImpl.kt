package com.moneymanagement.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepositoryImpl - Local-only authentication repository implementation
 * 
 * Uses DataStore to store user credentials and session tokens locally.
 * Passwords are hashed using SHA-256 before storage (basic security).
 * For production, consider using more secure methods like bcrypt.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PASSWORD_HASH = stringPreferencesKey("user_password_hash")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }
    
    override suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            // Check if user already exists
            val existingEmail = dataStore.data.first()[KEY_USER_EMAIL]
            if (existingEmail != null && existingEmail == email) {
                return AuthResult(
                    isSuccessful = false,
                    errorMessage = "User with this email already exists"
                )
            }
            
            // Hash password
            val passwordHash = hashPassword(password)
            
            // Save user data
            dataStore.edit { preferences ->
                preferences[KEY_USER_EMAIL] = email
                preferences[KEY_USER_PASSWORD_HASH] = passwordHash
                preferences[KEY_USER_NAME] = name
                preferences[KEY_IS_LOGGED_IN] = true
                preferences[KEY_AUTH_TOKEN] = generateToken(email)
            }
            
            AuthResult(
                isSuccessful = true,
                token = generateToken(email)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            AuthResult(
                isSuccessful = false,
                errorMessage = "Registration failed: ${e.message}"
            )
        }
    }
    
    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val preferences = dataStore.data.first()
            val storedEmail = preferences[KEY_USER_EMAIL]
            val storedPasswordHash = preferences[KEY_USER_PASSWORD_HASH]
            
            if (storedEmail == null || storedPasswordHash == null) {
                return AuthResult(
                    isSuccessful = false,
                    errorMessage = "No user found. Please register first."
                )
            }
            
            if (storedEmail != email) {
                return AuthResult(
                    isSuccessful = false,
                    errorMessage = "Invalid email or password"
                )
            }
            
            val passwordHash = hashPassword(password)
            if (passwordHash != storedPasswordHash) {
                return AuthResult(
                    isSuccessful = false,
                    errorMessage = "Invalid email or password"
                )
            }
            
            // Generate and save token
            val token = generateToken(email)
            dataStore.edit { prefs ->
                prefs[KEY_AUTH_TOKEN] = token
                prefs[KEY_IS_LOGGED_IN] = true
            }
            
            AuthResult(
                isSuccessful = true,
                token = token
            )
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            AuthResult(
                isSuccessful = false,
                errorMessage = "Login failed: ${e.message}"
            )
        }
    }
    
    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }
    
    override suspend fun getToken(): String? {
        return dataStore.data.first()[KEY_AUTH_TOKEN]
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[KEY_IS_LOGGED_IN] ?: false
    }
    
    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = ""
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }
    
    override suspend fun getCurrentUserEmail(): String? {
        return dataStore.data.first()[KEY_USER_EMAIL]
    }
    
    /**
     * Hash password using SHA-256
     * Note: For production, use bcrypt or Argon2
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate a simple token for local authentication
     * In production, this would come from backend
     */
    private fun generateToken(email: String): String {
        return "local_token_${email.hashCode()}_${System.currentTimeMillis()}"
    }
}

