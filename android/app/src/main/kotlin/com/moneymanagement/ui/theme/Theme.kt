// package com.moneymanagement.ui.theme

// import android.app.Activity
// import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.darkColorScheme
// import androidx.compose.material3.lightColorScheme
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.SideEffect
// import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.platform.LocalView
// import androidx.core.view.WindowCompat

package com.moneymanagement.ui.theme

import androidx.compose.ui.graphics.Color

// Money Management App - Brand Colors
val PrimaryGreen = Color(0xFF4CAF50)
val PrimaryGreenDark = Color(0xFF388E3C)
val PrimaryGreenLight = Color(0xFF81C784)

val AccentBlue = Color(0xFF2196F3)
val AccentBlueDark = Color(0xFF1976D2)
val AccentBlueLight = Color(0xFF64B5F6)

// Semantic Colors
val ExpenseRed = Color(0xFFF44336)

// [DRY FIX] Reference the primary brand color directly.
// Ensures that if PrimaryGreen changes, IncomeGreen updates automatically.
val IncomeGreen = PrimaryGreen 

// Neutral Colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

// [CLEANUP] Removed unused 'Purple' and 'Pink' template colors.
// Note: Ensure your Theme.kt is updated to use PrimaryGreen instead of Purple40.


"""
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    secondary = AccentBlueLight,
    tertiary = Pink80,
    background = BackgroundDark,
    surface = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = AccentBlue,
    tertiary = Pink40,
    background = BackgroundLight,
    surface = SurfaceLight
)

@Composable
fun MoneyManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
"""
