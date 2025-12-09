// Android Money Management App - Build Configuration
// This file defines all dependencies, SDK versions, and build settings for the Android app

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.moneymanagement"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // [SECURITY FIX] Added buildTypes to enable Obfuscation for Release builds
    buildTypes {
        release {
            isMinifyEnabled = true // Obfuscates code to prevent reverse engineering
            isShrinkResources = true // Removes unused resources to reduce APK size
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Note: Ensure this version matches your Kotlin version (e.g., Kotlin 1.9.0 -> 1.5.0)
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    implementation(platform("androidx.compose:compose-bom:2023.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // [PERFORMANCE FIX] Removed 'material-icons-extended' to prevent APK bloat. 
    // implementation("androidx.compose.material:material-icons-extended") 
    
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // [SECURITY FIX] Downgraded to Stable version. 
    // Avoid Alpha versions for Crypto in Finance apps unless strictly necessary.
    implementation("androidx.security:security-crypto:1.0.0") 
    
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    implementation("androidx.biometric:biometric:1.1.0")
    
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.test:core:1.5.0")
    
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
"""
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    // Target Android API level and minimum supported version
    compileSdk = 34
    defaultConfig {
        applicationId = "com.moneymanagement"
        minSdk = 26 // Minimum API level (Android 8.0)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        // Enable AndroidX and Jetpack Compose
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Compose configuration
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    // Kotlin compiler options
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Dependencies for the Android app
dependencies {
    // Core Android & AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Jetpack Compose - Modern UI framework
    implementation(platform("androidx.compose:compose-bom:2023.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room Database - Local SQLite storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Data Store - Encrypted SharedPreferences replacement
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Retrofit - HTTP client for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Hilt - Dependency Injection framework
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines - Async/await for background tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // WorkManager - Background task scheduler
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // Biometric - Fingerprint/Face authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Testing - Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.test:core:1.5.0")
    
    // Testing - UI/Instrumentation tests
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
"""