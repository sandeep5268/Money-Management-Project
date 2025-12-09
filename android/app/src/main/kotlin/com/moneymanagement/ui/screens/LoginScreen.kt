// LoginScreen.kt - Composable Design & Implementation Guide

// Purpose:
// - Provide a composable screen for user authentication (login and optionally registration). Handles input validation, accessibility, error states, and navigation on success.

// Core responsibilities:
// - Present email/username and password inputs, with proper keyboard types and IME actions.
// - Provide form-level validation and real-time feedback (e.g., inline error messages).
// - Show progress indicators during async login network requests.
// - Surface errors from the `AuthViewModel` (e.g., invalid credentials, network errors).
// - Navigate to the main application screen once authentication succeeds.

// Suggested UI components and behavior (descriptive):
// - `TextField` for email/username: set keyboard type to `KeyboardType.Email` and provide content description.
// - `TextField` for password: use `visualTransformation = PasswordVisualTransformation()` and toggling to show/hide password.
// - `Button` for primary action (`Sign in`) — disable when form invalid or request in-flight.
// - `TextButton` for secondary actions (e.g., `Forgot Password`, `Register`).
// - `Snackbar` or small inline banner for non-field errors (server errors, connectivity).

// State & ViewModel interaction:
// - Use `AuthViewModel` to expose UI state as `StateFlow` or `LiveData`:
//   - `uiState` containing fields: `isLoading`, `email`, `password`, `emailError`, `passwordError`, `loginError`, `isAuthenticated`.
// - UI should call `authViewModel.onLoginClicked()` and pass current credentials; avoid performing network calls directly in composable.
// - Use `LaunchedEffect` to react to authentication success and navigate using `NavController`.

// Accessibility & localization:
// - Provide content descriptions for interactive elements.
// - Use `string` resources for all visible text and support RTL/localization.
// - Ensure focus order is logical and `imeAction` on password launches the login action.

// Security considerations:
// - Never keep plaintext passwords in logs or persisted storage.
// - Use secure input flags to prevent screenshots if required for the product.

// Testing guidance:
// - Write Compose UI tests that validate:
//   - Input validation behavior (empty email, invalid email, short password).
//   - Disabled/enabled state of the Sign-in button.
//   - Display of loading indicator during authentication.
//   - Navigation on success using a fake `AuthViewModel` or DI-provided test doubles.

// Developer steps to implement (Kotlin, Compose):
// 1. Create `@Composable fun LoginScreen(viewModel: AuthViewModel, navController: NavController)`.
// 2. Observe ViewModel state with `collectAsState()`.
// 3. Render input fields, errors, and actions per the observed state.
// 4. Call `viewModel.login(email, password)` when user submits.

// This file contains guidance only. Implement the actual Kotlin Composable according to your project's theming and navigation conventions.

package com.moneymanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneymanagement.viewmodel.AuthViewModel

/**
 * LoginScreen - User authentication screen.
 * Refactored for State Hoisting, Accessibility, and One-Shot Events.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // [ARCH FIX] Collect state safely using lifecycle-aware collector
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Local UI state for password visibility toggle (strictly UI concern)
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    // Focus manager for handling IME actions (Next/Done)
    val focusManager = LocalFocusManager.current
    
    // [LOGIC FIX] Handle Navigation as a One-Shot Event
    // Note: Ideally, use a separate 'Effect' flow in ViewModel. 
    // Here, we consume the state and ensure we don't loop.
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            // Optional: viewModel.resetLoginState() 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // [ARCH FIX] Single Source of Truth
        // Value reads from ViewModel state, changes sent back to ViewModel.
        OutlinedTextField(
            value = uiState.email, // Read from VM
            onValueChange = { viewModel.updateEmail(it) }, // Write to VM
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.emailError != null,
            supportingText = {
                if (uiState.emailError != null) {
                    Text(uiState.emailError ?: "")
                }
            },
            // [UX FIX] Keyboard Configuration
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.passwordError != null,
            supportingText = {
                if (uiState.passwordError != null) {
                    Text(uiState.passwordError ?: "")
                }
            },
            // [UX FIX] Password Visibility Toggle
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    focusManager.clearFocus()
                    viewModel.login() 
                }
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            // [ARCH FIX] Parameter-less login. 
            // VM already has the current email/pass state.
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        // General Auth Error (Network, etc.)
        if (uiState.authError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.authError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}

"""
/**
 * LoginScreen - User authentication screen
 * 
 * Features:
 * - Email/Password login form
 * - Error message display
 * - Loading state handling
 * - Navigation to registration screen
 * - Biometric login option
 * 
 * Uses AuthViewModel for business logic and state management
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // State variables for email and password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password input field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation() // Hide password characters
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login button
        Button(
            onClick = { 
                viewModel.login(email, password) // Call ViewModel login method
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading // Disable during loading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }
        
        // Error message display
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Success navigation
        LaunchedEffect(uiState.isLoginSuccess) {
            if (uiState.isLoginSuccess) {
                onLoginSuccess()
            }
        }
    }
}
"""