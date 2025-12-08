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
                    // TODO: Add main navigation graph here
                    // Example: MoneyManagementApp()
                }
            }
        }
        
        // TODO: Request SMS and Biometric permissions at runtime
        // requestSMSPermission()
        // requestBiometricPermission()
    }
}
