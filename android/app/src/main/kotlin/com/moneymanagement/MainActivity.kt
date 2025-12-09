MainActivity.kt - Purpose and Implementation Guide

Purpose:
- Entry point of the Android application. Hosts the Compose UI tree and the navigation host.
- Responsible for application-level setup such as dependency injection initialization, theming wrapper, and top-level navigation container.

Responsibilities / What to implement here:
- Provide an Activity that extends `ComponentActivity` and calls `setContent {}` to set the Compose UI.
- Wrap the Compose content with your app `Theme` (Material3 or Material components), window insets handling, and any top-level UI state providers.
- Initialize and attach the `NavHost` (Compose Navigation) which defines the app's navigation graph (login, home, add/edit transaction, settings, etc.).
- Ensure that dependency injection (Hilt/Koin) entry points are defined at the Activity level if using Hilt annotation processing (e.g., `@AndroidEntryPoint`).
- Set up listeners for lifecycle events if your app needs to observe foreground/background state for analytics or sync triggers.

Suggested method signatures and components (descriptive):
- `onCreate(savedInstanceState)` — call `super`, optionally initialize DI, and call `setContent { AppTheme { AppNavHost() } }`.
- `AppNavHost()` — composable that registers routes/screens and navigation arguments.

Style and structure guidelines:
- Keep `MainActivity` slim: move business logic to ViewModels and single-responsibility components.
- Do not place repository or network initialization logic directly in Activity — provide them via DI modules.
- Use `rememberNavController()` for navigation controller and pass it to the `AppNavHost` composable.

Accessibility and startup performance notes:
- Keep startup work minimal to improve cold-start time.
- Do expensive initialization off the UI thread (e.g., in Application class or background work).
- Provide content descriptions for the initial screen and ensure the first screen is accessible.

Testing:
- UI tests should launch `MainActivity` and verify the nav graph starts on the expected destination (login or home depending on auth state).
- Keep the Activity testable by exposing test-only entrypoints or using DI to provide fake repositories.

Security & Privacy:
- Do not log sensitive information during startup (auth tokens, personal data).

Example implementation steps (developer to implement in Kotlin):
1. Annotate the Activity for DI if using Hilt (`@AndroidEntryPoint`).
2. In `onCreate`, set the Compose content and provide `AppTheme`.
3. Inside content, create and remember `NavController` and call `AppNavHost(navController)`.
4. Handle deep links and external intents via navigation configuration or intent handlers.

This file intentionally contains implementation guidance only. Implement the actual code in Kotlin following the project's DI and navigation conventions.
package com.moneymanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.moneymanagement.ui.theme.MoneyManagementTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point of the Android application
 * 
 * Responsibilities:
 * - Initialize Jetpack Compose UI
 * - Set up Material Design 3 theme
 * - Request runtime permissions (SMS, Biometric)
 * - Initialize dependency injection with Hilt
 * - Set up navigation flow
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set Compose content with Material Design 3 theme
        setContent {
            MoneyManagementTheme {
                // Surface provides background color from theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Main navigation graph
                    com.moneymanagement.navigation.AppNavHost()
                }
            }
        }
        
        // TODO: Request SMS and Biometric permissions at runtime
        // requestSMSPermission()
        // requestBiometricPermission()
    }
}
