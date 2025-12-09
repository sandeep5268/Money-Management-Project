package com.moneymanagement

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MoneyManagementApplication - Application class for Hilt dependency injection
 * 
 * This class initializes Hilt and serves as the entry point for dependency injection.
 * Hilt will generate the necessary code for dependency injection at compile time.
 */
@HiltAndroidApp
class MoneyManagementApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application-level initialization can be done here
        // Database and other singletons are initialized via Hilt modules
    }
}

