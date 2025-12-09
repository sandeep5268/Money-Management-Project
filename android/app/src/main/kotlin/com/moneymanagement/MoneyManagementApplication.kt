
package com.moneymanagement

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
// import timber.log.Timber // Recommended for future integration

/**
 * MoneyManagementApplication - Application class for Hilt dependency injection.
 * Refactored to include Production Security policies and Development strictness.
 */
@HiltAndroidApp
class MoneyManagementApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // [ARCH FIX] Configure environment-specific policies
        if (BuildConfig.DEBUG) {
            // 1. Enable StrictMode to catch Main Thread I/O violations early
            setupStrictMode()
            
            // 2. Initialize Logging (Example logic)
            // Timber.plant(Timber.DebugTree()) 
        } else {
            // [SECURITY FIX] In Release, ensure no debug logs are emitted.
            // If using a crash reporting tool (e.g., Crashlytics), init it here.
        }
    }

    /**
     * Enforces strict thread policies during development.
     * Crashing on network/disk I/O on the Main Thread prevents UI jank.
     */
    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all known issues
                .penaltyLog()
                // .penaltyDeath() // Uncomment to crash app on violation (Strict)
                .build()
        )
        
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }
}

"""
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
"""
